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

import com.ca.mat.application.performance.control.build.EndevorCommandLineBuilder;
import hudson.EnvVars;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;


/**
 * The autogen build step on the pipeline definition.
 *
 * @author Arthur Pessoa
 */
public class Autogen extends Builder implements SimpleBuildStep {
    /**
     * The environment.
     */
    private final String environment;
    /**
     * The system.
     */
    private final String system;
    /**
     * The subsystem.
     */
    private final String subsystem;
    /**
     * The stage.
     */
    private final String stage;
    /**
     * String for generating Endevor element passing parameters.
     */
    private static final String storProcFormat = "GENERATE ELEMENT %s FROM ENV '%s' SYS '%s' SUB '%s' TYPE '*' " +
            "STAGE %s OPTIONS CCID PMA COMMENT 'AUTOGEN' %s AUTOGEN SPAN NONE .";
    /**
     * The instance.
     */
    private final String instance;
    /**
     * Whether the element is signed out.
     */
    private final boolean signout;
    /**
     * The element.
     */
    private final String element;

    /**
     * Constructor.
     *
     * @param element     the Endevor element
     * @param environment the environment
     * @param system      the system
     * @param subsystem   the subsystem
     * @param stage       the stage
     * @param instance    the instance
     * @param signout     whether to override signout
     */
    @DataBoundConstructor
    public Autogen(String element, String environment, String system, String subsystem,
                   String stage, String instance, boolean signout) {
        this.environment = environment;
        this.system = system;
        this.subsystem = subsystem;
        this.stage = stage;
        this.signout = signout;
        this.instance = instance;
        this.element = element;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        String[] elements = element.split(",");
        for (String elm : elements) {
            listener.getLogger().println("Executing autogen for element: " + elm);
            elm = elm.trim();
            String content = String.format(storProcFormat, elm, environment, system, subsystem, stage,
                    signout ? "OVERRIDE SIGNOUT" : "");
            final EnvVars env = run.getEnvironment(listener);
            String jenkinsHome = env.get("JENKINS_HOME");
            String jobName = env.get("JOB_NAME");
            String buildID = env.get("BUILD_ID");
            String path = jenkinsHome + "/jobs/" + jobName + "/builds/" + buildID + "/scl";
            File destination = new File(path);
            Iterable<? extends CharSequence> sequence = Arrays.asList(content.split(System.lineSeparator()));
            Files.write(destination.toPath(), sequence, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            if (destination.exists()) {
                String response = new EndevorCommandLineBuilder().submitSCL(destination.getPath(), instance);
                if (response.toLowerCase().contains("error")) {
                    listener.getLogger().println(response);
                    throw new RuntimeException("An error occurred while submitting the SCL");
                }
                listener.getLogger().println(response);
            } else {
                throw new RuntimeException("Could not create SCL to generate the Endevor subsystem elements");
            }
        }
    }

    /**
     * Get the environment.
     *
     * @return the environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Get the stage.
     *
     * @return the stage
     */

    public String getStage() {
        return stage;
    }

    /**
     * Get the subsystem.
     *
     * @return the subsystem
     */

    public String getSubsystem() {
        return subsystem;
    }

    /**
     * Get the system.
     *
     * @return the system
     */
    public String getSystem() {
        return system;
    }

    /**
     * Get the instance.
     *
     * @return the instance
     */
    public String getInstance() {
        return instance;
    }

    /**
     * Get the sign out.
     *
     * @return the sign out
     */
    public boolean getSignout() {
        return signout;
    }

    /**
     * Get the element.
     *
     * @return the element
     */

    public String getElement() {
        return element;
    }

    /**
     * Inner class representing the descriptor.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Autogen";
        }

        @Override
        @SuppressWarnings("rawtypes")
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }
}
