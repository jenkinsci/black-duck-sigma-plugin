/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.TaskListener;
import io.jenkins.plugins.sigma.extension.tool.SigmaToolInstallation;
import java.util.Optional;
import javax.annotation.Nullable;

public class SigmaBuildContext {
    private final Launcher launcher;
    private final TaskListener listener;
    private final EnvVars environment;
    private final SigmaToolInstallation sigmaToolInstallation;

    public SigmaBuildContext(
            Launcher launcher,
            TaskListener listener,
            EnvVars environment,
            @Nullable SigmaToolInstallation sigmaToolInstallation) {
        this.launcher = launcher;
        this.listener = listener;
        this.environment = environment;
        this.sigmaToolInstallation = sigmaToolInstallation;
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public TaskListener getListener() {
        return listener;
    }

    public EnvVars getEnvironment() {
        return environment;
    }

    public Optional<SigmaToolInstallation> getSigmaToolInstallation() {
        return Optional.ofNullable(sigmaToolInstallation);
    }
}
