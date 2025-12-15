class CodeSageAgent:
    def __init__(self, llm, tools, patch_gen, rewrite_gen):
        self.tools = {t.name: t for t in tools}
        self.patch_gen = patch_gen
        self.rewrite_gen = rewrite_gen

    def run(self, code: str):
        # === Repair Stage ===
        analysis = self.tools["run_static_analysis"].run(code)
        patterns = self.tools["retrieve_pattern"].run(analysis)

        patched_code = self.patch_gen.generate(code, analysis, patterns)
        sandbox_result = self.tools["run_sandbox"].run(patched_code)

        if sandbox_result["status"] == "AC":
            return patched_code

        # === Rewrite Stage ===
        rewritten = self.rewrite_gen.rewrite(code)
        sandbox_result = self.tools["run_sandbox"].run(rewritten)

        return rewritten if sandbox_result["status"] == "AC" else rewritten
