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

import com.ca.mat.application.performance.control.future.GetMATProfileList;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.util.ProgressiveRendering;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Create a MAT Profile.
 */
@Extension
public final class CreateMATProfile extends CreateProfile<CreateMATProfile.AddProfile> {

    @Override
    public String getDescription() {
        return "The CA MAT Analyze profile enables the communication of the plugin with " +
                "CA Mainframe Application Tuner (CA MAT) that ensures the analysis of the detected performance issue.";
    }

    @Override
    public List<AddProfile> getEntries() {
        return getConfig().getEntries();
    }

    @Override
    protected void setConfig(MultipleEntryFields<AddProfile> read) {
        config = ((CreateMATProfile) read).getConfig();
    }

    @Override
    public synchronized ProgressiveRendering entries() {
        return new MATProgressViewRendering();
    }

    /**
     * Inner class for progress view rendering.
     */
    public class MATProgressViewRendering extends ProgressViewRendering {
        @Override
        protected void compute() {
            super.compute();
        }
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
        return "@broadcom/mat-analyze-for-zowe-cli";
    }

    /**
     * The zowe command group: e.g. zowe mat
     *
     * @return the zowe command group
     */
    protected String getPluginCmd() {
        return "mat";
    }

    @Override
    protected String getPluginDisplayName() {
        return "CA MAT Analyze";
    }

    /**
     * Get the URL name.
     *
     * @return static string <b>analyze-profile</b> .
     */
    public String getUrlName() {
        return "analyze-profile";
    }

    @Override
    public Collection<Callable<List<AddProfile>>> getProcesses() {
        Collection<Callable<List<AddProfile>>> processes = new ArrayList<>();
        processes.add(new GetMATProfileList());
        return processes;
    }

    @Override
    public List<AddProfile> setEntries(List<AddProfile> entries) {
        return config.entries = entries;
    }


    /**
     * The configuration.
     */
    private Config config;

    /**
     * Get the configuration.
     *
     * @return the configuration
     */
    public Config getConfig() {
        if (config == null) {
            config = new Config(null);
        }
        return config;
    }

    /**
     * Set the configuration.
     *
     * @param config the configuration to set
     */
    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * Inner class representing the configuration.
     */
    public static final class Config extends AbstractDescribableImpl<Config> {

        /**
         * List of configuration entries.
         */
        private List<AddProfile> entries;

        /**
         * Contructor.
         *
         * @param entries list of profile entries.
         */

        @DataBoundConstructor
        public Config(List<AddProfile> entries) {
            this.entries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
        }

        /**
         * Get the configuration entries.
         *
         * @return the configuration entries
         */
        public List<AddProfile> getEntries() {
            return entries;
        }

        /**
         * Inner class representing the descriptor.
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<Config> {
        }

    }

    /**
     * Inner class to add a profile.
     */
    public static final class AddProfile extends CreateProfile.AddProfile implements Describable<AddProfile> {

        /**
         * The port.
         */
        private String port;
        /**
         * The user name.
         */
        private String username;
        /**
         * The associated password.
         */
        private Secret password;
        /**
         * Whether this profile is Zowe discoverable.
         */
        private boolean zowediscoverable;
        /**
         * The protocol, http or https.
         */
        private String protocol;
        /**
         * The host name.
         */
        private String hostname;

        /**
         * Constructor.
         *
         * @param profileName      the profile name
         * @param hostname         the host name
         * @param protocol         http or https
         * @param port             the port
         * @param username         the user name
         * @param password         the password
         * @param zowediscoverable whether this profile is Zowe discoverable
         * @param defaultp         whether this is the default profile
         */
        @DataBoundConstructor
        public AddProfile(String profileName, String hostname, String protocol, String port,
                          String username, Secret password, boolean zowediscoverable, boolean defaultp) {
            super(profileName, defaultp);
            this.port = port;
            this.hostname = hostname;
            this.protocol = protocol;
            this.username = username;
            this.password = password;
            this.zowediscoverable = zowediscoverable;
        }

        /**
         * Constructor.
         *
         * @param profileName the profile name
         * @param defaulp     whether this is the default profile
         */
        public AddProfile(String profileName, boolean defaulp) {
            super(profileName, defaulp);
        }

        /**
         * Get the host name.
         *
         * @return the host name
         */
        public String getHostname() {
            return hostname;
        }

        /**
         * Get whether this profile is Zowe discoverable.
         *
         * @return true or false
         */
        public boolean getZowediscoverable() {
            return zowediscoverable;
        }

        /**
         * Get the protocol (http or https).
         *
         * @return the protocol
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * Get the port.
         *
         * @return the port
         */
        public String getPort() {
            return port;
        }

        /**
         * Get the password associated with the user.
         *
         * @return the password
         */
        public Secret getPassword() {
            return password;
        }

        /**
         * Get the user name.
         *
         * @return the user name
         */
        public String getUsername() {
            return username;
        }

        /**
         * Get the Jenkins descriptor.
         *
         * @return the descriptor.
         */
        @SuppressWarnings("unchecked")
        public Descriptor<AddProfile> getDescriptor() {
            return Jenkins.get().getDescriptorOrDie(this.getClass());
        }


        /**
         * Inner class representing the descriptor.
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<AddProfile> {
            @Override
            public String getDisplayName() {
                return "CA MAT Analyze Profile";

            }

            /**
             * Create a ListBoxModel object containing <b>http</b> and <b>https</b> .
             *
             * @return the ListBoxModel object
             */
            public ListBoxModel doFillProtocolItems() {
                ListBoxModel items = new ListBoxModel();

                items.add("http", "http");
                items.add("https", "https");

                return items;
            }

        }
    }

}
