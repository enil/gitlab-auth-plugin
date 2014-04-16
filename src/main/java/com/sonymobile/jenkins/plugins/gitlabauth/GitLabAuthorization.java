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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;
import hudson.security.PermissionGroup;

/**
 * Creates an authorization strategy for GitLab.
 * 
 * @author Andreas Alanko
 */
public class GitLabAuthorization extends AuthorizationStrategy {
    /** ACL for GitLab */
    private final GitLabACL rootACL;

    /**
     * Creates an Authorization Strategy for GitLab.
     * 
     * Also creates an ACL for GitLab.
     */
    public GitLabAuthorization(String adminUsernames, boolean useGitLabAdmins, Map<String, List<Permission>> grantedJenkinsPermissions) {
        rootACL = new GitLabACL(adminUsernames, useGitLabAdmins, grantedJenkinsPermissions);
    }
    
    /**
     * Returns a string with GitLab usernames who has full admin access in Jenkins.
     * 
     * The usernames are separated by commas.
     * 
     * @return a string with usernames separated by commas
     */
    public String getAdminUsernames() {
        return rootACL.getAdminUsernames();
    }
    
    /**
     * Checks if admins of GitLab also should be admins of Jenkins.
     * 
     * @return true if GitLab admins should be admins of Jenkins, else false
     */
    public boolean getUseGitLabAdmins() {
        return rootACL.getUseGitLabAdmins();
    }
    
    /**
     * Returns a collection of all group names.
     * 
     * Will always return an empty list.
     * 
     * @return a collection of groups
     */
    @Override
    public Collection<String> getGroups() {
        return new ArrayList<String>(0);
    }

    /**
     * Gets the root ACL object.
     * 
     * @return an ACL object
     */
    @Override
    public ACL getRootACL() {
        return rootACL;
    }
    
    /**
     * Checks if the given GitLab role has the given permission.
     * 
     * Mainly used to check if a checkbox should be checked or not in the config file.
     * 
     * @param role the role name
     * @param p the permission
     * @return true if the given role has the given permission
     */
    public boolean isPermissionSet(String role, Permission p) {
        if(rootACL != null && role != null && p != null) {
            return rootACL.isPermissionSet(role, p);
        }
        return false;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<AuthorizationStrategy> {
        
        @Override
        public String getDisplayName() {
            return "GitLab Authorization Strategy";
        }
        
        @Override
        public GitLabAuthorization newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            String adminUsernames = formData.getString("adminUsernames");
            boolean useGitLabAdmins = formData.getBoolean("useGitLabAdmins");

            HashMap<String, List<Permission>> grantedPermissions = new HashMap<String, List<Permission>>();
            
            Map<String, Object> tableData = formData.getJSONObject("permissionTable");
            
            for(Entry<String, Object> rolePermission : tableData.entrySet()) {
                String role = rolePermission.getKey();
                
                Map<String, Object> value = (JSONObject) rolePermission.getValue();
                
                for (Entry<String, Object> valueSet : value.entrySet()) {
                    if ((Boolean) valueSet.getValue()) {
                        if (!grantedPermissions.containsKey(role)) {
                            grantedPermissions.put(role, new ArrayList<Permission>());
                        }
                        
                        grantedPermissions.get(role).add(Permission.fromId(valueSet.getKey()));
                    }
                }
            }
            
            return new GitLabAuthorization(adminUsernames, useGitLabAdmins, grantedPermissions);
        }
        
        /**
         * Returns a map of all permission groups and their permissions except permission groups Item and Permission.
         * 
         * Item permission group is configured separately in each folder.
         * 
         * @return a List of permission groups
         */
        public Map<PermissionGroup, List<Permission>> getAllPermissionGroups() {
            List<PermissionGroup> groups = new ArrayList<PermissionGroup>(PermissionGroup.getAll());
            // Generic permissions, which we don't need
            groups.remove(PermissionGroup.get(Permission.class));
            // We configure this on each folder item
            groups.remove(PermissionGroup.get(Item.class));
            
            // Matrix with all permission groups and the permissions belonging to the permission group
            HashMap<PermissionGroup, List<Permission>> permissionMatrix = new HashMap<PermissionGroup, List<Permission>>();
            
            for (PermissionGroup pg : groups) {
                permissionMatrix.put(pg, new ArrayList<Permission>());
                for (Permission p : pg.getPermissions()) {
                    if (p.enabled) {
                        permissionMatrix.get(pg).add(p);
                    }
                }
            }
            
            return permissionMatrix;
        }
        
        /**
         * Returns a list with all Jenkins roles.
         * 
         * @return a list with all roles
         */
        public List<String> getAllRoles() {
            return new ArrayList<String>(Arrays.asList(GitLabACL.jenkinsAccessLevels));
        }
    }
}