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

import com.cloudbees.hudson.plugins.folder.Folder;
import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;

import hudson.model.TopLevelItem;

/**
 * Info holder class used to display information about a folder and the associated group.
 * 
 * @author Andreas Alanko
 */
public class GroupFolderInfo {
    /** Folder property associated with this object. */
    private final GitLabFolderAuthorization folderProperty;
    
    /** GitLab group associated with this object. */
    private final GitLabGroupInfo group;
    
    /**
     * Creates an info object for the provided folder property.
     * 
     * @param folderProperty the folder property
     */
    public GroupFolderInfo(GitLabFolderAuthorization folderProperty) {
        this.folderProperty = folderProperty;
        this.group = null;
    }
    
    public GroupFolderInfo(GitLabGroupInfo group) {
        this.folderProperty = null;
        this.group = group;
    }

    /**
     * Create an group folder info object from an item.
     * 
     * @param item the top level item
     * @return a group folder info object or null if the item isn't a group folder
     */
    public static GroupFolderInfo createFromItem(TopLevelItem item) {
        if (item instanceof Folder) {
            GitLabFolderAuthorization property = ((Folder)item).getProperties().get(GitLabFolderAuthorization.class);
            if (property != null) {
                return new GroupFolderInfo(property);
            }
        }
        // not a group folder
        return null;
    }

    /**
     * Gets the group info object.
     *
     * @return the group info object
     */
    public GitLabGroupInfo getGroup() throws GitLabApiException {
        if (group == null) {
            return folderProperty.getGroup();
        }
        return group;
    }

    /**
     * Gets the group ID.
     * 
     * @return the group ID
     */
    public int getGroupId() {
        if (group == null) {
            return folderProperty.getGroupId();
        }
        return group.getId();
    }
    
    /**
     * Gets the group name.
     * 
     * @return the group name
     */
    public String getGroupName() {
        if (group == null) {
            return folderProperty.getGroupName();
        }
        return group.getName();
    }
    
    /**
     * Gets the folder name.
     * 
     * @return the folder name
     */
    public String getFolderName() {
        if (group == null) {
            return folderProperty.getFolderName();
        }
        // Might need to return something else.
        return group.getName();
    }
    
    /**
     * Gets the group path.
     * 
     * @return the group path
     */
    public String getGroupPath() {
        if (group == null) {
            return folderProperty.getGroupPath();
        }
        return group.getPath();
    }
    
    /**
     * Gets the group URL.
     *
     * @return the group URL
     */
    public String getGroupUrl() {
        if (group == null) {
            return folderProperty.getGroupUrl();
        }
        return GitLab.getUrlForGroup(group);
    }
    
    /**
     * Gets the GitLab access level for a user ID in this GitLab group.
     * 
     * Will never return null.
     * 
     * @param userId the user ID
     * @return the GitLab access level
     */
    public GitLabAccessLevel getAccessLevelForUser(int userId) {
        try {
            return GitLab.getAccessLevelInGroup(userId, getGroupId());
        } catch (GitLabApiException e) {
            return GitLabAccessLevel.NONE;
        }
    }
}
