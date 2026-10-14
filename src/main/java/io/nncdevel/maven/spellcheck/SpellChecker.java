package io.nncdevel.maven.spellcheck;

import io.nncdevel.maven.spellcheck.config.SpellCheckConfiguration;
import io.nncdevel.maven.spellcheck.report.SpellCheckReport;
import io.nncdevel.maven.spellcheck.report.SpellError;
import org.apache.maven.plugin.logging.Log;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Core spell checker implementation using LanguageTool.
 *
 * @author T. Izuno
 * @since 1.0.0
 */
public class SpellChecker {

    private final SpellCheckConfiguration config;
    private final Log log;
    private final JLanguageTool languageTool;
    private final Set<String> ignoreWords;

    /**
     * Creates a new spell checker with the given configuration.
     *
     * @param config the spell check configuration
     * @param log    the Maven logger
     * @throws IOException if initialization fails
     */
    public SpellChecker(SpellCheckConfiguration config, Log log) throws IOException {
        this.config = config;
        this.log = log;
        this.languageTool = createLanguageTool(config.getLanguage());
        this.ignoreWords = new HashSet<>();

        // Add custom words to ignore
        if (config.getCustomDictionary() != null) {
            loadCustomDictionary(config.getCustomDictionary());
        }

        // Add ignore words from configuration
        if (config.getIgnoreWords() != null) {
            for (String word : config.getIgnoreWords()) {
                ignoreWords.add(word.toLowerCase(Locale.ROOT));
            }
        }
    }

    /**
     * Creates a JLanguageTool instance for the specified language.
     *
     * @param language the language code
     * @return the JLanguageTool instance
     */
    private JLanguageTool createLanguageTool(String language) {
        String languageCode = language == null ? "en-US" : language;
        if (!"en-US".equals(languageCode) && !"en-GB".equals(languageCode)) {
            throw new IllegalArgumentException("Unsupported language: " + languageCode
                + ". Supported languages: en-US, en-GB.");
        }
        // Language objects are shared; each checker owns its non-thread-safe JLanguageTool.
        return new JLanguageTool(Languages.getLanguageForShortCode(languageCode));
    }

    /**
     * Loads custom dictionary words.
     *
     * @param dictionaryFile the dictionary file
     * @throws IOException if reading fails
     */
    private void loadCustomDictionary(File dictionaryFile) throws IOException {
        log.debug("Loading custom dictionary from: " + dictionaryFile.getAbsolutePath());

        try (BufferedReader reader = Files.newBufferedReader(dictionaryFile.toPath(),
                Charset.forName(config.getEncoding()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    ignoreWords.add(line.toLowerCase(Locale.ROOT));
                }
            }
        }

        log.debug("Custom dictionary loaded successfully");
    }

    /**
     * Checks the given files for spelling errors.
     *
     * @param files the files to check
     * @return the spell check report
     * @throws IOException if file reading fails
     */
    public SpellCheckReport check(List<File> files) throws IOException {
        SpellCheckReport report = new SpellCheckReport();

        for (File file : files) {
            log.debug("Checking file: " + file.getAbsolutePath());
            checkFile(file, report);
        }

        return report;
    }

    /**
     * Checks a single file for spelling errors.
     *
     * @param file   the file to check
     * @param report the report to update
     * @throws IOException if file reading fails
     */
    private void checkFile(File file, SpellCheckReport report) throws IOException {
        report.incrementFilesChecked();

        String content = readFile(file);

        if (content.trim().isEmpty()) {
            log.debug("Skipping empty file: " + file.getName());
            return;
        }

        try {
            List<RuleMatch> matches = languageTool.check(content);
            List<Integer> lineStarts = getLineStarts(content);

            for (RuleMatch match : matches) {
                // Only report spelling errors, not grammar errors
                if (isSpellingError(match)) {
                    String word = content.substring(match.getFromPos(), match.getToPos());

                    // Skip words that are in the ignore list
                    if (ignoreWords.contains(word.toLowerCase(Locale.ROOT))) {
                        continue;
                    }

                    int lineIndex = Collections.binarySearch(lineStarts, match.getFromPos());
                    if (lineIndex < 0) {
                        lineIndex = -lineIndex - 2;
                    }
                    SpellError error = new SpellError(
                        file,
                        lineIndex + 1,
                        match.getFromPos() - lineStarts.get(lineIndex) + 1,
                        word,
                        match.getMessage(),
                        match.getSuggestedReplacements()
                    );

                    report.addError(error);

                    if (log.isDebugEnabled()) {
                        log.debug(String.format(
                            "Error in %s at line %d: %s",
                            file.getName(),
                            error.getLine(),
                            error.getWord()
                        ));
                    }
                }
            }

        } catch (RuntimeException e) {
            throw new IOException("Error checking file " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Finds line starts for one-based report positions, including CRLF and CR text.
     *
     * @param content the checked text
     * @return UTF-16 offsets of each line start
     */
    private List<Integer> getLineStarts(String content) {
        List<Integer> starts = new ArrayList<>();
        starts.add(0);
        for (int index = 0; index < content.length(); index++) {
            char character = content.charAt(index);
            if (character == '\r') {
                if (index + 1 < content.length() && content.charAt(index + 1) == '\n') {
                    index++;
                }
                starts.add(index + 1);
            } else if (character == '\n') {
                starts.add(index + 1);
            }
        }
        return starts;
    }

    /**
     * Reads the content of a file.
     *
     * @param file the file to read
     * @return the file content
     * @throws IOException if reading fails
     */
    private String readFile(File file) throws IOException {
        Charset charset = Charset.forName(config.getEncoding());
        return Files.readString(file.toPath(), charset);
    }

    /**
     * Determines if a rule match is a spelling error.
     *
     * @param match the rule match
     * @return true if it's a spelling error
     */
    private boolean isSpellingError(RuleMatch match) {
        String ruleId = match.getRule().getId();
        // Check if it's a spelling-related rule
        return ruleId.contains("SPELL") ||
               ruleId.contains("MORFOLOGIK") ||
               ruleId.contains("HUNSPELL") ||
               ruleId.contains("TYPO");
    }
}
