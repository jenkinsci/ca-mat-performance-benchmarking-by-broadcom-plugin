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
 */
package com.ca.mat.application.performance.view;

import hudson.Extension;
import hudson.model.RootAction;
import jenkins.model.ModelObjectWithContextMenu;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.util.List;

/**
 * Entry point to all the Manage PMA.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class Root implements RootAction, ModelObjectWithContextMenu {
    /**
     * Get the icon file name.
     *
     * @return literal <b>gear.png</b>.
     */
    public String getIconFileName() {
        return "gear.png";
    }

    /**
     * Get the display name.
     *
     * @return literal <b>Performance Benchmarking</b>.
     */

    public String getDisplayName() {
        return "Performance Benchmarking";
    }

    /**
     * Get the URL name.
     *
     * @return literal <b>performance-benchmarking</b>.
     */

    public String getUrlName() {
        return "performance-benchmarking";
    }

    /**
     * Return the sample matching the URL name.
     *
     * @param name the name to match
     * @return the corresponding sample
     */
    public PerformanceBenchmarking getDynamic(String name) {
        for (PerformanceBenchmarking ui : getAll()) {
            if (ui.getUrlName().equals(name)) {
                return ui;
            }
        }
        return null;
    }

    /**
     * Get all samples.
     *
     * @return the samples.
     */

    public List<PerformanceBenchmarking> getAll() {
        return PerformanceBenchmarking.all();
    }

    /**
     * Get all Groovy samples.
     *
     * @return the Groovy samples.
     */
    public List<PerformanceBenchmarking> getAllGroovy() {
        return PerformanceBenchmarking.getGroovySamples();
    }

    /**
     * Get all non-Groovy samples.
     *
     * @return the non-Groovy samples
     */
    public List<PerformanceBenchmarking> getAllOther() {
        return PerformanceBenchmarking.getOtherSamples();
    }

    /**
     * Get the context menu.
     * @param request - the HTTP request
     * @param response - the HTTP response
     * @return the context menu
     */
    public ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) {
        ContextMenu menu = new ContextMenu().addAll(getAll());
        menu.items.forEach((item) -> item.url = item.url.replaceAll("/jenkins//jenkins/", "/jenkins/"));
        return menu;
    }
}
