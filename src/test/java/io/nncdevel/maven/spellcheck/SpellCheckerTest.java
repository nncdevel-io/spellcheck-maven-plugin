package io.nncdevel.maven.spellcheck;

import io.nncdevel.maven.spellcheck.config.SpellCheckConfiguration;
import io.nncdevel.maven.spellcheck.report.SpellCheckReport;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/** Verifies real spelling, including initialization on Java 25. */
public class SpellCheckerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void detectsSpellingWithoutChangingXmlLimits() throws Exception {
        String limit = System.getProperty("jdk.xml.totalEntitySizeLimit");
        File file = temporaryFolder.newFile("sample.txt");
        Files.writeString(file.toPath(), "This is a speling mistake.", StandardCharsets.UTF_8);
        SpellChecker checker = new SpellChecker(new SpellCheckConfiguration(), new SystemStreamLog());
        SpellCheckReport report = checker.check(List.of(file));
        assertEquals(1, report.getErrorCount());
        assertEquals("speling", report.getErrors().get(0).getWord());
        assertEquals(limit, System.getProperty("jdk.xml.totalEntitySizeLimit"));
    }

    @Test
    public void acceptsBritishSpellingAndCustomWords() throws Exception {
        File file = temporaryFolder.newFile("british.txt");
        Files.writeString(file.toPath(), "The colour is codexword.", StandardCharsets.UTF_8);
        SpellCheckConfiguration config = new SpellCheckConfiguration();
        config.setLanguage("en-GB");
        config.setIgnoreWords(List.of("codexword"));
        assertFalse(new SpellChecker(config, new SystemStreamLog()).check(List.of(file)).hasErrors());
    }

    @Test
    public void rejectsUnsupportedLanguage() {
        SpellCheckConfiguration config = new SpellCheckConfiguration();
        config.setLanguage("fr");
        assertThrows(IllegalArgumentException.class, () -> new SpellChecker(config, new SystemStreamLog()));
    }

    @Test
    public void rejectsMissingCustomDictionary() {
        SpellCheckConfiguration config = new SpellCheckConfiguration();
        config.setCustomDictionary(new File(temporaryFolder.getRoot(), "missing.txt"));
        assertThrows(java.io.IOException.class, () -> new SpellChecker(config, new SystemStreamLog()));
    }

    @Test
    public void checksWithIndependentInstancesInParallel() throws Exception {
        File file = temporaryFolder.newFile("parallel.txt");
        Files.writeString(file.toPath(), "This is a speling mistake.", StandardCharsets.UTF_8);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> check = () -> new SpellChecker(new SpellCheckConfiguration(), new SystemStreamLog())
                .check(List.of(file)).getErrorCount();
            for (Future<Integer> result : executor.invokeAll(List.of(check, check, check, check))) {
                assertEquals(Integer.valueOf(1), result.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void rejectsMalformedTextRatherThanReportingSuccess() throws Exception {
        File file = temporaryFolder.newFile("invalid.txt");
        Files.write(file.toPath(), new byte[]{(byte) 0xc3, (byte) 0x28});
        SpellChecker checker = new SpellChecker(new SpellCheckConfiguration(), new SystemStreamLog());
        assertThrows(java.io.IOException.class, () -> checker.check(List.of(file)));
    }

    @Test
    public void reportsLineAndColumnForDifferentLineEndings() throws Exception {
        File file = temporaryFolder.newFile("lines.txt");
        SpellChecker checker = new SpellChecker(new SpellCheckConfiguration(), new SystemStreamLog());
        for (String newline : List.of("\n", "\r\n", "\r")) {
            Files.writeString(file.toPath(), "This is a sentence." + newline + "This is a speling mistake.");
            SpellCheckReport report = checker.check(List.of(file));
            assertEquals(1, report.getErrorCount());
            assertEquals(2, report.getErrors().get(0).getLine());
            assertEquals(11, report.getErrors().get(0).getColumn());
        }
    }
}
