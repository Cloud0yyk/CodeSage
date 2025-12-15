# repair/rewrite_generator.py
from langchain_core.prompts import ChatPromptTemplate

PROMPT = ChatPromptTemplate.from_template("""
你是一个编程题专家，请直接给出【正确、可通过OJ的代码】。

约束：
1. 函数名、参数必须与原代码保持一致
2. 不要输出多余解释

【原始代码】
{code}
""")

class RewriteGenerator:
    def __init__(self, llm):
        self.llm = llm

    def rewrite(self, code):
        return self.llm.predict(PROMPT.format(code=code))
