# repair/patch_generator.py
from langchain_core.prompts import ChatPromptTemplate
from langchain.chat_models import ChatOpenAI

PROMPT = ChatPromptTemplate.from_template("""
你是一个代码自动修复系统，请遵循以下原则：
1. 尽量少改动代码
2. 保持函数名、参数、整体结构不变
3. 仅修复静态分析报告中的问题
4. 不要做无关重构

【原始代码】
{code}

【静态分析结果】
{analysis}

【参考修复模式】
{patterns}

请输出【完整修复后的代码】，不要解释。
""")

class PatchGenerator:
    def __init__(self, llm: ChatOpenAI):
        self.llm = llm

    def generate(self, code, analysis, patterns):
        prompt = PROMPT.format(
            code=code,
            analysis=analysis,
            patterns=patterns
        )
        return self.llm.predict(prompt)
