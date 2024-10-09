/*
 * Copyright (c) 2024 Black Duck Software, Inc. All rights reserved worldwide.
 */
package io.jenkins.plugins.sigma.extension.issues;

import edu.hm.hafner.analysis.parser.JsonParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.sigma.Messages;
import javax.annotation.Nonnull;
import org.kohsuke.stapler.DataBoundConstructor;

public class SigmaTool extends ReportScanningTool {
    public static final String TOOL_ID = "black-duck-sigma-issues-tool";
    public static final String DEFAULT_FILE_PATTERN = "**/sigma-results.json";

    @DataBoundConstructor
    public SigmaTool() {
        super();
    }

    @Override
    public JsonParser createParser() {
        return new JsonParser();
    }

    /**
     * Descriptor for this static analysis tool.
     */
    @Extension
    public static class DescriptorImpl extends ReportScanningToolDescriptor {
        public DescriptorImpl() {
            super(TOOL_ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.issues_reporting_tool_displayName();
        }

        @Override
        public String getPattern() {
            return DEFAULT_FILE_PATTERN;
        }
    }
}
