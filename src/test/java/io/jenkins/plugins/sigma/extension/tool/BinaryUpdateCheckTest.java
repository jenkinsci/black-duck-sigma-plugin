/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.tool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.FilePath;
import io.jenkins.plugins.sigma.utils.SigmaTestUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BinaryUpdateCheckTest {
    private static final String HOME_DIRECTORY = "build/tmp/test/update_test/home";
    private SigmaTestUtil sigmaTestUtil = new SigmaTestUtil();
    private File homeDirectory = new File(HOME_DIRECTORY);
    private File fileToCheck = new File(homeDirectory, "sigma");

    @Before
    public void initializeData() throws IOException {
        sigmaTestUtil.loadProperties();
        homeDirectory.mkdirs();
        fileToCheck.createNewFile();
    }

    @After
    public void cleanupDirectories() throws IOException {
        FileUtils.deleteQuietly(homeDirectory);
    }

    @Test
    public void testUpToDate() throws IOException, InterruptedException {
        FilePath homeFilePath = new FilePath(homeDirectory);
        FilePath filePathToCheck = new FilePath(fileToCheck);
        URL binaryUrl = filePathToCheck.toURI().toURL();
        String binaryUrlString = binaryUrl.toString();
        FilePath installedFrom = homeFilePath.child(FileDownloadInstaller.INSTALLED_FROM_FILE_NAME);
        FilePath timestampPath = homeFilePath.child(FileDownloadInstaller.TIMESTAMP_FILE_NAME);
        installedFrom.write(binaryUrlString, StandardCharsets.UTF_8.name());
        timestampPath.touch(System.currentTimeMillis());

        BinaryUpdateCheck updateCheck = new BinaryUpdateCheck(binaryUrlString, 1);
        assertTrue(updateCheck.isUpToDate(installedFrom, timestampPath, filePathToCheck));
    }

    @Test
    public void testInstalledFromSource() throws IOException, InterruptedException {
        FilePath homeFilePath = new FilePath(homeDirectory);
        FilePath filePathToCheck = new FilePath(fileToCheck);
        URL binaryUrl = filePathToCheck.toURI().toURL();
        String binaryUrlString = binaryUrl.toString();
        FilePath installedFrom = homeFilePath.child(FileDownloadInstaller.INSTALLED_FROM_FILE_NAME);
        FilePath timestampPath = homeFilePath.child(FileDownloadInstaller.TIMESTAMP_FILE_NAME);
        installedFrom.write(binaryUrlString, StandardCharsets.UTF_8.name());
        timestampPath.touch(System.currentTimeMillis());

        BinaryUpdateCheck updateCheck = new BinaryUpdateCheck(binaryUrlString, 1);
        assertTrue(updateCheck.isUpToDate(installedFrom, timestampPath, filePathToCheck));
    }

    @Test
    public void testDifferentSource() throws IOException, InterruptedException {
        FilePath homeFilePath = new FilePath(homeDirectory);
        FilePath filePathToCheck = new FilePath(fileToCheck);
        URL binaryUrl = filePathToCheck.toURI().toURL();
        String binaryUrlString = binaryUrl.toString();
        FilePath installedFrom = homeFilePath.child(FileDownloadInstaller.INSTALLED_FROM_FILE_NAME);
        FilePath timestampPath = homeFilePath.child(FileDownloadInstaller.TIMESTAMP_FILE_NAME);
        installedFrom.write("a different source", StandardCharsets.UTF_8.name());
        timestampPath.touch(System.currentTimeMillis());

        BinaryUpdateCheck updateCheck = new BinaryUpdateCheck(binaryUrlString, 1);
        assertFalse(updateCheck.isUpToDate(installedFrom, timestampPath, filePathToCheck));
    }

    @Test
    public void testTimestampOlder() throws IOException, InterruptedException {
        FilePath homeFilePath = new FilePath(homeDirectory);
        FilePath filePathToCheck = new FilePath(fileToCheck);
        filePathToCheck.touch(System.currentTimeMillis());
        URL binaryUrl = filePathToCheck.toURI().toURL();
        String binaryUrlString = binaryUrl.toString();
        FilePath installedFrom = homeFilePath.child(FileDownloadInstaller.INSTALLED_FROM_FILE_NAME);
        FilePath timestampPath = homeFilePath.child(FileDownloadInstaller.TIMESTAMP_FILE_NAME);
        installedFrom.write(binaryUrlString, StandardCharsets.UTF_8.name());
        timestampPath.touch(System.currentTimeMillis() - 10000);

        BinaryUpdateCheck updateCheck = new BinaryUpdateCheck(binaryUrlString, 1);
        assertFalse(updateCheck.isUpToDate(installedFrom, timestampPath, filePathToCheck));
    }

    @Test
    public void testBinaryNonexistent() throws IOException, InterruptedException {
        FilePath homeFilePath = new FilePath(homeDirectory);
        FilePath filePathToCheck = new FilePath(fileToCheck);
        URL binaryUrl = filePathToCheck.toURI().toURL();
        String binaryUrlString = binaryUrl.toString();
        FilePath installedFrom = homeFilePath.child(FileDownloadInstaller.INSTALLED_FROM_FILE_NAME);
        FilePath timestampPath = homeFilePath.child(FileDownloadInstaller.TIMESTAMP_FILE_NAME);
        installedFrom.write(binaryUrlString, StandardCharsets.UTF_8.name());
        timestampPath.touch(System.currentTimeMillis());
        FilePath otherBinayFile = homeFilePath.child("a_different_sigma_binary");

        BinaryUpdateCheck updateCheck = new BinaryUpdateCheck(binaryUrlString, 1);
        assertFalse(updateCheck.isUpToDate(installedFrom, timestampPath, otherBinayFile));
    }
}
