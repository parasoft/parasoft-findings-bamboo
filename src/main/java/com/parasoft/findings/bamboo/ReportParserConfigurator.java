package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.collections.*;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.utils.error.*;
import com.atlassian.sal.api.message.*;

import java.util.*;

import org.jetbrains.annotations.*;

public class ReportParserConfigurator extends AbstractTaskConfigurator implements TaskTestResultsSupport {
    private static final String DEFAULT_REPORT_LOCATION = "**/report.xml";
    private I18nResolver i18nResolver;

    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params,
            @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN, params.getString(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN));
        config.put(TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE, params.getString(TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE));
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
        context.put(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN,
                taskDefinition.getConfiguration().get(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN));
        context.put(TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE,
                taskDefinition.getConfiguration().get(TaskConfigConstants.CFG_TEST_OUTDATED_RESULTS_FILE));
    }

    // NYI. See task SOA-8579, remove i18nResolver != null
    @Override
    public void validate(@NotNull ActionParametersMap params, @NotNull ErrorCollection errorCollection) {
        String reportLocation = params.getString(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN);
        if (i18nResolver != null && (reportLocation == null || reportLocation.trim().length() == 0)) {
            errorCollection.addError(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN,
                    i18nResolver.getText("report.location.pattern.error"));
        }
    }

    public boolean taskProducesTestResults(TaskDefinition taskDefinition) {
        return true;
    }

    // NYI. See task SOA-8579
    public void setI18nResolver(I18nResolver i18nResolver) {
        this.i18nResolver = i18nResolver;
    }
}
