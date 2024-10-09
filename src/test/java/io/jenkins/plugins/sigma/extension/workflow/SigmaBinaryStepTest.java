/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.util.ListBoxModel;
import hudson.util.StreamTaskListener;
import io.jenkins.plugins.sigma.extension.tool.SigmaToolInstallation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class SigmaBinaryStepTest {
    // this isn't used in this class but the getDescriptor method internally calls Jenkins.getInstance().
    // Need the Jenkins rule to be able to test the descriptors.
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testStepFields() {
        SigmaBinaryStep step = new SigmaBinaryStep();
        step.setSigmaToolName("sigma-test");
        step.setIgnorePolicies(false);
        step.setCommandLine("analyze");
        assertEquals("sigma-test", step.getSigmaToolName());
        assertEquals("analyze", step.getCommandLine());
        assertFalse(step.isIgnorePolicies());
    }

    @Test
    public void testDescriptor() {
        SigmaBinaryStep.DescriptorImpl descriptor = new SigmaBinaryStep.DescriptorImpl();
        SigmaToolInstallation toolInstallation =
                new SigmaToolInstallation("sigma-test", "home", Collections.emptyList());
        descriptor.setInstallations(toolInstallation);
        ListBoxModel toolOptions = descriptor.doFillSigmaToolNameItems(null);
        assertFalse(toolOptions.isEmpty());
        assertTrue(descriptor.hasToolsConfigured());
        assertEquals(toolInstallation.getDescriptor(), descriptor.getToolDescriptor());
    }

    @Test
    public void testPipelinePreviousBuildResult() throws IOException, InterruptedException {
        SigmaBinaryStep step = new SigmaBinaryStep();
        SigmaToolInstallation toolInstallation =
                new SigmaToolInstallation("sigma-test", "home", Collections.emptyList());
        step.setSigmaToolName("sigma-test");
        step.setIgnorePolicies(false);
        step.getDescriptor().setInstallations(toolInstallation);
        Run<?, ?> run = Mockito.mock(Run.class);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        TaskListener taskListener = new StreamTaskListener(byteArrayOutputStream);
        Launcher launcher = new Launcher.LocalLauncher(taskListener);
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars environment = prop.getEnvVars();

        ArgumentCaptor<Result> buildResultCaptor = ArgumentCaptor.forClass(Result.class);
        Mockito.when(run.getResult()).thenReturn(Result.ABORTED);
        Mockito.doNothing().when(run).setResult(buildResultCaptor.capture());
        step.perform(run, null, environment, launcher, taskListener);
        assertEquals(Result.UNSTABLE, buildResultCaptor.getValue());
    }
}
