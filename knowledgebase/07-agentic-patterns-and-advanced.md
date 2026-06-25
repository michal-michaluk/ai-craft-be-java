# Group 7: Agentic Patterns & Advanced Concepts

---

## Forward Deployed Engineer

**Basic:** A Forward Deployed Engineer is a hybrid role — part developer, part consultant, part teacher — who uses AI tools to deliver software faster and helps the client's team learn to do it themselves.

**Intermediate:** A Forward Deployed Engineer (FDE) is an emerging professional role that combines deep technical delivery capability with client advisory and knowledge transfer expertise, operating at the intersection of consulting and software engineering. Unlike traditional consultancies that separate delivery (developers) from advisory (architects) from training (instructors), the FDE is a "Pi-shaped" professional who can directly produce production-quality code, understand the client's business domain deeply enough to make architectural decisions, and simultaneously transfer capabilities to the client's team. The FDE role is enabled by AI-augmented delivery: AI tools handle boilerplate generation, test creation, documentation, and routine coding — freeing the FDE to focus on architecture, domain modeling, knowledge transfer, and quality assurance. This role represents a fundamental shift in the consulting business model — from selling "bodies" (staff augmentation) to selling "capabilities" (enablement and outcomes). Bottega's enablement & mentoring workflow is explicitly designed around FDE-style engagement.

**Synonyms:** AI-augmented consultant, hybrid engineer-educator, delivery enabler, client-side architect, Pi-shaped engineer.

**Example:** Bottega deploys a Forward Deployed Engineer to a Polish bank for 3 months — the FDE spends 50% of time building an AI-assisted testing framework (delivery), 30% pairing with the bank's developers (knowledge transfer), and 20% documenting the new system and creating training materials (enablement).

---

## Pi-Shaped Expert

**Basic:** A Pi-shaped expert knows two different things deeply AND can teach others — shaped like the letter π with two deep columns of knowledge and a horizontal bar of teaching skill connecting them.

**Intermediate:** A Pi-shaped expert is a professional with deep expertise in two distinct domains (the vertical pillars of the π) combined with the ability to transfer knowledge, teach, and enable others (the horizontal bar connecting the pillars). This is distinguished from an I-shaped expert (deep in one domain only) and a T-shaped expert (deep in one, broad across many). In the AI-SDLC context, the ideal Pi profile is: pillar 1 = software engineering and system architecture (DDD, clean architecture, testing, DevOps), pillar 2 = AI/ML engineering (prompt engineering, RAG, agent harness design, evaluation), and the horizontal bar = teaching, mentoring, consulting, and communication skills. The Pi-shaped expert is the core consulting unit that Bottega deploys because they can architect the solution (pillar 1), design the AI integration (pillar 2), and transfer both capabilities to the client's team (horizontal bar) — eliminating the handoff problems that plague multi-specialist teams.

**Synonyms:** Dual-deep expert, pi-skilled professional, deep generalist with teaching ability, expert-enabler.

**Example:** A Bottega consultant is a Pi-shaped expert: they have 15 years of Java/Spring/DDD architecture experience (pillar 1), 3 years of practical LLM/agent-system implementation (pillar 2), and 10 years of training delivery experience (horizontal bar) — they can single-handedly architect, implement, and teach an AI-augmented SDLC transformation.

---

## Agentic Swarm

**Basic:** An agentic swarm is a team of specialized AI agents working together like an ant colony — each one has a specific job, they talk to each other, and the colony can solve problems no single ant could handle alone.

**Intermediate:** An agentic swarm is a multi-agent architecture where multiple specialized LLM-powered agents collaborate asynchronously on complex tasks, each with distinct roles, tools, context windows, and instruction sets. The swarm architecture mirrors human team structures: a Planner agent decomposes high-level goals, a Developer agent writes code, a Reviewer agent critiques for quality, a Tester agent runs verification gates, a Documentation agent keeps living docs updated. Agents communicate through shared state (workspace files, message queues, structured outputs) rather than direct LLM-to-LLM conversation. The key architectural patterns in swarms include: fan-out (one coordinator delegates subtasks to many workers), fan-in (many workers report results back to a coordinator), pipeline (agents pass work sequentially like an assembly line), and debate (multiple agents propose solutions and a judge selects the best). Swarm architectures are emerging as the dominant pattern for complex software engineering tasks because they decompose the cognitive load across specialized agents, each with a focused context window, rather than forcing one agent to hold the entire problem in its attention.

**Synonyms:** Multi-agent system, agent collective, swarm intelligence, agent federation, collaborative agent architecture.

**Example:** A swarm handles a "migrate authentication from JWT to OAuth2" task: Analyst reads the current auth code and documents the migration plan, Architect designs the OAuth2 integration points, Developer implements the changes in three files, Reviewer checks for security regressions, Tester runs the test suite — all coordinated through the orchestration framework.

---

## Self-Healing Code

**Basic:** Self-healing code means the AI writes code, runs the compiler, reads the error message, and fixes its own mistakes automatically — over and over — until the code compiles and passes tests.

