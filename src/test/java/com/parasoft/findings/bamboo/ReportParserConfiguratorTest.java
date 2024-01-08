package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.sal.api.message.I18nResolver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportParserConfiguratorTest {

    @Test
    public void testTaskProducesTestResults () {
        I18nResolver i18nResolver = Mockito.mock(I18nResolver.class);
        TaskDefinition taskDefinition = Mockito.mock(TaskDefinition.class);
        ReportParserConfigurator reportParserConfigurator = new ReportParserConfigurator(i18nResolver);

        assertTrue(reportParserConfigurator.taskProducesTestResults(taskDefinition));
    }
}
