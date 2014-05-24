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

package com.sonymobile.jenkins.plugins.gitlabauth.folder;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.GroupFolderInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;
import com.sonymobile.jenkins.plugins.gitlabauth.exceptions.ItemNameCollisionException;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * Class for managing GitLab group folders.
 *
 * @author Emil Nilsson
 */
public class GroupFolderManager {
    /**
     * Predicate used to include or exclude groups.
     *
     * If set to null all groups are included.
     */
    private final ManagesGroupPredicate managesGroupPredicate;

    /** The item group for the folders. */
    private final ModifiableTopLevelItemGroup itemGroup;

    /** The descriptor of the folder item. */
    private final TopLevelItemDescriptor folderDescriptor;

    /**
     * Creates a GitLab group folder manager.
     */
    public GroupFolderManager() {
        // include all groups
        this(getJenkinsInstance(), getFolderDescriptor());
    }

    /**
     * Creates a GitLab group folder manager.
     *
     * @param managesGroupPredicate predicate to determine whether to manage a group.
     */
    public GroupFolderManager(ManagesGroupPredicate managesGroupPredicate) {
        this(managesGroupPredicate, getJenkinsInstance(), getFolderDescriptor());
    }

    /**
     * Creates a GitLab group folder manager for customized folder creation.
     *
     * @param itemGroup        the item group
     * @param folderDescriptor the folder descriptor
     */
    /* package */ GroupFolderManager(
            ModifiableTopLevelItemGroup itemGroup,
            TopLevelItemDescriptor folderDescriptor) {
        // include all groups
        this(null, itemGroup, folderDescriptor);
    }

    /**
     * Creates a GitLab group folder manager for customized folder creation.
     *
     * @param managesGroupPredicate predicate to determine whether to manage a group.
     * @param itemGroup             the item group
     * @param folderDescriptor      the folder descriptor
     */
    /* package */ GroupFolderManager(
            ManagesGroupPredicate managesGroupPredicate,
            ModifiableTopLevelItemGroup itemGroup,
            TopLevelItemDescriptor folderDescriptor) {
        this.managesGroupPredicate = managesGroupPredicate;
        this.itemGroup = itemGroup;
        this.folderDescriptor = folderDescriptor;
    }

    /**
     * Returns all included GitLab folders.
     *
     * @return a map of group IDs and folders
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public synchronized Map<Integer, GroupFolderInfo> getFolders() throws GitLabApiException {
        return filterGroupFoldersMap(getAllFolders());
    }

    /**
     * Creates folders for a collection of GitLab groups.
     *
     * Checks if folders already exists before attempting to create a new folder.
     *
     * @param groups the groups
     * @throws GitLabApiException         if the connection against GitLab failed
     * @throws ItemNameCollisionException if an item names for new folders already were in use
     * @throws IOException                if saving to persistent storage failed
     */
    public synchronized void createFolders(Iterable<GitLabGroupInfo> groups)
            throws GitLabApiException, ItemNameCollisionException, IOException {
        createSelectedFolders(filterGroups(groups));
    }

    /**
     * Gets all group folders.
     *
     * @return all group folders
     */
    private Map<Integer, GroupFolderInfo> getAllFolders() {
        Map<Integer, GroupFolderInfo> folders = new TreeMap<Integer, GroupFolderInfo>();
        for (TopLevelItem item : itemGroup.getItems()) {
            // check if the item is a group folder
            GroupFolderInfo groupFolderInfo = GroupFolderInfo.createFromItem(item);
            if (groupFolderInfo != null) {
                folders.put(groupFolderInfo.getGroupId(), groupFolderInfo);
            }
        }
        return folders;
    }

