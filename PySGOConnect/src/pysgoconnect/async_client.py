import asyncio
import functools
from typing import Protocol

import httpx

from pysgoconnect import errors


class Requester(Protocol):
    async def __call__(self, request: httpx.Request, *, follow_redirects: bool) -> httpx.Response: ...


class AsyncClientWrapper:
    def __init__(
        self,
        async_client: httpx.AsyncClient,
        default_requests_timeout: int,
        max_attempts: int,
        base_retry_delay: float,
    ):
        self.client = async_client
        self._default_requests_timeout = default_requests_timeout
        self._max_attempts = max_attempts
        self._base_retry_delay = base_retry_delay

    async def __aenter__(self) -> "AsyncClientWrapper":
        return self

    async def __aexit__(self, exc_type, exc_value, traceback):  # type: ignore
        await self.client.aclose()

    def make_requester(
        self,
        requests_timeout: int | None = None,
        max_attempts: int | None = None,
        base_retry_delay: float | None = None,
    ) -> Requester:
        """Создаёт частично применённую функцию-запросчик c заданными параметрами."""
        return functools.partial(
            self.request,
            requests_timeout=requests_timeout,
            max_attempts=max_attempts,
            base_retry_delay=base_retry_delay,
        )

    async def request(
        self,
        request: httpx.Request,
        requests_timeout: int | None = None,
        max_attempts: int | None = None,
        base_retry_delay: float | None = None,
        *,
        follow_redirects: bool = True,
    ) -> httpx.Response:
        requests_timeout = requests_timeout if requests_timeout is not None else self._default_requests_timeout
        max_attempts = max_attempts if max_attempts is not None else self._max_attempts
        base_retry_delay = base_retry_delay if base_retry_delay is not None else self._base_retry_delay
        try:
            if requests_timeout == 0:
                return await self._infinite_request(
                    request,
                    follow_redirects,
                    max_attempts,
                    base_retry_delay,
                )
            return await asyncio.wait_for(
                self._infinite_request(
                    request,
                    follow_redirects,
                    max_attempts,
                    base_retry_delay,
                ),
                timeout=requests_timeout,
            )
        except TimeoutError:
            raise errors.NoResponseFromServerError(
                f"No response from server for {request.url} after {requests_timeout} sec",
            ) from None

    async def _infinite_request(
        self,
        request: httpx.Request,
        follow_redirects: bool,  # noqa: FBT001
        max_attempts: int,
        base_retry_delay: float,
    ) -> httpx.Response:
        attempts = 0
        delay = base_retry_delay

        while attempts < max_attempts:
            try:
                response = await self.client.send(request, follow_redirects=follow_redirects)
            except (httpx.ReadTimeout, httpx.ConnectTimeout, httpx.ConnectError, httpx.PoolTimeout):
                attempts += 1
                await asyncio.sleep(delay)
                delay *= 2
            else:
                return response

        raise errors.NoResponseFromServerError(
            f"Failed after {max_attempts} attempts for {request.url}",
        )
