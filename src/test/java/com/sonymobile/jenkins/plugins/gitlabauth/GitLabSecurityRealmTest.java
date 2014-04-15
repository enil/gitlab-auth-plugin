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

package com.sonymobile.jenkins.plugins.gitlabauth;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.sonymobile.jenkins.plugins.gitlabapi.GitLabConfig;
import com.sonymobile.jenkins.plugins.gitlabauth.helpers.GitLabServerRule;
import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.jvnet.hudson.test.JenkinsRule;

import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.GitLabServerRule.INVALID_PASSWORD;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.GitLabServerRule.INVALID_USERNAME;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.GitLabServerRule.VALID_PASSWORD;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.GitLabServerRule.VALID_USERNAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link GitLabSecurityRealm}.
 *
 * Tests logging in on the system with GitLab authentication.
 * The test cases uses a mock GitLab server to authenticate against.
 *
 * @author Emil Nilsson
 */
public class GitLabSecurityRealmTest {
    /** The port to run the mocked GitLab server on. */
    private final static int GITLAB_PORT = 9090;

    /** A rule for creating a Jenkins environment. */
    @Rule public JenkinsRule jenkinsRule = new JenkinsRule();
    /** A rule for a mocked GitLab server. */
    @Rule public GitLabServerRule gitLabRule = new GitLabServerRule(GITLAB_PORT);
    /** A rule for catching expected exceptions. */
    @Rule public ExpectedException thrown = ExpectedException.none();
    /** The GitLab security realm to use. */
    private SecurityRealm securityRealm;
    /** The Jenkins instance. */
    private Jenkins jenkins;
    /** The Jenkins web client. */
    private JenkinsRule.WebClient webClient;

    /**
     * Set up the Jenkins environment and security realm.
     */
    @Before
    public void setUp() {
        jenkins = jenkinsRule.jenkins;
        webClient = jenkinsRule.createWebClient();

        // configure the GitLab API plugin
        configureApi();
        // configure GitLab authentication
        configureSecurityRealm();
    }

    /**
     * Test authenticating a user logging in using valid credentials.
     *
     * @throws Exception if the Jenkins web client throws any unexpected exception
     */
    @Test
    public void authenticateWithValidCredentials() throws Exception {
        // make GitLab respond with a valid session
        gitLabRule.expectValidSessionRequest();

        webClient.login(VALID_USERNAME, VALID_PASSWORD);

        assertThat("User should be logged in", jenkins.getAuthentication(), is(not(Jenkins.ANONYMOUS)));
    }

    /**
     * Test authenticating a user logging in using invalid credentials.
     *
     * @throws Exception if the Jenkins web client throws any unexpected exception
     */
    @Test
    public void authenticateWithInvalidCredentials() throws Exception {
        // make GitLab respond with an error
        gitLabRule.expectInvalidSessionRequest();
        // login should fail with HTTP 401 Unauthorized
        thrown.expect(FailingHttpStatusCodeException.class);
        thrown.expectMessage("401");

        webClient.login(INVALID_USERNAME, INVALID_PASSWORD);
    }

    /**
     * Configures the GitLab API plugin of the Jenkins instance.
     */
    private void configureApi() {
        GitLabConfig config = jenkinsRule.get(GitLabConfig.class).getInstance();
        config.setServerUrl("http://localhost:" + GITLAB_PORT);
        // private token can be anything
        config.setPrivateToken("private_token");
    }

    /**
     * Configures the Jenkins instance to authenticate against GitLab.
     */
    private void configureSecurityRealm() {
        // use GitLab authentication
        securityRealm = new GitLabSecurityRealm();
        jenkins.setSecurityRealm(securityRealm);
    }
}
