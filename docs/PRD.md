# Product Requirements — Enterprise Transaction Processor

## Problem

Demonstrate a production-grade, event-driven transaction processing system using
the patterns an enterprise Java team actually relies on: layered Spring Boot
services, async processing via AWS messaging, an immutable audit trail,
externalised secrets, and observability — all runnable locally against
LocalStack so the whole stack works without a real AWS account.

## Scope by phase

| Phase | Goal |
|-------|------|
| 0 | `docker compose up` brings up a working environment. CI pipeline exists. |
| 1 | POST a transaction — validated, persisted, returned. Full stack working. |
| 2 | Accepted transaction triggers an SQS message. Processor picks it up. |
| 3 | Processor applies business rules. Transaction reaches COMPLETED or FAILED. |
| 4 | Every state transition logged to DynamoDB. Completed transactions archived to S3. |
| 5 | Completion and failure events broadcast to multiple subscribers via SNS. |
| 6 | Duplicate requests handled. System resilient to downstream failures. |
| 7 | Zero hardcoded credentials. All secrets from Secrets Manager. |
| 8 | Metrics, logs, health checks all working. System fully observable. |
| 9 | Behaviour-driven tests. SonarQube gate enforced in CI. |

Tracked as [milestones](https://github.com/22KarthikV/enterprise-transaction-processor/milestones)
and [issues](https://github.com/22KarthikV/enterprise-transaction-processor/issues) in this repo.

## Out of scope

- Deploying to real AWS — LocalStack is the only target environment.
- Live FX rate feeds — exchange rates are static, configurable values.
- Multi-tenant auth — single JWT-secured API surface.
- A hosted SonarQube instance — SonarCloud analysis is wired but optional until `SONAR_TOKEN` is configured.
