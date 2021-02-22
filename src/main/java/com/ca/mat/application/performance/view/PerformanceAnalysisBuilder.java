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
import com.ca.mat.application.performance.model.JCLEntity;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the perfomance analysis build step on the pipeline definition.
 *
 * @author Arthur Pessoa
 */
public class PerformanceAnalysisBuilder extends Builder implements SimpleBuildStep {
    /**
     * Regex for valid characters in a dataset name.
     */
    private static final Pattern DATASET_NAME_PATTERN = Pattern.compile("^[\"\']?([a-zA-Z#@$][a-zA-Z#@$0-9]+\\." +
            "[a-zA-Z#@$0-9]+[a-zA-Z#@$0-9.]+?[a-zA-Z#@$0-9])(\\([a-zA-Z#@$][a-zA-Z0-9#@$]{0,7}\\))?[\"\']?$");
    /**
     * Regex fpr job name pattern.
     */
    private static final String JOB_NAME_PATTERN = "^ ([a-zA-Z#@$*&-_][a-zA-Z0-9#@$*&-_]{0,7})";
    /**
     * Regex fpr job name JES2 output pattern.
     */
    private static final String JOB_NAME_JES_OUTPUT_PATTERN = "JOBNAME  ([a-zA-Z0-9#@$]{0,8})";
    /**
     * Job output pattern, EXEC PGM=...
     */
    private static final String PROGRAM_NAME_JES_OUTPUT_PATTERN = "EXEC.*PGM=([a-zA-Z0-9#@$]{1,10})";
    /**
     * Mainframe maximum data set length of 44.
     */
    private static final int MAX_DATASET_LENGTH = 44;
    /**
     * The command line builder for Zowe.
     */
    private static final ZoweCommandLineBuilder zoweCommandLineBuilder = new ZoweCommandLineBuilder();
    /**
     * The test job.
     */
    private final String testjob;
    /**
     * The job name.
     */
    private String jobname;
    /**
     * The program.
     */
    private String program;

    /**
     * Constructor.
     *
     * @param testjob the job to test
     */
    @DataBoundConstructor
    public PerformanceAnalysisBuilder(String testjob) {
        this.testjob = testjob;
    }

    /**
     * Return the test job.
     *
     * @return the test job.
     */
    public String getTestjob() {
        return testjob;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) {
        listener.getLogger().println("Validating input parameters...");
        validateParameters();
        listener.getLogger().println("Input parameters validated");
        listener.getLogger().println("Running job " + testjob + "...");
        String output = submitJob(listener);
        listener.getLogger().println(output);
        listener.getLogger().println("Job " + jobname + " completed");
        listener.getLogger().println("Verifying if job is on inclusion list...");
        verifyPMAScope(listener);
        listener.getLogger().println("Running performance analysis...");
        String[] parameters = {"pma", "get", "perf", jobname};
        String performance = zoweCommandLineBuilder.getCommandOutputNoTimeout(parameters);
        listener.getLogger().println("Performance analysis is finished");
        listener.getLogger().println(performance);
        if (performance.toLowerCase().contains("error")) {
            throw new RuntimeException("An error occured while running pma performance analysis.");
        }
        listener.getLogger().println("Running alert analysis...");
        parameters[2] = "abyj";
        String alert = zoweCommandLineBuilder.getCommandOutputNoTimeout(parameters);
        listener.getLogger().println("Alert analysis is finished");
        listener.getLogger().println(alert);
        if (alert.toLowerCase().contains("error")) {
            throw new RuntimeException("An error occured while running the alert analysis.");
        }
        listener.getLogger().println("End of performance analysis");
    }

    private void verifyPMAScope(TaskListener listener) {
        String[] parameters = {"pma", "scope", "getlj"};
        String listInclusion = zoweCommandLineBuilder.getCommandOutputNoTimeout(parameters);
        if (listInclusion.toLowerCase().contains("error")) {
            listener.getLogger().println(listInclusion);
            throw new RuntimeException("An error occured while retrieving the list of included jobs");
        }
        parameters[2] = "getlp";
        String listExclusion = zoweCommandLineBuilder.getCommandOutputNoTimeout(parameters);
        if (listExclusion.toLowerCase().contains("error")) {
            listener.getLogger().println(listExclusion);
            throw new RuntimeException("An error occured while retrieving the list of excluded programs");
        }
        JCLEntity[] entities = new JCLEntity[]{new JCLEntity(jobname, JCLEntity.Type.JOB),
                new JCLEntity(program, JCLEntity.Type.PROGRAM)};
        for (JCLEntity entity : entities) {
            Pattern pattern = Pattern.compile(JOB_NAME_PATTERN, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(entity.isJob() ? listInclusion : listExclusion);
            boolean included = false;
            while (matcher.find()) {
                String inclusion = matcher.group(1).trim();
                inclusion = inclusion.replace(".", "\\. ").replace("_", ".").replace("*", ".*");
                if (inclusion.equals(".")) {
                    inclusion = "[.]";
                }
                Pattern inclusionPattern = Pattern.compile(inclusion);
                if (inclusionPattern.matcher(entity.getName()).find()) {
                    included = true;
                    break;
                }
                if (included) {
                    break;
                }
            }
            if (entity.isJob() ? !included : included) {
                String format = String.format("%s %s is %s in the %s list", entity.isJob() ?
                                "Job" : "Program", entity.getName(), entity.isJob() ? "included" : "excluded",
                        entity.isJob() ? "inclusion" : "exclusion");
                throw new RuntimeException(format);
            }
        }
    }

    private String submitJob(TaskListener listener) {
        String[] parameters = {"jobs", "sub", "ds", "--vasc", testjob};
        String output = zoweCommandLineBuilder.getCommandOutputNoTimeout(parameters);
        if (!output.contains("ENDED - RC=0000")) {
            listener.getLogger().println(output);
            throw new RuntimeException("Job " + testjob + " failed");
        }
        Pattern pattern = Pattern.compile(JOB_NAME_JES_OUTPUT_PATTERN);
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            jobname = matcher.group(1);
        } else {
            throw new RuntimeException("Could not evaluate jobname for test job: " + testjob);
        }
        pattern = Pattern.compile(PROGRAM_NAME_JES_OUTPUT_PATTERN);
        matcher = pattern.matcher(output);
        if (matcher.find()) {
            program = matcher.group(1);
        } else {
            throw new RuntimeException("Could not evaluate execute program for test job: " + testjob);
        }
        return output;
    }

    private void validateParameters() {
        Matcher datasetNameRules = DATASET_NAME_PATTERN.matcher(testjob);
        if (datasetNameRules.find()) {
            String dataset = datasetNameRules.group(1);
            if (dataset.length() > MAX_DATASET_LENGTH) {
                throw new RuntimeException("A data set name cannot be longer than 44 characters.");
            } else if (dataset.contains("..")) {
                throw new RuntimeException("A data set name cannot contain two successive periods;" +
                        " for example, HLQ..ABC");
            }
        } else {
            throw new RuntimeException("The input dataset " + testjob +
                    " does not conform the z/OS naming rules. Please refer to " +
                    "the documentation on z/OS data set naming rules.");
        }
    }

    /**
     * Inner class representing the descriptor.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        @SuppressWarnings("rawtypes")
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Performance Benchmarking";
        }

    }

}
