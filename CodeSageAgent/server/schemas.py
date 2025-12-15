# server/schemas.py
from pydantic import BaseModel
from typing import Optional


class RepairRequest(BaseModel):
    code: str
    language: Optional[str] = "cpp"
    mode: Optional[str] = "auto"  # auto | repair | rewrite


class RepairResponse(BaseModel):
    status: str
    stage: str
    result_code: str
