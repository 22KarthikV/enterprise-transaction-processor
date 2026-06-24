# enterprise-transaction-processor

![CI](https://github.com/22KarthikV/enterprise-transaction-processor/actions/workflows/ci.yml/badge.svg)

Production-grade enterprise transaction processor — Spring Boot, AWS via LocalStack, Docker, PostgreSQL, Redis.

Full requirements: [docs/PRD.md](docs/PRD.md)

## Architecture

```
                         ┌────────────┐
                         │   Client   │
                         └─────┬──────┘
                               │ HTTPS + JWT
                               ▼
 ┌───────────────────────────────────────────────┐       ┌──────────────┐
 │              transaction-service               │◄─────►│  PostgreSQL  │
 │   REST API · validation · auth · idempotency   │       │ transaction_db│
 └────────────────────┬────────────────────────────┘       └──────────────┘
                       │ publish                      ▲
                       ▼                               │ idempotency cache
                 ┌────────────┐                   ┌──────────┐
                 │    SQS     │                   │  Redis   │
                 │ queue + DLQ│                   └──────────┘
                 └─────┬──────┘
                       │ consume
                       ▼
 ┌───────────────────────────────────────────────┐
 │               processor-service                │
 │  currency normalisation · risk · pipeline      │
 └──────┬───────────────┬────────────────┬────────┘
        │ audit         │ archive        │ notify
        ▼               ▼                ▼
 ┌────────────┐  ┌─────────────┐  ┌─────────────┐
 │  DynamoDB  │  │     S3      │  │     SNS     │
 │   audit    │  │   archive   │  │   events    │
 └────────────┘  └─────────────┘  └─────────────┘

   All AWS services above are emulated locally via LocalStack.
   Secrets (DB password, JWT secret, FX rates) come from Secrets Manager.
```

## Prerequisites

- Java 17
- Docker + Docker Compose
- Maven (or use the bundled `./mvnw`)

## Quick start

```bash
git clone https://github.com/22KarthikV/enterprise-transaction-processor.git
cd enterprise-transaction-processor

# optional: copy and edit if you want non-default ports/credentials
cp .env.example .env

# start Postgres, Redis, and LocalStack (auto-provisions SQS/SNS/DynamoDB/S3/Secrets Manager)
docker compose up -d

# build all modules
./mvnw clean install -DskipTests

# run a service
java -jar transaction-service/target/transaction-service-0.0.1-SNAPSHOT.jar
```

The REST API (`POST /api/v1/transactions`) ships in Phase 1 — see
[open issues](https://github.com/22KarthikV/enterprise-transaction-processor/issues)
for current progress.

## Environment variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `POSTGRES_DB` | `transaction_db` | Database name |
| `POSTGRES_USER` | `postgres` | Database user |
| `POSTGRES_PASSWORD` | `changeme` | Database password (local only — real secrets come from Secrets Manager in Phase 7) |
| `POSTGRES_PORT` | `5432` | Host port for Postgres |
| `REDIS_PORT` | `6379` | Host port for Redis |
| `LOCALSTACK_PORT` | `4566` | Host port for the LocalStack gateway |
| `LOCALSTACK_DEBUG` | `0` | Set to `1` for verbose LocalStack logs |
| `AWS_DEFAULT_REGION` | `us-east-1` | Region used for all LocalStack resources |

## Project status

- [x] Phase 0 — Foundation and DevOps skeleton
- [ ] Phase 1 — Transaction ingestion
- [ ] Phase 2 — SQS event publishing
- [ ] Phase 3 — Business processing engine
- [ ] Phase 4 — Audit logging
- [ ] Phase 5 — SNS notifications
- [ ] Phase 6 — Idempotency and resilience
- [ ] Phase 7 — Secret management
- [ ] Phase 8 — Observability
- [ ] Phase 9 — BDD and SonarQube gate

See [milestones](https://github.com/22KarthikV/enterprise-transaction-processor/milestones) for detail.
