/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.workflow;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.sigma.SigmaBuildContext;
import io.jenkins.plugins.sigma.extension.tool.SigmaToolInstallation;
import io.jenkins.plugins.sigma.utils.ArgumentListAssertions;
import java.util.Collections;
import org.junit.Test;
import org.mockito.Mockito;

public class CommandLineBuilderTest {

    @Test
    public void testDefaultCommandLineUnix() throws Exception {
        Launcher launcher = Mockito.mock(Launcher.class);
        BuildListener listener = Mockito.mock(BuildListener.class);

        Mockito.when(launcher.isUnix()).thenReturn(Boolean.TRUE);
        Mockito.when(listener.getLogger()).thenReturn(System.out);

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        SigmaBuildContext sigmaBuildContext = new SigmaBuildContext(launcher, listener, envVars, null);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, false, null);
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();
        ArgumentListAssertions.assertArgumentList(argumentListBuilder, "sigma", "analyze", "--format", "jenkins");
    }

    @Test
    public void testCommandLineWithIgnoresPolicyUnix() throws Exception {
        Launcher launcher = Mockito.mock(Launcher.class);
        BuildListener listener = Mockito.mock(BuildListener.class);

        Mockito.when(launcher.isUnix()).thenReturn(Boolean.TRUE);
        Mockito.when(listener.getLogger()).thenReturn(System.out);

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        SigmaBuildContext sigmaBuildContext = new SigmaBuildContext(launcher, listener, envVars, null);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, true, null);
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();
        ArgumentListAssertions.assertArgumentList(
                argumentListBuilder,
                "sigma",
                "analyze",
                CommandLineBuilder.COMMAND_TOKEN_IGNORE_POLICIES,
                "--format",
                "jenkins");
    }

    @Test
    public void testDefaultCommandLineWindows() throws Exception {
        Launcher launcher = Mockito.mock(Launcher.class);
        BuildListener listener = Mockito.mock(BuildListener.class);

        Mockito.when(launcher.isUnix()).thenReturn(Boolean.FALSE);
        Mockito.when(listener.getLogger()).thenReturn(System.out);

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        SigmaBuildContext sigmaBuildContext = new SigmaBuildContext(launcher, listener, envVars, null);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, false, null);
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();
        ArgumentListAssertions.assertArgumentList(argumentListBuilder, "sigma.exe", "analyze", "--format", "jenkins");
    }

    @Test
    public void testCommandLineWithIgnoresPolicyWindows() throws Exception {
        Launcher launcher = Mockito.mock(Launcher.class);
        BuildListener listener = Mockito.mock(BuildListener.class);

        Mockito.when(launcher.isUnix()).thenReturn(Boolean.FALSE);
        Mockito.when(listener.getLogger()).thenReturn(System.out);

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        SigmaBuildContext sigmaBuildContext = new SigmaBuildContext(launcher, listener, envVars, null);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, true, null);
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();
        ArgumentListAssertions.assertArgumentList(
                argumentListBuilder,
                "sigma.exe",
                "analyze",
                CommandLineBuilder.COMMAND_TOKEN_IGNORE_POLICIES,
                "--format",
                "jenkins");
    }

    @Test
    public void testCommandLineOverrideWithIgnorePolicies() throws Exception {
        Launcher launcher = Mockito.mock(Launcher.class);
        BuildListener listener = Mockito.mock(BuildListener.class);

        Mockito.when(launcher.isUnix()).thenReturn(Boolean.TRUE);
        Mockito.when(listener.getLogger()).thenReturn(System.out);
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();

        String commandLineOverride = "--config config/file/path --policy policy/file/path analyze --format gitlab";
        SigmaBuildContext sigmaBuildContext = new SigmaBuildContext(launcher, listener, envVars, null);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, true, commandLineOverride);
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();
        ArgumentListAssertions.assertArgumentList(
                argumentListBuilder,
                "sigma",
                "--config",
                "config/file/path",
                "--policy",
                "policy/file/path",
                "analyze",
                CommandLineBuilder.COMMAND_TOKEN_IGNORE_POLICIES,
                "--format",
                "gitlab");
    }

    @Test
    public void testCommandLineOverrideMissingAnalyze() throws Exception {
        Launcher launcher = Mockito.mock(Launcher.class);
        BuildListener listener = Mockito.mock(BuildListener.class);

        Mockito.when(launcher.isUnix()).thenReturn(Boolean.TRUE);
        Mockito.when(listener.getLogger()).thenReturn(System.out);
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();

        String commandLineOverride = "--config config/file/path --policy policy/file/path checkers";
        SigmaBuildContext sigmaBuildContext = new SigmaBuildContext(launcher, listener, envVars, null);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, true, commandLineOverride);
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();
        ArgumentListAssertions.assertArgumentList(
                argumentListBuilder,
                "sigma",
                "--config",
                "config/file/path",
                "--policy",
                "policy/file/path",
                "checkers");
    }

    @Test
    public void testCommandLineOverrideEmptyString() throws Exception {
        Launcher launcher = Mockito.mock(Launcher.class);
        BuildListener listener = Mockito.mock(BuildListener.class);

        Mockito.when(launcher.isUnix()).thenReturn(Boolean.TRUE);
        Mockito.when(listener.getLogger()).thenReturn(System.out);

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        SigmaBuildContext sigmaBuildContext = new SigmaBuildContext(launcher, listener, envVars, null);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, false, "              \t\n");
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();
        ArgumentListAssertions.assertArgumentList(argumentListBuilder, "sigma", "analyze", "--format", "jenkins");
    }

    @Test
    public void testDefaultWithToolPath() throws Exception {
        VirtualChannel virtualChannel = Mockito.mock(VirtualChannel.class);
        Launcher launcher = Mockito.mock(Launcher.class);
        BuildListener listener = Mockito.mock(BuildListener.class);
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        SigmaToolInstallation sigmaToolInstallation =
                new SigmaToolInstallation("sigma-test", "test/home", Collections.emptyList());

        Mockito.when(virtualChannel.call(Mockito.any(Callable.class))).thenReturn(Boolean.TRUE);
        Mockito.when(launcher.isUnix()).thenReturn(Boolean.TRUE);
        Mockito.when(launcher.getChannel()).thenReturn(virtualChannel);
        Mockito.when(listener.getLogger()).thenReturn(System.out);

        SigmaBuildContext sigmaBuildContext = new SigmaBuildContext(launcher, listener, envVars, sigmaToolInstallation);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, false, null);
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();
        ArgumentListAssertions.assertArgumentList(
                argumentListBuilder, "test/home/sigma", "analyze", "--format", "jenkins");
    }

    @Test
    public void testCommandLineOverrideWithToolPath() throws Exception {
        VirtualChannel virtualChannel = Mockito.mock(VirtualChannel.class);
        Launcher launcher = Mockito.mock(Launcher.class);
        BuildListener listener = Mockito.mock(BuildListener.class);
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        SigmaToolInstallation sigmaToolInstallation =
                new SigmaToolInstallation("sigma-test", "test/home", Collections.emptyList());

        Mockito.when(virtualChannel.call(Mockito.any(Callable.class))).thenReturn(Boolean.TRUE);
        Mockito.when(launcher.isUnix()).thenReturn(Boolean.TRUE);
        Mockito.when(launcher.getChannel()).thenReturn(virtualChannel);
        Mockito.when(listener.getLogger()).thenReturn(System.out);

        String commandLineOverride = "--config config/file/path --policy policy/file/path checkers";
        SigmaBuildContext sigmaBuildContext = new SigmaBuildContext(launcher, listener, envVars, sigmaToolInstallation);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, true, commandLineOverride);
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();
        ArgumentListAssertions.assertArgumentList(
                argumentListBuilder,
                "test/home/sigma",
                "--config",
                "config/file/path",
                "--policy",
                "policy/file/path",
                "checkers");
    }
}
