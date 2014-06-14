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

package com.sonymobile.jenkins.plugins.gitlab.gitlabauth.folder;

import hudson.model.Item;
import hudson.model.TopLevelItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.acegisecurity.Authentication;

import jenkins.model.Jenkins;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.authorization.GitLabFolderAuthorization;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.exceptions.ItemNameCollisionException;

/**
 * Handles manual creation of group folders by users.
 * 
 * @author Andreas Alanko
 */
public class UserCreatedGroupFolder {
    /**
     * Gets a list of {@link GroupFolderInfo} for GitLab groups that does have an 
     * associated folder created.
     * 
     * @param auth the authentication object
     * @return a list of {@link GroupFolderInfo}
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static List<GroupFolderInfo> getExistingFolders(Authentication auth) throws GitLabApiException {
        List<GroupFolderInfo> groups = new ArrayList<GroupFolderInfo>();
        
        for (TopLevelItem item : Jenkins.getInstance().getItems()) {
            if (item instanceof Folder) {
                GitLabFolderAuthorization property = GitLabFolderAuthorization.getFolderProperty((Folder) item);
                
                if (property != null && property.getACL().hasPermission(auth, Item.READ)) {
                    groups.add(new GroupFolderInfo(property));
                }
            }
        }
        return groups;
    }
    
    /**
     * Gets a list of {@link GroupFolderInfo} for GitLab groups that does not
     * have an associated folder already created.
     * 
     * @param userId the user ID
     * @return a list of {@link GroupFolderInfo}
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static List<GroupFolderInfo> getNonExistingFolders(int userId) throws GitLabApiException {
        List<GitLabGroupInfo> userGroups = GitLab.getGroupsAsUser(userId);
        List<GroupFolderInfo> groups = new ArrayList<GroupFolderInfo>();
        
        for (GitLabGroupInfo group : userGroups) {
            if (GitLab.isGroupOwner(userId, group.getId())) {
                if (!folderExists(group)) {
                    groups.add(new GroupFolderInfo(group));
                }
            }
        }
        return groups;
    }
    
    /**
     * Checks if a folder can be created for the given group.
     * 
     * @param group the group
     * @return true if a folder can be created
     */
    private static boolean folderExists(GitLabGroupInfo group) {
        for (TopLevelItem item : Jenkins.getInstance().getItems()) {
            GroupFolderInfo folderInfo = GroupFolderInfo.createFromItem(item);
            
            // if not null, folderInfo is a folder
            if (folderInfo != null) {
                if (folderInfo.getGroupId() == group.getId()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Creates folders for the given list of GitLab groups.
     * 
     * @param groups the groups
     * @param userId the user ID
     * @throws GitLabApiException         if the connection against GitLab failed
     * @throws ItemNameCollisionException if an item names for new folders already were in use
     * @throws IOException                if saving to persistent storage failed
     */
    public static void createFolders(List<GitLabGroupInfo> groups, int userId) throws GitLabApiException, ItemNameCollisionException, IOException {
        new GroupFolderManager(new UserCreatedGroupFolderSynchronization(userId)).createFolders(groups);
    }
    
    private static class UserCreatedGroupFolderSynchronization extends GroupFolderSynchronizer {
        /** The user ID associated with this class. */
        private final int userId;
        
        /** The logger for this class. */
        private transient final Logger LOGGER = Logger.getLogger(UserCreatedGroupFolderSynchronization.class.getName());
        
        /**
         * Creates a synchronization strategy for manually user created folders.
         * 
         * @param userId the user ID
         */
        public UserCreatedGroupFolderSynchronization(int userId) {
            this.userId = userId;
        }

        /**
         * @see {@link GroupFolderSynchronizer.shouldManageGroup}
         */
        public boolean shouldManageGroup(GitLabGroupInfo group) {
            try {
                // Only owners should be able to create a new group
                if (GitLab.getAccessLevelInGroup(userId, group.getId()) == GitLabAccessLevel.OWNER) {
                    return true;
                }
            } catch (GitLabApiException e) {
                LOGGER.warning(e.getMessage());
            }
            return false;
        }
    }
}
