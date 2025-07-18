from json import loads
from typing import Any

from httpx import URL
from httpx import AsyncClient
from httpx import HTTPStatusError
from httpx import codes
from pydantic import ValidationError

from pysgoconnect.async_client import AsyncClientWrapper
from pysgoconnect.errors import TokenNotFoundError
from pysgoconnect.errors import TokenValidationError
from pysgoconnect.errors import TransmissionProtocolSecurityError
from pysgoconnect.schemas import Token
from pysgoconnect.schemas import TokenID

DEFAULT_BASE_URL: URL = URL("http://localhost:5000/")
DEFAULT_VERSION_API: str = "v1"
DEFAULT_REQUESTS_TIMEOUT: int = 5
DEFAULT_MAX_ATTEMPTS: int = 5
DEFAULT_BASE_RETRY_DELAY: float = 2
LIB_VERSION: str = "0.0.0"


class PySGOConnect:
    def __init__(
        self,
        base_url: URL | str = DEFAULT_BASE_URL,
        version_api: str = DEFAULT_VERSION_API,
        requests_timeout: int = DEFAULT_REQUESTS_TIMEOUT,
        max_attempts: int = DEFAULT_MAX_ATTEMPTS,
        base_retry_delay: float = DEFAULT_BASE_RETRY_DELAY,
        *,
        use_http2: bool = True,
        debug: bool = False,
    ):
        """Инициализирует клиент для работы c API PySGOConnect.

        Args:
            base_url (URL | str, optional): Базовый URL API.
            version_api (str, optional): Версия API, добавляемая к базовому URL.
            requests_timeout (int, optional): Таймаут в секундах для HTTP-запросов.
                При значении 0 включается механизм повторных попыток:
                Например:
                    1-я ошибка — ждать `base_retry_delay` секунд → попробовать снова
                    2-я ошибка — ждать `base_retry_delay*=2` секунды → попробовать снова
                    3-я ошибка — ждать `base_retry_delay*=2` секунды → и так далее пока количество попыток не будет больше `max_attempts`.
            max_attempts (int, optional): Максимальное количество повторных попыток HTTP-запросов.
            base_retry_delay (float, optional): Начальное время задержки перед повторным запросом (секунды).
            use_http2 (bool, optional): Включить поддержку HTTP/2. По умолчанию True.
            debug (bool, optional): Включить режим отладки, при отключение разрешён только защищённый протокол https.

        Raises:
            TransmissionProtocolSecurityError: Если debug=False и протокол в base_url не HTTPS.
        """  # noqa: E501
        self.base_url = URL(base_url) if isinstance(base_url, str) else base_url
        self.version_api = version_api
        self.debug = debug

        if not debug and self.base_url.scheme != "https":
            raise TransmissionProtocolSecurityError("Transmission protocol is not protected use https")

        self._wrapped_async_client = AsyncClientWrapper(
            async_client=AsyncClient(
                base_url=self.base_url,
                headers={"user-agent": f"PySGOConnect/{LIB_VERSION}"},
                http2=use_http2,
            ),
            default_requests_timeout=requests_timeout,
            max_attempts=max_attempts,
            base_retry_delay=base_retry_delay,
        )

    def _parse_token(self, token: Token | dict[str, Any] | str) -> Token:
        if isinstance(token, str):
            token_dict = loads(token)
            return Token.model_validate(token_dict)
        if isinstance(token, dict):
            return Token.model_validate(token)
        if isinstance(token, Token):  # type: ignore
            return token
        raise TypeError(f"Type {type(token)} not supported")

    async def add_token(
        self,
        token: Token | dict[str, Any] | str,
        requests_timeout: int | None = None,
        max_attempts: int | None = None,
        base_retry_delay: float | None = None,
    ) -> TokenID:
        """Добавляет токен и возвращает TokenID.

        Args:
            token (Token | dict[str, Any] | str): Pydantic класс или словарь/строка в формате **JSON**.
            requests_timeout (int | None, optional): Таймаут в секундах; по умолчанию берётся из настроек клиента.
            max_attempts (int | None, optional): Максимальное количество попыток запроса; по умолчанию из настроек.
            base_retry_delay (float | None, optional): Начальное время ожидания перед повторным запросом; по умолчанию из настроек.

        Raises:
            TypeError: Если параметр `token` не является `Token`, `dict` или `JSON-строкой`.
            pydantic.ValidationError: Если данные токена не проходят валидацию модели `Token`.
            NoResponseFromServerError: Если сервер не отвечает (выбрасывается внутри клиента).

        Returns:
            TokenID: Объект c ID токена, датой истечения и временем жизни токена в секундах.
        """  # noqa: E501
        try:
            parsed_token = self._parse_token(token)
        except ValidationError as e:
            raise ValidationError(f"Could not validate token: {e}") from None

        json_payload = parsed_token.model_dump(mode="json")

        rq = await self._wrapped_async_client.request(
            request=self._wrapped_async_client.client.build_request(
                method="POST",
                url=self.base_url.join(self.version_api + "/tokens"),
                json=json_payload,
            ),
            requests_timeout=requests_timeout,
            max_attempts=max_attempts,
            base_retry_delay=base_retry_delay,
        )

        return TokenID(**rq.json())

    async def get_token(
        self,
        token_id: TokenID | str,
        requests_timeout: int | None = None,
        max_attempts: int | None = None,
        base_retry_delay: float | None = None,
    ) -> Token:
        """Получает токен по идентификатору и возвращает объект `Token`.

        Args:
            token_id (TokenID | str): Идентификатор токена в виде Pydantic модели `TokenID` или строки `UUID`.
            requests_timeout (int | None, optional): Таймаут в секундах; по умолчанию берётся из настроек клиента.
            max_attempts (int | None, optional): Максимальное количество попыток запроса; по умолчанию из настроек.
            base_retry_delay (float | None, optional): Начальное время ожидания перед повторным запросом; по умолчанию из настроек.

        Raises:
            TypeError: Если `token_id` не является объектом `TokenID` или строкой.
            TokenValidationError: Если сервер вернул ошибку валидации токена (HTTP 400).
            TokenNotFoundError: Если токен c указанным ID не найден (HTTP 404).
            HTTPStatusError: При других ошибках HTTP.
            NoResponseFromServerError: Если сервер не отвечает (выбрасывается внутри клиента).

        Returns:
            Token: Pydantic-модель `Token` c данными токена.
        """  # noqa: E501
        if isinstance(token_id, TokenID):
            token_id = token_id.token_id
        elif not isinstance(token_id, str):  # type: ignore
            raise TypeError(f"Type {type(token_id)} not supported")

        try:
            rq = await self._wrapped_async_client.request(
                self._wrapped_async_client.client.build_request(
                    method="GET",
                    url=self.base_url.join(self.version_api + f"/tokens/{token_id}"),
                ),
                requests_timeout=requests_timeout,
                max_attempts=max_attempts,
                base_retry_delay=base_retry_delay,
            )

            return Token(**rq.json())
        except HTTPStatusError as E:
            if E.response.status_code == codes.BAD_REQUEST.value:
                raise TokenValidationError(**E.response.json()) from None
            if E.response.status_code == codes.NOT_FOUND.value:
                raise TokenNotFoundError(**E.response.json()) from None
            raise
