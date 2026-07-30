package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.build.test.TestCollectionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import net.sf.saxon.s9api.SaxonApiException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.io.TempDir;

public class ReportCollectorTest {
    @TempDir
    private Path tempDir;

    private ReportCollector reportCollector;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reportCollector = new ReportCollector();
    }

    @Test
    public void testCollect_parseUnitTestReport() {
        try {
            File unitTestReport = new File("src/test/resources/reports/unit_test_report.xml");
            TestCollectionResult result = reportCollector.collect(unitTestReport);

            assertNotNull(result);
            assertEquals(8, result.getSuccessfulTestResults().size());
            assertEquals(0, result.getFailedTestResults().size());
            assertEquals(0, result.getSkippedTestResults().size());
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testCollect_parseUnitTestReport_jTest_2025_2_0_unit_no_ExecutedTestsDetails_tag() {
        try {
            File unitTestReport = new File("src/test/resources/reports/jTest_2025.2.0_unit_no_ExecutedTestsDetails_tag.xml");
            TestCollectionResult result = reportCollector.collect(unitTestReport);

            assertNotNull(result);
            assertEquals(0 ,result.getSuccessfulTestResults().size());
            assertEquals(0, result.getFailedTestResults().size());
            assertEquals(0, result.getSkippedTestResults().size());
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testCollect_parseUnitTestReport_jTest_2025_2_0_unit_zero_total_test() {
        try {
            File unitTestReport = new File("src/test/resources/reports/jTest_2025.2.0_unit_zero_total_test.xml");
            TestCollectionResult result = reportCollector.collect(unitTestReport);

            assertNotNull(result);
            assertEquals(0, result.getSuccessfulTestResults().size());
            assertEquals(0, result.getFailedTestResults().size());
            assertEquals(0, result.getSkippedTestResults().size());
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testCollect_parseSOAtestReport() {
        try {
            File soaTestReport = new File("src/test/resources/reports/soatest_Report.xml");
            TestCollectionResult result = reportCollector.collect(soaTestReport);

            assertNotNull(result);
            assertEquals(5, result.getFailedTestResults().size());
            assertEquals(0, result.getSuccessfulTestResults().size());
            assertEquals(0, result.getSkippedTestResults().size());
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testCollect_parseSOAtestReport_SOAtest_functional_2025_3_0_no_ExecutedTestsDetails_tag() {
        try {
            File soaTestReport = new File("src/test/resources/reports/SOAtest_functional_2025.3.0_no_ExecutedTestsDetails_tag.xml");
            TestCollectionResult result = reportCollector.collect(soaTestReport);

            assertNotNull(result);
            assertEquals(0, result.getFailedTestResults().size());
            assertEquals(0, result.getSuccessfulTestResults().size());
            assertEquals(0, result.getSkippedTestResults().size());
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testCollect_parseSOAtestReport_SOAtest_functional_2025_3_0_zero_total_test() {
        try {
            File soaTestReport = new File("src/test/resources/reports/SOAtest_functional_2025.3.0_zero_total_test.xml");
            TestCollectionResult result = reportCollector.collect(soaTestReport);

            assertNotNull(result);
            assertEquals(0, result.getFailedTestResults().size());
            assertEquals(0, result.getSuccessfulTestResults().size());
            assertEquals(0, result.getSkippedTestResults().size());
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testCollect_parseUnsupportedReport() {
        try {
            File staticAnalysisReport = new File("src/test/resources/reports/static_analysis_report.xml");
            reportCollector.collect(staticAnalysisReport);
            fail("Should not reach here");
        } catch (Exception e) {
            assertEquals("Premature end of file.", e.getMessage());
        }
    }

    @Test
    public void testCollect_parseNonParasoftReport() {
        try {
            File staticAnalysisReport = new File("src/test/resources/reports/cobertura_report.xml");
            TestCollectionResult result = reportCollector.collect(staticAnalysisReport);

            assertEquals(0, result.getSuccessfulTestResults().size());
            assertEquals(0, result.getFailedTestResults().size());
            assertEquals(0, result.getSkippedTestResults().size());
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testGetSupportedFileExtensions() {
        Set<String> results = reportCollector.getSupportedFileExtensions();
        assertEquals(1, results.size());
        assertEquals("xml", results.iterator().next());
    }

    @Test
    public void testCollect_rejectsExternalEntitiesDuringTransformation() throws Exception {
        Path secret = tempDir.resolve("secret.txt");
        Files.write(secret, "sensitive-value".getBytes(StandardCharsets.UTF_8));

        Path report = tempDir.resolve("external-entity.xml");
        String reportXml = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE ResultsSession [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>"
                + "<ResultsSession toolName=\"SOAtest\"><details>&xxe;</details></ResultsSession>";
        Files.write(report, reportXml.getBytes(StandardCharsets.UTF_8));

        SaxonApiException exception = assertThrows(SaxonApiException.class,
                () -> reportCollector.collect(report.toFile()));
        assertTrue(exception.getMessage().contains("Undeclared general entity \"xxe\""));
    }
}
