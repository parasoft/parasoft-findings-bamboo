package com.parasoft.findings.bamboo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real local-resource transforms; expectations are hand-checked report diagnostics. */
public class SoatestXunitTransformationTest {
    private static final String EXECUTION = "SOAtest_functional_execution_violations.xml";
    private static final String VISUAL = "soatest_visual_difference.xml";
    private static final String EDGES = "association_edges.xml";
    private static final String CHANGE = "soatest_parabank_change_impact_";
    private static final String[] MODES = {
        "with_all_functional_tests", "without_functional_tests", "with_one_functional_test"
    };
    private static final String SCENARIO = "Test Suite: Test Suite/Test Suite: CartServicePort/";
    private static final String CHANGE_DETAILS =
        "Service Details (from the Service Catalog):\n"
        + "\tService Name:  http://localhost:18080/parabank/services/store-01V2?wsdl\n"
        + "\tSaved Service Version:  Old Version\n\tCurrent Service Version:  Current Version\n\n"
        + "Source: http://localhost:18081/current.wsdl";
    private static final Map<String, Document> REPORTS = new LinkedHashMap<>();
    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    @BeforeAll
    public static void transformLocalReports() throws Exception {
        TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
        URL stylesheet = SoatestXunitTransformationTest.class.getResource("/com/parasoft/findings/bamboo/soatest-xunit.xsl");
        assertNotNull(stylesheet, "The actual plugin stylesheet must be on the classpath");
        Templates templates;
        try (InputStream source = stylesheet.openStream()) {
            templates = factory.newTemplates(new StreamSource(source, stylesheet.toExternalForm()));
        }
        String[] filenames = {EXECUTION, VISUAL, EDGES,
            CHANGE + MODES[0] + ".xml", CHANGE + MODES[1] + ".xml", CHANGE + MODES[2] + ".xml"};
        for (String filename : filenames) {
            URL report = SoatestXunitTransformationTest.class.getResource("/reports/soatest-regression/" + filename);
            assertNotNull(report, filename);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (InputStream input = report.openStream()) {
                templates.newTransformer().transform(
                    new StreamSource(input, report.toExternalForm()), new StreamResult(output));
            }
            REPORTS.put(filename, DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(output.toByteArray())));
        }
    }

    @Test
    public void reportTotalsCountEachFailedCaseOnce() {
        counts(EXECUTION, 1, 14, 10);
        counts(VISUAL, 129, 526, 1);
        counts(EDGES, 1, 8, 3);
        for (String mode : MODES) {
            counts(CHANGE + mode + ".xml", 1, 7, 7);
        }
    }

    @Test
    public void suiteSummariesMatchEmittedCasesAndFailedCases() throws Exception {
        for (Map.Entry<String, Document> report : REPORTS.entrySet()) {
            NodeList suites = report.getValue().getElementsByTagName("testsuite");
            for (int i = 0; i < suites.getLength(); i++) {
                Element suite = (Element) suites.item(i);
                String context = report.getKey() + ": " + suite.getAttribute("name");
                int cases = ((NodeList) XPATH.evaluate("./testcase", suite, XPathConstants.NODESET)).getLength();
                int failed = ((NodeList) XPATH.evaluate("./testcase[failure]", suite, XPathConstants.NODESET)).getLength();
                assertEquals(cases, Integer.parseInt(suite.getAttribute("tests")), context);
                assertEquals(failed, Integer.parseInt(suite.getAttribute("failures")), context);
            }
        }
    }

    @Test
    public void allViolationKindsMatchSingleCasesAndOnlyTheAffectedParameterizedCase() {
        for (String kind : new String[] {"FuncViol", "ChangeImpact", "VisualDifference"}) {
            for (String shape : new String[] {"Single", "Parameterized"}) {
                String name = "Execution violations/" + shape + " " + kind
                    + ("Parameterized".equals(shape) ? "[Safari]" : "");
                failure(EXECUTION, name, kind + " failure", kind + " details");
                assertEquals("1.000", testcase(EXECUTION, name).getAttribute("time"), name);
            }
            passes(EXECUTION, "Execution violations/Parameterized " + kind + "[Chrome]");
        }
        passes(EXECUTION, "Execution violations/Passing test");
    }

    @Test
    public void mixedDiagnosticsKeepEveryMessageDetailAndErrorSeparator() {
        failure(EXECUTION, "Execution violations/Mixed violations", "Multiple errors reported",
            "==== ERROR ====\nFunctional failure"
            + "\n\n==== ERROR ====\nChange impact failure\nChange impact details"
            + "\n\n==== ERROR ====\nVisual difference failure\nSource: screenshot.png"
            + "\n\n==== ERROR ====\nFuncViol\nMixed details without summary");
    }

    @Test
    public void absentAndBlankMessagesFallBackToKindAndUnknownKindsStillFail() {
        failure(EXECUTION, "Execution violations/Functional without message", "FuncViol", "Details without summary");
        failure(EXECUTION, "Execution violations/Unknown violation", "Future violation failure", "Future violation details");
        failure(EXECUTION, "Execution violations/Without test case", "Visual comparison failed", "");
        failure(EDGES, "Edges/Whitespace message", "FutureViolation", "");
    }

    @Test
    public void delimiterBoundariesAndMissingIdsDoNotLeakFailures() {
        failure(EDGES, "Edges/Left boundary[gamma]", "Left failure", "Left details");
        failure(EDGES, "Edges/Right boundary[beta|gamma]", "Right failure", "Right details");
        for (String name : new String[] {"Edges/Left boundary[pass]", "Edges/Right boundary[|betagamma]",
            "", "Edges/Missing case ID[]", "Edges/Missing case ID[known]"}) {
            passes(EDGES, name);
        }
    }

    @Test
    public void nativeVisualReportKeepsScreenshotDiagnosticAndAllOtherCasesPass() {
        String affected = "Test Suite: Test Suite/Test Suite: Web Scenarios/"
            + "Scenario: SOA-18974/Test 7: Visually Compare Window 2";
        failure(VISUAL, affected, "Visual difference found at (0, 0) of size 1,920 x 870.",
            "Source: SOA-18974_ScreenshotOutputs_test4_window2.png");
        NodeList cases = REPORTS.get(VISUAL).getElementsByTagName("testcase");
        for (int i = 0; i < cases.getLength(); i++) {
            Element testcase = (Element) cases.item(i);
            if (!affected.equals(testcase.getAttribute("name"))) {
                assertEquals(0, testcase.getElementsByTagName("failure").getLength(), testcase.getAttribute("name"));
            }
        }
    }

    @Test
    public void changeAdvisorDisabledFilteredAndAllFunctionalModesKeepNativeDiagnostics() {
        for (String mode : MODES) {
            String filename = CHANGE + mode + ".xml";
            String details = failure(filename, SCENARIO + "Test 1: addItemToCart").getTextContent();
            assertTrue(details.contains("Operation \"addItemToCart\" - targetObjectUri changed"), mode);
            assertFalse(details.contains("Operation \"getItemByTitle\""), mode);
            assertEquals("with_all_functional_tests".equals(mode), details.contains("SOAP Fault"), mode);
        }
        failure(CHANGE + "with_all_functional_tests.xml", SCENARIO + "Test 6: submitOrder",
            "Multiple errors reported",
            "==== ERROR ====\nOperation \"submitOrder\" - targetObjectUri changed from \"http://bookstore.parasoft.com/\""
            + " to \"http://bookstore2.parasoft.com/\"\n" + CHANGE_DETAILS
            + "\n\n==== ERROR ====\nElement \"submitOrder\" - namespace changed from \"http://bookstore.parasoft.com/\""
            + " to \"http://bookstore2.parasoft.com/\"\n" + CHANGE_DETAILS);
        assertTrue(failure(CHANGE + "without_functional_tests.xml", SCENARIO + "Test 3: getItemById")
            .getTextContent().contains("getItemByIdentifier"));
    }

    private static void counts(String filename, int suites, int cases, int failures) {
        Document report = REPORTS.get(filename);
        assertEquals(suites, report.getElementsByTagName("testsuite").getLength(), filename + " suites");
        assertEquals(cases, report.getElementsByTagName("testcase").getLength(), filename + " cases");
        assertEquals(failures, report.getElementsByTagName("failure").getLength(), filename + " failures");
    }

    private static Element testcase(String filename, String name) {
        NodeList cases = REPORTS.get(filename).getElementsByTagName("testcase");
        Element found = null;
        int matches = 0;
        for (int i = 0; i < cases.getLength(); i++) {
            Element candidate = (Element) cases.item(i);
            if (name.equals(candidate.getAttribute("name"))) {
                found = candidate;
                matches++;
            }
        }
        assertEquals(1, matches, filename + ": expected one testcase named " + name);
        return found;
    }

    private static Element failure(String filename, String name) {
        NodeList failures = testcase(filename, name).getElementsByTagName("failure");
        assertEquals(1, failures.getLength(), filename + ": " + name + " must fail once");
        return (Element) failures.item(0);
    }

    private static void failure(String filename, String name, String message, String details) {
        Element failure = failure(filename, name);
        assertEquals(message, failure.getAttribute("message"), filename + ": " + name);
        assertEquals(details, failure.getTextContent(), filename + ": " + name);
    }

    private static void passes(String filename, String name) {
        assertEquals(0, testcase(filename, name).getElementsByTagName("failure").getLength(),
            filename + ": " + name + " must pass");
    }
}
