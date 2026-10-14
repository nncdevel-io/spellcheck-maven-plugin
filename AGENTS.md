# Repository Guidelines

## Project Structure & Module Organization

This Maven plugin checks spelling with LanguageTool and reads CSpell configuration.
Production code lives in `src/main/java/io/nncdevel/maven/spellcheck/`:
`SpellCheckMojo` provides the goal, `SpellChecker` checks text, `config/` loads
configuration, and `report/` produces JUnit and Checkstyle XML.
Tests mirror these packages under `src/test/java/`.
Fixture projects live in `src/test/resources/test-projects/`.
Use `README.md` for usage examples and `cspell.json.example` for configuration.
Keep generated artifacts in `target/` out of commits.

## Build, Test, and Development Commands

Check your installed tools with `java -version` and `mvn -version`.
The POM targets Java 11; CI runs Java 11, 17, and 21.
`CLAUDE.md` still mentions Java 8, which differs from the current build settings.

- `mvn clean compile`: compile from a clean build directory.
- `mvn test`: run the unit tests.
- `mvn test -Dtest=SpellCheckMojoTest`: run one test class.
- `mvn clean verify`: run the build and configured verification lifecycle.
- `mvn clean install`: install the plugin into your local Maven repository.

After installation, configure the plugin in a consumer project's POM as shown
in the README, then run `mvn spellcheck:check` there.

## Coding Style & Naming Conventions

Use UTF-8, four spaces without tabs, and a 120-character Java line limit.
Use PascalCase classes, camelCase methods and fields, UPPER_SNAKE_CASE constants,
and lowercase packages beneath `io.nncdevel.maven.spellcheck`.
Document public classes and public/protected methods with Javadoc.
Use Maven logging rather than `System.out` or `System.err`.
No Java formatter or style-check plugin is currently configured in `pom.xml`.

## Testing Guidelines

Use JUnit 4, Mockito, and the Maven Plugin Testing Harness as appropriate.
Name tests `ClassNameTest.java` and use descriptive methods such as
`testMojoInstantiation`.
Add regression tests for changed configuration, spelling, or report behavior.
`CLAUDE.md` sets an 80% coverage target; the POM has no coverage enforcement.
Maven Invoker references `src/it`, but that directory is currently absent.
Do not describe unit-test results as integration-test coverage.

## Commit & Pull Request Guidelines

Follow the Conventional Commit pattern found in history, such as
`feat(reports): Add XML output` or `refactor: Reorganize Java packages`.
Keep each change focused.
In pull requests, explain the problem, resulting behavior, related issues,
and verification commands with results.
Update README examples when changing user-facing configuration or reports.
Exclude local overrides, IDE files, and generated build output.
