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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import com.ca.mat.application.performance.control.future.GetListExclusionTask;
import com.ca.mat.application.performance.control.future.GetListInclusionsTask;
import com.ca.mat.application.performance.model.EntryAction;
import com.ca.mat.application.performance.model.UpdateAction;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import jenkins.util.ProgressiveRendering;

/**
 * PMA Inclusions and Exclusions.
 */
@Extension
public final class InclusionsAndExclusions extends MultipleEntryFields<InclusionsAndExclusions.Entry> {

    /**
     * Zowe PMA command for deleting jobs.
     */
    private static final String ZOWE_PMA_DEL_JOB_FORMAT = "zowe pma scope del-job \"%s\"";
    /**
     * Zowe PMA command for including jobs.
     */
    private static final String ZOWE_PMA_INC_JOB_CMD = "zowe pma scope incj";
    /**
     * Zowe PMA command for excluding jobs.
     */
    private static final String ZOWE_PMA_EXCL_JOB_CMD = "zowe pma scope exl-pgm";
    /**
     * Zowe PMA command for deleting programs.
     */
    private static final String ZOWE_PMA_DEL_PGM_CMD = "zowe pma scope del-pgm";

    @Override
    public String getDescription() {
        return "Include specific jobs and exclude specific programs to define the scope " +
                "of the performance monitoring scope for your mainframe applications.";
    }

    /**
     * Get the display name.
     *
     * @return the literal <b>Define the Monitoring Scope</b>.
     */
    public String getDisplayName() {
        return "Define the Monitoring Scope";
    }

    /**
     * Inner class representing the descriptor.
     */
    @Extension
    public static final class DescriptorImpl extends PerformanceBenchmarkingDescriptor {

    }

    @Override
    public String getUrlName() {
        return "scope";
    }

    @Override
    public String getXMLFile() {
        return "pma-scope.xml";
    }

    @Override
    protected void setConfig(MultipleEntryFields<Entry> read) {
        config = ((InclusionsAndExclusions) read).getConfig();
    }

    @Override
    public synchronized HttpResponse doConfigSubmit(StaplerRequest req) throws ServletException, IOException {
        config = null;
        return super.doConfigSubmit(req);
    }

    @Override
    protected boolean invalidEntries() {
        //Updates entries to upper case to avoid mismatch with the return value from mainframe
        for (Entry entry : getEntries()) {
            if (entry instanceof PMAInclusion) {
                PMAInclusion inclusion = ((PMAInclusion) entry);
                inclusion.description = inclusion.description.isEmpty() ? "INCL BY PMA JENKINS PLG"
                        : inclusion.description.toUpperCase();
                inclusion.procstep = inclusion.procstep.toUpperCase();
                inclusion.stepname = inclusion.stepname.toUpperCase();
                inclusion.jobname = inclusion.jobname.toUpperCase();
            } else {
                PMAExclusion exclusion = ((PMAExclusion) entry);
                exclusion.description = exclusion.description.isEmpty() ? "EXCL BY PMA JENKINS PLG"
                        : exclusion.description.toUpperCase();
                exclusion.program = exclusion.program.toUpperCase();
            }
        }
        return false;
    }

    @Override
    public List<Entry> getEntries() {
        if (config == null) {
            config = new Config(null);
        }
        return getConfig().getEntries();
    }

    @Override
    protected void postProcessEntries() {

    }

    @Override
    protected boolean isSuccessful(String result) {
        return result.contains("IS ADDED") || result.contains("ALREADY EXISTS");
    }

    @Override
    protected String getFinalUpdatedMessage(boolean isUpdated, String result, Entry entry) {
        String actionText = result.contains("IS ADDED") ? (isUpdated ? "IS UPDATED" : "IS ADDED") : "ALREADY EXISTS";
        String finalMessage = String.format("THE %s %s %s TO THE %s LIST",
                entry instanceof InclusionsAndExclusions.PMAInclusion ? "JOB" : "PROGRAM",
                entry instanceof InclusionsAndExclusions.PMAInclusion ?
                        ((InclusionsAndExclusions.PMAInclusion) entry).getJobname() :
                        ((InclusionsAndExclusions.PMAExclusion) entry).getProgram(),
                actionText,
                entry instanceof InclusionsAndExclusions.PMAInclusion ? "INCLUSION" : "EXCLUSION");
        return finalMessage;
    }

