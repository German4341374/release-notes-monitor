# Operations runbook

## Health check fails

1. Run `docker compose ps`.
2. Inspect `docker compose logs database` and confirm PostgreSQL reports that it is ready.
3. Inspect `docker compose logs app` for Flyway or connection errors.
4. Verify `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` use the same database values as the Compose
   service.
5. Run `curl --fail http://localhost:8080/health`.

## GitHub rate limit

An unauthenticated public API request has a lower rate limit. The application records a temporary
failure and includes the reset timestamp when the response contains it.

1. Wait until the recorded reset time, or set `GITHUB_TOKEN`.
2. Use a token with only the access required to read the target repositories.
3. Restart the app after changing the environment.
4. Trigger one product manually to confirm recovery.

Never print the environment or token in diagnostic output.

## Failed static JSON check

1. Verify that the URL uses HTTP or HTTPS and has no embedded credentials.
2. Fetch the endpoint from the same network as the application.
3. Confirm the response is smaller than the configured limit.
4. Confirm the JSON contains a text field named `version`, `latestVersion`, or `tag_name`.
5. Confirm the value uses semantic-version syntax.

## Flyway validation error

Applied migrations are immutable. Restore the original migration and create a new numbered migration
for the correction. Do not use `flyway repair` without first understanding and documenting why the
checksum changed.

## Backup and restore

Create a logical backup:

```bash
docker compose exec -T database pg_dump \
  -U release_monitor -d release_monitor --format=custom > release-monitor.dump
```

Restore into an empty database:

```bash
cat release-monitor.dump | docker compose exec -T database pg_restore \
  -U release_monitor -d release_monitor --clean --if-exists
```

Test restore procedures regularly and protect backup files as production data.
