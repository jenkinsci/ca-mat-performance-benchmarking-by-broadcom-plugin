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
package com.ca.mat.application.performance.control.email;

import com.ca.mat.application.performance.model.AnalysisOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * This helper class assists the pipeline executor to send the PMA Analysis report to the recipient users.
 *
 * @author Arthur Pessoa
 */

public class PMAReportNotificationHelper {

    /**
     * The normal range threshold, 75.
     */
    private static final int NORMAL_RANGE_THRESHOLD = 75;
    /**
     * Singleton instance.
     */
    private static PMAReportNotificationHelper instance;
    private final String reportTemplate1;
    /**
     * Report template for <b>Within the normal range</b>.
     */
    private final String reportTemplate2;
    /**
     * Report template for <b>Alert</b>.
     */
    private final String reportTemplate3;
    /**
     * Report template for <b>Warning</b>.
     */
    private final String reportTemplate4;

    //Patterns
    /**
     * Pattern for the average count.
     */
    private static final Pattern PATTERN_AVG_COUNT = Pattern.compile("\\n \\s+([0-9]+)\\s+");

    private static final Pattern PATTERN_FIRST = Pattern.compile(
            "APPL.\\n\\s(.{1,8})\\s(.{1,8})" +
                    "\\s(.{1,8})\\s(.{1,8})", Pattern.DOTALL);
    private static final Pattern PATTERN_SEC = Pattern.compile(
            "\\s+(.{10})\\s+(.{8})\\s+([0-9+])\\s+([a-zA-Z0-9_.-]+)" +
                    "\\s+(.{11})\\s+(.{11})\\s+([0-9]+)\\s+([0-9]+)");
    /**
     * Pattern for the AVERAGE value string.
     */
    private static final Pattern PATTERN_AVG = Pattern.compile(
            "AVERAGE VALUES ARE:- ELAPSED:\\s*([-+>\\d]+)% CPU:" +
                    "\\s*([-+>\\d]+)% EXCP:\\s*([-+>\\d]+)% SRVU:\\s*([-+>\\d]+)%");
    /**
     * Pattern for alerts.
     */
    private static final Pattern PATTERN_ALERT = Pattern.compile(
            "(.*)\\|(.*)\\|(.*)\\|(.*)\\|(.{10})\\|(.*)\\|(.*)" +
                    "\\|(.*)\\|(.*)\\|(.*)");
    private static final Pattern PATTERN_AVG_CALC = Pattern.compile(
            "\\s+([0-9]+)\\s+(.{11})\\s(.{11})" +
                    "\\s+([0-9]+)\\s+([0-9]+)");

    /**
     * The default getInstance method with the singleton pattern.
     *
     * @return the PMAReportNotification instance
     * @throws IOException        - In case the e-mail report templates cannot be loaded
     * @throws URISyntaxException - In case the e-mail report templates URIs are not valid
     */
    public static PMAReportNotificationHelper getInstance() throws IOException, URISyntaxException {
        if (instance == null) {
            instance = new PMAReportNotificationHelper();
        }
        return instance;
    }

