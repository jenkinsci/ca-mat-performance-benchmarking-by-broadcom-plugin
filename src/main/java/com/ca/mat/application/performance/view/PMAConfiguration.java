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

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * PMA Global configuration, containing the SMTP parameters.
 *
 * @author Arthur Pessoa
 */
@Extension
public class PMAConfiguration extends GlobalConfiguration {

    /**
     * Get the singleton instance.
     *
     * @return the singleton instance.
     */
    public static PMAConfiguration get() {
        return ExtensionList.lookupSingleton(PMAConfiguration.class);
    }

    /**
     * The email address.
     */
    private String email;
    /**
     * The SMTP server.
     */
    private String smtp;
    /**
     * The port.
     */
    private String port;
    /**
     * The user and password bean.
     */
    private Authentication authentication;

    /**
     * Default constructor.
     */
    public PMAConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }


    /**
     * Get the SMTP server.
     *
     * @return the SMTP server
     */
    public String getSmtp() {
        return smtp;
    }


    /**
     * Get the authentication.
     *
     * @return the authentication
     */
    public Authentication getAuthentication() {
        return authentication;
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
     * Get the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }


    /**
     * Together with {@link #getSmtp}, binds to entry in {@code config.jelly}.
     *
     * @param smtp the new value of this field
     */
    @DataBoundSetter
    public void setSmtp(String smtp) {
        this.smtp = smtp;
        save();
    }

    /**
     * Together with {@link #getSmtp}, binds to entry in {@code config.jelly}.
     *
     * @param email the new value of this field
     */
    @DataBoundSetter
    public void setEmail(String email) {
        this.email = email;
        save();
    }


    /**
     * Together with {@link #getSmtp}, binds to entry in {@code config.jelly}.
     *
     * @param port the new value of this field
     */
    @DataBoundSetter
    public void setPort(String port) {
        this.port = port;
        save();
    }


    /**
     * Together with {@link #getSmtp}, binds to entry in {@code config.jelly}.
     *
     * @param auth the new value of this field
     */
    @DataBoundSetter
    public void setAuthentication(Authentication auth) {
        this.authentication = auth;
        save();
    }

    /**
     * Check whether an email has been passed.
     *
     * @param value an email address
     * @return OK if an address passed, warning if not
     */
    public FormValidation doCheckLabel(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a e-mail.");
        }
        return FormValidation.ok();
    }

    /**
     * This model class represents the authentication fields of the SMTP configuration.
     */
    public static class Authentication {
        /**
         * The user name.
         */
        private final String username;
        /**
         * The associated password.
         */
        private final String password;

        /**
         * Inner class constructor.
         *
         * @param username the user name
         * @param password the associated password
         */
        @DataBoundConstructor
        public Authentication(String username, String password) {
            this.username = username;
            this.password = password;
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
         * Get the password.
         *
         * @return the password
         */
        public String getPassword() {
            return password;
        }


    }

}
