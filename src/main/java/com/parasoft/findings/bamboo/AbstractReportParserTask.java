/*
 * Copyright 2017 Parasoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.build.logger.interceptors.*;
import com.atlassian.bamboo.build.test.*;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.*;

public class AbstractReportParserTask implements TaskType {

    private final TestCollationService testCollationService;
    private final String xslFile;

    public AbstractReportParserTask(TestCollationService testCollationService, String xslFile) {
        this.testCollationService = testCollationService;
        this.xslFile = xslFile;
    }

    public TaskResult execute(TaskContext taskContext) throws TaskException {
        ErrorMemorisingInterceptor errorLines = ErrorMemorisingInterceptor.newInterceptor();
        taskContext.getBuildLogger().getInterceptorStack().add(errorLines);
        String filePattern = taskContext.getConfigurationMap().get(
                TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN);
        boolean pickupOutdatedFiles = Boolean.valueOf(taskContext.getConfigurationMap().get(
                TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE));
        try {
            testCollationService.collateTestResults(taskContext, filePattern,
                    new ReportCollector(xslFile), pickupOutdatedFiles);
            return TaskResultBuilder.newBuilder((CommonTaskContext) taskContext)
                    .checkTestFailures().build();
        } catch (Exception e) {
            throw new TaskException("Failed to execute task", e);
        } finally {
            CurrentResult currentResult = taskContext.getCommonContext().getCurrentResult();
            currentResult.addBuildErrors(errorLines.getErrorStringList());
        }
    }

}
