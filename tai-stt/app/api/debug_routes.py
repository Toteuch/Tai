from fastapi import APIRouter, Request

from app.pipeline.stt_pipeline import PipelineResult

router = APIRouter(prefix="/debug/mic", tags=["debug-mic"])


@router.post("/capture")
def capture_dry_run(request: Request) -> dict:
    runtime = request.app.state.runtime
    result: PipelineResult = runtime.pipeline.capture_transcribe_and_decide()
    return result.to_dict()


@router.post("/capture-and-callback")
def capture_and_callback(request: Request) -> dict:
    runtime = request.app.state.runtime
    result: PipelineResult = runtime.pipeline.capture_transcribe_decide_and_callback()
    return result.to_dict()