    /**
     * Sends the PMA Analysis results to the users on the pipeline.
     *
     * @param output - the pipeline report output.
     * @return String     - the e-mail template.
     */
    public String getTemplate(AnalysisOutput output) {
        String performance = output.getPerformance();
        String alert = output.getAlert();
        String history = output.getHistory();
        Baseline baseline = null;
        ReportStatus status;
        String measurement = "        ";
        Job job = null;
        Matcher matcher = PATTERN_AVG_COUNT.matcher(performance);
        if (matcher.find()) {
            String template = "unknown";
            int averageCount = Integer.parseInt(matcher.group(1));
            if (averageCount == 1) {
                template = reportTemplate1;
            } else {
                Matcher avgMatcher = PATTERN_AVG.matcher(performance);
                if (avgMatcher.find()) {
                    short avgElapsedPct = Short.parseShort(avgMatcher.group(1).replaceAll(">999", "+999"));
                    short avgCpuPct = Short.parseShort(avgMatcher.group(2).replaceAll(">999", "+999"));
                    short avgExcpPct = Short.parseShort(avgMatcher.group(3).replaceAll(">999", "+999"));
                    short avgSrvuPct = Short.parseShort(avgMatcher.group(4).replaceAll(">999", "+999"));
                    baseline = new Baseline(avgElapsedPct, avgCpuPct, avgExcpPct, avgSrvuPct);
                } else {
                    throw new RuntimeException("Could not evaluate avg_count for performance analysis");
                }
            }

            // Evaluate job and program information
            Matcher jobProgInf = PATTERN_FIRST.matcher(performance);
            if (jobProgInf.find()) {
                String jobName = jobProgInf.group(1);
                String stepName = jobProgInf.group(2);
                String procStep = jobProgInf.group(3);
                String program = jobProgInf.group(4);
                job = new Job(jobName, stepName, procStep, program);
            } else {
                throw new RuntimeException("Could not evaluate output information");
            }

            if (averageCount > 1) {
                String alertJob = "unknown";
                if (alert.contains("No ALERTS generated today")) {
                    status = ReportStatus.WITHIN_THE_NORMAL_RANGE;
                    template = reportTemplate2;
                } else {
                    Matcher alertMatcher = PATTERN_ALERT.matcher(alert);
                    if (alertMatcher.find()) {
                        alertMatcher.find(); //Second match
                        alertJob = alertMatcher.group(1);
                        measurement = alertJob;
                        if (alertJob.contains(job.name)) {
                            status = ReportStatus.ALERT;
                            template = reportTemplate3;
                        } else {
                            status = ReportStatus.WITHIN_THE_NORMAL_RANGE;
                            template = reportTemplate2;
                        }
                        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        String formattedDate = dateFormat.format(new Date());
                        if (history.contains(formattedDate)) {
                            template = template.replace("%MAT_MEASUREMENT_ALERT%",
                                    "PMA generated an alert and MAT measurement was performed");
                        } else {
                            template = template.replace("%MAT_MEASUREMENT_ALERT%", " ");
                        }
                    } else {
                        throw new RuntimeException("Could not evaluate alert information");
                    }
                }

                if ((status != ReportStatus.WARNING) && (status != ReportStatus.ALERT)) {
                    status = ReportStatus.WITHIN_THE_NORMAL_RANGE;
                    if (baseline.elapsed <= NORMAL_RANGE_THRESHOLD) {
                        status = ReportStatus.WARNING;
                    }
                    if (baseline.cpu <= NORMAL_RANGE_THRESHOLD) {
                        status = ReportStatus.WARNING;
                    }
                    if (baseline.excp <= NORMAL_RANGE_THRESHOLD) {
                        status = ReportStatus.WARNING;
                    }
                    if (baseline.srvu <= NORMAL_RANGE_THRESHOLD) {
                        status = ReportStatus.WARNING;
                    }
                }
                if (status == ReportStatus.WARNING) {
                    template = reportTemplate4;
                }

                template = template.replace("%ELP%", baseline.elapsed + "%");
                template = template.replace("%ELP_STATUS%", baseline.elapsed <= 0 ? "success" : "failures");
                template = template.replace("%CPU%", baseline.cpu + "%");
                template = template.replace("%CPU_STATUS%", baseline.cpu <= 0 ? "success" : "failures");
                template = template.replace("%EXCP%", baseline.excp + "%");
                template = template.replace("%EXCP_STATUS%", baseline.excp <= 0 ? "success" : "failures");
                template = template.replace("%SRVU%", baseline.srvu + "%");
                template = template.replace("%SRVU_STATUS%", baseline.srvu <= 0 ? "success" : "failures");

                Matcher previousMatcher = PATTERN_SEC.matcher(performance);
                String prefix = "CUR";
                while (previousMatcher.find()) {
                    template = template.replace("%" + prefix + "_DATE%", previousMatcher.group(1));
                    template = template.replace("%" + prefix + "_TIME%", previousMatcher.group(2));
                    template = template.replace("%" + prefix + "_CODE%", previousMatcher.group(3));
                    template = template.replace("%" + prefix + "_SYSTEM%", previousMatcher.group(4));
                    template = template.replace("%" + prefix + "_ELAPSED%", previousMatcher.group(5));
                    template = template.replace("%" + prefix + "_CPU%", previousMatcher.group(6));
                    template = template.replace("%" + prefix + "_EXCP%", previousMatcher.group(7));
                    template = template.replace("%" + prefix + "_SRVU%", previousMatcher.group(8));
                    prefix = "PREV";
                }

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                String reportGenerateTimestamp = sdf.format(new Date());
                Matcher avgCalc = PATTERN_AVG_CALC.matcher(performance);

                if (avgCalc.find()) {
                    template = template.replace("%AVG_CALCULATION%", avgCalc.group(1));
                    template = template.replace("%AVG_ELAPSED%", avgCalc.group(2));
                    template = template.replace("%AVG_CPU%", avgCalc.group(3));
                    template = template.replace("%AVG_EXCP%", avgCalc.group(4));
                    template = template.replace("%AVG_SRVU%", avgCalc.group(5));
                } else {
                    throw new RuntimeException("Could not evaluate average calculations");
                }
                template = template.replace("%MEASUREMENT%", measurement);
                template = template.replace("%INITIAL_TEST%", status.toString().replaceAll("_", " "));
                template = template.replace("%JOBNAME%", job.name);
                template = template.replace("%STEPNAME%", job.step);
                template = template.replace("%PROCSTEP%", job.proc);
                template = template.replace("%PROGRAM%", job.pgmJcl);
                template = template.replace("%REPORT_GENERATE_TIMESTAMP%", reportGenerateTimestamp);
                return template;
            }
        } else {
            throw new RuntimeException("Could not evaluate average count");
        }
        throw new RuntimeException("Could not create e-mail template with performance analysis results.");
    }


