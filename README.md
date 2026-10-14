# spellcheck-maven-plugin

A Maven plugin for spell-checking source files, documentation, and comments in your Maven projects.

## Features

- Spell-checking for Java source files, documentation, and other text files
- Support for CSpell configuration files (`cspell.json`, `.cspell.json`)
- Customizable dictionaries and ignore words
- Integration with Maven build lifecycle
- Configurable file patterns for inclusion and exclusion
- Detailed reporting of spelling errors

## Usage

### Basic Configuration

Add the plugin to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.nncdevel.maven</groupId>
            <artifactId>spellcheck-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### CSpell Configuration File Support

The plugin automatically detects and uses CSpell configuration files in your project root. Supported filenames:

- `cspell.json`
- `.cspell.json`
- `cSpell.json`
- `.cSpell.json`

#### Example CSpell Configuration

Create a `cspell.json` file in your project root:

```json
{
  "$schema": "https://raw.githubusercontent.com/streetsidesoftware/cspell/main/cspell.schema.json",
  "version": "0.2",
  "language": "en-US",
  "words": [
    "spellcheck",
    "customword"
  ],
  "ignoreWords": [
    "TODO",
    "FIXME"
  ],
  "ignorePaths": [
    "node_modules/**",
    "target/**",
    "*.min.js"
  ],
  "files": [
    "**/*.java",
    "**/*.md",
    "**/*.txt"
  ]
}
```

See [`cspell.json.example`](cspell.json.example) for a complete example.

### Plugin Configuration Options

You can configure the plugin in your `pom.xml`:

```xml
<plugin>
    <groupId>io.nncdevel.maven</groupId>
    <artifactId>spellcheck-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <!-- Enable/disable CSpell config file detection -->
        <useCSpellConfig>true</useCSpellConfig>

        <!-- Specify custom CSpell config file path -->
        <cspellConfigFile>${project.basedir}/custom-cspell.json</cspellConfigFile>

        <!-- Language for spell checking (overrides CSpell config) -->
        <language>en-US</language>

        <!-- File encoding -->
        <encoding>UTF-8</encoding>

        <!-- Custom dictionary file -->
        <customDictionary>${project.basedir}/custom-dictionary.txt</customDictionary>

        <!-- Words to ignore (merged with CSpell config) -->
        <ignoreWords>
            <ignoreWord>customword1</ignoreWord>
            <ignoreWord>customword2</ignoreWord>
        </ignoreWords>

        <!-- Fail build on errors -->
        <failOnError>true</failOnError>

        <!-- Skip spell check -->
        <skip>false</skip>

        <!-- Generate text report -->
        <generateReport>true</generateReport>

        <!-- Generate JUnit XML report for CI/CD integration -->
        <generateJUnitReport>false</generateJUnitReport>

        <!-- Generate Checkstyle XML report for CI/CD integration -->
        <generateCheckstyleReport>false</generateCheckstyleReport>
    </configuration>
</plugin>
```

### Command Line Usage

Run spell check manually:

```bash
mvn spellcheck:check
```

Skip spell check:

```bash
mvn verify -Dspellcheck.skip=true
```

Don't fail build on errors:

```bash
mvn verify -Dspellcheck.failOnError=false
```

Specify custom CSpell config:

```bash
mvn spellcheck:check -Dspellcheck.cspellConfig=path/to/cspell.json
```

Disable CSpell config file detection:

```bash
mvn spellcheck:check -Dspellcheck.useCSpellConfig=false
```

Generate CI/CD-friendly reports:

```bash
# Generate JUnit XML report
mvn spellcheck:check -Dspellcheck.generateJUnitReport=true

# Generate Checkstyle XML report
mvn spellcheck:check -Dspellcheck.generateCheckstyleReport=true

# Generate both CI reports
mvn spellcheck:check -Dspellcheck.generateJUnitReport=true -Dspellcheck.generateCheckstyleReport=true
```

## CSpell Integration

### Supported CSpell Features

The plugin supports the following CSpell configuration properties:

- `version`: Configuration format version
- `language`: Language locale(s) for spell checking
- `words`: List of words to be considered correct
- `ignoreWords`: List of words to be ignored
- `ignorePaths`: Glob patterns of files to be ignored
- `files`: Glob patterns of files to be checked
- `dictionaries`: List of dictionaries to use
- `dictionaryDefinitions`: Custom dictionary definitions
- `patterns`: Named patterns for regex matching
- `ignoreRegExpList`: Regular expressions to exclude from checking
- `includeRegExpList`: Regular expressions to include in checking
- `import`: Import other CSpell configuration files
- `enabled`: Enable/disable spell checking
- `caseSensitive`: Case-sensitive matching
- `allowCompoundWords`: Allow compound words
- `flagWords`: Words that are always considered incorrect
- `overrides`: File-specific configuration overrides

### Configuration Priority

When both CSpell configuration file and Maven plugin configuration are present:

