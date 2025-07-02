from fastapi import APIRouter

from src.routers.tokens import router as tokens_router

main_router = APIRouter()
main_router.include_router(tokens_router)
