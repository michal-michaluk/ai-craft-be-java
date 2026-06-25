# Group 2: Agent Harness & Runtime Architecture

---

## Agent Harness

**Basic:** The agent harness is a safety cage and supervisor that wraps around the AI — it runs the AI's code in a sandbox, checks for dangerous commands, and makes sure the AI stays within its boundaries.

**Intermediate:** The agent harness is the deterministic, stateful software scaffolding that encapsulates the probabilistic language model, establishing a controlled runtime environment with explicit operational boundaries and system invariants. It is the architectural layer that translates the model's raw text or structured output (function calls, JSON) into safe, executable system operations. The harness manages the complete lifecycle: it injects the system prompt (including AGENTS.md rules and constraints), maintains conversation state, intercepts tool calls emitted by the model, validates them against security policies, executes them in sandboxed environments (containers, restricted shells), collects results, and feeds them back into the model's context. Critically, the harness is deterministic and predictable — it is the reliable foundation upon which the unpredictable model operates. Without a harness, a model is just a text generator; with a harness, it becomes an autonomous agent capable of executing real work. Quality of the harness design directly determines safety, reliability, and debuggability of the overall system.

**Synonyms:** Agent scaffold, execution wrapper, deterministic runtime shell, system scaffolding.

**Example:** The OpenCode agent harness wraps around the LLM, intercepts every emitted bash tool call, validates it against a configured allow/deny list, and executes it inside a temporary Docker container before returning stdout/stderr to the model.

---

## Agent

**Basic:** An agent is the AI plus its tools and rules working together — not just the brain (the model), but the whole package that can actually do things like read files or run commands.

**Intermediate:** An agent is an LLM instance instantiated within a harness with a specific role, a defined tool set, a restricted context window, and a targeted instruction set. The agent is the atomic operational unit in an AI-native system: it receives a goal, processes available context, determines a plan, emits tool calls or text, receives results, and iterates until the goal is met or a terminal state is reached. Unlike a raw model which simply predicts the next token, an agent operates within a feedback loop — its outputs have consequences in the real system (files modified, commands executed, databases queried), and it can observe those consequences to guide subsequent actions. The distinction between model and agent is critical: the model is probabilistic and stateless; the agent is structured and stateful. Multiple agents can coexist in a system, each with specialized capabilities, role definitions, and access boundaries.

**Synonyms:** AI agent, autonomous agent, agentic system, agent instance, reasoning agent.

**Example:** In a software development workflow, a "Planner agent" has read-only access to the codebase and architecture documents, while a "Developer agent" has read-write access to source files but cannot modify CI/CD configuration.

---

## Multi-Agent Loop

**Basic:** A multi-agent loop is an assembly line where several specialized AI bots pass work to each other — one plans, another codes, a third reviews, and they go back and forth until everything is correct.

**Intermediate:** A multi-agent loop is a choreographed, asynchronous state machine where multiple specialized agent instances collaborate recursively to complete complex tasks that exceed the capability of any single agent. Each agent is allocated an explicit role, a restricted context window, distinct tools, and a targeted instruction set — this specialization allows each agent to focus deeply on its domain rather than splitting attention across disparate responsibilities. The loop structure typically follows a pattern: a "Planner" agent decomposes a high-level goal into subtasks; a "Developer" agent executes coding tasks; a "Reviewer" agent critiques the output against quality criteria; a "Tester" agent runs verification gates. Results from each agent feed into the next, and failures can trigger re-planning or re-execution. This role-specialization mirrors human team structures and provides natural error detection — the reviewer catches what the developer misses because it is optimized for a different cognitive function.

**Synonyms:** Agentic swarm, multi-agent system, recursive task loops, agent orchestration, swarm architecture.

**Example:** OpenCode coordinates a Planner agent (reads architecture docs, creates a refactoring plan), a Developer agent (executes the code changes), and a Reviewer agent (runs linting and type-checking, catches issues, sends them back to Developer) in a loop until all checks pass.

---

## Tool Calling / Function Calling

**Basic:** Tool calling lets the AI stop typing and actually do things — like asking the computer to search a file, run a database query, or open a website — instead of just guessing what the answer might be.

**Intermediate:** Tool calling (also known as function calling) is the deterministic mechanism through which a probabilistic language model interacts with external systems. During generation, when the model determines that it requires external capabilities — retrieving a file, executing a command, querying an API — it halts token generation and emits a structured JSON payload that conforms to a pre-defined schema exposed in the system prompt. The harness intercepts this payload, validates it against the schema, executes the corresponding function in the deterministic runtime, captures the result (success, data, or error), and injects it back into the model's context for the next generation step. This creates a tight feedback loop between the model's probabilistic reasoning and the deterministic system. The tool schemas define the complete contract: function name, parameter types, required fields, and documentation that the model uses to understand when and how to invoke each tool. The quality and clarity of these schemas directly impact the model's ability to use tools correctly.