**Intermediate:** Self-healing code is a pattern enabled by the Plan-Execute-Verify loop where the agent autonomously detects, diagnoses, and corrects errors in its own generated code without human intervention. The process is iterative: the agent generates code, executes compilation or testing gates, captures error output (compiler errors, test failures, lint violations, type errors), analyzes the error messages to determine root cause, modifies its plan or approach to address the identified issues, regenerates the code, and re-runs verification. This loop continues until all gates pass or a maximum retry limit is reached. Self-healing is particularly powerful because most generated coding errors are structural (wrong imports, type mismatches, missing methods, API misuse) that produce clear, deterministic error messages — the agent can use these error messages as precise feedback signals to correct its output. The pattern dramatically increases the reliability of AI-generated code without requiring the model to be perfect on the first attempt.

**Synonyms:** Autonomous error correction, code-fix loop, iterative compilation, self-repairing generation, auto-correcting agent.

**Example:** An agent generates a TypeScript function that uses `Array.flatMap()` but forgets the import — `tsc` emits "Cannot find name 'flatMap'." — the agent reads the error, adds the correct import statement, re-runs `tsc`, which now passes, and the final code is committed.

---

## Living Documentation

**Basic:** Living documentation is documentation that writes itself and stays up to date — every time the code changes, the AI automatically updates the docs to match, so they never go stale.

**Intermediate:** Living documentation is an AI-maintained documentation system that evolves continuously alongside the codebase, automatically generated and updated based on actual code state, architecture decisions, and development activity. Unlike traditional documentation that is manually written and inevitably goes stale (documentation drift), living documentation is produced by agents that observe the codebase, extract meaningful descriptions from code structure (function signatures, type definitions, module organization, architectural patterns), and maintain documents that are always synchronized with the current state of the system. The system operates through: change-triggered regeneration (when a PR modifies a module, the documentation agent updates the relevant docs), architecture extraction (agents read class hierarchies, interface implementations, and module dependencies to maintain architectural documentation), and decision capture (agents record and link ADRs to the affected code). Living documentation is a core differentiator for Bottega's AI-SDLC offering because it directly addresses the bus-factor and knowledge-retention problems that plague organizations with legacy systems and long-tenured teams.

**Synonyms:** Self-maintaining docs, AI-generated documentation, automated knowledge base, code-synced documentation.

**Example:** A developer refactors a payment processing module — renames the main class and changes the public API. The living documentation agent detects the changes in the git diff, reads the new class structure, updates the architecture documentation, revises the API reference, and notes the rationale (extracted from the commit message) — all before the developer opens the PR.

---

## Retrieval-Augmented Hydration

**Basic:** Retrieval-augmented hydration is like injecting the AI with project steroids — before it starts thinking, it gets injected with all the relevant context from your project (git history, runtime metrics, database schema) so it knows exactly what's going on.

**Intermediate:** Retrieval-Augmented Hydration is an advanced context engineering technique where the agent's attention matrix is pre-seeded with a rich, multi-dimensional injection of external metadata — repository git history (recent commits, blame information, branch status), runtime state (deployment environment, active configurations, log streams), dependency graphs (transitive dependencies, version conflicts), telemetry data (error rates, latency, resource usage), and developer signals (branch status, PR review state, CI/CD pipeline results). This goes beyond simple RAG (which retrieves static knowledge base chunks) by dynamically assembling a real-time operational snapshot of the entire system and injecting it into the model's context before task execution. The result is an agent that is aware not just of the code's structure but of its operational context: it knows which branch it's on, what the CI pipeline says, which deployment environment is active, and what the recent error trends look like — enabling contextually appropriate decisions.

**Synonyms:** Operational context injection, runtime state augmentation, dynamic context enrichment, telemetry-aware prompting.

**Example:** Before modifying a production incident response agent, the hydration system injects: the last 50 git log entries (showing a hotfix branch), the current PagerDuty alert status (a P2 incident is active), the last 100 lines of the error log (showing a NullPointerException pattern), and the deployment topology (production is on Kubernetes cluster prod-eu-1) — the agent knows to prioritize stability over refactoring given the active incident.

---

## Attention Degradation

**Basic:** Attention degradation means the AI gets fuzzy and forgetful when you give it too much text at once — it remembers the first and last things you said but loses detail on what was in the middle.

**Intermediate:** Attention degradation (also known as the "lost in the middle" effect) refers to the phenomenon where language models exhibit significantly worse performance on information presented in the middle of a long context window compared to information at the beginning or end. This is an empirically well-documented limitation of Transformer architectures: the model's attention mechanism processes the full context, but the positional encoding and attention distribution cause the model to systematically under-weight tokens in the middle region. The practical implications for AI-SDLC are substantial: critical information — architectural rules, method implementations, test cases — placed in the middle of a long prompt may be effectively invisible to the model. Mitigation strategies include: placing the most important information at the start or end of the context, using structured prompting with clear section delimiters and emphasis markers, chunking long inputs into smaller self-contained segments, reducing total context length through aggressive pruning, and using attention-prioritizing prompt patterns like XML tags or numbered lists.

**Synonyms:** Lost in the middle, positional bias, context decay, mid-context blindness, attention dropping.

**Example:** An agent is given a 60,000-token context containing 30 source files — the critical configuration class is file #15 (exactly in the middle). The agent consistently ignores the configuration settings and generates code with wrong paths. Restructuring the prompt to place the configuration at the start solves the problem.
