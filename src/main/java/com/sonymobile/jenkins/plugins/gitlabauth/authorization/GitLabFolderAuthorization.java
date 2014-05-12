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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;
import com.cloudbees.hudson.plugins.folder.Folder;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.JenkinsAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabFolderACL;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabGrantedPermissions;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity.IdentityType;

import hudson.Extension;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.security.PermissionGroup;

/**
 * Property used for configuring access rights for folders.
 * 
 * @author Andreas Alanko
 */
public class GitLabFolderAuthorization extends FolderProperty<Folder> {
    private GitLabFolderACL folderACL;
    private int groupId;
    
    public GitLabFolderAuthorization(GitLabGrantedPermissions grantedPermissions) {
        this.folderACL = new GitLabFolderACL(grantedPermissions);
    }
    
    /**
     * Gets the ACL belonging to this folder.
     * 
     * @return an ACL
     */
    public ACL getACL() {
        return folderACL;
    }
    
    /**
     * Gets the group id for this folder.
     * 
     * @return the groupId
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Sets the group id of this folder.
     * 
     * @param groupId the groupId to set
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
        folderACL.setGroupId(groupId);
    }

    /**
     * Gets the group path for this folder.
     * 
     * @return the groupPath
     */
    public String getGroupPath() {
        //TODO: Should get group info from GitLab instance.
        return "";
    }

    /**
     * Gets the group name for this folder.
     * 
     * @return the groupName
     */
    public String getGroupName() {
        //TODO: Should get group info from GitLab instance.
        return "";
    }

    /**
     * Checks if the given GitLab identity has the given permission.
     * 
     * Mainly used to check if a checkbox should be checked or not on the config page.
     * 
     * @param identity   the identity
     * @param permission the permission
     * @return true if the given identity has the given permission
     */
    public boolean isPermissionSet(GitLabPermissionIdentity identity, Permission permission) {
        return folderACL.isPermissionSet(identity, permission);
    }
    
    public List<GitLabPermissionIdentity> getFolderPermissionIdentities() {
        return folderACL.getPermissionIdentities(true);
    }
    
    @Extension
    public static class DescriptorImpl extends FolderPropertyDescriptor {
        
        @Override
        public boolean isApplicable(Class<? extends Folder> containerType) {
            try {
                return Jenkins.getInstance().getAuthorizationStrategy() instanceof GitLabAuthorization;
            } catch (NoClassDefFoundError e) {
                return false;
            }
        }
        
        @Override
        public String getDisplayName() {
            return "GitLab Folder Authorization";
        }
        
        @Override
        public GitLabFolderAuthorization newInstance(StaplerRequest req, JSONObject formData) throws FormException {
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
            return new GitLabFolderAuthorization(grantedPermissions);
        }
        
        /**
         * Returns the Permission Group belonging to the Item class.
         * 
         * @return a permission group
         */
        public PermissionGroup getItemPermissionGroup() {
            return PermissionGroup.get(Item.class);
        }
        
        /**
         * Gets the static permission identities, including the GitLab ones.
         * 
         * Will be used by the groovy view file when the folder property object
         * hasn't been created yet.
         * 
         * @return a list of permission identities
         */
        public List<GitLabPermissionIdentity> getStaticPermissionIdentities() {
            return GitLabPermissionIdentity.getGlobalStaticPermissionIdentities(true);
        }
    }
}