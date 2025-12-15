from langchain.chat_models import ChatOpenAI
from agent.codesage_agent import CodeSageAgent
from repair.patch_generator import PatchGenerator
from repair.rewrite_generator import RewriteGenerator
from tools.static_analysis import run_static_analysis
from tools.sandbox import run_sandbox
from tools.rag import retrieve_pattern
from config.settings import DEFAULT_LLM_MODEL, LLM_TEMPERATURE


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

user_code = "public static void main(String[] args) { System.out.println('Hello World') }";
fixed_code = agent.run(user_code)
print(fixed_code)
