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
package com.ca.mat.application.performance.control.future;

import com.ca.mat.application.performance.control.annotation.ProfileMapping;
import com.ca.mat.application.performance.control.build.ZoweCommandLineBuilder;
import com.ca.mat.application.performance.view.CreateProfile;
import hudson.util.Secret;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * The concurrent abstract worker class that retrieves the zowe profiles.
 *
 * @param <T> The Generic type (AddProfile for each subclass)
 * @author Arthur Pessoa
 */
public abstract class GetProfileList<T extends CreateProfile.AddProfile> implements Callable<List<T>> {
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetProfileList.class);

    /**
     * The zowe plugin command.
     *
     * @return plugin command, e.g.: endevor, mat, pma.
     */
    protected abstract String getPluginCmd();

    @Override
    public List<T> call() {
        LOGGER.info(String.format("Getting list of %s profiles...", getPluginCmd()));
        List<T> entries = new ArrayList<>();
        String[] parameters = new String[]{"profiles", "list", getPluginCmd(), "--rfj"};
        String profiles = new ZoweCommandLineBuilder().getCommandOutputNoTimeout(parameters);
        try {
            JSONObject response = JSONObject.fromObject(profiles);
            if (response.has("success")) {
                Object success = response.get("success");
                if (success instanceof Boolean && Boolean.parseBoolean(success.toString())) {
                    if (response.has("data")) {
                        JSONArray listProfiles = response.getJSONArray("data");
                        for (Object profile : listProfiles) {
                            if (profile instanceof JSONObject) {
                                JSONObject profileObj = ((JSONObject) profile);
                                String profileName = profileObj.getString("name");
                                boolean defaultProfile = false;
                                if (profileName.contains("(default)")) {
                                    profileName = profileName.replace("(default)", "").trim();
                                    defaultProfile = true;
                                }
                                if (profileObj.containsKey("profile")) {
                                    JSONObject profilesObj = (JSONObject) profileObj.get("profile");
                                    Type[] types = ((ParameterizedType) getClass()
                                            .getGenericSuperclass()).getActualTypeArguments();
                                    for (Type type : types) {
                                        Class<?> classType = Class.forName(type.getTypeName());
                                        Constructor<?> constructor = classType.getConstructor(String.class,
                                                boolean.class);
                                        @SuppressWarnings("unchecked")
                                        T instance = (T) constructor.newInstance(profileName,
                                                defaultProfile);
                                        for (Field field : classType.getDeclaredFields()) {
                                            if (!field.isAccessible()) {
                                                field.setAccessible(true);
                                            }
                                            String fieldName = field.getName();
                                            if (!profilesObj.has(fieldName)) {
                                                if (field.isAnnotationPresent(ProfileMapping.class)) {
                                                    fieldName = field.getAnnotation(ProfileMapping.class).value();
                                                }
                                            }
                                            Object content = profilesObj.has(fieldName) ?
                                                    profilesObj.get(fieldName) : "";
                                            switch (field.getType().getTypeName()) {
                                                case "long":
                                                    content = Long.parseLong(content.toString());
                                                    break;
                                                case "double":
                                                    content = Double.parseDouble(content.toString());
                                                    break;
                                                case "int":
                                                    content = Integer.parseInt(content.toString());
                                                    break;
                                                case "boolean":
                                                    content = Boolean.parseBoolean(content.toString());
                                                    break;
                                                case "hudson.util.Secret":
                                                    content = Secret.fromString(content.toString());
                                                    break;
                                                default:
                                                    content = content.toString();
                                                    break;
                                            }
                                            field.set(instance, content);
                                        }
                                        entries.add(instance);
                                    }
                                }
                            }
                        }
                    } else {
                        LOGGER.info("No data reported on JSON");
                    }
                } else {
                    if (response.has("message")) {
                        String errorMsg = response.get("message").toString();
                        if (errorMsg.contains("Unknown argument: " + getPluginCmd())) {
                            LOGGER.info("The " + getPluginCmd() + " is not installed, no profiles will be listed.");
                            return entries;
                        }
                    }
                }
            }
            LOGGER.info(String.format("List of %s profiles: %d", getPluginCmd(), entries.size()));
        } catch (JSONException e) {
            LOGGER.info(String.format("An error occurred while trying to parse a output JSON \"%s\"", profiles));
        } catch (Exception e) {
            LOGGER.info("An error occurred while retrieving the profiles list", e);
        }
        return entries;
    }
}
