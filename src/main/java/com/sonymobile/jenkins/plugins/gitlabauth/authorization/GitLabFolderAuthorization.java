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
import com.sonymobile.jenkins.plugins.gitlabauth.JenkinsAccessLevels;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabFolderACL;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static java.util.Collections.EMPTY_MAP;

/**
 * Property used for configuring access rights for folders.
 *
 * @author Andreas Alanko
 * @author Emil Nilsson
 */
public class GitLabFolderAuthorization extends FolderProperty<Folder> {
    /** The per-folder ACL. */
    private final GitLabFolderACL folderACL;

    /** The group ID of the GitLab group for the folder. */
    private int groupId;

    /** The logger for the class. */
    private transient final Logger LOGGER = Logger.getLogger(GitLabFolderAuthorization.class.getName());

    /**
     * Creates a GitLab folder with a group ID.
     *
     * @param groupId
     */
    public GitLabFolderAuthorization(int groupId) {
        // no permissions set
        this(new GitLabFolderACL(EMPTY_MAP));

        this.groupId = groupId;
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
        return groupId;
    }

    /**
     * Gets the group path for this folder.
     *
     * @return the groupPath
     */
    public String getGroupPath() {
        try {
            return getGroupInfo().getPath();
        } catch (GitLabApiException e) {
            return "<could not fetch group information>";
        }
    }

    /**
     * Gets the group name for this folder.
     *
     * @return the groupName
     */
    public String getGroupName() {
        try {
            return getGroupInfo().getName();
        } catch (GitLabApiException e) {
            return "<could not fetch group information>";
        }
    }

    /**
     * Checks if the given GitLab role has the given permission.
     *
     * Mainly used to check if a checkbox should be checked or not on the config page.
     *
     * @param role       the role name
     * @param permission the permission
     * @return true if the given role has the given permission
     */
    public boolean isPermissionSet(String role, Permission permission) {
        return folderACL != null && folderACL.isPermissionSet(role, permission);
    }

    @Override
    public GitLabFolderAuthorization reconfigure(StaplerRequest request, JSONObject formData)
            throws Descriptor.FormException {
        // create a new instance from the form data
        GitLabFolderAuthorization newInstance = (GitLabFolderAuthorization)super.reconfigure(request, formData);
        if (newInstance != null) {
            // preserve group ID
            newInstance.groupId = groupId;
        }

        return newInstance;
    }

    /**
     * Gets group information for the GitLab group from the API.
     *
     * Logger will warn if fetching the group information failed.
     *
     * @return a group info object
     * @throws GitLabApiException if fetching the group failed
     */
    private GitLabGroupInfo getGroupInfo() throws GitLabApiException {
        try {
            return GitLab.getGroup(groupId);
        } catch (GitLabApiException e) {
            LOGGER.warning("Failed for fetch group with ID " + groupId);
            throw e;
        }
    }

    @Extension
    public static class DescriptorImpl extends FolderPropertyDescriptor {
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
            // the permissions set in the
            HashMap<String, List<Permission>> permissions = new HashMap<String, List<Permission>>();

            for (Entry<String, Object> rolePermission : tableData.entrySet()) {
                String role = rolePermission.getKey();

                Map<String, Object> value = (JSONObject)rolePermission.getValue();

                for (Entry<String, Object> valueSet : value.entrySet()) {
                    if ((Boolean)valueSet.getValue()) {
                        if (!permissions.containsKey(role)) {
                            permissions.put(role, new ArrayList<Permission>());
                        }

                        permissions.get(role).add(Permission.fromId(valueSet.getKey()));
                    }
                }
            }

            return new GitLabFolderACL(permissions);
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
         * Returns a list with all roles.
         *
         * @return a list with all roles
         */
        public List<String> getAllRoles() {
            List<String> allRoles = new ArrayList<String>(Arrays.asList(GitLabAccessLevel.all));
            allRoles.addAll(Arrays.asList(JenkinsAccessLevels.all));

            return allRoles;
        }

        @Override
        public boolean isApplicable(Class<? extends Folder> containerType) {
            try {
                // don't show unless GitLab authorization is used
                return Jenkins.getInstance().getAuthorizationStrategy() instanceof GitLabAuthorization;
            } catch (NoClassDefFoundError e) {
                return false;
            }
        }
    }
}
