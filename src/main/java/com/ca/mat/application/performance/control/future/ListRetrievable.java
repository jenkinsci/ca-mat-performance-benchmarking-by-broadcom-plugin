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

import com.ca.mat.application.performance.view.MultipleEntryFields;
import hudson.XmlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Needs a description, left without full stop to pick this up in checkstyle.
 *
 * @param <T> the multiple field instance
 */
public class ListRetrievable<T> implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListRetrievable.class);

    /**
     * The multiple field instance.
     */
    private MultipleEntryFields<T> instance;
    /**
     * The XML file.
     */
    private XmlFile xmlFile;

    /**
     * Constructor.
     *
     * @param xmlFile  the XML file
     * @param instance the multiple field instance
     */
    public ListRetrievable(XmlFile xmlFile, MultipleEntryFields<T> instance) {
        this.xmlFile = xmlFile;
        this.instance = instance;
    }

    @Override
    public void run() {
        try {
            ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<List<T>>> futureEntries = threadPool.invokeAll(instance.getProcesses());
            List<T> totalEntries = new ArrayList<>();
            boolean noEntries = false;
            for (Future<List<T>> futureEntry : futureEntries) {
                List<T> entries = futureEntry.get();
                if (entries == null) {
                    noEntries = true;
                    break;
                }
                totalEntries.addAll(entries);
            }
            if (!noEntries) {
                instance.setEntries(totalEntries);
                xmlFile.write(instance);
                xmlFile.unmarshal(instance);
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.info("An error occurred while retrieving the list of entities", e);
        }
    }
}