    /**
     * Creates group folders.
     *
     * This is used to create the included folders after filtering.
     *
     * @param groups the folders
     */
    private void createSelectedFolders(Iterable<GitLabGroupInfo> groups)
            throws ItemNameCollisionException, IOException {
        // get existing group folders
        Map<Integer, GroupFolderInfo> existingFolders = getAllFolders();

        // groups which item names collide with existing items
        List<String> collidedGroupPaths = new LinkedList<String>();

        for (GitLabGroupInfo group : groups) {
            try {
                // only create folders that don't already exist
                if (!existingFolders.containsKey(group.getId())) {
                    createFolder(group);
                }
            } catch (ItemNameCollisionException e) {
                collidedGroupPaths.add(group.getPath());
            }
        }

        if (!collidedGroupPaths.isEmpty()) {
            throw new ItemNameCollisionException("Cannot create folder(s) because items with the names "
                    + join(collidedGroupPaths, ", ") + " already exist(s)");
        }
    }

    /**
     * Creates a group folder.
     *
     * Does not check if a folder already exists for the GitLab group.
     *
     * @param group the group
     * @throws ItemNameCollisionException if an item name for a new folder already was in use
     * @throws IOException                if saving to persistent storage failed
     */
    private void createFolder(GitLabGroupInfo group)
            throws ItemNameCollisionException, IOException {
        try {
            Folder folder = (Folder)itemGroup.createProject(folderDescriptor, group.getPath(), true);
            folder.addProperty(new GitLabFolderAuthorization(group.getId()));
        } catch (IllegalArgumentException e) {
            throw new ItemNameCollisionException("Cannot create folder because an item with the name "
                    + group.getPath() + " already exists");
        }
    }

    /**
     * Filters groups excluded by the predicate.
     *
     * @param groups the groups
     * @return the groups
     * @throws GitLabApiException if the connection against GitLab failed
     */
    private Iterable<GitLabGroupInfo> filterGroups(Iterable<GitLabGroupInfo> groups)
            throws GitLabApiException {
        Iterator<GitLabGroupInfo> iterator = groups.iterator();

        while (iterator.hasNext()) {
            if (!managesGroup(iterator.next())) { iterator.remove(); }
        }
        return groups;
    }

    /**
     * Filters group folders excluded by the predicate.
     *
     * @param groupFolders the group folders
     * @return the group folders
     * @throws GitLabApiException if the connection against GitLab failed
     */
    private Map<Integer, GroupFolderInfo> filterGroupFoldersMap(Map<Integer, GroupFolderInfo> groupFolders)
            throws GitLabApiException {
        Iterator<GroupFolderInfo> iterator = groupFolders.values().iterator();

        while (iterator.hasNext()) {
            if (!managesGroupFolder(iterator.next())) { iterator.remove(); }
        }
        return groupFolders;
    }

    /**
     * Checks whether a group folder should be included.
     *
     * @param group the group
     * @return true if the group should be included
     */
    private boolean managesGroup(GitLabGroupInfo group) throws GitLabApiException {
        return managesGroupPredicate == null || managesGroupPredicate.shouldManageGroup(group);
    }

    /**
     * Checks whether a group folder should be included.
     *
     * @param groupFolder the group folder
     * @return true if the group folder should be included
     */
    private boolean managesGroupFolder(GroupFolderInfo groupFolder) throws GitLabApiException {
        GitLabGroupInfo group = groupFolder.getGroup();
        return managesGroupPredicate == null || (group != null && managesGroupPredicate.shouldManageGroup(group));
    }


    /**
     * Gets the Jenkins instance
     *
     * @return the Jenkins instance
     */
    private static Jenkins getJenkinsInstance() {
        return Jenkins.getInstance();
    }

    /**
     * Gets the descriptor of {@link com.cloudbees.hudson.plugins.folder.Folder}
     *
     * @return the descriptor or null if Jenkins can't be accessed
     */
    private static TopLevelItemDescriptor getFolderDescriptor() {
        Jenkins jenkins = getJenkinsInstance();

        return jenkins != null ? jenkins.getDescriptorByType(Folder.DescriptorImpl.class) : null;
    }

    /**
     * Predicate for determining whether to include a certain group.
     */
    public static interface ManagesGroupPredicate {
        /**
         * Checks whether a group should be included by the folder manager.
         *
         * @param group the group
         * @return true if the group should be included
         * @throws GitLabApiException
         */
        public boolean shouldManageGroup(GitLabGroupInfo group) throws GitLabApiException;
    }
}