1. CSpell configuration file is loaded first (if `useCSpellConfig=true`)
2. Maven plugin parameters override CSpell settings
3. For list properties (like `ignoreWords`), both are merged

### Import Support

The plugin supports CSpell's `import` feature to load configuration from other files:

```json
{
  "version": "0.2",
  "import": [
    "./base-cspell.json",
    "../shared-config.json"
  ],
  "words": [
    "projectspecific"
  ]
}
```

## Reports

The plugin generates spell check reports in the `target/spellcheck` directory:

### Text Report (Default)

- `spellcheck-report.txt`: Human-readable text report with all spelling errors
- Enabled by default with `generateReport=true`

### CI/CD Integration Reports

#### JUnit XML Report

- **File**: `spellcheck-junit.xml`
- **Enable**: Set `generateJUnitReport=true`
- **Format**: JUnit XML format compatible with most CI/CD tools
- **Supported by**:
  - Jenkins (JUnit plugin)
  - GitLab CI/CD
  - GitHub Actions
  - Azure DevOps
  - CircleCI
  - Travis CI
  - And many other CI/CD platforms

**Example configuration for Jenkins**:

```xml
<plugin>
    <groupId>io.nncdevel.maven</groupId>
    <artifactId>spellcheck-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <generateJUnitReport>true</generateJUnitReport>
        <failOnError>false</failOnError> <!-- Let CI handle failures -->
    </configuration>
</plugin>
```

In your Jenkinsfile:

```groovy
stage('Spell Check') {
    steps {
        sh 'mvn spellcheck:check'
    }
    post {
        always {
            junit 'target/spellcheck/spellcheck-junit.xml'
        }
    }
}
```

#### Checkstyle XML Report

- **File**: `spellcheck-checkstyle.xml`
- **Enable**: Set `generateCheckstyleReport=true`
- **Format**: Checkstyle XML format for code quality tools
- **Supported by**:
  - Jenkins Warnings Next Generation plugin
  - SonarQube
  - Code quality dashboards
  - IDE integrations

**Example configuration for Jenkins Warnings NG**:

```xml
<plugin>
    <groupId>io.nncdevel.maven</groupId>
    <artifactId>spellcheck-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <generateCheckstyleReport>true</generateCheckstyleReport>
        <failOnError>false</failOnError>
    </configuration>
</plugin>
```

In your Jenkinsfile:

```groovy
stage('Spell Check') {
    steps {
        sh 'mvn spellcheck:check'
    }
    post {
        always {
            recordIssues(
                enabledForFailure: true,
                tools: [checkStyle(pattern: 'target/spellcheck/spellcheck-checkstyle.xml', reportEncoding: 'UTF-8')]
            )
        }
    }
}
```

### GitHub Actions Example

```yaml
name: Spell Check

on: [push, pull_request]

jobs:
  spellcheck:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Spell Check
        run: mvn spellcheck:check -Dspellcheck.generateJUnitReport=true

      - name: Publish Test Report
        uses: mikepenz/action-junit-report@v3
        if: always()
        with:
          report_paths: 'target/spellcheck/spellcheck-junit.xml'
          check_name: 'Spell Check Results'
```

### GitLab CI Example

```yaml
spell-check:
  stage: test
  script:
    - mvn spellcheck:check -Dspellcheck.generateJUnitReport=true
  artifacts:
    when: always
    reports:
      junit: target/spellcheck/spellcheck-junit.xml
```

## Requirements

- Java 17 or higher to run Maven and this plugin
- Maven 3.8.1 or higher

LanguageTool 6.8 runs inside Maven; no HTTP server is required. The runtime Java
requirement is independent of the target Java version of the project being
checked. The plugin supports `en-US` and `en-GB` and rejects other language codes.
Jackson dependencies are aligned with LanguageTool through the Jackson 2.18.0 BOM.

Run `mvn clean verify` to execute unit tests and Maven Invoker consumer tests.
CI uses Java 17, 21, and 25. Consumer tests cover successful checks, spelling
failures, warning-only mode, British English, CSpell words, XML report creation,
and a parallel multi-module build.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

### Repository Spell Check

Use the CSpell CLI to check this repository's source code, documentation, and configuration files.
The setup has been verified with CSpell 10.3.6.
With CSpell installed, run from the repository root:

```bash
cspell lint --config cspell.json --no-config-search
```

See the official [CSpell documentation](https://cspell.org/) and [configuration reference](https://cspell.org/configuration/).
The root `cspell.json` respects `.gitignore` and excludes build output and integration-test text fixtures containing intentional spelling errors or custom words.
The configuration itself is excluded because its exception patterns contain those intentional errors.
Unit-test vocabulary and documentation examples have narrowly scoped exceptions; they are not accepted in production code.
Add only verified project terms to `words`, and correct ordinary typos in the source.

## Links

- [CSpell Documentation](https://cspell.org/)
- [CSpell Configuration](https://cspell.org/configuration/)
- [Maven Plugin Development](https://maven.apache.org/plugin-developers/)
