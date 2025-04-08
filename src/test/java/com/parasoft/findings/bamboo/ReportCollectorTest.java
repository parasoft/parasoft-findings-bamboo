package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.build.test.TestCollectionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ReportCollectorTest {
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
            assertEquals(8 ,result.getSuccessfulTestResults().size());
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
            assertEquals(5 ,result.getFailedTestResults().size());
            assertEquals(0 ,result.getSuccessfulTestResults().size());
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

            assertEquals(0 ,result.getSuccessfulTestResults().size());
            assertEquals(0 ,result.getFailedTestResults().size());
            assertEquals(0 ,result.getSkippedTestResults().size());
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
}
