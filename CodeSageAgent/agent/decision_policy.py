class DecisionPolicy:
    def decide(self, sandbox_result: dict) -> str:
        if sandbox_result.get("status") == "AC":
            return "accept"
        if sandbox_result.get("runtime_error"):
            return "repair"
        return "rewrite"
