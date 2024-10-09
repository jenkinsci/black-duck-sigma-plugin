/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.workflow;

import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.sigma.SigmaBuildContext;
import io.jenkins.plugins.sigma.extension.tool.SigmaToolInstallation;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;

public class CommandLineBuilder {
    public static final String DEFAULT_COMMAND_LINE = "analyze --format jenkins";
    public static final String COMMAND_TOKEN_IGNORE_POLICIES = "--ignore-policies";
    private static final String COMMAND_TOKEN_ANALYZE = "analyze";
    private SigmaBuildContext sigmaBuildContext;
    private boolean ignorePolicies;
    private String commandLineOverride;

    public CommandLineBuilder(
            SigmaBuildContext sigmaBuildContext, boolean ignorePolicies, @Nullable String commandLineOverride) {
        this.sigmaBuildContext = sigmaBuildContext;
        this.ignorePolicies = ignorePolicies;
        this.commandLineOverride = commandLineOverride;
    }

    public ArgumentListBuilder buildArgumentList() throws IOException, InterruptedException {
        String currentCommandLine = DEFAULT_COMMAND_LINE;
        PrintStream logger = sigmaBuildContext.getListener().getLogger();
        if (StringUtils.isBlank(commandLineOverride)) {
            logger.println("Using default command line.");
        } else {
            logger.println("Using command line defined in the build step.");
            currentCommandLine = commandLineOverride;
        }
        currentCommandLine = handleIgnorePoliciesOption(logger, currentCommandLine);
        ArgumentListBuilder argumentListBuilder = new ArgumentListBuilder();
        addSigmaExecutableToCommand(sigmaBuildContext, argumentListBuilder);
        argumentListBuilder.addTokenized(currentCommandLine);
        return argumentListBuilder;
    }

    private String handleIgnorePoliciesOption(PrintStream logger, String currentCommandLine) {
        String updatedCommandLine = currentCommandLine;

        if (ignorePolicies && !currentCommandLine.contains(COMMAND_TOKEN_IGNORE_POLICIES)) {
            int analyzeIndex = currentCommandLine.indexOf(COMMAND_TOKEN_ANALYZE);
            if (analyzeIndex >= 0) {
                logger.println(String.format("Adding %s to command line", COMMAND_TOKEN_IGNORE_POLICIES));
                int insertIndex = analyzeIndex + COMMAND_TOKEN_ANALYZE.length() + 1;
                StringBuilder commandBuilder = new StringBuilder(currentCommandLine);
                // add the commandFlag command line argument with space before and after it.
                commandBuilder.insert(insertIndex, String.format(" %s ", COMMAND_TOKEN_IGNORE_POLICIES));
                updatedCommandLine = commandBuilder.toString();
            } else {
                logger.println(String.format(
                        "The analyze sub-command was not found.  Cannot add the %s command line option",
                        COMMAND_TOKEN_IGNORE_POLICIES));
            }
        }

        return updatedCommandLine;
    }

    private void addSigmaExecutableToCommand(
            SigmaBuildContext sigmaBuildContext, ArgumentListBuilder commandLineBuilder)
            throws IOException, InterruptedException {
        Launcher launcher = sigmaBuildContext.getLauncher();
        TaskListener listener = sigmaBuildContext.getListener();
        Optional<SigmaToolInstallation> sigmaToolInstallation = sigmaBuildContext.getSigmaToolInstallation();
        if (sigmaToolInstallation.isPresent()) {
            SigmaToolInstallation installation = sigmaToolInstallation.get();
            listener.getLogger()
                    .println(String.format("Rapid Scan Static tool installation found. '%s'", installation.getHome()));

            installation.getExecutablePath(launcher, listener).ifPresent(commandLineBuilder::add);
        } else {
            // sigma tool installation not defined try to run sigma with a simple command.  It will work if sigma is on
            // executable on the path
            if (sigmaBuildContext.getLauncher().isUnix()) {
                commandLineBuilder.add(SigmaToolInstallation.UNIX_SIGMA_COMMAND);
            } else {
                commandLineBuilder.add(SigmaToolInstallation.WINDOWS_SIGMA_COMMAND);
            }
        }
    }
}
