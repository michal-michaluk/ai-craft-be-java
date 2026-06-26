# AGENTS

This repository uses architecture-first development. Before coding, load the relevant docs from `src/docs` and follow them strictly.

## Mandatory Workflow

1. Identify the task type (domain change, API, persistence, projection, cross-context flow, tests).
2. Read matching doc files listed below.
3. Implement only through existing architectural patterns.
4. Add or update tests in the same context package.
5. Verify ArchUnit constraints remain valid.

If a task touches multiple contexts, read `src/docs/context-boundaries.md` and `src/docs/adapter-mediator.md` first.

## Document Map (What to read for what)

TODO

## Hard Constraints

- Keep bounded context internals package-private by default.
- Access the aggregate lifecycle through services, not directly from adapters.
- Do not call sibling context repositories/services directly; use mediator + contract.
- Do not expose new shared kernel types without an explicit architecture test update.
- Keep controllers thin: validation + mapping + service call only.
- Keep business invariants in aggregates.
- Keep repository ports domain-oriented (no JPA entities in port signatures).

## Implementation NOGO

- No cross-context imports of internal model classes.
- No business logic inside controllers, projections, or mediators.
- No direct write-model queries for read API when projection exists.
- No weakening ArchUnit rules to make code compile.
- No security assumptions from dependencies only; each new endpoint needs explicit security decision.

## Testing NOGO

- No skipping architecture tests for new contexts.
- No testing domain logic only through integration tests.
- No mutable shared fixture instances across tests.

