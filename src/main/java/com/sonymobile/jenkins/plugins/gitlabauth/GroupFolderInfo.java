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

import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;

/**
 * Info holder class used to display information about a folder and the associated group.
 * 
 * @author Andreas Alanko
 */
public class GroupFolderInfo {
    /** Folder property associated with this object. */
    private final GitLabFolderAuthorization folderProperty;
    
    /**
     * Creates an info object for the provided folder property.
     * 
     * @param folderProperty the folder property
     */
    public GroupFolderInfo(GitLabFolderAuthorization folderProperty) {
        this.folderProperty = folderProperty;
    }

    /**
     * Gets the group id.
     * 
     * @return the group id
     */
    public int getGroupId() {
        return folderProperty.getGroupId();
    }
    
    /**
     * Gets the group name.
     * 
     * @return the group name
     */
    public String getGroupName() {
        return folderProperty.getGroupName();
    }
    
    /**
     * Gets the folder name.
     * 
     * @return the folder name
     */
    public String getFolderName() {
        return folderProperty.getFolderName();
    }
    
    /**
     * Gets the group path.
     * 
     * @return the group path
     */
    public String getGroupPath() {
        return folderProperty.getGroupPath();
    }
    
    /**
     * Gets the group url.
     * 
     * Return "N/A" if a group url couldn't be found.
     * 
     * @return
     */
    public String getGroupUrl() {
        GitLabGroupInfo group = null;
        try {
            group = GitLab.getGroup(getGroupId());
            
        } catch (GitLabApiException e) {}
        
        if (group != null) {
            //return group.getUrl();
        }
        return "N/A";
    }
    
    /**
     * Gets the GitLab access level for a user id in this GitLab group.
     * 
     * @param userId the user id
     * @return the GitLab access level
     */
    public GitLabAccessLevel getAccessLevelForUser(int userId) {
        GitLabAccessLevel accessLevel = null;
        try {
            accessLevel = GitLab.getAccessLevelInGroup(userId, getGroupId());
        } catch (GitLabApiException e) {}
        
        if (accessLevel != null) {
            return accessLevel;
        }
        return GitLabAccessLevel.NONE;
    }
}