    @Override
    protected String handleSingleNewEntry(EntryAction<Entry> entryAction) {
        Entry entry = entryAction.getEntry();
        String cli = "";
        if (entry instanceof InclusionsAndExclusions.PMAInclusion) {
            //Inclusions here
            InclusionsAndExclusions.PMAInclusion obj = (InclusionsAndExclusions.PMAInclusion) entry;
            cli = ZOWE_PMA_INC_JOB_CMD;
            String name = obj.getJobname();
            String description = obj.getDescription();
            String procstep = obj.getProcstep();
            String stepname = obj.getStepname();
            cli = cli + " \"" + name + "\"";
            if (!stepname.isEmpty()) {
                cli = cli + " --st \"" + stepname + "\"";
            }
            if (!procstep.isEmpty()) {
                cli = cli + " --ps \"" + procstep + "\"";
            }
            if (!description.isEmpty()) {
                cli = cli + " --dc \"" + description + "\"";
            } else {
                cli = cli + " --dc \"INCL BY PMA JENKINS PLG\"";
            }
        } else {
            //exclusions here
            InclusionsAndExclusions.PMAExclusion obj = (InclusionsAndExclusions.PMAExclusion) entry;
            cli = ZOWE_PMA_EXCL_JOB_CMD;
            String program = obj.getProgram();
            String description = obj.getDescription();
            cli = cli + " \"" + program + "\"";
            if (!description.isEmpty()) {
                cli = cli + " --dc \"" + description + "\"";
            } else {
                cli = cli + " --dc \"EXCL BY PMA JENKINS PLG\"";
            }
        }
        return cli;
    }

    @Override
    protected String handleSingleUpdate(UpdateAction<Entry> entry) {
        EntryAction<Entry> deleteAction = new EntryAction<Entry>(entry.getOldEntry(), EntryAction.ActionType.delete);
        String cli = handleSingleDelete(deleteAction);
        zoweCmd.getCommandOutputNoTimeout(cli);
        return handleSingleNewEntry(entry);
    }

    @Override
    protected String handleSingleDelete(EntryAction<Entry> entryAction) {
        Entry entry = entryAction.getEntry();
        String cli = "";
        if (entry instanceof InclusionsAndExclusions.PMAInclusion) {
            InclusionsAndExclusions.PMAInclusion inclusionEntry = ((InclusionsAndExclusions.PMAInclusion) entry);
            if (!inclusionEntry.getJobname().contains("*")) {
                cli = String.format(ZOWE_PMA_DEL_JOB_FORMAT + " --ps \"%s\" --st \"%s\"",
                        inclusionEntry.getJobname().isEmpty() ? " " : inclusionEntry.getJobname(),
                        inclusionEntry.getProcstep().isEmpty() ? " " : inclusionEntry.getProcstep(),
                        inclusionEntry.getStepname().isEmpty() ? " " : inclusionEntry.getStepname());
            } else {
                cli = String.format(ZOWE_PMA_DEL_JOB_FORMAT, inclusionEntry.getJobname());
            }
        } else {
            cli = ZOWE_PMA_DEL_PGM_CMD + " " + ((InclusionsAndExclusions.PMAExclusion) entry).getProgram();
        }
        return cli;
    }

    @Override
    public synchronized ProgressiveRendering entries() {
        return new ScopeProgressViewRendering();
    }

    /**
     * Inner class representing progress rendering.
     */
    public class ScopeProgressViewRendering extends ProgressViewRendering {
        @Override
        protected void compute() {
            super.compute();
        }
    }

