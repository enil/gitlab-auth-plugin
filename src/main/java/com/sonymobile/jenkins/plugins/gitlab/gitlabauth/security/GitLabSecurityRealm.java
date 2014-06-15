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

package com.sonymobile.jenkins.plugins.gitlab.gitlabauth.security;

import com.sonymobile.gitlab.api.GitLabApiClient;
import com.sonymobile.gitlab.exceptions.ApiConnectionFailureException;
import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabSessionInfo;
import com.sonymobile.jenkins.plugins.gitlab.gitlabapi.GitLabConfiguration;
import com.sonymobile.jenkins.plugins.gitlab.gitlabapi.exception.GitLabConfigurationException;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.AbstractPasswordBasedSecurityRealm;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import net.sf.json.JSONObject;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.dao.DataAccessException;

/**
 * A security realm to support the use of login in with GitLab credentials to a Jenkins server.
 *
 * @author Andreas Alanko
 */
public class GitLabSecurityRealm extends AbstractPasswordBasedSecurityRealm {

    @DataBoundConstructor
    public GitLabSecurityRealm() {
        super();
    }

    /**
     * Specifies if the configured Security Realm allows signup.
     *
     * GitLabSecurityRealm will not allow signups through Jenkins.
     *
     * @return true if signup is allowed
     */
    @Override
    public boolean allowsSignup() {
        return false;
    }

    /**
     * Tries to authenticate a user with the given username and password.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return a UserDetails object with user information
     * @throws AuthenticationException if the authentication failed
     */
    @Override
    protected UserDetails authenticate(String username, String password) throws AuthenticationException {
        try {
            // authenticate credentials and create user details
            return loadUserWithCredentials(username, password);
        } catch (GitLabApiException e) {
            // authentication or connection to the API failed
            throw new BadCredentialsException("Authentication against GitLab failed", e);
        }
    }

    /**
     * Gets user details for a user matching a username and password.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return user details for a user matching the credentials
     * @throws ApiConnectionFailureException if the API connection failed
     */
    private UserDetails loadUserWithCredentials(String username, String password) throws GitLabApiException {
        GitLabApiClient client = GitLabConfiguration.getApiClient();
        
        if(client == null) {
            throw new GitLabConfigurationException("Failed to create the API client");
        }
        
        GitLabSessionInfo session = GitLabConfiguration.getApiClient().getSession(username, password);
        // create user details from the session
        return new GitLabUserDetails(session);
    }

    /**
     * Gets user information about the user with the given username.
     *
     * @param username the user of the user
     * @return a UserDetails object with information about the user
     * @throws UsernameNotFoundException if user with username does not exist
     * @throws DataAccessException       will never be thrown
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        //TODO
        return null;
    }

    /**
     * This feature is not supported.
     *
     * Will throw UsernameNotFoundException at all times.
     *
     * @param username the username of the user
     * @throws UsernameNotFoundException will be thrown at all times
     * @throws DataAccessException       will never be thrown
     */
    @Override
    public GroupDetails loadGroupByGroupname(String groupname) throws UsernameNotFoundException, DataAccessException {
        throw new UsernameNotFoundException("Feature not supported");
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

        /**
         * Returns a new GitLabSecurityRealm object.
         *
         * @param req      the http request
         * @param formData form data
         * @return a GitLabSecurityRealm object
         */
        @Override
        public SecurityRealm newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new GitLabSecurityRealm();
        }

        /**
         * Gives the name to be displayed by the Jenkins view in the security configuration page.
         *
         * @return the display name
         */
        public String getDisplayName() {
            return "GitLab Authentication";
        }
    }
}