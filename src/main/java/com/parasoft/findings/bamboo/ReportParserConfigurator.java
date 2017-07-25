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

import com.atlassian.bamboo.collections.*;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.utils.error.*;
import com.atlassian.plugin.spring.scanner.annotation.imports.*;
import com.atlassian.sal.api.message.*;

import java.util.*;

import javax.inject.*;

import org.jetbrains.annotations.*;

public class ReportParserConfigurator extends AbstractTaskConfigurator implements TaskTestResultsSupport {
    private static final String DEFAULT_REPORT_LOCATION = "**/rep*.xml";
    private static final List<String> FIELD_KEYS =
            Arrays.asList(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN, TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE);
    private I18nResolver i18nResolver;

    @Inject
    public ReportParserConfigurator(@ComponentImport I18nResolver i18nResolver,
            @ComponentImport TaskConfiguratorHelper taskConfiguratorHelper) {
        this.i18nResolver = i18nResolver;
        this.taskConfiguratorHelper = taskConfiguratorHelper;
    }

    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
            @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(config, params, FIELD_KEYS);
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN, DEFAULT_REPORT_LOCATION);
        context.put(TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE, Boolean.FALSE);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context,
            @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELD_KEYS);
    }

    @Override
    public void validate(@NotNull ActionParametersMap params, @NotNull ErrorCollection errorCollection) {
        String reportLocation = params.getString(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN);
        if (reportLocation == null || reportLocation.trim().length() == 0) {
            errorCollection.addError(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN,
                    i18nResolver.getText("report.location.pattern.error"));
        }
    }

    public boolean taskProducesTestResults(TaskDefinition taskDefinition) {
        return true;
    }
}
