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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;

import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabGroupMemberInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlabauth.security.GitLabUserDetails;

import hudson.security.ACL;
import hudson.security.Permission;

/**
 * Global ACL for GitLab
 * 
 * @author Andreas Alanko
 */
public class GitLabGlobalACL extends GitLabAbstactACL {
    /** GitLab usernames with admin rights on Jenkins. */
    private List<String> adminUsernames;
    
    /** GitLab groups with admin rights on Jenkins. */
    private List<String> adminGroups;
    
    /** If we want all GitLab admins to be Jenkins admins aswell. */
    private boolean useGitLabAdmins;
    
    /** Logger for this class. */
    private final transient Logger LOGGER = Logger.getLogger(GitLabGlobalACL.class.getName());
    
    /**
     * Creates a global ACL to use for GitLabAuthorization.
     * 
     * Identities such as usernames and groups should be separated by commas.
     * Example: "myUsername, anotherUsername"
     * Example: "myGroup, anotherGroup"
     * 
     * @param adminUsernames     the admin usernames
     * @param adminGroups        the admin groups
     * @param useGitLabAdmins    if GitLab admins should also be Jenkins admins
     * @param grantedPermissions the granted permissions
     */
    public GitLabGlobalACL(String adminUsernames, String adminGroups, boolean useGitLabAdmins, 
            GitLabGrantedPermissions grantedPermissions) {
        super(grantedPermissions);
        this.useGitLabAdmins = useGitLabAdmins;
        this.adminUsernames = splitAdminIdentitiesIntoList(adminUsernames);
        this.adminGroups = splitAdminIdentitiesIntoList(adminGroups);
    }
    
    /**
     * Splits a string of identities separated by commas and adds them to
     * a List. 
     * 
     * @param adminIdentities the string
     * @return a list
     */
    private List<String> splitAdminIdentitiesIntoList(String adminIdentities) {
        List<String> list = new ArrayList<String>();
        
        if (adminIdentities != null && adminIdentities.length() > 0) {
            adminIdentities = adminIdentities.trim();
            String[] split = adminIdentities.split(",");
            
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
                
                if (!split[i].isEmpty()) {
                    list.add(split[i]);
                }
            }
        }
        return list;
    }

    /**
     * Checks if the given principal has permission to use the permission.
     * 
     * @param auth       the authentication object
     * @param permission the permission
     * @return true if the given principal has permission
     */
    @Override
    public boolean hasPermission(Authentication auth, Permission permission) {
        if (auth == ACL.SYSTEM) {
            return true;
        }
        
        if(isLoggedIn(auth)) {
            GitLabUserDetails user = (GitLabUserDetails) auth.getPrincipal();
            
            if (isPermissionSetStandard(user, permission)) {
                return true;
            }
        }
        return isPermissionSetAnon(permission);
    }
    
    /**
     * Checks if the given user has admin access on the jenkins server.
     * 
     * @param user the user
     * @return true is the user has admin access else false
     */
    public boolean isAdmin(GitLabUserDetails user) {
        try {
            GitLabGroupMemberInfo groupMember;
            
            for (int i = 0; i < adminGroups.size(); i++) {
                groupMember = GitLab.getGroupMember(user.getId(), adminGroups.get(i));
                
                if(groupMember != null && !groupMember.isBlocked()) {
                    return true;
                }
            }
            return adminUsernames.contains(user.getUsername()) || (useGitLabAdmins && GitLab.isAdmin(user.getId()));
        } catch (GitLabApiException e) {
            LOGGER.warning("Connection to the GitLab API failed.");
        }
        return false;
    }
    
    /**
     * Returns a string with GitLab usernames who has admin access in Jenkins.
     * 
     * The usernames are separated by commas.
     * 
     * @return a string with GitLab usernames
     */
    public String getAdminUsernames() {
        return StringUtils.join(adminUsernames.iterator(), ", ");
    }
    
    /**
     * Returns a string with GitLab groups who has admin access in Jenkins.
     * 
     * The groups are separated by commas.
     * 
     * @return a string with GitLab groups
     */
    public String getAdminGroups() {
        return StringUtils.join(adminGroups.iterator(), ", ");
    }
    
    /**
     * Checks if admins of GitLab also should be admins of Jenkins.
     * 
     * @return true if GitLab admins should be admins of Jenkins, else false
     */
    public boolean getUseGitLabAdmins() {
        return useGitLabAdmins;
    }
}
