/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.utils;

import hudson.tools.InstallSourceProperty;
import io.jenkins.plugins.sigma.extension.tool.SigmaBinaryInstaller;
import io.jenkins.plugins.sigma.extension.tool.SigmaToolInstallation;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.rules.TestWatcher;

public class SigmaTestUtil extends TestWatcher {
    public static final String PROPERTY_KEY_INSTALLER_DOWNLOAD_URL = "tool.installer.downloadUrl";
    public static final String PROPERTY_KEY_INSTALLER_TIMEOUT = "tool.installer.timeout";
    public static final String TEST_TOOL_NAME = "sigma-test";
    public static final String TEST_TOOL_HOME = "";
    private Properties properties;
    private String downloadUrl;

    public SigmaTestUtil() {
        properties = new Properties();
    }

    public void loadProperties() throws IOException {
        try (InputStream propsInputStream = getClass().getClassLoader().getResourceAsStream("test.properties")) {
            properties.load(propsInputStream);
        }
        downloadUrl = getProperties().getProperty(PROPERTY_KEY_INSTALLER_DOWNLOAD_URL);
        if (StringUtils.isBlank(downloadUrl)) {
            downloadUrl = getTestShellScriptAsURL().orElse(null);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getTimeoutInSeconds() {
        return Integer.valueOf(getProperties().getProperty(PROPERTY_KEY_INSTALLER_TIMEOUT));
    }

    public void addInstallation(Supplier<SigmaToolInstallation.DescriptorImpl> sigmaToolSupplier) throws IOException {
        String downloadUrl = getDownloadUrl();
        int timeout = getTimeoutInSeconds();
        SigmaBinaryInstaller sigmaBinaryInstaller = new SigmaBinaryInstaller("");
        sigmaBinaryInstaller.setDownloadUrl(downloadUrl);
        sigmaBinaryInstaller.setTimeout(timeout);
        InstallSourceProperty installSourceProperty =
                new InstallSourceProperty(Collections.singletonList(sigmaBinaryInstaller));
        List<InstallSourceProperty> propertySet = Collections.singletonList(installSourceProperty);
        SigmaToolInstallation installation = new SigmaToolInstallation(TEST_TOOL_NAME, TEST_TOOL_HOME, propertySet);
        SigmaToolInstallation.DescriptorImpl toolInstallationDescriptor = sigmaToolSupplier.get();
        toolInstallationDescriptor.setInstallations(installation);
    }

    private Optional<String> getTestShellScriptAsURL() throws MalformedURLException {
        File sigmaShellLocation = new File("build/tmp/test/binary/home/sigma");
        try (InputStream shellScriptInput = getClass().getClassLoader().getResourceAsStream("sigma.sh")) {
            FileUtils.copyToFile(shellScriptInput, sigmaShellLocation);
            return Optional.of(sigmaShellLocation.toURI().toURL().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public String readPipelineScript(String resourcePath) {
        String pipelineScript = "";
        try {
            File resourceFile = new File(
                    getClass().getClassLoader().getResource(resourcePath).toURI());
            pipelineScript = FileUtils.readFileToString(resourceFile, StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return pipelineScript;
    }
}
