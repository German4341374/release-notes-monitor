# Security policy

## Reporting a vulnerability

Do not open a public issue containing exploit details, tokens, or production data. Use GitHub's
private vulnerability reporting feature when it is enabled for the repository.

Include the affected version, reproduction steps using safe sample data, expected impact, and a
suggested mitigation if available.

## Deployment guidance

- Place the application behind an authenticated reverse proxy before using it on a shared network.
- Store database credentials and the optional GitHub token in a secret manager or protected
  environment configuration.
- Restrict outbound network access to approved version sources.
- Keep Java, container images, PostgreSQL, and Maven dependencies updated.
- Back up PostgreSQL and test restore procedures.
- Do not enable verbose HTTP header logging where authorization headers could be exposed.

There is no authentication or authorization layer in this portfolio-sized application.
