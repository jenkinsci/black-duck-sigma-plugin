/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.workflow;

import hudson.AbortException;
import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.ArgumentListBuilder;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.sigma.Messages;
import io.jenkins.plugins.sigma.SigmaBuildContext;
import io.jenkins.plugins.sigma.extension.tool.SigmaToolInstallation;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.verb.POST;

public class SigmaBinaryStep extends Builder implements SimpleBuildStep {
    public static final String FAILURE_MESSAGE = "Unable to perform Black Duck Rapid Scan Static static analysis: ";

    private String sigmaToolName;
    private String commandLine;
    private boolean ignorePolicies;

    @DataBoundConstructor
    public SigmaBinaryStep() {
        this.ignorePolicies = true;
    }

    public String getSigmaToolName() {
        return sigmaToolName;
    }

    @DataBoundSetter
    public void setSigmaToolName(final String sigmaToolName) {
        this.sigmaToolName = sigmaToolName;
    }

    public String getCommandLine() {
        return commandLine;
    }

    @DataBoundSetter
    public void setCommandLine(final String commandLine) {
        this.commandLine = commandLine;
    }

    public boolean isIgnorePolicies() {
        return ignorePolicies;
    }

    @DataBoundSetter
    public void setIgnorePolicies(final boolean ignorePolicies) {
        this.ignorePolicies = ignorePolicies;
    }

    @Override
    public void perform(
            Run<?, ?> run, FilePath workspace, EnvVars environment, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        listener.getLogger().println("Executing Rapid Scan Static binary Build Step.");
        try {
            if (Result.ABORTED.equals(run.getResult())) {
                throw new AbortException(FAILURE_MESSAGE + "The build was aborted.");
            }
            Computer computer = workspace.toComputer();
            Node node = (computer != null) ? computer.getNode() : null;
            Optional<SigmaToolInstallation> sigmaToolInstallation = getSigma(node, environment, listener);
            execute(run, workspace, environment, launcher, listener, sigmaToolInstallation.orElse(null));
        } catch (final InterruptedException e) {
            listener.error("[ERROR] Black Duck Rapid Scan Static thread was interrupted.", e);
            run.setResult(Result.ABORTED);
            Thread.currentThread().interrupt();
        } catch (final Exception ex) {
            listener.error("[ERROR] " + ex.getMessage());
            ex.printStackTrace(listener.fatalError(FAILURE_MESSAGE + "sigma command execution failed."));
            run.setResult(Result.UNSTABLE);
        }
    }

    private boolean execute(
            Run<?, ?> run,
            FilePath workingDirectory,
            EnvVars environment,
            Launcher launcher,
            TaskListener listener,
            SigmaToolInstallation sigmaToolInstallation)
            throws IOException, InterruptedException {
        SigmaBuildContext sigmaBuildContext =
                createBuildContext(environment, launcher, listener, sigmaToolInstallation);
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder(sigmaBuildContext, ignorePolicies, commandLine);
        ArgumentListBuilder argumentListBuilder = commandLineBuilder.buildArgumentList();

        Result result = executeSigma(sigmaBuildContext, argumentListBuilder, workingDirectory);
        run.setResult(result);
        return result == Result.SUCCESS;
    }

    private SigmaBuildContext createBuildContext(
            EnvVars environment, Launcher launcher, TaskListener listener, SigmaToolInstallation sigmaToolInstallation)
            throws IOException, InterruptedException {
        return new SigmaBuildContext(launcher, listener, environment, sigmaToolInstallation);
    }

    private Result executeSigma(
            SigmaBuildContext sigmaBuildContext, ArgumentListBuilder commandLineBuilder, FilePath workingDirectory)
            throws IOException, InterruptedException {
        ArgumentListBuilder commands = commandLineBuilder;
        if (!sigmaBuildContext.getLauncher().isUnix()) {
            // convert to a windows command line
            commands = commandLineBuilder.toWindowsCommand();
        }

        int returnCode = sigmaBuildContext
                .getLauncher()
                .launch()
                .cmds(commands)
                .envs(sigmaBuildContext.getEnvironment())
                .pwd(workingDirectory)
                .stdout(sigmaBuildContext.getListener())
                .join();

        if (returnCode != 0) {
            return Result.FAILURE;
        }
        return Result.SUCCESS;
    }

    private Optional<SigmaToolInstallation> getSigma(Node node, EnvVars environment, TaskListener listener)
            throws IOException, InterruptedException {
        Predicate<SigmaToolInstallation> sigmaToolFilter =
                (installation) -> sigmaToolName != null && sigmaToolName.equals(installation.getName());
        Optional<SigmaToolInstallation> sigmaToolInstallation = Arrays.stream(
                        getDescriptor().getInstallations())
                .filter(sigmaToolFilter)
                .findFirst();
        SigmaToolInstallation currentTool = null;
        if (sigmaToolInstallation.isPresent()) {
            currentTool = sigmaToolInstallation.get();
            if (node != null) {
                currentTool = currentTool.forNode(node, listener);
            }
            currentTool = currentTool.forEnvironment(environment);
        }

        return Optional.ofNullable(currentTool);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    @Symbol("sigma")
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @CopyOnWrite
        private volatile SigmaToolInstallation[] installations = new SigmaToolInstallation[0];

        public DescriptorImpl() {
            load();
        }

        protected DescriptorImpl(Class<? extends SigmaBinaryStep> clazz) {
            super(clazz);
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.workflow_step_displayName();
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        public SigmaToolInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(SigmaToolInstallation.DescriptorImpl.class);
        }

        public SigmaToolInstallation[] getInstallations() {
            return Arrays.copyOf(installations, installations.length);
        }

        public void setInstallations(SigmaToolInstallation... installations) {
            this.installations = installations;
            save();
        }

        @SuppressWarnings("unused")
        public boolean hasToolsConfigured() {
            return installations.length > 0;
        }

        @POST
        @SuppressWarnings("unused")
        public ListBoxModel doFillSigmaToolNameItems(@AncestorInPath Item item) {
            ListBoxModel items = new ListBoxModel();
            if (item == null && Jenkins.get().hasPermission(Jenkins.ADMINISTER)
                    || item != null && item.hasPermission(Item.CONFIGURE)) {
                for (SigmaToolInstallation installation : installations) {
                    items.add(installation.getName());
                }
            }

            return items;
        }
    }
}
