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

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;
import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabFolderACL;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabGrantedPermissions;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.JenkinsAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity.IdentityType;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * Property used for configuring access rights for folders.
 *
 * @author Andreas Alanko
 * @author Emil Nilsson
 */
public class GitLabFolderAuthorization extends FolderProperty<Folder> {
    /** The per-folder ACL. */
    private final GitLabFolderACL folderACL;

    /** The logger for the class. */
    private transient final Logger LOGGER = Logger.getLogger(GitLabFolderAuthorization.class.getName());

    /**
     * Creates a GitLab folder with a group ID.
     *
     * @param groupId
     */
    public GitLabFolderAuthorization(int groupId) {
        // no permissions set
        this(new GitLabFolderACL(groupId));
    }

    /**
     * Creates a GitLab folder property with a folder ACL.
     *
     * @param acl the folder ACL
     */
    private GitLabFolderAuthorization(GitLabFolderACL acl) {
        this.folderACL = acl;
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
        return folderACL.getGroupId();
    }

    /**
     * Gets the group path for this folder.
     *
     * @return the groupPath
     */
    public String getGroupPath() {
        GitLabGroupInfo groupInfo = getGroupInfo();
        return (groupInfo != null) ? groupInfo.getPath() : "<could not fetch group information>";
    }

    /**
     * Gets the group name for this folder.
     *
     * @return the groupName
     */
    public String getGroupName() {
        GitLabGroupInfo groupInfo = getGroupInfo();
        return (groupInfo != null) ? groupInfo.getName() : "<could not fetch group information>";
    }

    /**
     * Gets the group URL of this folder.
     *
     * @return the URL
     */
    public String getGroupUrl() {
        GitLabGroupInfo groupInfo = getGroupInfo();
        return (groupInfo != null) ? GitLab.getUrlForGroup(groupInfo) : "<could not fetch group information>";
    }

    /**
     * Gets the folder name of the associated folder.
     * 
     * @return the folder name
     */
    public String getFolderName() {
        return owner.getName();
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

    @Override
    public GitLabFolderAuthorization reconfigure(StaplerRequest request, JSONObject formData)
            throws Descriptor.FormException {
        // create a new instance from the form data
        GitLabFolderAuthorization newFolderAuth = (GitLabFolderAuthorization)super.reconfigure(request, formData);
        if (newFolderAuth != null) {
            // preserve group ID
            newFolderAuth.folderACL.setGroupId(getGroupId());
        }

        return newFolderAuth;
    }

    /**
     * Gets group information for the GitLab group from the API.
     * 
     * Logger will warn if fetching the group information failed.
     *
     * @return a group info object or null if fetch failed
     */
    private GitLabGroupInfo getGroupInfo() {
        try {
            return GitLab.getGroup(getGroupId());
        } catch (GitLabApiException e) {
            LOGGER.warning("Failed for fetch group with ID " + getGroupId());
        }
        return null;
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
            return "GitLab Folder";
        }

        @Override
        public GitLabFolderAuthorization newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // read permissions from the table in the form
            return new GitLabFolderAuthorization(aclFromPermissionTable(formData.getJSONObject("permissionTable")));
        }

        /**
         * Creates a folder ACL from form data from a table.
         *
         * @param tableData data from the table
         * @return a folder ACL
         */
        private GitLabFolderACL aclFromPermissionTable(Map<String, Object> tableData) {
            GitLabGrantedPermissions grantedPermissions = new GitLabGrantedPermissions();

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

                Map<String, Object> value = (JSONObject)identityPermission.getValue();

                for (Entry<String, Object> valueSet : value.entrySet()) {
                    if ((Boolean)valueSet.getValue() && identity != null) {
                        grantedPermissions.addPermission(identity, Permission.fromId(valueSet.getKey()));
                    }
                }
            }

            return new GitLabFolderACL(grantedPermissions);
        }

        /**
         * Gets the static permission identities, including the GitLab ones.
         *
         * Will be used by the groovy view file when the folder property object hasn't been created yet.
         *
         * @return a list of permission identities
         */
        public List<GitLabPermissionIdentity> getStaticPermissionIdentities() {
            return GitLabPermissionIdentity.getGlobalStaticPermissionIdentities(true);
        }
    }
}
