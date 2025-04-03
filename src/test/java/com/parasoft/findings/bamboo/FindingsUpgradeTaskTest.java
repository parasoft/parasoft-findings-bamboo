package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.build.BuildDefinitionForBuild;
import com.atlassian.bamboo.build.DefaultBuildDefinitionForBuild;
import com.atlassian.bamboo.build.DefaultJob;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class FindingsUpgradeTaskTest {
    @Mock
    private PlanManager planManager;

    private FindingsUpgradeTask findingsUpgradeTask;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        findingsUpgradeTask = new FindingsUpgradeTask(planManager);
    }

    @Test
    public void testGetBuildNumber() {
        assertEquals(1,  findingsUpgradeTask.getBuildNumber());
    }

    @Test
    public void testGetShortDescription() {
        assertEquals("Upgrades Parasoft Findings configuration.",  findingsUpgradeTask.getShortDescription());
    }

    @Test
    public void testDoUpgrade_normal() {
        try {
            prepareForTestingDoUpgrade(true, true);
            findingsUpgradeTask.doUpgrade();
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testDoUpgrade_buildDefinitionXmlIsNull() {
        try {
            prepareForTestingDoUpgrade(false, false);
            findingsUpgradeTask.doUpgrade();
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testDoUpgrade_xmlDataIsNull() {
        try {
            prepareForTestingDoUpgrade(true, false);
            findingsUpgradeTask.doUpgrade();
        } catch (Exception e) {
            fail("Should not reach here");
        }
    }

    @Test
    public void testGetPluginKey() {
        assertEquals("com.parasoft.parasoft-findings-bamboo",  findingsUpgradeTask.getPluginKey());
    }

    private void prepareForTestingDoUpgrade(boolean hasBuildDefinitionXml, boolean hasXmlData) {
        List<Plan> plans = new ArrayList<>();
        List<Job> jobs = new ArrayList<>();
        Job job = createTestJob(hasBuildDefinitionXml, hasXmlData);
        jobs.add(job);
        Chain plan = mock(Chain.class);
        plans.add(plan);


        doReturn(plans).when(planManager).getAllPlans(any(), anyInt(), anyInt());
        doReturn(jobs).when(plan).getAllJobs();
        doReturn(1).when(planManager).getPlanCount(Chain.class);
    }

    private Job createTestJob(boolean isBuildDefinitionXml, boolean isXmlData) {
        Job job = new DefaultJob();
        if (isBuildDefinitionXml) {
            BuildDefinitionForBuild buildDefinitionXml = new DefaultBuildDefinitionForBuild(false);
            if (isXmlData) {
                buildDefinitionXml.setXmlData(
                    "<atlassian-plugin>" +
                        "<taskType key=\"parasoftAnalyzers10xReportParserTask\" name=\"Parasoft Analyzers 10.x Report Parser\" class=\"com.parasoft.findings.bamboo.Analyzers10xReportParserTask\">...</taskType>" +
                        "<taskType key=\"parasoftSOAtest9xReportParserTask\" name=\"Parasoft SOAtest 9.x Report Parser\" class=\"com.parasoft.findings.bamboo.SOAtest9xReportParserTask\">...</taskType>" +
                    "</atlassian-plugin>"
                );
            }
            job.setBuildDefinitionXml(buildDefinitionXml);
        }
        return job;
    }
}
