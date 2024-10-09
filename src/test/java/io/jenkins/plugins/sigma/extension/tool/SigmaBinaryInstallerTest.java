/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tasks.Maven;
import hudson.util.FormValidation;
import hudson.util.StreamTaskListener;
import io.jenkins.plugins.sigma.Messages;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

public class SigmaBinaryInstallerTest {
    // this isn't used in this class but the getDescriptor method internally calls Jenkins.getInstance().
    // Need the Jenkins rule to be able to test the descriptors.
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testGettersOfInstaller() {
        String label = "sigma-test";
        String downloadUrl = "downloadUrl";
        int timeout = 60;
        SigmaBinaryInstaller installer = new SigmaBinaryInstaller(label);
        installer.setDownloadUrl(downloadUrl);
        installer.setTimeout(timeout);
        assertEquals(label, installer.getLabel());
        assertEquals(downloadUrl, installer.getDownloadUrl());
        assertEquals(timeout, installer.getTimeout());
    }

    @Test
    public void testDescriptor() {
        String label = "sigma-test";
        String downloadUrl = "downloadUrl";
        int timeout = 60;
        SigmaBinaryInstaller installer = new SigmaBinaryInstaller(label);
        installer.setDownloadUrl(downloadUrl);
        installer.setTimeout(timeout);
        assertEquals(Messages.installer_displayName(), installer.getDescriptor().getDisplayName());
        assertTrue(installer.getDescriptor().isApplicable(SigmaToolInstallation.class));
    }

    @Test
    public void testDescriptorInstallerNotApplicable() {
        String label = "sigma-test";
        String downloadUrl = "downloadUrl";
        int timeout = 60;
        SigmaBinaryInstaller installer = new SigmaBinaryInstaller(label);
        installer.setDownloadUrl(downloadUrl);
        installer.setTimeout(timeout);
        assertFalse(installer.getDescriptor().isApplicable(Maven.MavenInstallation.class));
    }

    @Test
    public void testDescriptorValidatorEmptyURL() throws ServletException, IOException {
        String label = "sigma-test";
        String downloadUrl = "      \t\n";
        int timeout = 60;
        SigmaBinaryInstaller installer = new SigmaBinaryInstaller(label);
        installer.setDownloadUrl(downloadUrl);
        installer.setTimeout(timeout);
        assertTrue(installer.getDescriptor().doCheckDownloadUrl(downloadUrl).kind == FormValidation.Kind.ERROR);
    }

    @Test
    public void testDescriptorValidatorNullURL() throws ServletException, IOException {
        String label = "sigma-test";
        String downloadUrl = null;
        int timeout = 60;
        SigmaBinaryInstaller installer = new SigmaBinaryInstaller(label);
        installer.setDownloadUrl(downloadUrl);
        installer.setTimeout(timeout);
        assertTrue(installer.getDescriptor().doCheckDownloadUrl(downloadUrl).kind == FormValidation.Kind.ERROR);
    }

    @Test
    public void testDescriptorValidatorMalformedURL() throws ServletException, IOException {
        String label = "sigma-test";
        String downloadUrl = "htp:a_bad_url.example.com?443";
        int timeout = 60;
        SigmaBinaryInstaller installer = new SigmaBinaryInstaller(label);
        installer.setDownloadUrl(downloadUrl);
        installer.setTimeout(timeout);
        assertTrue(installer.getDescriptor().doCheckDownloadUrl(downloadUrl).kind == FormValidation.Kind.ERROR);
    }

    @Test
    public void testDescriptorValidatorValidURL() throws ServletException, IOException {
        String label = "sigma-test";
        String downloadUrl = "https://www.blackduck.com/";
        int timeout = 60;
        SigmaBinaryInstaller installer = new SigmaBinaryInstaller(label);
        installer.setDownloadUrl(downloadUrl);
        installer.setTimeout(timeout);
        assertTrue(installer.getDescriptor().doCheckDownloadUrl(downloadUrl).kind == FormValidation.Kind.OK);
    }

    @Test
    public void testAbortException() {
        String label = "sigma-test";
        String downloadUrl = "downloadUrl";
        int timeout = 60;
        Node node = Mockito.mock(Node.class);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        TaskListener log = new StreamTaskListener(byteArrayOutputStream);
        SigmaToolInstallation toolInstallation =
                new SigmaToolInstallation("sigma-installer-test", "", Collections.emptyList());

        Mockito.when(node.getRootPath()).thenReturn(new FilePath(new File(".")));
        Mockito.when(node.getDisplayName()).thenReturn("sigma-test-node");
        SigmaBinaryInstaller installer = new SigmaBinaryInstaller(label);
        installer.setDownloadUrl(downloadUrl);
        installer.setTimeout(timeout);
        try {
            installer.performInstallation(toolInstallation, node, log);
            fail("Installer didn't throw abort exception when channel is expected to be null.");
        } catch (AbortException ex) {
            assertEquals(
                    "Failed to install Rapid Scan Static on Node sigma-test-node from downloadUrl.", ex.getMessage());
        }
    }
}
