/*
 * The 3-Clause BSD License

 * Copyright © 2021 Broadcom. All rights reserved. The term “Broadcom” refers to Broadcom Inc. and/or its
 * affiliates. All authorized reproductions of this software must be marked with this language.

 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.

 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.

 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.ca.mat.application.performance.view;

import com.ca.mat.application.performance.control.build.ZoweCommandLineBuilder;
import com.ca.mat.application.performance.control.email.PMAReportNotificationHelper;
import com.ca.mat.application.performance.control.email.PerformanceAnalysisMailSender;
import com.ca.mat.application.performance.model.AnalysisOutput;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Mailer;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The email post build action on the pipeline definition.
 *
 * @author Arthur Pessoa
 */
public class EmailPostBuildAction extends Notifier implements SimpleBuildStep {

    /**
     * The recipients.
     */
    private final String recipients;

    /**
     * Constructor passing recipients.
     *
     * @param recipients the email recipients
     */
    @DataBoundConstructor
    public EmailPostBuildAction(String recipients) {
        this.recipients = recipients;
    }


    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher, @Nonnull TaskListener listener) {
        try {
            List<String> logLines = run.getLog(1000);
            StringBuilder log = new StringBuilder();
            for (String line : logLines) {
                log.append(line);
                log.append(System.lineSeparator());
            }
            Pattern pattern = Pattern.compile("Job (.*) completed.*Performance analysis is finished(.*)" +
                    "Running alert analysis.*Alert analysis is finished(.*)End of performance analysis", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(log.toString());
            if (matcher.find()) {
                String jobname = matcher.group(1);
                String performance = matcher.group(2);
                String alert = matcher.group(3);
                String history = "unknown";
                if (!alert.toLowerCase().contains("no alerts generated")) {
                    listener.getLogger().println("Alerts found! Executing MAT History...");
                    listener.getLogger().println("Running measurement history...");
                    String[] parameters = {"mat", "monitor", "history", "--profile", jobname};
                    history = new ZoweCommandLineBuilder().getCommandOutputNoTimeout(parameters);
                    listener.getLogger().println("Measurement history is finished");
                }
                AnalysisOutput analysisOutput = new AnalysisOutput(performance, alert, history);
                String template = PMAReportNotificationHelper.getInstance().getTemplate(analysisOutput);
                new PerformanceAnalysisMailSender(recipients, template, true, true, Mailer.descriptor().getCharset()).run(run, listener);

            } else {
                listener.getLogger().println("Could not evaluate performance and alert... Skipping e-mail notification.");
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException("An internal error occurred while reading the build log", e);
        }
    }

    /**
     * Get the recipients.
     *
     * @return the recipients
     */
    public String getRecipients() {
        return recipients;
    }

    /**
     * Inner class representing the descriptor.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Performance Benchmarking Report";
        }

    }
}
