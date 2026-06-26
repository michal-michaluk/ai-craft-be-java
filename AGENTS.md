# AGENTS

This repository uses architecture-first development. Before coding, load the relevant docs from `src/docs` and follow them strictly.

## Document Structure

The documentation is organized into focused documents covering specific aspects of the architecture:

- **@src/docs/code-structure.md**: Package layout, naming conventions, module organization — read first for project orientation
- **@src/docs/ports.md**: Primary/secondary ports and mediator-based cross-context contracts — read when adding services or repository interfaces
- **@src/docs/domain-model.md**: Domain-Driven Design patterns (Aggregates, Value Objects, Domain Events) and unit testing — read when adding or changing business logic
- **@src/docs/policy.md**: Business calculations and decision policies — read when implementing business rules
- **@src/docs/adapter-http-command.md**: Write-side HTTP controllers and command DTO mapping — read when adding REST write endpoints
- **@src/docs/adapter-http-query.md**: Read-side HTTP endpoints, pagination, and content negotiation — read when adding REST query endpoints
- **@src/docs/adapter-persistence-event-sourcing.md**: Event-sourcing repository (event store + replay) — read when implementing event-sourced persistence
- **@src/docs/adapter-persistence-document.md**: JSONB document snapshot persistence with optional event history — read when implementing document-style persistence
- **@src/docs/adapter-persistence-normalizing.md**: Normalized relational mapping adapter — read when implementing relational persistence
- **@src/docs/adapter-projection.md**: Event-driven projection / read-model update patterns — read when adding read model projections
- **@src/docs/adapter-mediator.md**: Cross-context orchestration adapter rules — read when implementing cross-context flows
- **@src/docs/context-boundaries.md**: Bounded contexts, shared kernel, dependency policy — read when adding new bounded contexts
- **@src/docs/security.md**: OAuth2 security, endpoint protection, Keycloak test setup — read when adding new endpoints
- **@src/docs/testing.md**: Test pyramid, fixtures, integration tests, custom assertions — read before writing tests
- **@src/docs/arch-unit.md**: Architecture rules and how to add/update context architecture tests — read when adding new contexts

## Mandatory Workflow

1. Identify the task type (domain change, API, persistence, projection, cross-context flow, tests).
2. Read matching doc files listed below.
3. Implement only through existing architectural patterns.
4. Add or update tests in the same context package.
5. Verify ArchUnit constraints remain valid.

If a task touches multiple contexts, read `src/docs/context-boundaries.md` and `src/docs/adapter-mediator.md` first.

## Document Map (What to read for what)

- `src/docs/code-structure.md`: package layout, naming conventions, module organization.
- `src/docs/ports.md`: primary/secondary ports and mediator-based cross-context contracts.
- `src/docs/domain-model.md`: aggregates, value objects, domain events, invariant placement.
- `src/docs/policy.md`: business calculations and decision policies (static, strategy, configured).
- `src/docs/adapter-http-command.md`: write-side HTTP controllers and command DTO mapping.
- `src/docs/adapter-http-query.md`: read-side HTTP endpoints and pagination/read payload patterns.
- `src/docs/adapter-persistence-event-sourcing.md`: event-sourcing repositories and replay/save flow.
- `src/docs/adapter-persistence-document.md`: document snapshot persistence with history.
- `src/docs/adapter-persistence-normalizing.md`: normalized relational mapping adapter.
- `src/docs/adapter-projection.md`: event-driven projection/read-model update patterns.
- `src/docs/adapter-mediator.md`: cross-context orchestration adapter rules.
- `src/docs/context-boundaries.md`: bounded contexts, shared kernel, dependency policy.
- `src/docs/security.md`: current security posture and required explicit authN/authZ decisions.
- `src/docs/testing.md`: test pyramid, fixtures, integration tests, custom asserts.
- `src/docs/arch-unit.md`: architecture rules and how to add/update context architecture tests.

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

## Available Skills

- `.agents/skills/reverse-engineering/` — Reverse-engineer architecture docs from source code. Use when asked to document architecture, patterns, or design decisions.

## Quick Task Routing

- Add or change aggregate behavior -> `domain-model.md`, `ports.md`, `testing.md`.
- Add or change business calculation/decision policy -> `policy.md`, `domain-model.md`, `testing.md`.
- Add or change REST write endpoint -> `adapter-http-command.md`, `security.md`, `testing.md`.
- Add or change REST read endpoint -> `adapter-http-query.md`, `adapter-projection.md`, `testing.md`.
- Add new persistence strategy/change mapping -> matching `adapter-persistence-*.md`, `ports.md`, `testing.md`.
- Add cross-context use-case -> `adapter-mediator.md`, `context-boundaries.md`, `arch-unit.md`.
- Add new bounded context -> `code-structure.md`, `context-boundaries.md`, `arch-unit.md`, `testing.md`.
