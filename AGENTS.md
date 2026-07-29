# Repository guidance

- Use English for source code, comments, documentation, and commit messages.
- Preserve the modular monolith and the `VersionSource` strategy boundary.
- Add a Flyway migration for every schema change; never edit an applied migration.
- Keep credentials and real production URLs out of the repository.
- Run `./mvnw verify` before proposing changes.
- Use Conventional Commits.
