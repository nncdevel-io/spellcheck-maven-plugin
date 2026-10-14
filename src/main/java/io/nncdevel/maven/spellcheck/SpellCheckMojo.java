package io.nncdevel.maven.spellcheck;

import io.nncdevel.maven.spellcheck.config.CSpellConfig;
import io.nncdevel.maven.spellcheck.config.CSpellConfigLoader;
import io.nncdevel.maven.spellcheck.config.SpellCheckConfiguration;
import io.nncdevel.maven.spellcheck.report.CheckstyleXmlReportGenerator;
import io.nncdevel.maven.spellcheck.report.JUnitXmlReportGenerator;
import io.nncdevel.maven.spellcheck.report.SpellCheckReport;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Maven Mojo for spell checking source files, documentation, and comments.
 *
 * @author T. Izuno
 * @since 1.0.0
 */
@Mojo(name = "check",
      defaultPhase = LifecyclePhase.VERIFY,
      requiresProject = true,
      threadSafe = true)
public class SpellCheckMojo extends AbstractMojo {

    /**
     * The Maven project instance.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Skip the spell check execution.
     */
    @Parameter(property = "spellcheck.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Fail the build on spell check errors.
     */
    @Parameter(property = "spellcheck.failOnError", defaultValue = "true")
    private boolean failOnError;

    /**
     * Source file encoding.
     */
    @Parameter(property = "spellcheck.encoding", defaultValue = "${project.build.sourceEncoding}")
    private String encoding;

    /**
     * File includes pattern (Ant-style).
     */
    @Parameter(property = "spellcheck.includes")
    private String[] includes;

    /**
     * File excludes pattern (Ant-style).
     */
    @Parameter(property = "spellcheck.excludes")
    private String[] excludes;

    /**
     * Directories to scan for files to spell check.
     */
    @Parameter
    private File[] sourceDirectories;

    /**
     * Language for spell checking (e.g., "en-US", "en-GB").
     */
    @Parameter(property = "spellcheck.language", defaultValue = "en-US")
    private String language;

    /**
     * Custom dictionary file containing words to ignore.
     */
    @Parameter(property = "spellcheck.customDictionary")
    private File customDictionary;

    /**
     * Words to ignore during spell checking.
     */
    @Parameter
    private List<String> ignoreWords;

    /**
     * Output directory for reports.
     */
    @Parameter(defaultValue = "${project.build.directory}/spellcheck", readonly = true)
    private File outputDirectory;

    /**
     * Generate HTML report.
     */
    @Parameter(property = "spellcheck.generateReport", defaultValue = "true")
    private boolean generateReport;

    /**
     * Generate JUnit XML report for CI/CD integration.
     * This format is supported by Jenkins, GitLab CI, GitHub Actions, and many other CI tools.
     */
    @Parameter(property = "spellcheck.generateJUnitReport", defaultValue = "false")
    private boolean generateJUnitReport;

    /**
     * Generate Checkstyle XML report for CI/CD integration.
     * This format is supported by Jenkins Warnings Next Generation plugin, SonarQube, and other tools.
     */
    @Parameter(property = "spellcheck.generateCheckstyleReport", defaultValue = "false")
    private boolean generateCheckstyleReport;

    /**
     * Path to CSpell configuration file (cspell.json or .cspell.json).
     * If not specified, the plugin will search for the configuration file
     * in the project base directory.
     */
    @Parameter(property = "spellcheck.cspellConfig")
    private File cspellConfigFile;

    /**
     * Enable automatic detection and loading of CSpell configuration files.
     */
    @Parameter(property = "spellcheck.useCSpellConfig", defaultValue = "true")
    private boolean useCSpellConfig;

    private SpellChecker spellChecker;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Spell check is skipped.");
            return;
        }

        getLog().info("Starting spell check...");
        getLog().info("Language: " + language);
        getLog().info("Encoding: " + (encoding != null ? encoding : "UTF-8"));

