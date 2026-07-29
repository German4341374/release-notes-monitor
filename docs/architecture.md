# Architecture decisions

## Context

The application is intended for a small application-support team. It needs a reliable shared record,
simple deployment, and clear extension points without introducing microservices or a messaging
platform.

## Modular monolith

The code is organized around products, version sources, web delivery, and infrastructure
configuration. A modular monolith keeps deployment and transactions simple while still separating
business logic from HTTP and persistence concerns.

## Strategy interface

`VersionSource` isolates vendor-specific discovery behavior. `StaticJsonVersionSource`,
`GitHubReleaseVersionSource`, and `ManualVersionSource` are selected by `CheckStrategy`. Adding a
source does not require changing the controller or persistence layer.

## Transaction boundary

Remote version discovery runs outside a database transaction. `CheckResultRecorder` opens a short
transaction only after the remote request has completed. This prevents a slow upstream service from
holding a database connection and row locks.

## Retry policy

Retries apply only to transport failures and temporary status codes. Client errors such as invalid
URLs, authentication failures, and missing releases are recorded immediately. Exponential backoff
is bounded by the configured retry count.

## PostgreSQL and Flyway

PostgreSQL provides durable concurrent access and predictable timestamp semantics. Flyway owns the
schema, while Hibernate uses `ddl-auto=validate` to detect drift without mutating production data.

## Scheduling

The Spring scheduler is sufficient for a single application instance. Manual sources are excluded.
A multi-replica deployment would require a distributed lock or an external scheduler to prevent
duplicate checks.

## Web and API delivery

Thymeleaf and the REST API share the same service layer. This avoids duplicating business rules and
keeps the UI deployable as part of one container.