    @Override
    public List<Entry> setEntries(List<Entry> entries) {
        return config.entries = entries;
    }

    @Override
    protected String getMainFieldLabel(Entry entry) {
        return entry instanceof PMAInclusion ? "job name" : "program";
    }

    @Override
    protected String getMainField(Entry entry) {
        return entry instanceof PMAInclusion ? ((PMAInclusion) entry).getJobname()
                : ((PMAExclusion) entry).getProgram();
    }

    @Override
    public Collection<Callable<List<Entry>>> getProcesses() {
        Collection<Callable<List<Entry>>> processes = new ArrayList<>();
        processes.add(new GetListInclusionsTask());
        processes.add(new GetListExclusionTask());
        return processes;
    }

    /**
     * The configuration.
     */
    private Config config;

    /**
     * Get the configuration.
     *
     * @return the config
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
     * @param config the configuration to set.
     */
    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * Inner class epresenting the configuration.
     */
    public static final class Config extends AbstractDescribableImpl<Config> {

        /**
         * The configuration entries.
         */
        private List<Entry> entries;

        /**
         * Constructor passing the configuration entries.
         *
         * @param entries the configuration entries.
         */
        @DataBoundConstructor
        public Config(List<Entry> entries) {
            this.entries = Collections.synchronizedList(entries != null ? new ArrayList<>(entries) : new ArrayList<>());
        }

        /**
         * Get the configuration entries.
         *
         * @return the entries.
         */
        public List<Entry> getEntries() {
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
     * Inner class representing an abstract  entry.
     */
    public abstract static class Entry extends AbstractDescribableImpl<Entry> {

    }

    /**
     * Concrete entry class.
     */
    public static final class PMAInclusion extends Entry {

        /**
         * The job name.
         */
        private String jobname;
        /**
         * The step name.
         */
        private String stepname;
        /**
         * The procedure step.
         */
        private String procstep;
        /**
         * The description.
         */
        private String description;

        /**
         * Constructor.
         *
         * @param jobname     the job name
         * @param stepname    the step name
         * @param procstep    the procedure step
         * @param description the description
         */
        @DataBoundConstructor
        public PMAInclusion(String jobname, String stepname, String procstep, String description) {
            this.jobname = jobname;
            this.stepname = stepname;
            this.procstep = procstep;
            this.description = description;
        }

        /**
         * Get the job name.
         *
         * @return the job name
         */

        public String getJobname() {
            return jobname;
        }

        /**
         * Get the description.
         *
         * @return the description
         */

        public String getDescription() {
            return description;
        }

        /**
         * Get the procedure step.
         *
         * @return the step
         */
        public String getProcstep() {
            return procstep;
        }

        /**
         * Get the step name.
         *
         * @return the step name
         */

        public String getStepname() {
            return stepname;
        }

        /**
         * Inner class representing the descriptor.
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<Entry> {
            @Override
            public String getDisplayName() {
                return "Job Inclusion";
            }
        }

        /**
         * Get the job name.
         *
         * @return the job name
         */
        public String getName() {
            return jobname;
        }

    }

    /**
     * Inner class for PMA exclusions.
     */
    public static final class PMAExclusion extends Entry {

        /**
         * The program.
         */
        private String program;
        /**
         * The description.
         */
        private String description;

        /**
         * Constructor.
         *
         * @param program     the program to exclude
         * @param description the description
         */
        @DataBoundConstructor
        public PMAExclusion(String program, String description) {
            this.program = program;
            this.description = description;
        }

        /**
         * Get the program.
         *
         * @return the program
         */
        public String getProgram() {
            return program;
        }

        /**
         * Get the description.
         *
         * @return the description.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Get the name.
         *
         * @return the name
         */
        public String getName() {
            return program;
        }

        /**
         * Inner class representing the descriptor.
         */
        @Extension
        public static class DescriptorImpl extends Descriptor<Entry> {

            @Override
            public String getDisplayName() {
                return "Program Exclusion";
            }

        }
    }
}
