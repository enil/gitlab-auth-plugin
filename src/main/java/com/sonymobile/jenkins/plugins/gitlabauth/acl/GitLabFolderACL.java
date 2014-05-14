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

package com.sonymobile.jenkins.plugins.gitlabauth.acl;

import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabAuthorization;
import com.sonymobile.jenkins.plugins.gitlabauth.security.GitLabUserDetails;
import hudson.model.Item;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

import java.util.Collection;
import java.util.logging.Logger;

import static java.util.Collections.*;

/**
 * Folder ACL for GitLab.
 * 
 * @author Andreas Alanko
 */
public class GitLabFolderACL extends GitLabAbstractACL {
    /** The group id associated with this ACL */
    private int groupId;
    
    /** Logger for this class. */
    private final transient Logger LOGGER = Logger.getLogger(GitLabFolderACL.class.getName());


    /**
     * Creates a folder ACL to use for GitLabFolderAuthorization with default permissions.
     *
     * @param groupId the GitLab group ID
     */
    public GitLabFolderACL(int groupId) {
        super();
        this.groupId = groupId;
    }
    
    /**
     * Creates a folder ACL to use for GitLabFolderAuthorization.
     * 
     * @param grantedPermissions the granted permissions
     */
    public GitLabFolderACL(GitLabGrantedPermissions grantedPermissions) {
        super(grantedPermissions);
    }

    @Override
    public Collection<PermissionGroup> getApplicablePermissionGroups() {
        return singletonList(PermissionGroup.get(Item.class));
    }

    /**
     * Gets the group id associated with this ACL.
     * 
     * @return the group id
     */
    public int getGroupId() {
        return groupId;
    }
    
    /**
     * Sets the group id associated with this ACL.
     * 
     * @param groupId the group id
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * Checks if the given principal has permission to use the permission.
     * 
     * @param auth       the authentication object
     * @param permission the permission
     * @return true if permission is granted
     */
    @Override
    public boolean hasPermission(Authentication auth, Permission permission) {
        if(hasGlobalPermission(auth, permission)) {
            return true;
        }
        
        if (isLoggedIn(auth)) {
            GitLabUserDetails user = (GitLabUserDetails) auth.getPrincipal();
            
            if (isPermissionSetStandard(user, permission)) {
                return true;
            }
            
            if (isPermissionSetGitLab(user.getId(), groupId, permission)) {
                return true;
            }
        }
        return isPermissionSetAnon(permission);
    }
    
    /**
     * Checks if the given permission is set for the given user for a GitLab folder.
     * 
     * @param userId     the GitLab id of the user
     * @param groupId    the GitLab id of the group
     * @param permission the permission
     * @return true if permission is granted
     */
    private boolean isPermissionSetGitLab(int userId, int groupId, Permission permission) {
        try {
            GitLabAccessLevel accessLevel = GitLab.getAccessLevelInGroup(userId, groupId);
            
            if (isPermissionSet(GitLabPermissionIdentity.getGitLabIdentityFromAccessLevel(accessLevel), permission)) {
                return true;
            }
        } catch (GitLabApiException e) {
            LOGGER.warning("Connection to the GitLab API failed.");
        }
        return false;
    }

    /**
     * Checks if the user has global access rights.
     * 
     * @param auth       the authentication
     * @param permission the permission
     * @return true if the given user has the given permission
     */
    private boolean hasGlobalPermission(Authentication auth, Permission permission) {
        GitLabAuthorization authorization = getGitLabAuthorization();
        if (authorization != null) {
            return ((GitLabGlobalACL) authorization.getRootACL()).hasPermission(auth, permission);
        }
        return false;
    }

    @Override
    protected void setDefaultPermissions() {
        getGrantedPermissions().addPermissionGroups(GitLabPermissionIdentity.GITLAB_OWNER,
                getApplicablePermissionGroups());
    }

    /**
     * Checks if the given user has admin access on the Jenkins server.
     * 
     * @param user the user
     * @return true is the user has admin access else false
     */
    protected boolean isAdmin(GitLabUserDetails user) {
        GitLabAuthorization authorization = getGitLabAuthorization();
        if (authorization != null) {
            return ((GitLabGlobalACL) authorization.getRootACL()).isAdmin(user);
        }
        return false;
    }
    
    /**
     * Gets the GitLabAuthorization object if its configured to be used by Jenkins.
     * 
     * @return a GitLabAuthorization object or null if its not configured
     */
    private GitLabAuthorization getGitLabAuthorization() {
        if(Jenkins.getInstance().getAuthorizationStrategy() instanceof GitLabAuthorization) {
            return (GitLabAuthorization) Jenkins.getInstance().getAuthorizationStrategy();
        }
        return null;
    }
}