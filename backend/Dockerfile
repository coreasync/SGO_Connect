FROM ghcr.io/astral-sh/uv:alpine3.21

RUN apk add gcc musl-dev linux-headers

ENV WORKDIR=/app/
ENV PYTHONUNBUFFERED=1 PYTHONPATH=${WORKDIR}
ENV UV_COMPILE_BYTECODE=1 UV_LINK_MODE=copy

WORKDIR ${WORKDIR}

COPY pyproject.toml README.md uv.lock .python-version ${WORKDIR}

RUN --mount=type=cache,target=/root/.cache/uv \
    uv sync --frozen --no-install-project

COPY ./src ${WORKDIR}src


CMD [".venv/bin/python3", "src/main.py"]
