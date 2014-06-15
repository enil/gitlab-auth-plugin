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

package com.sonymobile.jenkins.plugins.gitlab.gitlabauth;

import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.acegisecurity.Authentication;

import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabUserInfo;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.security.GitLabUserDetails;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

/**
 * UserProperty used to display GitLab user information on the user summary page.
 * GitLabUserInformation/summary.jelly is used to display the information.
 * 
 * @author Andreas Alanko
 */

public class GitLabUserProperty extends UserProperty {
    /** The GitLab user associated with this object. */
    private GitLabUserInfo user;

    /**
     * Creates a new property for the given user. 
     * 
     * @param user the user
     */
    public GitLabUserProperty(GitLabUserInfo user) {
        this.user = user;
    }
    
    public GitLabUserProperty() {
        this.user = null;
    }

    /**
     * Gets the GitLab username for the user associated to
     * this property.
     * 
     * @return the username or "Anonymous" if the user does not exist.
     */
    public String getUsername() {
        return (user != null) ? user.getUsername() : "Anonymous";
    }

    /**
     * Gets the GitLab full name for the user associated to
     * this property.
     * 
     * @return the full name or the username if a full name is not set.
     */
    public String getFullname() {
        return (user != null) ? user.getName() : this.getUsername();
    }

    /**
     * Gets the GitLab email address for the user associated to
     * this property.
     * 
     * @return the email address or "N/A" if no email is available
     */
    public String getEmail() {
        return (user != null) ? user.getEmail() : "N/A";
    }
    
    /**
     * Gets the GitLab user ID for the user associated to
     * this property.
     * 
     * @return the user ID or -1 if something went wrong
     */
    public int getUserId() {
        return (user != null) ? user.getId() : -1;
    }

    @Extension
    public static final class DescriptorImpl extends UserPropertyDescriptor {
        /** Logger for this class. */
        private final transient Logger LOGGER = Logger.getLogger(GitLabUserProperty.class.getName());
        
        /**
         * Gets the display name for the configuration page.
         * 
         * @return the display name
         */
        public String getDisplayName() {
            return "GitLab user information";
        }

        /**
         * Creates a new instance of the GitLabUserInformation object containing information about the logged in user.
         * 
         * @return the UserPropery object
         */
        @Override
        public UserProperty newInstance(User user) {
            Authentication auth = Jenkins.getAuthentication();
            
            if (auth instanceof GitLabUserDetails) {
                GitLabUserInfo gitLabUser;
                try {
                    gitLabUser = GitLab.getUser(((GitLabUserDetails) auth.getPrincipal()).getId());
                    return new GitLabUserProperty(gitLabUser);
                } catch (GitLabApiException e) {
                    LOGGER.warning(e.getMessage());
                }
            }
            return new GitLabUserProperty();
        }

        /**
         * Check to see if the user property is enabled.
         * 
         * This is not enabled because no configuration should be done
         * and only information should be displayed.
         * 
         * @return true if the plugin is enabled.
         */
        public boolean isEnabled() {
            return false;
        }
    }
}
