/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.tool;

import hudson.FilePath;
import hudson.Functions;
import hudson.ProxyConfiguration;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.io.FileUtils;

public class FileDownloadInstaller extends MasterToSlaveCallable<Void, IOException> {
    public static final String INSTALLED_FROM_FILE_NAME = ".installedFrom";
    public static final String TIMESTAMP_FILE_NAME = ".timestamp";
    private static final String LOG_PREFIX = "Rapid Scan Static installation: ";
    private final String downloadUrl;
    private final FilePath downloadLocation;
    private final int timeoutInMilliseconds;
    private final TaskListener log;
    private final BinaryUpdateCheck binaryUpdateCheck;

    public FileDownloadInstaller(
            String downloadUrl,
            FilePath downloadLocation,
            int timeoutInMilliseconds,
            TaskListener log,
            BinaryUpdateCheck updateChecker) {
        this.downloadUrl = downloadUrl;
        this.downloadLocation = downloadLocation;
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        this.log = log;
        this.binaryUpdateCheck = updateChecker;
    }

    @Override
    public Void call() throws IOException {
        try {
            URL binarySourceUrl = new URL(downloadUrl);
            FilePath installedFrom = downloadLocation.child(INSTALLED_FROM_FILE_NAME);
            FilePath timestampPath = downloadLocation.child(TIMESTAMP_FILE_NAME);
            String binaryFileName = Functions.isWindows()
                    ? SigmaToolInstallation.WINDOWS_SIGMA_COMMAND
                    : SigmaToolInstallation.UNIX_SIGMA_COMMAND;
            FilePath binaryPath = downloadLocation.child(binaryFileName);
            if (binaryUpdateCheck.isUpToDate(installedFrom, timestampPath, binaryPath)) {
                log.getLogger().println(LOG_PREFIX + "Skipping tool installation already up to date on node.");
                return null;
            }
            downloadLocation.mkdirs();
            File fileToWrite = new File(binaryPath.getRemote());
            log.getLogger().println(LOG_PREFIX + "Installing Rapid Scan Static binary...");
            URLConnection binaryHostConnection = ProxyConfiguration.open(binarySourceUrl);
            binaryHostConnection.setConnectTimeout(timeoutInMilliseconds);
            binaryHostConnection.connect();
            try (InputStream inputStream = binaryHostConnection.getInputStream()) {
                FileUtils.copyToFile(inputStream, fileToWrite);
            }
            installedFrom.write(downloadUrl, StandardCharsets.UTF_8.name());
            timestampPath.touch(binaryHostConnection.getLastModified());
            // set the binary to be executable on linux based systems
            if (!Functions.isWindows()) {
                fileToWrite.setExecutable(true, false);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        return null;
    }
}
