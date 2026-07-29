INSERT INTO products
    (name, vendor, installed_version, latest_version, version_source_url, check_strategy,
     last_checked_at, last_check_status, notes)
VALUES
    ('Spring Boot', 'Broadcom', '4.0.7', '4.1.0', 'https://github.com/spring-projects/spring-boot',
     'GITHUB_RELEASES', CURRENT_TIMESTAMP - INTERVAL '30 minutes', 'UPDATE_AVAILABLE',
     'Core application framework. Review migration notes before upgrading.'),
    ('Testcontainers', 'AtomicJar', '2.0.5', '2.0.5', 'https://github.com/testcontainers/testcontainers-java',
     'GITHUB_RELEASES', CURRENT_TIMESTAMP - INTERVAL '50 minutes', 'UP_TO_DATE',
     'Integration test infrastructure.'),
    ('PostgreSQL', 'PostgreSQL Global Development Group', '18.3', '18.4', NULL,
     'MANUAL', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'UPDATE_AVAILABLE',
     'Database patch tracked manually after compatibility review.'),
    ('Nginx', 'F5', '1.28.0', '1.28.0', NULL,
     'MANUAL', CURRENT_TIMESTAMP - INTERVAL '4 hours', 'UP_TO_DATE',
     'Reverse proxy baseline.'),
    ('OpenJDK', 'Eclipse Adoptium', '25.0.4', '25.0.4', NULL,
     'MANUAL', CURRENT_TIMESTAMP - INTERVAL '1 day', 'UP_TO_DATE',
     'LTS runtime used by the support platform.'),
    ('Grafana', 'Grafana Labs', '12.0.0', '12.1.1', 'https://github.com/grafana/grafana',
     'GITHUB_RELEASES', CURRENT_TIMESTAMP - INTERVAL '2 days', 'UPDATE_AVAILABLE',
     'Dashboard platform; plugins require compatibility validation.'),
    ('Internal Agent', 'Example Operations', '3.4.2', NULL, 'https://versions.example.test/agent.json',
     'STATIC_JSON', NULL, 'UNKNOWN',
     'Example static JSON source. Replace with a reachable internal endpoint.'),
    ('Legacy Connector', 'Example Operations', '1.9.0', NULL, NULL,
     'MANUAL', CURRENT_TIMESTAMP - INTERVAL '7 days', 'CHECK_FAILED',
     'Manual follow-up required after the previous check failed.');

INSERT INTO product_check_history
    (product_id, checked_at, installed_version, detected_version, status, message)
SELECT id, last_checked_at, installed_version, latest_version, last_check_status,
       CASE
           WHEN last_check_status = 'UPDATE_AVAILABLE' THEN 'A newer version is available.'
           WHEN last_check_status = 'UP_TO_DATE' THEN 'Installed version is current.'
           WHEN last_check_status = 'CHECK_FAILED' THEN 'The previous source check failed.'
           ELSE 'No successful check has been recorded.'
       END
FROM products
WHERE last_checked_at IS NOT NULL;
