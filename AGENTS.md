# Repository Guidelines

## Project Structure & Module Organization
servaaibase is a Maven-based Java 21 library. Source lives in `src/main/java/org/neo/servaaibase`, grouped by package: `factory` builds provider facades, `ifc` declares shared interfaces, `impl` holds concrete AI provider clients (OpenAI, Google, etc.), `model` captures request/response payloads, `util` aggregates helpers such as `CommonUtil`, and `NeoAIException.java` defines the domain exception. Tests mirror the main layout in `src/test/java/org/neo/servaaibase`; create new test classes beside the code they cover. Keep generated artifacts and temporary logs under `logs/` out of version control.

## Build, Test, and Development Commands
- `mvn clean package` – compile with Java 21 and assemble the distributable JAR.
- `mvn test` – execute the JUnit 5 suite via Surefire, failing fast on compilation or assertion errors.
- `mvn -Dtest=OpenAIImplTest test` – target a single test class while iterating on a provider implementation.

## Coding Style & Naming Conventions
Use 4-space indentation and place braces on the same line as class or method signatures (`public class Foo {`). Class names are `PascalCase`, methods and variables `camelCase`, and shared string keys or configuration constants `UPPER_SNAKE_CASE` (see `OpenAIImpl.gpt_5`). Prefer immutable `Map` views where possible; when mutability is required, stick to thread-safe collections such as `ConcurrentHashMap`. Keep imports explicit and avoid wildcard imports outside of test fixtures.

## Testing Guidelines
JUnit 5 (`org.junit.jupiter`) is the standard. Name test classes with the `*Test` suffix and align packages with the code under test (`impl/OpenAIImpl` ↔ `impl/OpenAIImplTest`). Write focused tests that assert behaviour rather than console output; heavily exercised utilities belong in `CommonUtilTest`. Run `mvn test` before pushing, and include regression coverage for new provider integrations or model updates.

## Commit & Pull Request Guidelines
Commits should be concise, present-tense summaries under ~60 characters (`add support for gpt-5 serials`). Reference related issues in the body when relevant. Pull requests must describe the change motivator, outline testing evidence (`mvn test` output), and attach configuration implications or screenshots when UI-affecting. Tag reviewers who own the touched packages (e.g., implementation versus util) to speed up review.
