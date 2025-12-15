# tools/sandbox.py
from langchain.tools import Tool
from utils.http import post_json
from config.settings import SANDBOX_URL

def _run_sandbox(code: str):
    return post_json(SANDBOX_URL, {"code": code})

run_sandbox = Tool(
    name="run_sandbox",
    func=_run_sandbox,
    description="Compile & run code in online judge sandbox"
)
