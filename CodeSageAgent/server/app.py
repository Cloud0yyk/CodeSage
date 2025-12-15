# server/app.py

from fastapi import FastAPI, HTTPException

from langchain.chat_models import ChatOpenAI

from agent.codesage_agent import CodeSageAgent
from repair.patch_generator import PatchGenerator
from repair.rewrite_generator import RewriteGenerator
from tools.static_analysis import run_static_analysis
from tools.sandbox import run_sandbox
from tools.rag import retrieve_pattern
from config.settings import DEFAULT_LLM_MODEL, LLM_TEMPERATURE

from server.schemas import RepairRequest, RepairResponse


app = FastAPI(
    title="CodeSage Repair Service",
    description="Static Analysis + RAG + LLM based Code Repair Agent",
    version="0.1.0"
)

# ===== 全局初始化 =====
llm = ChatOpenAI(
    model=DEFAULT_LLM_MODEL,
    temperature=LLM_TEMPERATURE
)

agent = CodeSageAgent(
    llm=llm,
    tools=[run_static_analysis, run_sandbox, retrieve_pattern],
    patch_gen=PatchGenerator(llm),
    rewrite_gen=RewriteGenerator(llm)
)


@app.post("/repair", response_model=RepairResponse)
def repair_code(req: RepairRequest):
    try:
        fixed_code = agent.run(req.code)
        return RepairResponse(
            status="success",
            stage="auto",
            result_code=fixed_code
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=str(e)
        )
