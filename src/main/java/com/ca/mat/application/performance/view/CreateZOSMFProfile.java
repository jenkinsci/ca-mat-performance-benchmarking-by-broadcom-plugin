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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.ServletException;

import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import com.ca.mat.application.performance.control.annotation.Default;
import com.ca.mat.application.performance.control.annotation.ProfileMapping;
import com.ca.mat.application.performance.control.future.GetZOSMFProfileList;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import jenkins.util.ProgressiveRendering;

/**
 * Create a ZOSMF profile.
 */
@Extension
public final class CreateZOSMFProfile extends CreateProfile<CreateZOSMFProfile.AddProfile> {

    @Override
    public String getDescription() {
        return "The z/OSMF profile enables the communication of the plugin with your instance " +
                "of the IBM z/OS Management Facility.";
    }

    @Override
    public List<CreateZOSMFProfile.AddProfile> getEntries() {
        return getConfig().getEntries();
    }

    @Override
    public List<AddProfile> setEntries(List<AddProfile> entries) {
        return config.entries = entries;
    }

    @Override
    protected void setConfig(MultipleEntryFields<AddProfile> read) {
        config = ((CreateZOSMFProfile) read).getConfig();
    }

    @Override
    public synchronized ProgressiveRendering entries() {
        return new ZOSMFProgressViewRendering();
    }

    /**
     * Inner class representing progress rendering.
     */
    public class ZOSMFProgressViewRendering extends ProgressViewRendering {
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
        return "@brightside/core";
    }

    /**
     * The zowe command group: e.g. zowe zosmf.
     *
     * @return the zowe command group
     */
    protected String getPluginCmd() {
        return "zosmf";
    }

    @Override
    protected String getPluginDisplayName() {
        return "z/OSMF";
    }

    /**
     * Get the URL name.
     *
     * @return static string <b>zosmf-profile</b> .
     */
    public String getUrlName() {
        return "zosmf-profile";
    }

    @Override
    public Collection<Callable<List<AddProfile>>> getProcesses() {
        Collection<Callable<List<AddProfile>>> processes = new ArrayList<>();
        processes.add(new GetZOSMFProfileList());
        return processes;
    }

    @Override
    protected void downloadDependencies() {
        downloadZoweCLI();
    }

    /**
     * The ZOSMF profile configuration.
     */
    private CreateZOSMFProfile.Config config;

    /**
     * Get the ZOSMF profile configuration.
     *
     * @return the ZOSMF profile configuration
     */
    public CreateZOSMFProfile.Config getConfig() {
        if (config == null) {
            config = new CreateZOSMFProfile.Config(null);
        }
        return config;
    }

    /**
     * Set the config.
     *
     * @param config the config to set
     */
    public void setConfig(CreateZOSMFProfile.Config config) {
        this.config = config;
    }

    /**
     * Inner class representing the configuration.
     */
    public static final class Config extends AbstractDescribableImpl<CreateZOSMFProfile.Config> {

        /**
         * List of configuration entries.
         */
        private List<CreateZOSMFProfile.AddProfile> entries;

        /**
         * Constructor passing profile entries.
         *
         * @param entries the entries
         */
        @DataBoundConstructor
        public Config(List<CreateZOSMFProfile.AddProfile> entries) {
            this.entries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
        }

        /**
         * Get the configuration entries.
         *
         * @return the configuration entries
         */
        public List<CreateZOSMFProfile.AddProfile> getEntries() {
            return entries;
        }

        /**
         * Inner class representing the descriptor.
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<CreateZOSMFProfile.Config> {
        }

    }

    /**
     * Inner class to add a profile.
     */
    public static final class AddProfile extends CreateProfile.AddProfile
            implements Describable<CreateZOSMFProfile.AddProfile> {

        /**
         * The host name.
         */
        @ProfileMapping("host")
        private String hostname;
        /**
         * The port, default 443.
         */
        @Default("443")
        private String port;
        /**
         * The user name.
         */
        @ProfileMapping("user")
        private String username;
        /**
         * The password.
         */
        private Secret password;
        /**
         * The base path.
         */
        @ProfileMapping("base-path")
        private String basePath;
        /**
         * The encoding.
         */
        @ProfileMapping("encoding")
        @Default("1047")
        private String encoding;
        /**
         * The response timeout.
         */
        private String responseTimeout;
        /**
         * Whether to reject unauthorised requests.
         */
        @ProfileMapping("reject-unauthorized")
        private boolean rejectUnauthorized;

        /**
         * Constructor.
         *
         * @param profileName        the profile name
         * @param hostname           the host name
         * @param port               the port name
         * @param username           the user name
         * @param password           the password
         * @param basePath           the base path
         * @param encoding           the encoding
         * @param responseTimeout    the response timeout
         * @param rejectUnauthorized whether to reject unauthorised requests
         * @param defaultp           whether this is the default profile
         */
        @DataBoundConstructor
        public AddProfile(String profileName, String hostname, String port, String username, Secret password,
                          String basePath, String encoding, String responseTimeout, boolean rejectUnauthorized,
                          boolean defaultp) {
            super(profileName, defaultp);
            this.hostname = hostname;
            this.port = port;
            this.username = username;
            this.password = password;
            this.basePath = basePath;
            this.encoding = encoding;
            this.responseTimeout = responseTimeout;
            this.rejectUnauthorized = rejectUnauthorized;
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
         * Get whether to reject unauthorised requests.
         *
         * @return true or false
         */
        public boolean getRejectUnauthorized() {
            return rejectUnauthorized;
        }

        /**
         * Get the base path.
         *
         * @return the base path
         */

        public String getBasePath() {
            return basePath;
        }

        /**
         * Get the encoding.
         *
         * @return the encoding
         */

        public String getEncoding() {
            return encoding;
        }

        /**
         * Get the response timeout.
         *
         * @return the response timeout
         */

        public String getResponseTimeout() {
            return responseTimeout;
        }

        /**
         * Get the password.
         *
         * @return the password
         */
        public Secret getPassword() {
            return password;
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
         * Get the user name.
         *
         * @return the user name
         */
        public String getUsername() {
            return username;
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
         * Set the port.
         *
         * @param port the port to set
         */
        public void setPort(String port) {
            this.port = port;
        }

        /**
         * Set the encoding.
         *
         * @param encoding the encoding to set
         */
        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        @Override
        public String getProfileName() {
            return super.getProfileName();
        }

        /**
         * Get the Jenkins descriptor.
         *
         * @return the descriptor
         */
        @SuppressWarnings("unchecked")
        public Descriptor<CreateZOSMFProfile.AddProfile> getDescriptor() {
            return Jenkins.get().getDescriptorOrDie(this.getClass());
        }

        /**
         * Inner class representing the descriptor.
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<CreateZOSMFProfile.AddProfile> {
            @Override
            public String getDisplayName() {
                return "z/OSMF Profile";
            }
        }

    }
}
