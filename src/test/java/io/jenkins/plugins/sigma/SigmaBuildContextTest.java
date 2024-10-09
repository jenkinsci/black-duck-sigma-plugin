/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import io.jenkins.plugins.sigma.extension.tool.SigmaToolInstallation;
import java.util.Collections;
import org.junit.Test;
import org.mockito.Mockito;

public class SigmaBuildContextTest {
    @Test
    public void testBuildContext() {
        TaskListener taskListener = null;
        BuildListener buildListener = Mockito.mock(BuildListener.class);
        SigmaToolInstallation sigmaToolInstallation =
                new SigmaToolInstallation("sigma-test", "test/home", Collections.emptyList());
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        Launcher launcher = new Launcher.LocalLauncher(taskListener);
        SigmaBuildContext buildContext = new SigmaBuildContext(launcher, buildListener, envVars, sigmaToolInstallation);
        assertEquals(launcher, buildContext.getLauncher());
        assertEquals(buildListener, buildContext.getListener());
        assertTrue(buildContext.getSigmaToolInstallation().isPresent());
        assertEquals(
                sigmaToolInstallation, buildContext.getSigmaToolInstallation().get());
        assertEquals(envVars, buildContext.getEnvironment());
    }

    @Test
    public void testBuildContextEmptyFields() {
        TaskListener taskListener = null;
        BuildListener buildListener = Mockito.mock(BuildListener.class);
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        Launcher launcher = new Launcher.LocalLauncher(taskListener);
        SigmaBuildContext buildContext = new SigmaBuildContext(launcher, buildListener, envVars, null);
        assertEquals(launcher, buildContext.getLauncher());
        assertEquals(buildListener, buildContext.getListener());
        assertEquals(envVars, buildContext.getEnvironment());
        assertFalse(buildContext.getSigmaToolInstallation().isPresent());
    }
}
