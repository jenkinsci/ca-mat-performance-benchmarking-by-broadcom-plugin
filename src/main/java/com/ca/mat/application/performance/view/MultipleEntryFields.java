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
import com.ca.mat.application.performance.control.future.ListRetrievable;
import com.ca.mat.application.performance.model.EntryAction;
import com.ca.mat.application.performance.model.UpdateAction;
import hudson.XmlFile;
import hudson.util.FormApply;
import jenkins.model.Jenkins;
import jenkins.util.ProgressiveRendering;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Callable;

/**
 * This abstract class represents the superclass for the CreateProfile class.
 *
 * @param <T> - the generic type that represents the entry
 */
public abstract class MultipleEntryFields<T> extends PerformanceBenchmarking {

    /**
     * The command line builder.
     */
    protected ZoweCommandLineBuilder zoweCmd = new ZoweCommandLineBuilder();
    /**
     * A list of modified entries.
     */
    private List<EntryAction<T>> modifiedEntries = new ArrayList<>();
    /**
     * A list of error messages.
     */
    final List<String> errorMessages = new LinkedList<>();
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleEntryFields.class);

    /**
     * Get the configuration file.
     *
     * @return the configuration file.
     */
    public XmlFile getConfigFile() {
        return new XmlFile(new File(Jenkins.get().getRootDir(), getXMLFile()));
    }

    /**
     * Retrieves the list of modified entries.
     *
     * @return the list of entry actions.
     */
    public List<EntryAction<T>> getModifiedEntries() {
        return modifiedEntries;
    }

    /**
     * Returns the XMLFile name to be stored in the jenkins_home.
     *
     * @return the xml file name
     */
    public abstract String getXMLFile();

    /**
     * Sets the configuration after the object modification.
     *
     * @param read - the previous instance.
     */
    protected abstract void setConfig(MultipleEntryFields<T> read);

    /**
     * Post processes the entries after the action items.
     */
    protected abstract void postProcessEntries();

    /**
     * Handles whether the output of the entry action is successful.
     *
     * @param result - the result of the entry action, e.g.: the command output after saving the config file.
     * @return whether the result is successful or not
     */
    protected abstract boolean isSuccessful(String result);

    /**
     * Returns the final message from a update of new entry action.
     *
     * @param isUpdated  - whether an update action occurred.
     * @param actionText - action response, .e.g: the command output
     * @param entry      - the generic type that represents the entry
     * @return the final updated message
     */
    protected abstract String getFinalUpdatedMessage(boolean isUpdated, String actionText, T entry);

    /**
     * Handles a single new swipeable entry after saving the configuration.
     * e.g.: a command, a rest call, a file save.
     *
     * @param entry - the generic type that corresponds the entry.
     * @return the entry action or result
     */
    protected abstract String handleSingleNewEntry(EntryAction<T> entry);

    /**
     * Handles a single new update entry after saving the configuration.
     * e.g.: a command, a rest call, a file save.
     *
     * @param entry - the generic type that corresponds the entry.
     * @return the entry action or result
     */
    protected abstract String handleSingleUpdate(UpdateAction<T> entry);

    /**
     * Handles a single new delete entry after saving the configuration.
     *
     * @param entry - the generic type that corresponds the entry.
     * @return the entry action or result
     */
    protected abstract String handleSingleDelete(EntryAction<T> entry);

    /**
     * Refers to an unique label field that represents the entry, like a name or ID.
     *
     * @param entry - the generic type that corresponds the entry.
     * @return the entry action or result
     */
    protected abstract String getMainFieldLabel(T entry);


    /**
     * Refers to an unique field that represents the entry, e.g.: a name or ID.
     *
     * @param entry - the generic type that corresponds the entry.
     * @return the entry action or result
     */
    protected abstract String getMainField(T entry);

    /**
     * Evaluates whether the configuration has invalid entries.
     *
     * @return whether the configuration has an invalid entry
     */
    protected abstract boolean invalidEntries();

    /**
     * Returns the list of entries.
     *
     * @return List of saved entries
     */
    public abstract List<T> getEntries();

    /**
     * Set the list of entries.
     *
     * @param entries the list of entries
     * @return the list of entries.
     */
    public abstract List<T> setEntries(List<T> entries);

    /**
     * Get the collection of the processes to retrieve the configuration on the start-up.
     *
     * @return the future processes to be concurrently processed.
     */
    public abstract Collection<Callable<List<T>>> getProcesses();

    @Override
    public List<SourceFile> getSourceFiles() {
        return super.getSourceFiles();
    }

    /**
     * This method returns the class instance that will handle the progress bar handler.
     *
     * @return the instance that binds the UI with the control class.
     */
    public synchronized ProgressiveRendering entries() {
        return new ProgressViewRendering();
    }


    /**
     * This class represents the progressive rendering that displays the progress bar on the UI.
     */
    protected class ProgressViewRendering extends ProgressiveRendering {

        @Override
        protected void compute() {
            XmlFile cc = getConfigFile();
            @SuppressWarnings("unchecked")
            MultipleEntryFields<T> read = null;
            int totalEntries = 0;
            try {
                if (cc.exists())
                    read = (MultipleEntryFields<T>) cc.read();
                 totalEntries = modifiedEntries.size();
                if (totalEntries > 0) {
                    processEntries(modifiedEntries, totalEntries);
                    postProcessEntries();
                } else {
                    progress(0.5);
                }
            } catch (Exception err) {
                err.printStackTrace();
                errorMessages.add("An internal error occurred while processing an entry, " +
                        "please check the logs for more details");
                LOGGER.info("An error occured while processing the entries", err);
                return;
            } finally {
                if (read != null) {
                    modifiedEntries.clear();
                    read.modifiedEntries.clear();
                    try {
                        getConfigFile().write(read);
                    } catch (IOException e) {
                        LOGGER.info("Could not save configuration file.");
                    }
                    setConfig(read);
                }
            }
            int size = getEntries().size();
            Runnable runnable = new ListRetrievable<>(getConfigFile(),
                    MultipleEntryFields.this);
            runnable.run();
            if (getEntries().size() != size)
                errorMessages.add("Please, refresh the page to obtain the latest results.");
            else if (totalEntries == 0) {
                errorMessages.add("The current state of your configuration:");
            }
        }

        private int processEntries(List<EntryAction<T>> entries, int totalEntries) {
            int index = 0;
            for (int j = 0; j < entries.size(); j++) {
                if (j == 0) {
                    index += j;
                } else {
                    index++;
                }
                if (totalEntries > 1) {
                    if (index != totalEntries - 1) {
                        double progress = ((double) (index + 1) / (double) totalEntries);
                        progress(progress);
                    }
                } else {
                    progress(0.5);
                }
                processSingleEntry(entries.get(j));
            }
            return index;
        }

        @Override
        protected synchronized JSON data() {
            JSONArray r = new JSONArray();
            for (String i : errorMessages) {
                r.add(i);
            }
            errorMessages.clear();
            return new JSONObject().accumulate("errorMessages", r);
        }
    }

    /**
     * Processes the single entry action.
     *
     * @param entry the generic type representing the entry.
     */
    protected void processSingleEntry(EntryAction<T> entry) {
        String cli = "";
        if (entry.getType() == EntryAction.ActionType.delete) {
            cli = handleSingleDelete(entry);
        } else {
            String mainField = getMainField(entry.getEntry());
            if (mainField == null || mainField.isEmpty()) {
                String errorMessage = String.format("Missing %s", getMainFieldLabel(entry.getEntry()));
                errorMessages.add(errorMessage);
                return;
            }
            cli = entry.getType() == EntryAction.ActionType.update ?
                    handleSingleUpdate((UpdateAction<T>) entry) : handleSingleNewEntry(entry);
        }

        String result = zoweCmd.getCommandOutputNoTimeout(cli);
        if (entry.getType() == EntryAction.ActionType.delete) {
            int indexDelete = result.indexOf("IS DELETED");
            if (indexDelete > 0 && !(entry.getType() == EntryAction.ActionType.update)) {
                String entityName = getMainField(entry.getEntry());
                result = result.substring(0, indexDelete) + entityName + " " +
                        result.substring(indexDelete, result.length() - 1);
            }
            if (!(entry.getType() == EntryAction.ActionType.update)) {
                errorMessages.add(result);
            }
        } else {
            if (result.contains("IS NOT FOUND") || result.trim().equals("")) {
                return;
            } else if (isSuccessful(result)) {
                String finalMessage = getFinalUpdatedMessage(entry.getType() == EntryAction.ActionType.update,
                        result, entry.getEntry());
                errorMessages.add(finalMessage);
            } else {
                errorMessages.add(result);
            }
        }
    }

    /**
     * This method handles the post request after pressing the save button on the config UI.
     *
     * @param req - the POST request
     * @return The POST request HTTP response
     * @throws ServletException if an error occurs in the application server
     * @throws IOException      if an I/O error occurs
     */
    @POST
    public synchronized HttpResponse doConfigSubmit(StaplerRequest req) throws ServletException, IOException {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        JSONObject json = req.getSubmittedForm();
        if (json.get("config") instanceof JSONObject) {
            JSONObject config = (JSONObject) json.get("config");
            if (!config.has("entries")) {
                if (config.get("entries") instanceof JSONObject) {
                    JSONArray array = new JSONArray();
                    array.add(config.get("entries"));
                    config.put("entries", array);
                }
            }
        } else if (!json.has("config") || json.get("config") instanceof Boolean) {
            JSONObject config = new JSONObject();
            JSONArray entries = new JSONArray();
            if (json.has("entries")) {
                if (json.get("entries") instanceof JSONArray) {
                    entries = (JSONArray) json.get("entries");
                } else {
                    entries.add(json.get("entries"));
                }
            }
            config.put("entries", entries);
            json.put("config", config);
        }
        req.bindJSON(this, json);
        if (invalidEntries()) {
            return FormApply.success(".");
        }
        if (getConfigFile().exists()) {
            @SuppressWarnings("unchecked")
            MultipleEntryFields<T> saved = (MultipleEntryFields<T>) getConfigFile().read();
            List<EntryAction<T>> deleteEntries = getDifferentEntries(saved.getEntries(), this.getEntries(),
                    EntryAction.ActionType.delete);
            List<EntryAction<T>> newEntries = getDifferentEntries(this.getEntries(), saved.getEntries(),
                    EntryAction.ActionType.add);
            List<EntryAction<T>> updatedEntries = getUpdatedEntries(deleteEntries, newEntries);
            modifiedEntries.addAll(deleteEntries);
            modifiedEntries.addAll(newEntries);
            modifiedEntries.addAll(updatedEntries);
        }
        getConfigFile().write(this);
        return FormApply.success(".");
    }

    private List<EntryAction<T>> getUpdatedEntries(List<EntryAction<T>> deleteEntries,
                                                   List<EntryAction<T>> newEntries) {
        final List<EntryAction<T>> updatedEntries = new ArrayList<>();
        Iterator<EntryAction<T>> deleteIterator = deleteEntries.iterator();
        while (deleteIterator.hasNext()) {
            EntryAction<T> deletedEntry = deleteIterator.next();
            Iterator<EntryAction<T>> addIterator = newEntries.iterator();
            while (addIterator.hasNext()) {
                EntryAction<T> newEntry = addIterator.next();
                if (getMainField(deletedEntry.getEntry()).equals(getMainField(newEntry.getEntry()))) {
                    List<String> updatedFields = getUpdatedFields(deletedEntry.getEntry(), newEntry.getEntry());
                    updatedEntries.add(new UpdateAction<>(newEntry.getEntry(), deletedEntry.getEntry(), updatedFields));
                    addIterator.remove();
                    deleteIterator.remove();
                    break;
                }
            }
        }
        return updatedEntries;
    }

    private List<String> getUpdatedFields(T deletedEntry, T newEntry) {
        List<String> updatedFields = new ArrayList<>();
        Field[] fields = deletedEntry.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                if (!field.get(deletedEntry).equals(field.get(newEntry))) {
                    updatedFields.add(field.getName());
                }
            } catch (IllegalAccessException e) {
                LOGGER.info("An error occured while evaluating the updated fields", e);
            }
        }
        return updatedFields;
    }

    private boolean checkObjectContent(T entry1, T entry2) {
        if (entry1.getClass() == entry2.getClass()) {
            Field[] declaredFields = entry1.getClass().getDeclaredFields();
            Field[] parentDeclaredFields = entry1.getClass().getSuperclass().getDeclaredFields();
            int found = checkDeclaredFields(declaredFields, entry1, entry2);
            found += checkDeclaredFields(parentDeclaredFields, entry1, entry2);
            if (found == declaredFields.length + parentDeclaredFields.length) {
                return true;
            }
        }
        return false;
    }

    private int checkDeclaredFields(Field[] declaredFields, T entry1, T entry2) {
        int found = 0;
        for (Field field : declaredFields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                Object field1 = field.get(entry1);
                Object field2 = field.get(entry2);
                if (field1 != null && field2 != null && field1.equals(field2)) {
                    found++;
                }
            } catch (IllegalAccessException e) {
                LOGGER.info("An error occurred while checking the declared fields", e);
            }
        }
        return found;
    }

    /**
     * Get the different entries.
     *
     * @param firstCompared  the first compared list
     * @param secondCompared the second compared list
     * @param type           the action type
     * @return list of entry actions.
     */
    public List<EntryAction<T>> getDifferentEntries(List<T> firstCompared, List<T> secondCompared,
                                                    EntryAction.ActionType type) {
        List<EntryAction<T>> entries = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < firstCompared.size(); i++) {
            T firstEntry = firstCompared.get(i);
            boolean found = false;
            for (T sencondEntry :
                    secondCompared) {
                if (checkObjectContent(firstEntry, sencondEntry) == true) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                entries.add(new EntryAction<T>(firstEntry, type));
            }
        }
        return entries;
    }

    /**
     * Default constructor.
     */
    public MultipleEntryFields() {
        synchronized (this) {
            XmlFile xml = getConfigFile();
            if (xml.exists()) {
                try {
                    xml.unmarshal(this);
                } catch (IOException e) {
                    LOGGER.info("Could not unmarshall xml to load in the UI", e);
                }
            }
        }
    }

}
