/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.tool;

import hudson.FilePath;
import hudson.ProxyConfiguration;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

public class BinaryUpdateCheck implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String downloadUrl;
    private final int timeoutInMilliseconds;

    public BinaryUpdateCheck(final String downloadUrl, final int timeoutInMilliseconds) {
        this.downloadUrl = downloadUrl;
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    public boolean isUpToDate(FilePath installedFrom, FilePath timestampPath, FilePath binaryPath)
            throws IOException, InterruptedException {
        boolean sameInstalledFromURL =
                installedFrom.exists() && installedFrom.readToString().equals(downloadUrl);
        boolean notModifiedSinceInstalled = !hasModifiedSinceInstalled(timestampPath);
        // SIGMA-3269: Added check for binaryPathExists because windows might have a "sigma" binary installed,
        // not a "sigma.exe" binary because of the bug in SIGMA-3269. So we need to download the binary as "sigma.exe".
        boolean binaryPathExists = binaryPath.exists();
        return sameInstalledFromURL && notModifiedSinceInstalled && binaryPathExists;
    }

    public boolean hasModifiedSinceInstalled(FilePath timestampFilePath) throws IOException, InterruptedException {
        if (!timestampFilePath.exists()) {
            // if timestamp file doesn't exist assume it has modified to attempt a download.
            return true;
        }
        long lastModified = timestampFilePath.lastModified();
        URL binarySourceUrl = new URL(downloadUrl);
        URLConnection binaryHostConnection = ProxyConfiguration.open(binarySourceUrl);
        if (lastModified != 0) {
            binaryHostConnection.setIfModifiedSince(lastModified);
        }
        binaryHostConnection.setConnectTimeout(timeoutInMilliseconds);
        binaryHostConnection.connect();
        long serverLastModified = binaryHostConnection.getLastModified();
        return serverLastModified > lastModified;
    }
}
