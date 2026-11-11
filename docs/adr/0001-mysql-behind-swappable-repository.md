# MySQL behind a swappable repository, tested against a real MySQL

WiseSayings moved from one-JSON-file-per-WiseSaying storage to MySQL by adding `WiseSayingDbRepository` (plain JDBC) behind the existing `WiseSayingRepository` interface and swapping it in `AppContext`; controllers and services do not change, and the file repository stays as an alternative implementation. Integration tests run against a real `wise_saying_test` MySQL database (truncated before each test) rather than H2 or an in-memory fake, so the SQL that ships — `LIKE` with escaped wildcards, `ORDER BY id DESC LIMIT/OFFSET`, `AUTO_INCREMENT` that never reuses deleted Ids — is the SQL that is tested.

## Consequences

- Running the test suite requires a local MySQL with `sql/schema.sql` applied; tests fail (not skip) when it is unreachable.
- `TRUNCATE` resets `AUTO_INCREMENT`, which is what lets each test expect Id 1 again. Only test setup truncates; the dev database never does, so Ids are never reused there.
