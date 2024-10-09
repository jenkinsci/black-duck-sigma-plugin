/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.workflow;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.slaves.DumbSlave;
import io.jenkins.plugins.sigma.extension.tool.SigmaToolInstallation;
import io.jenkins.plugins.sigma.utils.SigmaTestUtil;
import java.io.IOException;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

public class SigmaBinaryStepIntegrationTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    public SigmaTestUtil sigmaTestUtil = new SigmaTestUtil();

    @Before
    public void loadProps() throws IOException {
        sigmaTestUtil.loadProperties();
    }

    @Test
    public void testSimpleBuildStepSucceeds() throws Exception {
        sigmaTestUtil.addInstallation(
                () -> jenkinsRule.jenkins.getDescriptorByType(SigmaToolInstallation.DescriptorImpl.class));
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("Test Project");
        project.setScm(new SingleFileSCM("JenkinsSigmaTestClass.java", "public class JenkinsSigmaTestClass {}"));
        SigmaBinaryStep step = new SigmaBinaryStep();
        step.setSigmaToolName(SigmaTestUtil.TEST_TOOL_NAME);
        assertNotNull(step.getDescriptor().getInstallations());
        project.getBuildersList().add(step);
        assertTrue(step.getDescriptor().isApplicable(FreeStyleProject.class));
        jenkinsRule.buildAndAssertSuccess(project);
    }

    @Test
    public void testSlaveBuildStepSucceeds() throws Exception {
        sigmaTestUtil.addInstallation(
                () -> jenkinsRule.jenkins.getDescriptorByType(SigmaToolInstallation.DescriptorImpl.class));
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("Test Project");
        DumbSlave slave = jenkinsRule.createSlave();
        project.setScm(new SingleFileSCM("JenkinsSigmaTestClass.java", "public class JenkinsSigmaTestClass {}"));
        project.setAssignedNode(jenkinsRule.jenkins.getNode(slave.getNodeName()));
        SigmaBinaryStep step = new SigmaBinaryStep();
        step.setSigmaToolName(SigmaTestUtil.TEST_TOOL_NAME);
        assertNotNull(step.getDescriptor().getInstallations());
        project.getBuildersList().add(step);
        assertTrue(step.getDescriptor().isApplicable(FreeStyleProject.class));
        jenkinsRule.buildAndAssertSuccess(project);
    }

    @Test
    public void testPipelineBuildStepSucceeds() throws Exception {
        sigmaTestUtil.addInstallation(
                () -> jenkinsRule.jenkins.getDescriptorByType(SigmaToolInstallation.DescriptorImpl.class));
        String script = sigmaTestUtil.readPipelineScript("pipeline-example.txt");
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "Test Pipeline Project");
        CpsFlowDefinition pipelineDefinition = new CpsFlowDefinition(script, true);
        project.setDefinition(pipelineDefinition);
        SigmaBinaryStep step = new SigmaBinaryStep();
        step.setSigmaToolName(SigmaTestUtil.TEST_TOOL_NAME);
        assertNotNull(step.getDescriptor().getInstallations());
        jenkinsRule.buildAndAssertSuccess(project);
    }

    @Test
    public void testPipelineBuildStepWithWarningsPluginSucceeds() throws Exception {
        sigmaTestUtil.addInstallation(
                () -> jenkinsRule.jenkins.getDescriptorByType(SigmaToolInstallation.DescriptorImpl.class));
        String script = sigmaTestUtil.readPipelineScript("pipeline-with-warnings-example.txt");
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "Test Pipeline with Warnings Project");
        CpsFlowDefinition pipelineDefinition = new CpsFlowDefinition(script, true);
        project.setDefinition(pipelineDefinition);
        SigmaBinaryStep step = new SigmaBinaryStep();
        step.setSigmaToolName(SigmaTestUtil.TEST_TOOL_NAME);
        assertNotNull(step.getDescriptor().getInstallations());
        jenkinsRule.buildAndAssertSuccess(project);
    }
}
