# tools/static_analysis.py
from langchain.tools import Tool
from utils.http import post_json
from config.settings import STATIC_ANALYSIS_URL

def _run_static_analysis(code: str):
    return post_json(STATIC_ANALYSIS_URL, {"code": code})

run_static_analysis = Tool(
    name="run_static_analysis",
    func=_run_static_analysis,
    description="Run static analysis on code and return bug reports"
)
