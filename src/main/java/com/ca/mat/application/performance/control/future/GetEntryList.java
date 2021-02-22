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

import com.ca.mat.application.performance.control.build.ZoweCommandLineBuilder;
import com.ca.mat.application.performance.view.InclusionsAndExclusions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The abstract class that retrieves the zowe pma scope entry list (for programs and jobs).
 *
 * @author Arthur Pessoa
 */
public abstract class GetEntryList implements Callable<List<InclusionsAndExclusions.Entry>> {

    // Matches the return output of the mat-detect-for-zowe-cli command "zowe pma scope getlj or getlp.txt,
    // examples of the output are referenced in the project resources (getlj.txt and getlp.txt.txt)"
    private static final String SCOPE_GET_LIST_INCLUSION_PATTERN =
            "^\\s([a-zA-Z#@&$._)][a-zA-Z0-9#@%&$_)*]{0,7})\\s{1,8}([a-zA-Z#@&$._)]" +
                    "[a-zA-Z0-9#@%&$)*]{0,7})?\\s{1,8}([a-z0-9A-Z#@&$._)]" +
                    "[a-zA-Z0-9#@%&$)*]{0,7})?\\s{1,8}[I|E](.*)$";
    private static final String SCOPE_GET_LIST_EXCLUSION_PATTERN =
            "\\s([a-zA-Z#@&$._)][a-zA-Z0-9#@%&$)*]" +
            "{0,9})\\s{1,9}EXCLUDED (.*)";
    private static final Logger LOGGER = LoggerFactory.getLogger(GetEntryList.class);

    /**
     * The get list parameter.
     * @return - whether 'j' for jobs or 'p' for programs.
     */
    protected abstract String getListParam();

    @Override
    public List<InclusionsAndExclusions.Entry> call() {
        List<InclusionsAndExclusions.Entry> entries = new ArrayList<>();
        String scopeType = this instanceof GetListInclusionsTask ? " jobs" : "programs";
        LOGGER.info("Getting scope list of " + scopeType);
        String[] parameters = new String[]{"pma", "scope", "getl" + getListParam()};
        ZoweCommandLineBuilder zoweCmd = new ZoweCommandLineBuilder();
        String response = zoweCmd.getCommandOutputNoTimeout(parameters);
        String[] outputLines = response.split(System.lineSeparator());
        final byte index = 2; // represents the starting line with the scope definition from the command output.
        for (int i = index; i < outputLines.length; i++) {
            String line = outputLines[i];
            if (this instanceof GetListInclusionsTask) {
                Pattern pattern = Pattern.compile(SCOPE_GET_LIST_INCLUSION_PATTERN);
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String jobName = matcher.group(1) == null ? "" : matcher.group(1);
                    String stepName = matcher.group(2) == null ? "" : matcher.group(2);
                    String procStep = matcher.group(3) == null ? "" : matcher.group(3);
                    String description = matcher.group(4) == null ? "" : matcher.group(4).trim();
                    entries.add(new InclusionsAndExclusions.PMAInclusion(jobName, stepName, procStep, description));
                }
            } else {
                Pattern pattern = Pattern.compile(SCOPE_GET_LIST_EXCLUSION_PATTERN);
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String program = matcher.group(1) == null ? "" : matcher.group(1);
                    String description = matcher.group(2) == null ? "" : matcher.group(2).trim();
                    entries.add(new InclusionsAndExclusions.PMAExclusion(program, description));
                }
            }
        }
        LOGGER.info("Scope list of " + scopeType + " is: " + entries.size());
        return entries;
    }

}
