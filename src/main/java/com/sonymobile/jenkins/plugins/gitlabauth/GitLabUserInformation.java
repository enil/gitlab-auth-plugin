package com.sonymobile.jenkins.plugins.gitlabauth;

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

public class GitLabUserInformation extends UserProperty {
    /** the logged in user object with GitLab information */
    private User mUser;

    /**
     * Creates a new object for the given user. 
     * The constructor gets the GitLab information for the given user.
     * 
     * @param user the logged in user
     */
    public GitLabUserInformation(User user) {
        //TODO: Get GitLabUser object with information
        this.mUser = user;
    }

    /**
     * Gets the GitLab username of the logged in user.
     * 
     * @return the username or "Anonymous" if the user does not exist.
     */
    public String getUsername() {
        return (mUser != null) ? mUser.getDisplayName() : "Anonymous";
    }

    /**
     * Gets the GitLab full name of the logged in user.
     * 
     * @return the full name or the username if a full name is not set.
     */
    public String getFullname() {
        return (mUser != null) ? mUser.getFullName() : this.getUsername();
    }

    /**
     * Gets the GitLab email address of the logged in user.
     * 
     * @return the email adress
     */
    public String getEmail() {
        //TODO
        return "Not available";
    }

    @Extension
    public static final class DescriptorImpl extends UserPropertyDescriptor {

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
            return new GitLabUserInformation(user);
        }

        /**
         * Check to see if the UserProperty is enabled.
         * 
         * This is set to not be enabled because we don't want to enable any configuration and only display user information from GitLab.
         * 
         * @return true if the plugin is enabled.
         */
        public boolean isEnabled() {
            return false;
        }
    }
}
