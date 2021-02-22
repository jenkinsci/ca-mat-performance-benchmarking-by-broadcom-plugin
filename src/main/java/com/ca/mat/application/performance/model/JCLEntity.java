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
package com.ca.mat.application.performance.model;

/**
 * The model class that represents the JCL entities for jobs or programs.
 */
public class JCLEntity {
    /**
     * The name.
     */
    private final String name;
    /**
     * The type.
     */
    private final Type type;

    /**
     * The JCL Entity default constructor.
     *
     * @param name - the job or program name
     * @param type - JOB or PROGRAM
     */
    public JCLEntity(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Whether this is a job.
     *
     * @return true if it is a job, false if not
     */
    public boolean isJob() {
        return type == Type.JOB;
    }

    /**
     * Whether this is a program.
     *
     * @return true if it is a program, false if not
     */
    public boolean isProgram() {
        return type == Type.PROGRAM;
    }

    /**
     * Get the type.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Enumeration enforcing that only jobs and programs are supported.
     */
    public enum Type {
        /**
         * Enumeration type of JOB.
         */
        JOB,
        /**
         * Enumeration type of PROGRAM.
         */
        PROGRAM
    }
}
