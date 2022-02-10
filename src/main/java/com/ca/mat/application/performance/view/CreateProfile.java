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
import com.ca.mat.application.performance.model.EntryAction;
import com.ca.mat.application.performance.model.UpdateAction;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import javax.servlet.ServletException;

/**
 * This abstract class processes and handles the generic zowe profiles
 * actions for each subclass (create, delete, and process).
 *
 * @param <T> - the generic type representing the entry (AddProfile for each subclass))
 */
public abstract class CreateProfile<T extends CreateProfile.AddProfile> extends MultipleEntryFields<T> {
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateProfile.class);

    @Override
    public List<SourceFile> getSourceFiles() {
        return super.getSourceFiles();
    }

    @Override
    @POST
    public synchronized HttpResponse doConfigSubmit(StaplerRequest req) throws ServletException, IOException {
        downloadDependencies();
        return super.doConfigSubmit(req);
    }

    @Override
    protected boolean invalidEntries() {
        boolean invalid = false;
        setDefaultValues();
        long defaultProfiles = getEntries().stream().filter(entry -> ((entry).getDefaultp())).count();
        //Sets the first profile as default if no entry has a default profile.
        if (defaultProfiles == 0 && getEntries().size() > 0) {
            getEntries().get(0).setDefaultp();
        }
        //Evaluates duplicate profiles
        HashSet<String> profileNames = new HashSet<>();
        for (T entryAction : getEntries()) {
            if (entryAction.getProfileName().isEmpty()) {
                errorMessages.add("You cannot have profiles with empty names");
                invalid = true;
                break;
            } else if (!profileNames.add(entryAction.getProfileName())) {
                errorMessages.add("You cannot have two profiles with the same name. Profile name: \"" +
                        entryAction.getProfileName() + "\"");
                invalid = true;
                break;
            }
        }
        if (defaultProfiles > 1) {
            invalid = true;
            errorMessages.add("You can only have one profile as default.");
        }
        return invalid;
    }

    @Override
    public String handleSingleDelete(EntryAction<T> entryAction) {
        T entry = entryAction.getEntry();
        String deleteCommand = String.format("zowe profiles delete %s %s", getPluginCmd(), (entry).getProfileName());
        return deleteCommand;
    }


    @Override
    public String handleSingleUpdate(UpdateAction<T> entryAction) {
        T entry = entryAction.getEntry();
        StringBuilder cli = new StringBuilder(String.format("zowe profiles update %s %s",
                getPluginCmd(), entry.getProfileName()));
        for (String field :
                entryAction.getFields()) {
            try {
                String fieldMapping;
                Field declaredField = entry.getClass().getDeclaredField(field);
                if (!declaredField.isAccessible()) {
                    declaredField.setAccessible(true);
                }
                if (declaredField.isAnnotationPresent(ProfileMapping.class)) {
                    fieldMapping = declaredField.getAnnotation(ProfileMapping.class).value();
                } else {
                    fieldMapping = field;
                }
                Object value = declaredField.get(entry);
                cli.append(String.format(" --%s \"%s\"", fieldMapping, value.toString()));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.info("An error occurred while issuing the update command", e);
            }
        }
        return cli.toString();
    }


    @Override
    public String handleSingleNewEntry(EntryAction<T> entryAction) {
        T entry = entryAction.getEntry();
        String command = "zowe profiles create %s %s --ow";
        String format = String.format(command, getPluginCmd(), entry.getProfileName());
        StringBuilder cli = new StringBuilder(format);
        Field[] declaredFIelds = entry.getClass().getDeclaredFields();
        for (Field field :
                declaredFIelds) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                String content = field.get(entry).toString();
                if (content != null && !content.isEmpty()) {
                    format = String.format(" --%s \"%s\"", field.isAnnotationPresent(ProfileMapping.class) ?
                            field.getAnnotation(ProfileMapping.class).value() : field.getName(), content);
                    cli.append(format);
                }
            } catch (IllegalAccessException e) {
                LOGGER.info("An error occurred while handling a new single entry", e);
            }
        }
        return cli.toString();
    }

    private void setDefaultValues() {
        for (T entry :
                getEntries()) {
            Field[] declaredFields = entry.getClass().getDeclaredFields();
            for (Field field :
                    declaredFields) {
                try {
                    if (field.isAnnotationPresent(Default.class)) {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        Object fieldValue = field.get(entry);
                        if (fieldValue == null || fieldValue.toString().isEmpty()) {
                            String value = field.getAnnotation(Default.class).value();
                            try {
                                field.set(entry, value);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(
                                        String.format("Could not set field %s to default value of %s",
                                                field.getName(), value), e);
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(String.format("Could not get value for field %s", field.getName()), e);
                }
            }
        }
    }

    @Override
    public String getMainFieldLabel(T entry) {
        return "profile name";
    }

    @Override
    public String getMainField(T entry) {
        return (entry).getProfileName();
    }

    @Override
    public String getFinalUpdatedMessage(boolean isUpdated, String result, T entry) {
        String finalUpdatedMessage = String.format("Profile %s was %s successfully",
                (entry).getProfileName(), isUpdated ? "updated" : "created");
        return finalUpdatedMessage;
    }

    @Override
    public boolean isSuccessful(String result) {
        return result.contains("successfully");
    }

    @Override
    public String getXMLFile() {
        return getPluginCmd() + "-profile.xml";
    }

    /**
     * Return the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return String.format("Define the %s Profile", getPluginDisplayName());
    }

    @Override
    protected void postProcessEntries() {
        for (EntryAction<T> entryAction :
                getModifiedEntries()) {
            if (entryAction.getType() != EntryAction.ActionType.delete) {
                T entry = entryAction.getEntry();
                if (entry.getDefaultp()) {
                    String name = (entry).getProfileName();
                    String cliCommand = String.format("zowe profiles set-default %s \"%s\"", getPluginCmd(), name);
                    try {
                        zoweCmd.getCommandOutputNoTimeout(cliCommand);
                    } catch (Exception e) {
                        throw new RuntimeException("An error occurred while setting the profile "
                                + entry.getProfileName() + " to default", e);
                    }
                }
            }
        }
    }

    /**
     * This method downloads and installs the latest version of the zowe cli, if not installed in the computer.
     */
    protected boolean downloadZoweCLI() {
        String cli = "zowe";
        LOGGER.info("Checking installation of zowe");
        String response = zoweCmd.getCommandOutputNoTimeout(cli).toLowerCase();
        if (response.contains("error") || response.contains("not recognized")) {
            LOGGER.info("Not found... Trying to install zowe");
            cli = "npm install -g @zowe/cli@zowe-" + getZoweVersionLTS();
            response = zoweCmd.getCommandOutputNoTimeout(cli);
            if (downloadZoweCLI()) {
                LOGGER.info("Zowe CLI " + getZoweVersionLTS() + " installed sucessfully.");
                return true;
            } else {
                LOGGER.info("Could not install zowe cli plugin.");
                LOGGER.info(response);
                return false;
            }
        } else {
            LOGGER.info("Zowe is installed correctly.");
            return true;
        }
    }

    private String getZoweVersionLTS() {
        String zoweVersion = PluginConfiguration.get().getZowe();
        if (zoweVersion == null) {
            //Default Zowe Version.
            zoweVersion = "v2-lts";
        }
        return zoweVersion;
    }

    /**
     * This method downloads and installs the latest version of the zowe ***-for-zowe-cli plugin,
     * if not installed in the computer.
     * *** being the getPluginCmd(). E.g.; endevor, mat, pma...
     *
     */
    protected void downloadDependencies() {
        downloadZoweCLI();
        LOGGER.info("Verifying zowe " + getPluginCmd() + " plugin");
        String cli = "zowe " + getPluginCmd();
        String response = zoweCmd.getCommandOutputNoTimeout(cli).toLowerCase();
        if (response.contains("error")) {
            LOGGER.info("Not found, trying to install zowe " + getPluginCmd() + " plugin");
            cli = "zowe plugins install " + getPluginName() + "@zowe-" + getZoweVersionLTS();
            response = zoweCmd.getCommandOutputNoTimeout(cli);
            if (response.contains("error")) {
                LOGGER.info("Could not install " + getPluginName());
                LOGGER.info(response);
            } else {
                LOGGER.info("Zowe " + getPluginCmd() + " plugin installed");
            }
        }
    }

    /**
     * This abstract class defines the generic fields for every zowe profile.
     */
    public abstract static class AddProfile {
        /**
         * The profile name.
         */
        private final String profileName;
        /**
         * Whether this is to be the default profile.
         */
        private Boolean defaultp;

        /**
         * Inner class to add a profile.
         *
         * @param profileName the profile to add
         * @param defaultp    whether this is the default profile
         */
        public AddProfile(String profileName, Boolean defaultp) {
            this.profileName = profileName;
            this.defaultp = defaultp;
        }

        /**
         * Return the profile name.
         *
         * @return the profile name.
         */
        public String getProfileName() {
            return profileName;
        }

        /**
         * Get whether this is the default profile.
         *
         * @return true or false
         */
        public Boolean getDefaultp() {
            return defaultp;
        }

        /**
         * Set this as the default profile.
         */
        public void setDefaultp() {
            this.defaultp = true;
        }
    }

    /**
     * Returns the plugin name.
     *
     * @return E.g.: endevor-for-zowe-cli, mat-analyze-for-zowe-cli
     */
    protected abstract String getPluginName();

    /**
     * Returns the plugin zowe command.
     *
     * @return e.g.: endevor, zosmf, mat, pma...
     */
    protected abstract String getPluginCmd();

    /**
     * Returns the plugin display name.
     *
     * @return e.g.: CA Endevor, CA MAT...
     */
    protected abstract String getPluginDisplayName();
}
