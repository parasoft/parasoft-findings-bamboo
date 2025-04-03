package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.LogInterceptorStack;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.build.test.TestReportCollector;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.configuration.ConfigurationMapImpl;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.CommonContext;
import com.atlassian.bamboo.v2.build.CurrentResult;
import com.atlassian.bamboo.v2.build.CurrentResultImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReportParserTaskTest {
    @Mock
    private TaskContext taskContext;

    @Mock
    private TestCollationService testCollationService;

    @Mock
    private BuildLogger buildLogger;

    @Mock
    private CommonContext commonTaskContext;

    @Mock
    private TaskResultBuilder taskResultBuilder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExecute() {
        ReportParserTask reportParserTask = new ReportParserTask(testCollationService);
        LogInterceptorStack logInterceptorStack = new LogInterceptorStack();
        CurrentResult currentResult = new CurrentResultImpl();
        ConfigurationMap configurationMap = new ConfigurationMapImpl();
        configurationMap.put(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN, "**/rep*.xml");
        configurationMap.put(TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE, "false");

        doReturn(buildLogger).when(taskContext).getBuildLogger();
        doReturn(logInterceptorStack).when(buildLogger).getInterceptorStack();
        doReturn(configurationMap).when(taskContext).getConfigurationMap();
        doReturn(commonTaskContext).when(taskContext).getCommonContext();
        doReturn(currentResult).when(commonTaskContext).getCurrentResult();
        doNothing().when(testCollationService).collateTestResults(any(TaskContext.class), anyString(), any(TestReportCollector.class), anyBoolean());

        try (MockedStatic<TaskResultBuilder> mockedTaskResultBuilderStatic = mockStatic(TaskResultBuilder.class)) {
            mockedTaskResultBuilderStatic.when(() -> TaskResultBuilder.newBuilder(taskContext)).thenReturn(taskResultBuilder);
            doReturn(taskResultBuilder).when(taskResultBuilder).checkTestFailures();

            reportParserTask.execute(taskContext);
            verify(taskResultBuilder, times(1)).build();
        } catch (TaskException e) {
            fail("Should not reach here");
        }
    }
}
