/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Andreas Alanko, Emil Nilsson, Sony Mobile Communications AB. 
 * All rights reserved.
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
import org.json.JSONObject;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

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
    /** The body of a request to /session with valid user credentials. */
    private static final String VALID_SESSION_REQUEST_BODY =
            String.format("login=%s&password=%s", VALID_USERNAME, VALID_PASSWORD);
    /** The body of a request to /session with invalid user credentials. */
    private static final String INVALID_SESSION_REQUEST_BODY =
            String.format("login=%s&password=%s", INVALID_USERNAME, INVALID_PASSWORD);
    /** The JSON response of a valid request to /session. */
    public static final JSONObject VALID_RESPONSE_OBJECT = new JSONObject();
    static {
        VALID_RESPONSE_OBJECT
                .put("id", 1)
                .put("username", VALID_USERNAME)
                .put("email", "user@example.com")
                .put("name", "User Name")
                .put("private_token", "0123456789abcdef")
                .put("blocked", false);
    }

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
        stubFor(post(urlEqualTo("/api/v3/session"))
                .withRequestBody(equalTo(VALID_SESSION_REQUEST_BODY))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody(VALID_RESPONSE_OBJECT.toString())));
    }

    /**
     * Makes the server expect an invalid session request and responds with an error.
     *
     * Expects to get a session request with the invalid username {@link #INVALID_PASSWORD} and the invalid password
     * {@link #INVALID_PASSWORD}.
     */
    public void expectInvalidSessionRequest() {
        stubFor(post(urlEqualTo("/api/v3/session"))
                .withRequestBody(equalTo(INVALID_SESSION_REQUEST_BODY))
                .willReturn(aResponse()
                        .withStatus(401)));
    }
}
