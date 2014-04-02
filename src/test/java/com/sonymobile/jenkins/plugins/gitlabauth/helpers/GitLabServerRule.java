/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Sony Mobile Communications AB. All rights reserved.
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

package com.sonymobile.jenkins.plugins.gitlabauth.helpers;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * A JUnit rule for mocking a GitLab API.
 *
 * @author Emil Nilsson
 */
public class GitLabServerRule extends WireMockRule {
    /** A valid username expected by the server. */
    public static final String VALID_USERNAME = "dilbert";
    /** An invalid username expected by the server. */
    public static final String INVALID_USERNAME = "wally";
    /** A valid password expected by the server. */
    public static final String VALID_PASSWORD = "foo";
    /** An invalid password expected by the server. */
    public static final String INVALID_PASSWORD = "bar";

    /**
     * Creates a GitLab API mock running on a specified port.
     *
     * @param port the port
     */
    public GitLabServerRule(int port) {
        super(port);
    }

    /**
     * Creates a GitLab API mock with options for WireMock.
     *
     * @param options the options
     */
    public GitLabServerRule(Options options) {
        super(options);
    }

    /**
     * Makes the server expect a valid session request and returns a normal response.
     *
     * Expects to get a session request with the valid username {@link #VALID_PASSWORD} and the valid password
     * {@link #VALID_PASSWORD}.
     */
    public void expectValidSessionRequest() {
        // todo: implement
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Makes the server expect an invalid session request and responds with an error.
     *
     * Expects to get a session request with the invalid username {@link #INVALID_PASSWORD} and the invalid password
     * {@link #INVALID_PASSWORD}.
     */
    public void expectInvalidSessionRequest() {
        // todo: implement
        throw new UnsupportedOperationException("Not implemented");
    }
}
