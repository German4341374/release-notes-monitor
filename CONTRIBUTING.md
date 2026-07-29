# Contributing

## Development workflow

1. Create a focused branch from `main`.
2. Keep changes small and include tests for behavior changes.
3. Run `./mvnw verify`.
4. If Docker is available, run `docker compose up --build --detach` and the documented smoke checks.
5. Update documentation when configuration or behavior changes.

## Commit messages

Use Conventional Commits:

```text
feat: add vendor release adapter
fix: handle upstream rate limit reset
test: cover pre-release comparison
docs: clarify scheduled checks
```

## Pull requests

Describe the problem, the chosen approach, test evidence, security impact, and any operational
changes. Never commit `.env`, tokens, database dumps, or production URLs.
