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

package com.sonymobile.jenkins.plugins.gitlabauth.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabAbstractACL;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.JenkinsAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabGlobalACL;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabGrantedPermissions;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity.IdentityType;

import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Descriptor;
import hudson.model.Job;
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
    private final GitLabGlobalACL rootACL;

    /**
     * Creates an Authorization Strategy for GitLab.
     * 
     * @param adminUsernames     the admin usernames seperated by a comma
     * @param adminGroups        the admin groups seperated by a comma
     * @param useGitLabAdmins    if GitLab admins should be Jenkins admins
     * @param grantedPermissions map of all Jenkins roles and their respective granted permissions
     */
    public GitLabAuthorization(String adminUsernames, String adminGroups, boolean useGitLabAdmins,
            GitLabGrantedPermissions grantedPermissions) {
        rootACL = new GitLabGlobalACL(adminUsernames, adminGroups, useGitLabAdmins, grantedPermissions);
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
     * Returns a string with GitLab group names who has full admin access in Jenkins.
     * 
     * The group names are separated by commas.
     * 
     * @return a string with group names separated by commas
     */
    public String getAdminGroups() {
        return rootACL.getAdminGroups();
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
    
    @Override
    public ACL getACL(Job<?,?> project) {
        if(project.getParent() instanceof Folder) {
            return getACL((Folder) project.getParent());
        }
        return getRootACL();
    }
    
    /**
     * Gets the ACL for the given folder.
     * 
     * @param folder the folder
     * @return an ACL
     */
    public ACL getACL(Folder folder) {
        GitLabFolderAuthorization folderAuth = folder.getProperties().get(GitLabFolderAuthorization.class);
        
        if (folder.getParent() instanceof Folder) {
            return getACL((Folder) folder.getParent());
        }
        
        if(folderAuth != null) {
            // groupId is 0 if its not a GitLab folder.
            if (folderAuth.getGroupId() != 0) {
                return folderAuth.getACL();
            }
        }
        return getRootACL();
    }
    
    /**
     * Tries to get the ACL of a folder.
     * Otherwise it will return the root ACL of the Authorization Strategy.
     * 
     * @param item the item
     * @return an ACL
     */
    @Override
    public ACL getACL(AbstractItem item) {
        if (item instanceof Folder) {
            return getACL((Folder) item);
        }
        return getRootACL();
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
            String adminGroups = formData.getString("adminGroups");
            boolean useGitLabAdmins = formData.getBoolean("useGitLabAdmins");
            
            GitLabGrantedPermissions grantedPermissions = new GitLabGrantedPermissions();
            
            Map<String, Object> tableData = formData.getJSONObject("permissionTable");
            
            for (Entry<String, Object> identityPermission : tableData.entrySet()) {
                GitLabPermissionIdentity identity = null;
                String[] identityValue = identityPermission.getKey().split(":");
                
                if (identityValue.length == 2) {
                    IdentityType type = IdentityType.valueOf(identityValue[0]);
                    String id = identityValue[1];
                                        
                    switch (type) {
                    case GITLAB:
                        identity = GitLabPermissionIdentity.getGitLabIdentityFromAccessLevel(
                                GitLabAccessLevel.getAccessLevelWithName(id));
                        break;
                    case JENKINS:
                        identity = GitLabPermissionIdentity.getJenkinsIdentityFromAccessLevel(
                                JenkinsAccessLevel.getAccessLevelWithName(id));
                        break;
                    case GROUP:
                        identity = GitLabPermissionIdentity.group(id);
                        break;
                    case USER:
                        identity = GitLabPermissionIdentity.user(id);
                        break;
                    }
                }
                
                Map<String, Object> value = (JSONObject) identityPermission.getValue();
                
                for (Entry<String, Object> valueSet : value.entrySet()) {
                    if ((Boolean) valueSet.getValue() && identity != null) {
                        grantedPermissions.addPermission(identity, Permission.fromId(valueSet.getKey()));
                    }
                }
            }
            
            return new GitLabAuthorization(adminUsernames, adminGroups, useGitLabAdmins, grantedPermissions);
        }

        /**
         * Gets a list of all permission identities configured to be used globally by Jenkins.
         * 
         * @return a list of permission identities
         */
        public List<GitLabPermissionIdentity> getPermissionIdentities() {
            AuthorizationStrategy strategy = Jenkins.getInstance().getAuthorizationStrategy();
            if (strategy instanceof GitLabAuthorization) {
                GitLabAbstractACL acl = (GitLabAbstractACL) strategy.getRootACL();
                return acl.getPermissionIdentities(false);
            }
            return GitLabPermissionIdentity.getGlobalStaticPermissionIdentities(false);
        }
    }
}