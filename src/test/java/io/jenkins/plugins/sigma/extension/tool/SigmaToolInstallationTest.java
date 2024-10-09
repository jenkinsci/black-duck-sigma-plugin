/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import io.jenkins.plugins.sigma.Messages;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SigmaToolInstallationTest {
    private static final String HOME_DIRECTORY = "build/tmp/test/home";

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @After
    public void cleanupExecutableFile() {
        FileUtils.deleteQuietly(new File(HOME_DIRECTORY));
    }

    @Test
    public void testDescriptor() {
        SigmaToolInstallation installation =
                new SigmaToolInstallation("sigma-test", HOME_DIRECTORY, Collections.emptyList());
        SigmaToolInstallation.DescriptorImpl descriptor =
                (SigmaToolInstallation.DescriptorImpl) installation.getDescriptor();
        descriptor.setInstallations(installation);
        assertEquals(Messages.installation_displayName(), descriptor.getDisplayName());
        assertEquals(1, descriptor.getInstallations().length);
        assertEquals(installation, descriptor.getInstallations()[0]);
        assertEquals(1, descriptor.getDefaultInstallers().size());
    }

    @Test
    public void testExecutablePathNull() throws IOException, InterruptedException {
        SigmaToolInstallation installation =
                new SigmaToolInstallation("sigma-test", HOME_DIRECTORY, Collections.emptyList());
        Launcher launcher = jenkinsRule.createLocalLauncher();
        TaskListener listener = jenkinsRule.createTaskListener();
        Optional<String> path = installation.getExecutablePath(launcher, listener);
        assertFalse(path.isPresent());
    }

    @Test
    public void testExecutablePath() throws IOException, InterruptedException {
        SigmaToolInstallation installation =
                new SigmaToolInstallation("sigma-test", HOME_DIRECTORY, Collections.emptyList());
        String excutablePath = HOME_DIRECTORY + "/" + SigmaToolInstallation.UNIX_SIGMA_COMMAND;
        FilePath createExecutable = new FilePath(new File(excutablePath));
        Launcher fileLauncher = createExecutable.createLauncher(jenkinsRule.createTaskListener());
        TaskListener listener = jenkinsRule.createTaskListener();
        createExecutable.mkdirs();
        createExecutable.touch(System.currentTimeMillis());
        Optional<String> path = installation.getExecutablePath(fileLauncher, listener);
        assertTrue(path.isPresent());
        assertTrue(path.get().contains(excutablePath));
    }
}
