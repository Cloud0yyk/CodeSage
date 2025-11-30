# 🚀 CodeSage — 智能代码分析与自动修复 Agent

>CodeSage 是一个为工程代码与编程题而生的智能代码分析 Agent。  
>它将 **静态分析 + RAG 检索 + MCP 工具链 + 大模型推理** 组合成统一系统，支持多语言代码的自动诊断、缺陷定位和自动修复，为在线判题系统（Online Judge）与 IDE 插件提供新一代智能能力。

<p align="center">
☕ Java &nbsp;|&nbsp; 🧵 C++ &nbsp;|&nbsp; 🔌 MCP &nbsp;|&nbsp; 📚 RAG &nbsp;|&nbsp; 🛡️ Static Analysis &nbsp;|&nbsp; 🤖 LLM Code Intelligence
</p>


---

## ✨ 项目亮点（Why CodeSage?）

- 🧠 **组合式智能**：静态分析做“确定性诊断”，RAG 提供“知识补全”，LLM 负责编写高质量补丁。  
- 🔌 **MCP Tooling**：代码分析、沙箱执行、知识库查询都以工具的形式暴露给大模型，实现高度可控的智能代理链路。  
- 🛡️ **真正可用的自动修复**：不仅能“说”，还能“修”。所有补丁通过 OJ 沙箱验证后再输出。  
- 📚 **可扩展的缺陷知识库**：内置 300+ 缺陷与修复模板，RAG 检索助力高精度提示。  
- 📊 **专业安全/质量报告**：支持 SARIF，与 GitHub Code Scanning 对接，也可用于企业 CI。

---

## 🔧 核心功能

### 1. 🏗️ 静态分析引擎（Java / C++）
- AST 解析（JavaParser / Clang LibTooling）  
- CFG 控制流图构建  
- 数据流分析（Def-Use / 未使用变量）  
- 复杂度指标  
  - 圈复杂度  
  - 最大嵌套深度  
  - LOC  
- 常见 Bug Pattern 检测  
  - 数组越界  
  - 空指针使用  
  - 未初始化变量  
  - 死循环  
  - 不可达代码  
  - 异常分支缺失  
- 输出 SARIF/JSON 供后端或 IDE 使用  



### 2. 🔍 RAG 驱动的“代码缺陷知识库”
- 收录 300+ 缺陷描述、真实案例、修复模板  
- 使用向量检索（FAISS / Milvus）  
- 根据静态分析结果匹配最接近的缺陷类型  
- 为 LLM 注入“结构化修复知识”作为提示上下文  



### 3. 🔧 MCP 工具链集成（核心）
为智能 Agent 提供可控的外部能力：

| 工具名 | 功能 |
|-------|------|
| `run_static_analysis` | 调用静态分析器并返回结构化问题列表 |
| `retrieve_pattern` | 从向量库检索最相关缺陷与修复模板 |
| `run_sandbox` | 在 OJ 安全沙箱中执行代码验证补丁是否有效 |
| `generate_report` | 输出 Markdown / SARIF / JSON 报告 |

Agent 会自动组合这些工具完成完整诊断。



### 4. 🛠️ 自动修复系统（核心亮点）
自动修复由三层组成：

#### **① 静态分析层 → 找到问题点**  
定位风险代码块（行号 + 描述 + CFG/AST 上下文）

#### **② RAG 模板层 → 找到相关补丁策略**  
检索“最相似的缺陷模式 + 修复示例”

#### **③ LLM 生成层 → 生成实际补丁代码**  
大模型基于上述上下文输出精确修改建议与 diff 补丁。

> 所有补丁都会在沙箱中执行测试，确保可运行、可编译、可通过样例。

---

## 📄 输出报告示例
支持 3 种格式：

- **Markdown**（用于平台展示）
- **SARIF**（用于 GitHub / Azure CI 安全扫描）
- **JSON**（便于后端集成）

报告内容包括：

- 代码质量评分  
- 风险等级分类  
- 复杂度分析图  
- 缺陷列表  
- 修复建议  
- 自动修复补丁（diff 格式）  

---

## 🛠️ 技术栈

### **语言**
- Java（主要逻辑 & MCP & RAG 服务）
- C++（Clang 静态分析工具链）
- Python（可选，向量库 + 数据预处理）

### **依赖与工具**
- JavaParser / Clang LibTooling  
- FAISS / Milvus  
- Spring Boot + MCP  
- Docker 沙箱（seccomp + namespace）  
- GitHub SARIF Schema  

---

## 🧪 示例流程：一次完整的代码诊断

1. 用户上传代码 / OJ 评测失败  
2. Agent 调用 `run_static_analysis`  
3. 根据结果调用 `retrieve_pattern` 找到类似缺陷  
4. Agent 将两者融合，让 LLM 输出修复补丁  
5. `run_sandbox` 执行补丁后的代码  
6. 最终生成风险报告 + 自动修复版本  

---

## 📌 项目适用场景
- Online Judge 平台智能升级  
- IDE 代码检查插件（VSCode / JetBrains 可扩展）  
- 企业 CI 代码质量门禁  
- 安全扫描（与 SARIF/SAST 兼容）  
- 课程设计/毕业设计（非常有亮点）  
- 面向大模型的“可解释可控代码 Agent”研发  

---

## 📄 Roadmap
- [x] 多语言 AST 解析（Java / C++）  
- [x] 基础 Bug Pattern 检测  
- [x] RAG 缺陷知识库  
- [x] MCP 工具链  
- [ ] 函数间数据流分析  
- [ ] 更复杂的自动修复（跨文件 / API 重构）  
- [ ] LLM 产生自动测试用例  
- [ ] 在线 IDE 插件化  

---

## ⭐ Star This Repo
如果你觉得 CodeSage 有价值，欢迎点个 ⭐！  
你也可以提交新的缺陷模式或修复模板，成为 CodeSage 贡献者！


