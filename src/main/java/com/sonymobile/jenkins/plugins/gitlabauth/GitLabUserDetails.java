package com.sonymobile.jenkins.plugins.gitlabauth;

import com.sonymobile.gitlab.model.GitLabSessionInfo;
import hudson.security.SecurityRealm;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * User details for a user logged in through GitLab.
 *
 * @author Emil Nilsson
 */
public class GitLabUserDetails implements UserDetails {
    /** The GitLab session information. */
    private final GitLabSessionInfo session;

    /**
     * Creates a user object with a GitLab session.
     *
     * @param session the GitLab session
     */
    public GitLabUserDetails(GitLabSessionInfo session) {
        this.session = session;
    }

    /**
     * Returns the authorities granted to the user.
     *
     * This will return authorities required for a logged-in Jenkins use.
     *
     * @return an array of granted authorities
     */
    public GrantedAuthority[] getAuthorities() {
        return new GrantedAuthority[] { SecurityRealm.AUTHENTICATED_AUTHORITY };
    }

    /**
     * Returns the password.
     *
     * This will not contain the actual password but an empty string.
     *
     * @return an empty string
     */
    public String getPassword() {
        return EMPTY;
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
        return session.getUsername();
    }
    
    /**
     * Returns the name of the user.
     * 
     * @return the name
     */
    public String getName() {
        return session.getName();
    }

    /**
     * Checks whether the account has expired.
     *
     * Currently this isn't GitLab doesn't implement this feature.
     * The method always returns true.
     *
     * @return false if the account has expired
     */
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Checks whether the account is locked.
     *
     * Currently this isn't GitLab doesn't implement this feature.
     * The method always returns true.
     *
     * @return false if the account is locked
     */
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Checks whether the password has expired.
     *
     * Currently this isn't GitLab doesn't implement this feature.
     * The method always returns true.
     *
     * @return false if the password has expired
     */
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Checks whether the account is enabled.
     *
     * This is equivalent to whether the account is active or blocked in GitLab.
     *
     * @return true if the account is enabled
     */
    public boolean isEnabled() {
        return session.isActive();
    }

    /**
     * Returns the user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return session.getEmail();
    }

    /**
     * Returns the user's ID.
     *
     * @return the user ID
     */
    public int getId() {
        return session.getId();
    }

    /**
     * Returns the user's private token
     *
     * @return the private token
     */
    public String getPrivateToken() {
        return session.getPrivateToken();
    }
}
