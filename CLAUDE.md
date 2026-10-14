# CLAUDE.md - AI Assistant Guide for spellcheck-maven-plugin

> **Last Updated**: 2025-11-13
> **Project Type**: Maven Plugin
> **Language**: Java
> **Build Tool**: Apache Maven

## Table of Contents

- [Project Overview](#project-overview)
- [Current State](#current-state)
- [Repository Structure](#repository-structure)
- [Development Workflow](#development-workflow)
- [Maven Plugin Development Guidelines](#maven-plugin-development-guidelines)
- [Code Conventions](#code-conventions)
- [Testing Guidelines](#testing-guidelines)
- [Git Workflow](#git-workflow)
- [Common Tasks](#common-tasks)

---

## Project Overview

**spellcheck-maven-plugin** is a Maven plugin designed to provide spell-checking capabilities for Maven-based projects.

### Purpose

This plugin will integrate spell-checking into the Maven build lifecycle, allowing developers to catch spelling errors in documentation, comments, and potentially source code during the build process.

### Key Features (Planned)

- Integration with Maven build lifecycle
- Configurable spell-check rules
- Support for multiple languages/dictionaries
- Custom dictionary support
- Reporting capabilities

---

## Current State

**Status**: 🚀 Initial Setup Phase

The repository is currently in its initial state with only a README.md file. The project structure and core implementation are yet to be created.

### What Exists

- `README.md` - Basic project identification file
- Git repository initialized
- Remote origin configured

### What Needs to Be Created

- [ ] Maven project structure (`pom.xml`)
- [ ] Source directories (`src/main/java`, `src/test/java`)
- [ ] Maven Mojo classes
- [ ] Configuration classes
- [ ] Unit and integration tests
- [ ] Documentation

---

## Repository Structure

### Expected Structure (Once Developed)

```text
spellcheck-maven-plugin/
├── .git/                           # Git repository data
├── .gitignore                      # Git ignore patterns
├── README.md                       # Project overview and usage
├── CLAUDE.md                       # This file - AI assistant guide
├── LICENSE                         # Project license
├── pom.xml                         # Maven project descriptor
├── src/
│   ├── main/
│   │   ├── java/                   # Java source code
│   │   │   └── io/nncdevel/maven/spellcheck/
│   │   │       ├── SpellCheckMojo.java          # Main Mojo class
│   │   │       ├── SpellChecker.java            # Core spell-check logic
│   │   │       ├── config/                      # Configuration classes
│   │   │       ├── dictionary/                  # Dictionary management
│   │   │       ├── report/                      # Reporting functionality
│   │   │       └── util/                        # Utility classes
│   │   └── resources/
│   │       ├── dictionaries/                    # Default dictionaries
│   │       └── META-INF/
│   │           └── maven/
│   │               └── plugin.xml               # Plugin descriptor (if not using annotations)
│   └── test/
│       ├── java/                   # Test source code
│       │   └── io/nncdevel/maven/spellcheck/
│       │       ├── SpellCheckMojoTest.java
│       │       └── integration/                 # Integration tests
│       └── resources/
│           └── test-projects/                   # Sample projects for testing
├── target/                         # Build output (gitignored)
└── .mvn/                           # Maven wrapper (optional)
```

### Key Directories

- **`src/main/java/`**: Main Java source code for the plugin
- **`src/main/resources/`**: Resources including dictionaries, configuration files
- **`src/test/java/`**: Unit and integration tests
- **`src/test/resources/`**: Test resources and sample projects
- **`target/`**: Build output directory (should be gitignored)

---

## Development Workflow

### Prerequisites

Before starting development, ensure:

- JDK 17 or higher is installed
- Maven 3.8.1+ is installed
- Git is configured

### Initial Setup Steps

When setting up the project structure:

1. **Create `pom.xml`** with proper Maven plugin parent/dependencies
2. **Set up directory structure** following Maven conventions
3. **Configure `.gitignore`** for Java/Maven projects
4. **Implement basic Mojo class** extending `AbstractMojo`
5. **Add Maven plugin annotations** (`@Mojo`, `@Parameter`, etc.)

### Build Process

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Run integration tests
mvn verify

# Install to local repository
mvn install

# Deploy (when ready)
mvn deploy
```

### Testing Your Plugin

To test the plugin during development:

```bash
# Install to local repo
mvn clean install

# Use in a test project
cd /path/to/test-project
mvn io.nncdevel.maven:spellcheck-maven-plugin:1.0-SNAPSHOT:check
```

---

## Maven Plugin Development Guidelines

### Maven Mojo Basics

A Mojo (Maven Plain Old Java Object) is the core execution unit of a Maven plugin.

#### Essential Annotations

```java
@Mojo(name = "check",
      defaultPhase = LifecyclePhase.VERIFY,
      requiresProject = true,
      threadSafe = true)
public class SpellCheckMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "spellcheck.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(property = "spellcheck.encoding", defaultValue = "${project.build.sourceEncoding}")
    private String encoding;

    @Component
    private BuildPluginManager pluginManager;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // Implementation
    }
}
```

### Key Maven Plugin Concepts

1. **Lifecycle Phases**: Understand Maven lifecycle (validate, compile, test, package, verify, install, deploy)
2. **Goals**: Each Mojo represents a goal (e.g., `spellcheck:check`)
3. **Parameters**: Configuration options exposed to users
4. **Components**: Maven-injected services
5. **Thread Safety**: Mark as `threadSafe = true` when possible

### Dependencies to Include in pom.xml

```xml
<dependencies>
    <!-- Maven Plugin API -->
    <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-plugin-api</artifactId>
        <version>3.8.1</version>
        <scope>provided</scope>
    </dependency>

    <!-- Maven Plugin Annotations -->
    <dependency>
        <groupId>org.apache.maven.plugin-tools</groupId>
        <artifactId>maven-plugin-annotations</artifactId>
        <version>3.6.4</version>
        <scope>provided</scope>
    </dependency>

    <!-- Maven Core -->
    <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-core</artifactId>
        <version>3.8.1</version>
        <scope>provided</scope>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.apache.maven.plugin-testing</groupId>
        <artifactId>maven-plugin-testing-harness</artifactId>
        <version>3.3.0</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Code Conventions

### Java Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Maximum 120 characters
- **Encoding**: UTF-8 for all source files
- **Java Version**: Target Java 17; verify on Java 17, 21, and 25

### Naming Conventions

- **Classes**: PascalCase (e.g., `SpellCheckMojo`, `DictionaryManager`)
- **Methods**: camelCase (e.g., `checkSpelling()`, `loadDictionary()`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_ENCODING`)
- **Packages**: lowercase (e.g., `io.nncdevel.maven.spellcheck`)

### Documentation

- **JavaDoc Required For**:
  - All public classes and interfaces
  - All public and protected methods
  - All Mojo parameters (use `@parameter` tag)

- **Comments**:
  - Use `//` for single-line comments
  - Use `/* */` for multi-line explanations
  - Use `/** */` for JavaDoc

### Error Handling

```java
// Use appropriate Maven exceptions
throw new MojoExecutionException("Error message", cause);
throw new MojoFailureException("Build should fail because...");

// Log appropriately
getLog().debug("Detailed debug information");
getLog().info("User-relevant information");
getLog().warn("Warning message");
getLog().error("Error message");
```

### Logging Best Practices

- Use `getLog()` from AbstractMojo, not System.out/err
- DEBUG: Detailed execution flow, variable values
- INFO: High-level progress, summary information
- WARN: Recoverable issues, deprecated usage
- ERROR: Serious problems that don't halt execution

---

## Testing Guidelines

### Unit Testing

- **Framework**: JUnit 4 or 5
- **Coverage Target**: Aim for 80%+ code coverage
- **Test Naming**: `ClassNameTest.java` or `ClassNameTests.java`
- **Method Naming**: Descriptive names (e.g., `testSpellCheckFindsTypo()`)

### Integration Testing

Use Maven Invoker Plugin for integration tests:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-invoker-plugin</artifactId>
    <version>3.2.2</version>
    <configuration>
        <projectsDirectory>src/it</projectsDirectory>
        <cloneProjectsTo>${project.build.directory}/it</cloneProjectsTo>
        <pomIncludes>
            <pomInclude>*/pom.xml</pomInclude>
        </pomIncludes>
        <postBuildHookScript>verify</postBuildHookScript>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>install</goal>
                <goal>run</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Test Structure

```text
src/test/
├── java/
│   └── io/nncdevel/maven/spellcheck/
│       ├── SpellCheckMojoTest.java
│       ├── DictionaryManagerTest.java
│       └── integration/
│           └── FullBuildTest.java
└── resources/
    ├── test-dictionaries/
    └── test-files/
```

---

## Git Workflow

### Branch Naming

- **Feature branches**: `feature/description` or `claude/claude-md-*`
- **Bug fixes**: `fix/issue-description`
- **Documentation**: `docs/description`

### Commit Messages

Follow conventional commits format:

```text
<type>(<scope>): <subject>

<body>

<footer>
```

Types:

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `test`: Adding/updating tests
- `refactor`: Code refactoring
- `chore`: Build process, dependencies

Examples:

```text
feat(mojo): Add basic spell-check Mojo implementation

Implement the core SpellCheckMojo class with basic
dictionary loading and text processing capabilities.

Refs: #1
```

### What to Commit

**DO Commit**:

- Source code (`src/`)
- Build configuration (`pom.xml`)
- Documentation (`.md` files)
- Test resources
- Configuration files

**DON'T Commit**:

- Build output (`target/`)
- IDE-specific files (`.idea/`, `.eclipse/`, etc.)
- OS-specific files (`.DS_Store`, `Thumbs.db`)
- Local configuration overrides

### .gitignore Template

```gitignore
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties

# IDE
.idea/
*.iml
.eclipse/
.settings/
.classpath
.project
*.swp
*.swo
*~

# OS
.DS_Store
Thumbs.db

# Logs
*.log
```

---

## Common Tasks

### Adding a New Mojo (Goal)

1. Create a new class extending `AbstractMojo`
2. Add `@Mojo` annotation with appropriate configuration
3. Implement `execute()` method
4. Add `@Parameter` annotations for configuration options
5. Write tests
6. Document in README.md

### Adding a Configuration Parameter

```java
@Parameter(
    property = "spellcheck.failOnError",  // Command-line property
    defaultValue = "true",                 // Default value
    required = false                       // Whether required
)
private boolean failOnError;
```

Users can then configure:

```xml
<configuration>
    <failOnError>false</failOnError>
</configuration>
```

Or via command line:

```bash
mvn spellcheck:check -Dspellcheck.failOnError=false
```

### Debugging the Plugin

1. **Build and install locally**:

   ```bash
   mvn clean install
   ```

2. **Enable Maven debug output**:

   ```bash
   mvn -X spellcheck:check
   ```

3. **Remote debugging**:

   ```bash
   mvnDebug spellcheck:check
   # Then attach debugger to port 8000
   ```

### Running Specific Tests

```bash
# Single test class
mvn test -Dtest=SpellCheckMojoTest

# Single test method
mvn test -Dtest=SpellCheckMojoTest#testSpecificMethod

# Integration tests only
mvn verify -DskipUnitTests

# Skip tests
mvn install -DskipTests
```

---

## AI Assistant Guidelines

### When Working on This Project

1. **Always check current state** before making changes
   - Run `mvn -v` to verify Maven availability
   - Check existing files with `ls` or `find`
   - Review `pom.xml` for current dependencies

2. **Follow Maven conventions**
   - Use standard directory structure
   - Leverage Maven plugin parent POM when appropriate
   - Use provided scope for Maven dependencies

3. **Test before committing**
   - Run `mvn clean verify` to ensure build succeeds
   - Run integration tests if available
   - Check for compilation errors

4. **Documentation is crucial**
   - Update README.md with usage examples
   - Add JavaDoc to all public APIs
   - Update this CLAUDE.md when structure changes

5. **Version management**
   - Use Maven version plugin for releases
   - Follow semantic versioning (MAJOR.MINOR.PATCH)
   - Update version in pom.xml consistently

### Project-Specific Considerations

- **Dictionary Management**: Consider how to package/distribute dictionaries
- **Performance**: Spell-checking can be slow; implement caching strategies
- **File Scanning**: Decide which file types to scan by default
- **Exclusion Patterns**: Support for ignoring certain files/directories
- **Reporting**: Generate readable reports in formats like HTML, XML, or plain text
- **IDE Integration**: Consider creating IDE-specific configurations

### Questions to Ask When Unclear

- What files should be spell-checked by default?
- Should the plugin fail the build on spelling errors, or just warn?
- What dictionary format should be used?
- Should it integrate with online spell-check services?
- What are the performance requirements?
- Should it support custom dictionaries per-project?

---

## Resources

### Maven Plugin Development

- [Maven Plugin Documentation](https://maven.apache.org/plugin-developers/index.html)
- [Maven Plugin API](https://maven.apache.org/ref/current/maven-plugin-api/)
- [Maven Plugin Testing](https://maven.apache.org/plugin-testing/)
- [Mojo API Specification](https://maven.apache.org/developers/mojo-api-specification.html)

### Spell-Checking Libraries (Java)

- [LanguageTool](https://languagetool.org/) - Open source grammar and spell checker
- [Hunspell Java](https://github.com/dren-dk/HunspellJNA) - Java binding for Hunspell
- [JaSpell](https://jaspell.sourceforge.net/) - Pure Java spell checker
- [Apache Lucene SpellChecker](https://lucene.apache.org/) - Part of Lucene project

### Maven Resources

- [Maven Central Repository](https://search.maven.org/)
- [Maven Guides](https://maven.apache.org/guides/)
- [Maven by Example](https://books.sonatype.com/mvnex-book/reference/index.html)

---

## Changelog

### 2025-11-13

- Initial CLAUDE.md created
- Established project structure guidelines
- Defined development workflows and conventions
- Added Maven plugin development guidelines

---

## Support

For questions or issues:

1. Check existing documentation in README.md
2. Review Maven plugin documentation
3. Consult this CLAUDE.md for conventions
4. Check git history for context on previous decisions

---

**Remember**: This is a Maven plugin project. Always think about how end-users will configure and use the plugin in their Maven builds. Make it intuitive, well-documented, and follow Maven best practices.