**Synonyms:** Function invocation, structured output routines, tool interception, model action triggers, JSON-mode execution.

**Example:** When GitHub Copilot needs to find a file, the model emits `{"function": "search_files", "parameters": {"pattern": "**/*.ts", "query": "DatabaseConnection"}}` — the IDE intercepts this, runs a local file search, and returns the results to continue the generation.

---

## Model Context Protocol (MCP)

**Basic:** MCP is a universal plug-and-play connector that lets any AI tool talk to any data source or service — like a USB-C port but for AI tools instead of devices.

**Intermediate:** The Model Context Protocol (MCP) is an open-standard, client-server protocol that standardizes how AI applications discover, connect to, and interact with external data sources and tools. Before MCP, every AI tool integration required custom adapter code — each model provider, each data source, each tool had its own bespoke integration pattern. MCP introduces a uniform protocol where a host application (the MCP client) connects to MCP servers that expose resources (data), tools (executable capabilities), and prompts (templates). The server advertises its capabilities, and the host routes these to the model's tool-calling system via standardized schema definitions. This abstraction allows tool builders to write one MCP server implementation and instantly make it available to any MCP-compatible AI application. MCP is analogous to what USB or Bluetooth did for hardware peripherals — a universal standard replacing a tangle of proprietary connectors.

**Synonyms:** MCP, AI tool protocol, standardized tool interface, agent capability protocol.

**Example:** An MCP server wrapping the GitHub API exposes "list_issues", "create_pr", "search_code" as standardized tools. Any MCP-compatible agent — whether using GPT, Claude, or Gemini — can automatically discover and use these tools without custom integration code.

---

## Skills

**Basic:** Skills are plug-in modules that teach the AI how to do specific tasks — like adding a "spreadsheet ninja" skill that gives the AI formulas and spreadsheet knowledge.

**Intermediate:** Skills are modular, declarative capability packages loaded into an agent's runtime through the Model Context Protocol or similar mechanism. Each skill encapsulates a specific domain of expertise with its own set of tools, instructions, rules files, and optionally specialized prompts or few-shot examples. A skill might cover "marketing analysis" — bringing in tools for web search, pricing analysis, and content extraction, along with instructions on how to structure a competitive analysis report. Skills are composable: an agent can be loaded with multiple skills simultaneously, with the harness managing potential conflicts, tool name collisions, and instruction prioritization. The skill paradigm allows organizations to build and maintain reusable capability libraries that can be shared across teams, projects, and agent instances, reducing duplication and ensuring consistency.

**Synonyms:** Capability modules, agent skills, domain expertise packages, plugin modules.

**Example:** The `brand-research` skill loads web search tools, competitor analysis templates, and structured output schemas — any agent that loads this skill gains the ability to perform comprehensive competitive brand research without manual configuration.

---

## AGENTS.md

**Basic:** AGENTS.md is a note you leave at the root of your project folder that tells the AI who it is, what rules to follow, and what parts of the project it's allowed to touch.

**Intermediate:** AGENTS.md is a repository-level manifest file that explicitly defines agent personas, behavioral constraints, tool access boundaries, and project-specific conventions for any AI agent operating within the repository. The file is read by the agent harness at startup and its contents are injected into the system prompt, forming the persistent foundation of the agent's operational context. It typically contains: role definition ("You are a senior software engineer specializing in Rust backend systems"), architectural rules ("All new code must follow hexagonal architecture patterns"), file access permissions ("Read-only access to docs/ directory, read-write to src/"), coding standards ("Use 2-space indentation, prefer Result over exceptions"), tool invocation policies ("Never run destructive git commands"), and project-specific conventions ("Use the `crates/*` workspace structure"). AGENTS.md serves the same function as a README for human developers but targets the agent runtime instead.

**Synonyms:** Agent manifest, agent configuration, persona definition, agent policy file.

**Example:** A project's AGENTS.md specifies "You are a TypeScript/React specialist. You have read-write access to src/ and tests/. Before proposing database schema changes, you must consult the architect." — the harness reads this and configures the agent's system prompt accordingly.

---

## Rules Files / Constraints

**Basic:** Rules files are rulebooks you place in different folders of your project to tell the AI exactly what style and rules apply in each part — stricter rules for core libraries, looser rules for prototypes.

**Intermediate:** Rules files are hierarchical, declarative configuration documents deployed at various directory levels within a repository to enforce behavioral invariants, style compliance, architectural boundaries, and security policies on AI agents. The harness parses these files in a bottom-up or top-down resolution order and injects them into the system prompt at the appropriate priority level. Common patterns include a root-level rules file defining organization-wide standards (coding conventions, banned patterns, security requirements), team-level rules in subdirectories (team-specific library usage, API design conventions), and module-level rules in package directories (export restrictions, dependency constraints, test requirements). This hierarchical system mirrors the directory structure of the codebase, allowing granular control — core libraries can have strict, minimal rules while prototype or experimental directories have permissive rules.

