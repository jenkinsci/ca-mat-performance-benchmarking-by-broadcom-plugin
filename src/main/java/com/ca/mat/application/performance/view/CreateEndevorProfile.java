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
import com.ca.mat.application.performance.control.future.GetEndevorProfileList;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.util.ProgressiveRendering;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This class represents the create endevor entry on the config UI. It implements, when applicable,
 * the necessary methods to process zowe profiles endevor actions.
 *
 * @author Arthur Pessoa
 */

@Extension
public final class CreateEndevorProfile extends CreateProfile<CreateEndevorProfile.AddProfile> {

    @Override
    public String getDescription() {
        return "The CA Endevor profile enables the communication of the plugin with CA Endevor® " +
                "and enables you to remotely interact with your source code.";
    }

    @Override
    public List<CreateEndevorProfile.AddProfile> getEntries() {
        return getConfig().getEntries();
    }

    @Override
    protected void setConfig(MultipleEntryFields<AddProfile> read) {
        config = ((CreateEndevorProfile) read).getConfig();
    }

    @Override
    public List<AddProfile> setEntries(List<AddProfile> entries) {
        return config.entries = entries;
    }

    @Override
    public synchronized ProgressiveRendering entries() {
        return new EndevorProgressViewRendering();
    }

    /**
     * This class represents the progress bar handler, and binds the UI with the controller.
     */
    public class EndevorProgressViewRendering extends ProgressViewRendering {
        @Override
        protected void compute() {
            super.compute();
        }
    }

    /**
     * This inner class represents the descriptor.
     */
    @Extension
    public static final class DescriptorImpl extends PerformanceBenchmarkingDescriptor {
    }

    /**
     * The official published plugin name on NPM Registry.
     * @return the plugin name
     */
    protected String getPluginName() {
        return "@broadcom/endevor-for-zowe-cli";
    }


    /**
     * The zowe command group. E.g.: zowe endevor
     * @return the zowe command group
     */
    protected String getPluginCmd() {
        return "endevor";
    }

    @Override
    protected String getPluginDisplayName() {
        return "CA Endevor";
    }

    /**
     * Get the URL name.
     *
     * @return static string <b>endevor-profile</b>
     */
    public String getUrlName() {
        return "endevor-profile";
    }

    @Override
    public Collection<Callable<List<AddProfile>>> getProcesses() {
        Collection<Callable<List<AddProfile>>> processes = new ArrayList<>();
        processes.add(new GetEndevorProfileList());
        return processes;
    }

    /**
     * The config.
     */
    private CreateEndevorProfile.Config config;

    /**
     * Get the configuration.
     *
     * @return the configuration
     */
    public CreateEndevorProfile.Config getConfig() {
        if (config == null) {
            config = new CreateEndevorProfile.Config(null);
        }
        return config;
    }

    /**
     * Set the configuration.
     *
     * @param config the configuration to set
     */
    public void setConfig(CreateEndevorProfile.Config config) {
        this.config = config;
    }

    /**
     * This inner class represents the configuration of the CA Endevor entries.
     */
    public static final class Config extends AbstractDescribableImpl<CreateEndevorProfile.Config> {

        /**
         * List of configuration entries.
         */
        private List<CreateEndevorProfile.AddProfile> entries;

        /**
         * Inner class constructor passing profile entries.
         *
         * @param entries the profile entries
         */
        @DataBoundConstructor
        public Config(List<CreateEndevorProfile.AddProfile> entries) {
            this.entries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
        }

        /**
         * Get the configuration entries.
         *
         * @return the configuration entries
         */
        public List<CreateEndevorProfile.AddProfile> getEntries() {
            return entries;
        }

        /**
         * Get the configuration entries.
         *
         * @return the configuration entries
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<CreateEndevorProfile.Config> {
        }

    }

    /**
     * This class represents the model that holds and maps the Endevor profile fields.
     */
    public static final class AddProfile extends CreateProfile.AddProfile
            implements Describable<CreateEndevorProfile.AddProfile> {
        /**
         * Set the default as http.
         */
        @Default("http")
        @ProfileMapping("prot")
        String protocol;
        /**
         * The host.
         */
        @ProfileMapping("host")
        private String hostname;
        /**
         * The port.
         */
        private String port;
        /**
         * The user.
         */
        @ProfileMapping("user")
        private String username;
        /**
         * The password associated with the user.
         */
        private Secret password;
        /**
         * The base path.
         */
        @ProfileMapping("base-path")
        @Default("EndevorService/rest")
        private String basePath;
        /**
         * Whether to reject unauthorised requests.
         */
        @ProfileMapping("reject-unauthorized")
        private boolean rejectUnauthorized;

        /**
         * Inner class constructor.
         *
         * @param profileName        the profile name
         * @param hostname           the host name
         * @param port               the port
         * @param username           the user name
         * @param password           the password
         * @param protocol           http or https
         * @param basePath           the base path
         * @param rejectUnauthorized whether to reject unauthorised requests
         * @param defaultp           whether this is the default profile
         */
        @DataBoundConstructor
        public AddProfile(String profileName, String hostname, String port, String username, Secret password,
                          String protocol, String basePath, boolean rejectUnauthorized, boolean defaultp) {
            super(profileName, defaultp);
            this.hostname = hostname;
            this.port = port;
            this.username = username;
            this.password = password;
            this.basePath = basePath;
            this.protocol = protocol;
            this.rejectUnauthorized = rejectUnauthorized;
        }

        /**
         * Inner class constructor.
         *
         * @param profileName the profile name
         * @param defaulp     whether this is the default profile
         */

        public AddProfile(String profileName, boolean defaulp) {
            super(profileName, defaulp);
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
         * @return the name
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
         * Get the protocol.
         *
         * @return the protocol
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * Set the protocol.
         * @param protocol the protocol
         */
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        /**
         * Set the port.
         * @param port - the port
         */
        public void setPort(String port) {
            this.port = port;
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

        @Override
        public String getProfileName() {
            return super.getProfileName();
        }

        /**
         * Return the Jenkins descriptor.
         *
         * @return the descriptor
         */
        @SuppressWarnings("unchecked")
        public Descriptor<CreateEndevorProfile.AddProfile> getDescriptor() {
            return Jenkins.get().getDescriptorOrDie(this.getClass());
        }

        /**
         * Inner class providing a display name and the list of available protocols.
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<CreateEndevorProfile.AddProfile> {
            @Override
            public String getDisplayName() {
                return "CA Endevor Profile";
            }

            /**
             * Returns the possible endevor protocol options.
             *
             * @return HTTP or HTTPS
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