    private PMAReportNotificationHelper() {

        reportTemplate1 = getUri("report_template_1.html");
        reportTemplate2 = getUri("report_template_2.html");
        reportTemplate3 = getUri("report_template_3.html");
        reportTemplate4 = getUri("report_template_4.html");
    }

    private String getUri(String file) {
        InputStream in = getClass().getResourceAsStream(file);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
            return reader.lines().collect(Collectors.joining());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("An error occurred while closing the reader stream for file " + file);
                }
            }
        }
    }

    /**
     * Inner bean class holding baseline information.
     */
    private static class Baseline {
        /**
         * The elapsed time.
         */
        private final short elapsed;
        /**
         * The CPU time.
         */
        private final short cpu;
        /**
         * The EXCP count.
         */
        private final short excp;
        /**
         * The number of SRV units.
         */
        private final short srvu;

        private Baseline(short elapsed, short cpu, short excp, short srvu) {
            this.elapsed = elapsed;
            this.cpu = cpu;
            this.excp = excp;
            this.srvu = srvu;
        }
    }

    /**
     * Inner bean class holding job information.
     */
    private static class Job {
        /**
         * The job name.
         */
        private String name;
        /**
         * The step.
         */
        private String step;
        /**
         * The procedure name.
         */
        private String proc;
        /**
         * The program JCL.
         */
        private String pgmJcl;

        private Job(String name, String step, String proc, String pgmJcl) {
            this.name = name;
            this.step = step;
            this.proc = proc;
            this.pgmJcl = pgmJcl;
        }
    }

    /**
     * Enum with valid report statuses.
     */
    private enum ReportStatus {
        /**
         * Warning enum.
         */
        WARNING,
        /**
         * Alert enum.
         */
        ALERT,
        /**
         * Within the normal range enum.
         */
        WITHIN_THE_NORMAL_RANGE,
        /**
         * Initial test enum.
         */
        INITIAL_TEST
    }

}
