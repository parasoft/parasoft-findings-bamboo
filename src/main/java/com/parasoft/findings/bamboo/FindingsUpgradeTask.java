/*
 * (C) Copyright Parasoft Corporation 2022.  All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

package com.parasoft.findings.bamboo;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.atlassian.bamboo.build.BuildDefinitionForBuild;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.PlanPredicates;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

@ExportAsService
@Component
public class FindingsUpgradeTask implements PluginUpgradeTask {
    private static final Logger log = Logger.getLogger(FindingsUpgradeTask.class);

    private final PlanManager planManager;

    @Inject
    public FindingsUpgradeTask(@ComponentImport PlanManager planManager) {
        this.planManager = planManager;
    }

    @Override
    public int getBuildNumber() {
        return 1;
    }

    @Override
    public String getShortDescription() {
        return "Upgrades Parasoft Findings configuration."; //$NON-NLS-1$
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception {
        log.info("upgrading task configurations"); //$NON-NLS-1$
        updatePlans();
        return Collections.emptySet();
    }

    // see com.atlassian.bamboo.upgrade.tasks.AbstractTaskConfigurationUpgradeTask
    private void updatePlans() {
        int numberOfChains = planManager.getPlanCount(Chain.class);
        for (int i = 0; i < numberOfChains; i += 100) {
            int firstResult = i;
            planManager.getAllPlans(Chain.class, firstResult, 100).stream()
            .filter(PlanPredicates::planIsMaster)
            .flatMap(plan -> plan.getAllJobs().stream()).forEach(this::upgradeJob);
        }
    }

    private void upgradeJob(Job job) {
        BuildDefinitionForBuild buildDefinitionXml = job.getBuildDefinitionXml();
        if (buildDefinitionXml == null) {
            return;
        }
        String xmlData = buildDefinitionXml.getXmlData();
        if (xmlData == null) {
            return;
        }
        boolean modify = false;
        if (xmlData.contains("parasoftSOAtest9xReportParserTask")) { //$NON-NLS-1$
            xmlData = xmlData.replace("parasoftSOAtest9xReportParserTask", "parasoftReportParserTask"); //$NON-NLS-1$ //$NON-NLS-2$
            modify = true;
        }
        if (xmlData.contains("parasoftAnalyzers10xReportParserTask")) { //$NON-NLS-1$
            xmlData = xmlData.replace("parasoftAnalyzers10xReportParserTask", "parasoftReportParserTask"); //$NON-NLS-1$ //$NON-NLS-2$
            modify = true;
        }
        if (modify) {
            log.info("updating job: " + job.getPlanKey()); //$NON-NLS-1$
            buildDefinitionXml.setXmlData(xmlData);
            planManager.savePlan(job);
        }
    }

    @Override
    public String getPluginKey() {
        return "com.parasoft.parasoft-findings-bamboo"; //$NON-NLS-1$
    }
}