        try {
            // Initialize configuration
            SpellCheckConfiguration config = createConfiguration();

            // Initialize spell checker
            spellChecker = new SpellChecker(config, getLog());

            // Get files to check
            List<File> filesToCheck = getFilesToCheck();

            if (filesToCheck.isEmpty()) {
                getLog().warn("No files found to spell check.");
                return;
            }

            getLog().info("Checking " + filesToCheck.size() + " file(s)...");

            // Perform spell checking
            SpellCheckReport report = spellChecker.check(filesToCheck);

            // Generate reports if requested
            if (generateReport) {
                generateTextReport(report);
            }
            if (generateJUnitReport) {
                generateJUnitXmlReport(report);
            }
            if (generateCheckstyleReport) {
                generateCheckstyleXmlReport(report);
            }

            // Log summary
            logSummary(report);

            // Fail build if errors found and failOnError is true
            if (failOnError && report.hasErrors()) {
                throw new MojoFailureException(
                    "Spell check found " + report.getErrorCount() + " error(s). " +
                    "See report at: " + new File(outputDirectory, "spellcheck-report.txt").getAbsolutePath()
                );
            }

        } catch (IOException | IllegalArgumentException e) {
            throw new MojoExecutionException("Error during spell check execution", e);
        }
    }

    /**
     * Creates the spell check configuration from plugin parameters.
     */
    private SpellCheckConfiguration createConfiguration() throws IOException {
        SpellCheckConfiguration config = new SpellCheckConfiguration();

        // Try to load CSpell configuration if enabled
        CSpellConfig cspellConfig = null;
        if (useCSpellConfig) {
            cspellConfig = loadCSpellConfig();
        }

        // If CSpell config was loaded, use it as base configuration
        if (cspellConfig != null) {
            CSpellConfigLoader loader = new CSpellConfigLoader(getLog());
            config = loader.toSpellCheckConfiguration(cspellConfig);
            getLog().info("Using CSpell configuration file");
        }

        // Override with explicit Maven plugin parameters (they take precedence)
        if (language != null) {
            config.setLanguage(language);
        }
        if (encoding != null) {
            config.setEncoding(encoding);
        }
        if (customDictionary != null) {
            config.setCustomDictionary(customDictionary);
        }
        if (ignoreWords != null && !ignoreWords.isEmpty()) {
            // Merge with existing ignore words from CSpell config
            List<String> mergedIgnoreWords = new ArrayList<>(config.getIgnoreWords());
            mergedIgnoreWords.addAll(ignoreWords);
            config.setIgnoreWords(mergedIgnoreWords);
        }

        // Ensure encoding is set
        if (config.getEncoding() == null || config.getEncoding().isEmpty()) {
            config.setEncoding("UTF-8");
        }

        return config;
    }

    /**
     * Loads CSpell configuration from file.
     *
     * @return the CSpell configuration, or null if not found
     */
    private CSpellConfig loadCSpellConfig() {
        try {
            CSpellConfigLoader loader = new CSpellConfigLoader(getLog());

            // If specific config file is specified
            if (cspellConfigFile != null) {
                if (cspellConfigFile.exists()) {
                    return loader.loadConfig(cspellConfigFile);
                } else {
                    getLog().warn("Specified CSpell config file not found: " + cspellConfigFile.getAbsolutePath());
                    return null;
                }
            }

            // Otherwise, search in project base directory
            return loader.loadConfig(project.getBasedir());
        } catch (IOException e) {
            getLog().warn("Failed to load CSpell configuration: " + e.getMessage());
            getLog().debug("CSpell configuration load error", e);
            return null;
        }
    }

    /**
     * Gets the list of files to spell check.
     */
    private List<File> getFilesToCheck() throws IOException {
        List<File> files = new ArrayList<>();

        // Determine source directories
        List<File> dirsToScan = new ArrayList<>();
        if (sourceDirectories != null && sourceDirectories.length > 0) {
            for (File dir : sourceDirectories) {
                if (dir.exists() && dir.isDirectory()) {
                    dirsToScan.add(dir);
                }
            }
        } else {
            // Default to project source directories
            File srcMainJava = new File(project.getBasedir(), "src/main/java");
            File srcTestJava = new File(project.getBasedir(), "src/test/java");
            File srcMainResources = new File(project.getBasedir(), "src/main/resources");

            if (srcMainJava.exists()) dirsToScan.add(srcMainJava);
            if (srcTestJava.exists()) dirsToScan.add(srcTestJava);
            if (srcMainResources.exists()) dirsToScan.add(srcMainResources);

            // Check for README and markdown files in root
            File readme = new File(project.getBasedir(), "README.md");
            if (readme.exists()) files.add(readme);
        }

        // Scan directories and collect files
        for (File dir : dirsToScan) {
            try (Stream<Path> paths = Files.walk(dir.toPath())) {
                paths.filter(Files::isRegularFile)
                     .filter(this::shouldCheckFile)
                     .forEach(p -> files.add(p.toFile()));
            }
        }

        return files;
    }

    /**
     * Determines if a file should be checked based on includes/excludes patterns.
     */
    private boolean shouldCheckFile(Path path) {
        String fileName = path.getFileName().toString();

        // Default file types to check
        if (fileName.endsWith(".java") ||
            fileName.endsWith(".md") ||
            fileName.endsWith(".txt") ||
            fileName.endsWith(".properties") ||
            fileName.endsWith(".xml")) {
            return true;
        }

        // TODO: Implement proper Ant-style pattern matching for includes/excludes
        return false;
    }

    /**
     * Generates the text format spell check report.
     */
    private void generateTextReport(SpellCheckReport report) throws IOException {
        ensureOutputDirectory();

        File reportFile = new File(outputDirectory, "spellcheck-report.txt");
        report.writeToFile(reportFile);

        getLog().info("Text report generated at: " + reportFile.getAbsolutePath());
    }

    /**
     * Generates the JUnit XML format spell check report.
     */
    private void generateJUnitXmlReport(SpellCheckReport report) throws IOException {
        ensureOutputDirectory();

        File reportFile = new File(outputDirectory, "spellcheck-junit.xml");
        JUnitXmlReportGenerator generator = new JUnitXmlReportGenerator();
        generator.generateReport(report, reportFile);

        getLog().info("JUnit XML report generated at: " + reportFile.getAbsolutePath());
    }

    /**
     * Generates the Checkstyle XML format spell check report.
     */
    private void generateCheckstyleXmlReport(SpellCheckReport report) throws IOException {
        ensureOutputDirectory();

        File reportFile = new File(outputDirectory, "spellcheck-checkstyle.xml");
        CheckstyleXmlReportGenerator generator = new CheckstyleXmlReportGenerator();
        generator.generateReport(report, reportFile);

        getLog().info("Checkstyle XML report generated at: " + reportFile.getAbsolutePath());
    }

    /**
     * Ensures the output directory exists.
     */
    private void ensureOutputDirectory() {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
    }

    /**
     * Logs the spell check summary.
     */
    private void logSummary(SpellCheckReport report) {
        getLog().info("========================================");
        getLog().info("Spell Check Summary");
        getLog().info("========================================");
        getLog().info("Files checked: " + report.getFilesChecked());
        getLog().info("Errors found: " + report.getErrorCount());

        if (report.hasErrors()) {
            getLog().warn("Spell check completed with errors!");
        } else {
            getLog().info("Spell check completed successfully!");
        }
        getLog().info("========================================");
    }
}
