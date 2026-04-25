from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI

from app.api.debug_routes import router as debug_router
from app.config import load_settings
from app.runtime import Runtime


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = load_settings()
    runtime = Runtime.create(settings)
    runtime.whisper_service.load_model()
    app.state.runtime = runtime
    yield
    runtime.shutdown()


app = FastAPI(title="tai-stt", version="0.1.0", lifespan=lifespan)
app.include_router(debug_router)


def main() -> None:
    settings = load_settings()
    uvicorn.run(
        "app.main:app",
        host=settings.server.host,
        port=settings.server.port,
        reload=False,
    )


if __name__ == "__main__":
    main()
