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

import java.util.List;

import hudson.security.ACL;
import hudson.security.Permission;

import org.acegisecurity.Authentication;

import com.sonymobile.jenkins.plugins.gitlabauth.security.GitLabUserDetails;

/**
 * Abstract ACL class for other GitLab ACLs to extend.
 * 
 * @author Andreas Alanko
 */
public abstract class GitLabAbstactACL extends ACL {
    /** Contains all identities and their respective granted permissions. */
    private GitLabGrantedPermissions grantedPermissions;
    
    /**
     * Creates an ACL based on the given map of granted permissions.
     * 
     * @param grantedPermissions map of granted permissions
     */
    protected GitLabAbstactACL(GitLabGrantedPermissions grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }
    
    public List<GitLabPermissionIdentity> getPermissionIdentities(boolean getGitLabIdentities) {
        return grantedPermissions.getPermissionIdentities(getGitLabIdentities);
    }

    /**
     * Checks if the given user has admin access on the Jenkins server.
     * 
     * @param user the user
     * @return true is the user has admin access else false
     */
    protected abstract boolean isAdmin(GitLabUserDetails user);

    /**
     * Checks if the user is logged in and if the principal is a 
     * GitLabUserDetails object.
     * 
     * @param auth the authentication object
     * @return true if logged in and the principal object is a GitLabUserDetails object
     */
    protected boolean isLoggedIn(Authentication auth) {
        return auth.isAuthenticated() && auth.getPrincipal() instanceof GitLabUserDetails;
    }

    /**
     * Checks if the given identity has the given permission.
     * 
     * @param identity   the identity
     * @param permission the permission
     * @return true if permission is granted
     */
    public boolean isPermissionSet(GitLabPermissionIdentity identity, Permission permission) {
        return grantedPermissions.isPermissionSet(identity, permission);
    }

    /**
     * Checks if the permission is granted for the identity type Jenkins.
     * Excluding access level for anonymous.
     * 
     * @param user       the user
     * @param permission the permission
     * @return true if permission is granted
     */
    private boolean isPermissionSetJenkins(GitLabUserDetails user, Permission permission) {
        if (isAdmin(user)) {
            if (isPermissionSet(GitLabPermissionIdentity.JENKINS_ADMIN, permission)) {
                return true;
            }
        }
        
        if (isPermissionSet(GitLabPermissionIdentity.JENKINS_LOGGED_IN, permission)) {
            return true;
        }
        return false;
    }
    
    /**
     * Checks if the permission is granted for the user with the given username 
     * within the identity type User.
     * 
     * @param username   the username
     * @param permission the permission
     * @return true if permission is granted
     */
    private boolean isPermissionSetUser(String username, Permission permission) {
        return isPermissionSet(GitLabPermissionIdentity.user(username), permission);
    }
    
    /**
     * Checks if the permission is granted for the user with the given username
     * within the identity type Group.
     * 
     * @param username   the username
     * @param permission the permission
     * @return true if permission is granted
     */
    private boolean isPermissionSetGroup(String username, Permission permission) {
        //TODO: Have to check against all groups added
        return false;
    }
    
    /**
     * Checks if GitLabIdentity Anonymous has the given permission.
     * 
     * @param permission the permission
     * @return true if permission is granted
     */
    protected boolean isPermissionSetAnon(Permission permission) {
        return isPermissionSet(GitLabPermissionIdentity.JENKINS_ANONYMOUS, permission);
    }
    
    /**
     * Checks if the given permission is set for the given user.
     * 
     * @param user       the user
     * @param permission the permission
     * @return true if permission is granted
     */
    protected boolean isPermissionSetStandard(GitLabUserDetails user, Permission permission) {
        if (isPermissionSetJenkins(user, permission)) {
            return true;
        }
        
        if (isPermissionSetUser(user.getUsername(), permission)) {
            return true;
        }
        
        if (isPermissionSetGroup(user.getUsername(), permission)) {
            return true;
        }
        return false;
    }
}