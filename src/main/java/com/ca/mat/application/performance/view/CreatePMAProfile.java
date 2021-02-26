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

import com.ca.mat.application.performance.control.annotation.Default;
import com.ca.mat.application.performance.control.annotation.ProfileMapping;
import com.ca.mat.application.performance.control.future.GetPMAProfileList;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import jenkins.util.ProgressiveRendering;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Class to create a PMA profile.
 */
@Extension
public class CreatePMAProfile extends CreateProfile<CreatePMAProfile.AddProfile> {

    @Override
    public String getDescription() {
        return "The CA MAT Detect profile enables the communication of the plugin with the Performance Management " +
                "Assistant component (PMA) of CA MAT that ensures the automatic performance issue detection.";
    }

    @Override
    public List<AddProfile> getEntries() {
        return getConfig().getEntries();
    }

    @Override
    public List<AddProfile> setEntries(List<AddProfile> entries) {
        return config.entries = entries;
    }

    @Override
    protected void setConfig(MultipleEntryFields<AddProfile> read) {
        config = ((CreatePMAProfile) read).getConfig();
    }

    /**
     * Inner class representing the descriptor.
     */
    @Extension
    public static final class DescriptorImpl extends PerformanceBenchmarkingDescriptor {
    }

    /**
     * The official plugin name published on NPM Registry.
     *
     * @return the offical plugin name
     */
    protected String getPluginName() {
        return "@broadcom/mat-detect-for-zowe-cli";
    }

    /**
     * The zowe command group: e.g. zowe pma.
     *
     * @return the zowe command group
     */
    protected String getPluginCmd() {
        return "pma";
    }

    @Override
    protected String getPluginDisplayName() {
        return "CA MAT Detect";
    }

    /**
     * Get the URL name, returning a default string <b>detect-profile</b> .
     * @return the URL name
     */
    public String getUrlName() {
        return "detect-profile";
    }

    @Override
    public Collection<Callable<List<AddProfile>>> getProcesses() {
        Collection<Callable<List<AddProfile>>> processes = new ArrayList<>();
        processes.add(new GetPMAProfileList());
        return processes;
    }

    /**
     * The PMA profile configuration.
     */
    private CreatePMAProfile.Config config;

    /**
     * Get the PMA profile configuration.
     *
     * @return the PMA profile configuration
     */
    public CreatePMAProfile.Config getConfig() {
        if (config == null) {
            config = new CreatePMAProfile.Config(null);
        }
        return config;
    }

    /**
     * Set the config.
     *
     * @param config the config to set
     */

    public void setConfig(CreatePMAProfile.Config config) {
        this.config = config;
    }

    /**
     * Inner class representing the configuration.
     */
    public static final class Config extends AbstractDescribableImpl<CreatePMAProfile.Config> {

        /**
         * List of configuration entries.
         */
        private List<CreatePMAProfile.AddProfile> entries;

        /**
         * Constructor passing the configuration entries.
         *
         * @param entries the configuration entries.
         */
        @DataBoundConstructor
        public Config(List<CreatePMAProfile.AddProfile> entries) {
            this.entries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
        }

        /**
         * Get the entries.
         *
         * @return the entries
         */
        public List<CreatePMAProfile.AddProfile> getEntries() {
            return entries;
        }

        /**
         * Inner class representing the descriptor.
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<CreatePMAProfile.Config> {
        }

    }

    @Override
    public synchronized ProgressiveRendering entries() {
        return new CreatePMAProfile.PMAProgressViewRendering();
    }

    /**
     * Inner class representing progress rendering.
     */
    public class PMAProgressViewRendering extends ProgressViewRendering {
        @Override
        protected void compute() {
            super.compute();
        }
    }

    /**
     * Inner class to add a profile.
     */
    public static final class AddProfile extends CreateProfile.AddProfile
            implements Describable<CreatePMAProfile.AddProfile> {

        /**
         * The account number, bound to job_acct.
         */
        @ProfileMapping("job_acct")
        private String accountNumber;
        /**
         * The job class, bound to job_class.
         */
        @ProfileMapping("job_class")
        private String jobClass;
        /**
         * The job load, bound to job_load.
         */
        @ProfileMapping("job_load")
        private String loadlib;
        /**
         * The job PMA high level qualifier, bound to job_pmahlq.
         */
        @ProfileMapping("job_pmahlq")
        private String hlq;
        /**
         * The message class, with default <b>A</b>, bound to job_mclass.
         */
        @Default("A")
        @ProfileMapping("job_mclass")
        private String msgClass;

        /**
         * Constructor.
         *
         * @param profileName   the profile
         * @param accountNumber the job accounting information
         * @param jobClass      the job class
         * @param msgClass      the message class
         * @param loadlib       the load library
         * @param hlq           the high level qualifier
         * @param defaultp      whether this is the default profile
         */
        @DataBoundConstructor
        public AddProfile(String profileName, String accountNumber, String jobClass, String msgClass,
                          String loadlib, String hlq, boolean defaultp) {
            super(profileName, defaultp);
            this.accountNumber = accountNumber;
            this.jobClass = jobClass;
            this.msgClass = msgClass;
            this.loadlib = loadlib;
            this.hlq = hlq;
        }

        /**
         * Get the message class.
         *
         * @return the message class
         */
        public String getMsgClass() {
            return msgClass;
        }

        /**
         * Get the load library.
         *
         * @return the load library
         */
        public String getLoadlib() {
            return loadlib;
        }

        /**
         * Get the job class.
         *
         * @return the job class
         */
        public String getJobClass() {
            return jobClass;
        }

        /**
         * Get the account number.
         *
         * @return the account number
         */
        public String getAccountNumber() {
            return accountNumber;
        }

        /**
         * Get the high level qualifier.
         *
         * @return the high level qualifier
         */

        public String getHlq() {
            return hlq;
        }

        /**
         * Constructor.
         *
         * @param profileName the profile
         * @param defaulp     whether this is the default profile
         */
        public AddProfile(String profileName, boolean defaulp) {
            super(profileName, defaulp);
        }

        /**
         * Get the Jenkins descriptor.
         *
         * @return the descriptor
         */
        @SuppressWarnings("unchecked")
        public Descriptor<CreatePMAProfile.AddProfile> getDescriptor() {
            return Jenkins.get().getDescriptorOrDie(this.getClass());
        }

        /**
         * Inner class representing the descriptor.
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<CreatePMAProfile.AddProfile> {
            @Override
            public String getDisplayName() {
                return "CA MAT Detect Profile";
            }
        }

    }
}