**Synonyms:** Constraint files, policy manifests, behavioral invariants, directive configurations, AI linting rules.

**Example:** A repository places `RULES.md` at the root specifying "Never use `any` type in TypeScript" and `services/RULES.md` specifying "All service classes must implement the `Service` trait" — both files are parsed by the harness and injected into every agent prompt operating on the relevant paths.

---

## Orchestration Framework

**Basic:** The orchestration framework is the traffic control system that routes tasks between agents, saves conversation history, and manages the overall flow of a complex AI workflow.

**Intermediate:** An orchestration framework is the specialized execution engine responsible for managing state graphs, directed acyclic graphs (DAGs), conditional routing, context propagation, and lifecycle management across multi-agent systems and foundation model calls. It provides the fundamental runtime primitives upon which agent harnesses are built: state persistence (checkpointing conversation state to survive failures), retry policies with exponential backoff, parallel execution of independent branches, fan-out/fan-in patterns for multi-agent coordination, conditional branching based on tool outputs, human-in-the-loop escalation points, and observability hooks for tracing. Modern orchestration frameworks implement a state machine or graph abstraction where nodes represent agent invocations, tool calls, or human approval steps and edges represent data flow and control flow between them. The framework handles the serialization, deserialization, and routing that makes complex multi-step workflows reliable and reproducible.

**Synonyms:** Agent runtime, state graph engine, workflow orchestrator, execution framework, workflow DAG engine.

**Example:** An OpenCode workflow orchestration framework routes a "refactor this module" task through nodes: Planner → Code Review (parallel) → Developer → Tester → Human Approval → Merge, with automatic retry if the Tester node reports compilation errors.

---

## Plan-Execute-Verify (PEV) Loop

**Basic:** The PEV loop is a cycle where the AI first plans what to do, then does it, then checks if it worked — and if not, it reads the error and tries again with a better plan.

**Intermediate:** The Plan-Execute-Verify loop is a deterministic self-correction circuit that structures an agent's execution into three explicit sequential phases. In the Plan phase, the agent analyzes the current state and goal, decomposes the work into explicit steps, and presents a plan for confirmation. In the Execute phase, the agent carries out the planned actions via tool calls — writing code, modifying files, running commands. In the Verify phase, the agent runs validation gates: compilation checks, test execution, lint analysis, or user-defined assertions. If verification fails, the error output is fed back into the Plan phase, and the loop repeats with the agent using the failure information to correct its approach. This is fundamentally different from naive generation because the agent has a mechanism to detect and recover from its own mistakes autonomously. The loop terminates when verification passes or a maximum retry limit is reached. PEV transforms the agent from a "generate and hope" system into a "generate, test, and fix" system.

**Synonyms:** Corrective generation loop, self-healing circuit, actor-critic loop, iterative refinement, code-test-fix cycle.

**Example:** An OpenCode refactoring agent in a PEV loop: Plan → "I will extract the database logic into a Repository class", Execute → writes the code, Verify → runs `cargo check` which fails with a borrow checker error, Plan → "I see the lifetime issue, I will restructure to avoid shared references", Execute → fixes, Verify → `cargo test` passes, exit.

---

## Deterministic vs. Probabilistic Boundary

**Basic:** This boundary is the line between the AI's squishy, creative thinking side and the computer's rigid, rules-based side — it's where the AI's ideas get turned into actual commands.

**Intermediate:** The deterministic vs. probabilistic boundary is the architectural seam within an agentic system where the model's inherently stochastic, probabilistic output is transformed into deterministic, verifiable system actions. On one side of the boundary, the model operates in token-probability space — it generates distributions over possible next tokens, samples from these distributions, and produces text or structured JSON that may contain errors, hallucinations, or creative interpretations. On the other side, the harness and tools operate in deterministic space — file systems have exact states, compilers produce definitive error messages, APIs return specific responses. The boundary is enforced by the harness through schema validation of tool calls, policy checks before execution, output sanitization, and sandboxing. Understanding and explicitly managing this boundary is arguably the most critical design concern in building reliable agentic systems: failures on the deterministic side (file corruption, security breaches) are far more consequential than failures on the probabilistic side (hallucinated function names, incorrect reasoning).

**Synonyms:** Stochastic-deterministic seam, model-system boundary, probability-reality interface, LLM-harness contract.

**Example:** When the model probabilistically decides to call `fs.write_file` with content it generated, the harness catches this at the deterministic boundary, checks the target path against the access control policy (deterministic), validates the content against no-secrets rules (deterministic), and only then executes the write (deterministic).
