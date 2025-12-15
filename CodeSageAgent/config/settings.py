# config/settings.py

"""
CodeSage configuration
You can modify these URLs to point to your own services
"""

# ===== Static Analysis Service =====
STATIC_ANALYSIS_URL = "http://localhost:8001/static-analysis"

# ===== RAG / Fix Pattern Retrieval =====
PATTERN_RETRIEVAL_URL = "http://localhost:8002/retrieve-pattern"

# ===== Online Judge Sandbox =====
SANDBOX_URL = "http://localhost:8003/run-sandbox"


# ===== LLM Config (optional) =====
DEFAULT_LLM_MODEL = "gpt-4o-mini"
LLM_TEMPERATURE = 0.0
