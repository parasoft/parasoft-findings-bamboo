package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.atlassian.bamboo.webwork.util.ActionParametersMapImpl;
import com.atlassian.sal.api.message.I18nResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;

public class ReportParserConfiguratorTest {
    @Mock
    private I18nResolver i18nResolver;

    @Mock
    private TaskDefinition taskDefinition;

    private ReportParserConfigurator reportParserConfigurator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reportParserConfigurator = new ReportParserConfigurator(i18nResolver);
    }

    @Test
    public void testGenerateTaskConfigMap() {
        ActionParametersMap params = new ActionParametersMapImpl(createParamsMap());
        Map<String, String> results = reportParserConfigurator.generateTaskConfigMap(params, null);
        paramsAssertation(results);
    }

    @Test
    public void testPopulateContextForCreate() {
        Map<String, Object> results = new HashMap<>();
        reportParserConfigurator.populateContextForCreate(results);
        paramsAssertation(results);
    }

    @Test
    public void testPopulateContextForEdit() {
        Map<String, String> testMap = createParamsMap();
        doReturn(testMap).when(taskDefinition).getConfiguration();

        Map<String, Object> results = new HashMap<>();
        reportParserConfigurator.populateContextForEdit(results, taskDefinition);
        paramsAssertation(results);
    }

    @Test
    public void testValidate_withoutError() {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ActionParametersMap params = new ActionParametersMapImpl(createParamsMap());

        reportParserConfigurator.validate(params, errorCollection);
        assertEquals(0, errorCollection.getErrors().size());
    }

    @Test
    public void testValidate_withError() {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        doReturn("Expected test error").when(i18nResolver).getText(anyString());
        ActionParametersMap params = new ActionParametersMapImpl(new HashMap<>());

        reportParserConfigurator.validate(params, errorCollection);

        assertEquals(1, errorCollection.getErrors().size());
        assertEquals("Expected test error", errorCollection.getErrors().get(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN).get(0));
    }

    @Test
    public void testTaskProducesTestResults () {
        assertTrue(reportParserConfigurator.taskProducesTestResults(taskDefinition));
    }

    private Map<String, String> createParamsMap() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN, "**/rep*.xml");
        paramMap.put(TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE, "false");
        return paramMap;
    }

    private void paramsAssertation(Map<String, ?> results) {
        assertEquals(2, results.size());
        assertEquals("**/rep*.xml", results.get(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN));
        assertEquals("false", results.get(TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE).toString());
    }
}
