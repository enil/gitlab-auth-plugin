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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;

import hudson.security.ACL;
import hudson.security.Permission;

/**
 * ACL for GitLab
 * 
 * @author Andreas Alanko
 */
public class GitLabACL extends ACL {
    /** GitLab usernames with admin rights on Jenkins */
    private List<String> adminUsernames;
    /** If we want all GitLab admins to be Jenkins admins aswell */
    private boolean useGitLabAdmins;
    
    /** Jenkins roles */
    private static final String JAL_ADMIN = "Admin";
    private static final String JAL_LOGGED_IN = "Logged In";
    private static final String JAL_ANONYMOUS = "Anonymous";
    public static final String[] jenkinsAccessLevels = {JAL_ADMIN, JAL_LOGGED_IN, JAL_ANONYMOUS};
    
    /** Map of all Jenkins roles and their respective permissions */
    private HashMap<String, List<Permission>> grantedJenkinsPermissions;
    
    /**
     * Creates an ACL to use for GitLabAuthorization.
     */
    public GitLabACL(String adminUsernames, boolean useGitLabAdmins) {
        this.useGitLabAdmins = useGitLabAdmins;
        this.adminUsernames = new ArrayList<String>();
        
        grantedJenkinsPermissions = new HashMap<String, List<Permission>>();
        
        for (int i = 0; i < jenkinsAccessLevels.length; i++) {
            grantedJenkinsPermissions.put(jenkinsAccessLevels[i], new ArrayList<Permission>());
        }
        
        if (adminUsernames != null && adminUsernames.length() > 0) {
            adminUsernames = adminUsernames.trim();
            String[] split = adminUsernames.split(",");
            
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
                
                if (!split[i].isEmpty()) {
                    this.adminUsernames.add(split[i]);
                }
            }
        }
    }

    /**
     * Checks if the given principal has permission to use the permission.
     * 
     * @param a the authentication object
     * @param permission the permission
     * @return true if the given principal has permission
     */
    @Override
    public boolean hasPermission(Authentication a, Permission permission) {
        //TODO: Implement
        return true;
    }
    
    /**
     * Returns a string with GitLab usernames who has admin access in Jenkins.
     * 
     * The usernames are separated by commas.
     * 
     * @return a string with usernames separated by commas
     */
    public String getAdminUsernames() {
        return StringUtils.join(adminUsernames.iterator(), ",");
    }
    
    /**
     * Checks if admins of GitLab also should be admins of Jenkins.
     * 
     * @return true if GitLab admins should be admins of Jenkins, else false
     */
    public boolean getUseGitLabAdmins() {
        return useGitLabAdmins;
    }

    /**
     * Returns a map with the given permissions to the different GitLab roles.
     * 
     * GitLab roles are represented as a String object, which is the key to this map.
     * The value of each key is the permissions granted to the specific role.
     * 
     * @return a map with the granted permissions
     */
    public Map<String, List<Permission>> getGrantedJenkinsPermissions() {
        return grantedJenkinsPermissions;
    }

    /**
     * Checks if the given Jenkins role has the given permission.
     * 
     * @param role the role
     * @param p the permission
     * @return true if the role has permission
     */
    public boolean isPermissionSet(String role, Permission p) {
        if (roleExists(role)) {
            return grantedJenkinsPermissions.get(role).contains(p);
        }
        return false;
    }
    
    /**
     * Checks if the given Jenkins role exists.
     * 
     * @param role the role
     * @return true if role exists
     */
    private boolean roleExists(String role) {
        for (int i = 0; i < jenkinsAccessLevels.length; i++) {
            if (jenkinsAccessLevels[i].equals(role)) {
                return true;
            }
        }
        return false;
    }
}
