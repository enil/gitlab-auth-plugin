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

import hudson.security.ACL;
import hudson.security.Permission;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.acegisecurity.Authentication;

import com.sonymobile.jenkins.plugins.gitlabauth.security.GitLabUserDetails;

/**
 * Abstract ACL class for other GitLab ACLs to extend.
 * 
 * @author Andreas Alanko
 */
public abstract class GitLabAbstactACL extends ACL {
    /** Map of all roles and their respective granted permissions. */
    private Map<String, List<Permission>> grantedPermissions;
    
    /**
     * Creates an ACL based on the given map of granted permissions.
     * 
     * @param grantedPermissions map of granted permissions
     */
    protected GitLabAbstactACL(Map<String, List<Permission>> grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }
    
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
     * Checks if the given role has the given permission.
     * 
     * @param role       the role
     * @param permission the permission
     * @return true if the role has permission
     */
    public boolean isPermissionSet(String role, Permission permission) {
        if(role != null && permission != null && grantedPermissions.containsKey(role)) {
            return grantedPermissions.get(role).contains(permission);
        }
        return false;
    }
    
    /**
     * Returns a map with the given permissions to the different roles.
     * 
     * Roles are represented as a String object, which is the key to this map.
     * The value of each key is a list of the permissions granted to the specific role.
     * 
     * @return a map with the granted permissions
     */
    public Map<String, List<Permission>> getGrantedPermissions() {
        return Collections.unmodifiableMap(grantedPermissions);
    }
}
