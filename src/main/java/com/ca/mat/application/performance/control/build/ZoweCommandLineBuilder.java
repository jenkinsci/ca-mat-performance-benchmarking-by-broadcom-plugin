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
package com.ca.mat.application.performance.control.build;

import com.ca.mat.application.performance.model.CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * This a builder class that helps to constructs, execute and handle zowe commands using ProcessBuilder.
 *
 * @author Arthur Pessoa
 */

public class ZoweCommandLineBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZoweCommandLineBuilder.class);

    /**
     * This method submits a command on the zowe cli and returns the command output as string.
     * The program waits for the execution and doesn't time out. This method is intended for
     * commands that do not hold the main thread, and can be stopped by the user.
     *
     * @param parameters - the command parameters and arguments, e.g.: [zowe, jobs, submit, ds] ...
     * @return the command output
     */
    public String getCommandOutputNoTimeout(String... parameters) {
        return getCommandResponse(parameters);
    }

    /**
     * This method submits a command on the zowe cli and returns the command output as string.
     * The program waits for the execution and doesn't time out. This method is intended for
     * commands that do not hold the main thread, and can be stopped by the user.
     *
     * @param cli - the command parameters and arguments, e.g.: zowe jobs submit ds ...
     * @return the command output
     */
    public String getCommandOutputNoTimeout(String cli) {
        return getCommandResponse(cli);
    }

    /**
     * This method submits a command on the zowe cli and returns the command output as CommandResponse.
     * The method time outs after three seconds, used for operations that does not need internet protocols
     * and run in the local machine. (e.g.: retrieving zowe profiles)
     *
     * @param parameters - the command parameters and arguments, e.g.: [zowe, jobs, submit, ds] ...
     * @return the command output
     */
    public CommandResponse getCommandOutput(String... parameters) {
        Process p = getExecutedProcess(parameters);
        return getCommandResponse(p);
    }

    /**
     * This method submits a command on the zowe cli and returns the command output as CommandResponse.
     * The method time outs after three seconds, used for operations that does not need internet protocols
     * and run in the local machine. (e.g.: retrieving zowe profiles)
     *
     * @param cli - the command parameters and arguments, e.g.: zowe jobs submit ds...
     * @return the command output
     */
    public CommandResponse getCommandOutput(String cli) {
        Process p = getExecutedProcess(cli);
        return getCommandResponse(p);
    }

    private CommandResponse getCommandResponse(Process p) {
        BufferedReader reader = null;
        try {
            int status = p.waitFor(); // the process is now dead
            InputStream stream = status == 0 ? p.getInputStream() : p.getErrorStream();
            reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line = null;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                result.append(line);
            }
            return new CommandResponse(status, result.toString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("an error occured while reading the output from the zowe command.");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.info("Could not close BufferedReader after retrieving the latest command output", e);
                }

            }
        }
    }

    private String getCommandResponse(String cli) {
        try {
            LOGGER.info("Executing command: " + cli);
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", cli);
            Process p = pb.start();
            String response = getResultFromStream(p.getInputStream());
            response += System.lineSeparator() + getResultFromStream(p.getErrorStream());
            return response;
        } catch (IOException e) {
            throw new RuntimeException("an error occured while reading the output from the zowe command.");
        }
    }

    private String getResultFromStream(InputStream stream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        String line = null;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            result.append(line);
            result.append(System.lineSeparator());
        }
        return result.toString();
    }

    private String getCommandResponse(String... parameters) {
        StringBuilder sb = new StringBuilder();
        if (parameters.length > 0) {
            if (!parameters[0].startsWith("zowe")) {
                sb.append("zowe ");
            }
        }
        for (String parm : parameters) {
            sb.append(parm + " ");
        }
        return getCommandResponse(sb.toString());
    }

    private Process getExecutedProcess(String... parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("zowe ");
        for (String parm : parameters) {
            sb.append(parm + " ");
        }
        return getExecutedProcess(sb.toString());
    }

    private Process getExecutedProcess(String cli) {
        try {
            LOGGER.info("Executing zowe command " + cli);
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", cli);
            Process p = pb.start();
            p.waitFor(3, TimeUnit.SECONDS);  // let the process run for 5 seconds
            p.destroy();                     // tell the process to stop
            p.waitFor(2, TimeUnit.SECONDS); // give it a chance to stop
            p.destroyForcibly();             // tell the OS to kill the process
            return p;
        } catch (InterruptedException e) {
            throw new RuntimeException("An exception occurred while executing executing the zowe command", e);
        } catch (IOException e) {
            throw new RuntimeException("An I/O exception occurred while executing executing the zowe command", e);
        }
    }

}
