# tools/pattern_retriever.py
from langchain.tools import Tool
from utils.http import post_json
from config.settings import PATTERN_RETRIEVAL_URL

def _retrieve_pattern(analysis_result: dict):
    return post_json(PATTERN_RETRIEVAL_URL, analysis_result)

retrieve_pattern = Tool(
    name="retrieve_pattern",
    func=_retrieve_pattern,
    description="Retrieve fix patterns/templates using static analysis result"
)
