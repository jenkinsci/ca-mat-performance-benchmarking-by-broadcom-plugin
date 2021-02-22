/*
 * The MIT License
 *
 * Copyright 2013 Jesse Glick.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Copyright © 2021 Broadcom. All rights reserved. The term “Broadcom” refers to Broadcom Inc. and/or its
 * affiliates. All authorized reproductions of this software must be marked with this language.
 *
 * This file has been changed by Broadcom.
 */
package com.ca.mat.application.performance.view;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Action;
import hudson.model.Describable;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.io.IOUtils.copy;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class PerformanceBenchmarking implements ExtensionPoint, Action, Describable<PerformanceBenchmarking> {

    /**
     * Gets the icon file name from the jenkins library.
     * @return the icon file name.
     */
    public String getIconFileName() {
        return "gear.png";
    }

    /**
     * Gets the final URL name.
     * @return the URL name
     */
    public String getUrlName() {
        return getClass().getSimpleName();
    }

    /**
     * Default display name.
     * @return the default display name
     */
    public String getDisplayName() {
        return getClass().getSimpleName();
    }

    /**
     * Source files associated with this sample.
     * @return the list of source files
     */
    public List<SourceFile> getSourceFiles() {
        List<SourceFile> r = new ArrayList<SourceFile>();

        r.add(new SourceFile(getClass().getSimpleName() + ".java"));
        for (String name : new String[]{"index.jelly", "index.groovy"}) {
            SourceFile s = new SourceFile(name);
            if (s.resolve() != null) {
                r.add(s);
            }
        }
        return r;
    }

    /**
     * Binds {@link SourceFile}s into URL.
     * @param req - the http request.
     * @param rsp - the http response.
     * @throws IOException - if source file cannot be read
     */
    @SuppressWarnings("static-access")
    public void doSourceFile(StaplerRequest req, StaplerResponse rsp) throws IOException {
        String name = req.getRestOfPath().substring(1); // Remove leading /
        for (SourceFile sf : getSourceFiles()) {
            if (sf.name.equals(name)) {
                sf.doIndex(rsp);
                return;
            }
        }
        rsp.sendError(rsp.SC_NOT_FOUND);
    }

    /**
     * Returns a paragraph of natural text that describes this sample.
     * Interpreted as HTML.
     * @return the description
     */
    public abstract String getDescription();

    /**
     * Gets the page descriptor.
     * @return the page descriptor
     */
    public PerformanceBenchmarkingDescriptor getDescriptor() {
        return (PerformanceBenchmarkingDescriptor) Jenkins.get().getDescriptorOrDie(getClass());
    }

    /**
     * Returns all the registered {@link PerformanceBenchmarking}s.
     * @return the extension list of pages
     */
    public static ExtensionList<PerformanceBenchmarking> all() {
        return Jenkins.get().getExtensionList(PerformanceBenchmarking.class);
    }

    /**
     * Returns the list of groovy sample files.
     * @return the list of groovy files.
     */
    public static List<PerformanceBenchmarking> getGroovySamples() {
        List<PerformanceBenchmarking> r = new ArrayList<PerformanceBenchmarking>();
        for (PerformanceBenchmarking performanceBenchmarking : PerformanceBenchmarking.all()) {
            for (SourceFile src : performanceBenchmarking.getSourceFiles()) {
                if (src.name.contains("groovy")) {
                    r.add(performanceBenchmarking);
                    break;
                }
            }
        }
        return r;
    }

    /**
     * Returns the list of other samples.
     * @return the list of other samples
     */
    public static List<PerformanceBenchmarking> getOtherSamples() {
        List<PerformanceBenchmarking> r = new ArrayList<PerformanceBenchmarking>();
        OUTER:
        for (PerformanceBenchmarking performanceBenchmarking : PerformanceBenchmarking.all()) {
            for (SourceFile src : performanceBenchmarking.getSourceFiles()) {
                if (src.name.contains("groovy")) {
                    continue OUTER;
                }
            }
            r.add(performanceBenchmarking);
        }
        return r;
    }

    /**
     * @author Kohsuke Kawaguchi
     */
    public class SourceFile {
        public final String name;

        /**
         * The source file constructor.
         * @param name the source file name
         */
        public SourceFile(String name) {
            this.name = name;
        }

        /**
         * Resolves the URL depending on the name of the source file.
         * @return the URL
         */
        public URL resolve() {
            return PerformanceBenchmarking.this.getClass().getResource(
                    (name.endsWith(".jelly") || name.endsWith(".groovy")) ?
                            PerformanceBenchmarking.this.getClass().getSimpleName() + "/" + name : name);
        }

        /**
         * Serves this source file.
         * @param rsp the http response
         * @throws IOException if the content cannot be copied
         */
        public void doIndex(StaplerResponse rsp) throws IOException {
            rsp.setContentType("text/plain;charset=UTF-8");
            copy(resolve().openStream(), rsp.getOutputStream());
        }
    }

}
