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

import javax.inject.Inject;

import com.atlassian.bamboo.build.logger.interceptors.ErrorMemorisingInterceptor;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.v2.build.CurrentResult;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

public class ReportParserTask implements TaskType {

    private final TestCollationService testCollationService;

    @Inject
    public ReportParserTask(@ComponentImport TestCollationService testCollationService) {
        this.testCollationService = testCollationService;
    }

    @Override
    public TaskResult execute(TaskContext taskContext) throws TaskException {
        ErrorMemorisingInterceptor errorLines = ErrorMemorisingInterceptor.newInterceptor();
        taskContext.getBuildLogger().getInterceptorStack().add(errorLines);
        String filePattern = taskContext.getConfigurationMap().get(
                TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN);
        boolean pickupOutdatedFiles = Boolean.parseBoolean(taskContext.getConfigurationMap().get(
                TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE));
        try {
            testCollationService.collateTestResults(taskContext, filePattern,
                    new ReportCollector(), pickupOutdatedFiles);
            return TaskResultBuilder.newBuilder(taskContext)
                    .checkTestFailures().build();
        } finally {
            CurrentResult currentResult = taskContext.getCommonContext().getCurrentResult();
            currentResult.addBuildErrors(errorLines.getErrorStringList());
        }
    }

}
