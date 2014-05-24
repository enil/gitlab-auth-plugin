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
import com.google.common.base.Predicate;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.filterValues;
import static com.sonymobile.jenkins.plugins.gitlabauth.utils.Predicates.truePredicate;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * Class for managing GitLab group folders.
 *
 * @author Emil Nilsson
 */
public class GroupFolderManager {
    /** Predicate used to determine whether a group should be managed by the manager. */
    private final Predicate<GitLabGroupInfo> managesGroupPredicate;

    /** Predicate used to determine whether a group folder should be managed by the manager. */
    private final Predicate<GroupFolderInfo> managesGroupFolderPredicate;

    /** The item group for the folders. */
    private final ModifiableTopLevelItemGroup itemGroup;

    /** The descriptor of the folder item. */
    private final TopLevelItemDescriptor folderDescriptor;

    /**
     * Creates a GitLab group folder manager.
     */
    public GroupFolderManager() {
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
    private GroupFolderManager(
            ModifiableTopLevelItemGroup itemGroup,
            TopLevelItemDescriptor folderDescriptor) {
        /* include all groups */
        managesGroupPredicate = truePredicate();
        managesGroupFolderPredicate = truePredicate();
        this.itemGroup = itemGroup;
        this.folderDescriptor = folderDescriptor;
    }

    /**
     * Creates a GitLab group folder manager for customized folder creation.
     *
     * @param managesGroupPredicate predicate to determine whether to manage a group.
     * @param itemGroup             the item group
     * @param folderDescriptor      the folder descriptor
     */
    private GroupFolderManager(
            ManagesGroupPredicate managesGroupPredicate,
            ModifiableTopLevelItemGroup itemGroup,
            TopLevelItemDescriptor folderDescriptor) {
        // convert into regular predicates
        this.managesGroupPredicate = convertManagesGroupPredicate(managesGroupPredicate);
        managesGroupFolderPredicate = convertManagesGroupFolderPredicate(managesGroupPredicate);
        this.itemGroup = itemGroup;
        this.folderDescriptor = folderDescriptor;
    }

    /**
     * Returns all managed GitLab folders.
     *
     * @return a map of group IDs and folders
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public synchronized Map<Integer, GroupFolderInfo> getFolders() throws GitLabApiException {
        try {
            return filterValues(getAllFolders(), managesGroupFolderPredicate);
        } catch (WrappedGitLabApiException e) {
            // unwrap exception
            throw e.getCause();
        }
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
        try {
            createSelectedFolders(filter(groups, managesGroupPredicate));
        } catch (WrappedGitLabApiException e) {
            // unwrap exception
            throw e.getCause();
        }
    }

    /**
     * Gets all group folders including filtered folders.
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
     * This is used to actually create the folders were filtered using the predicate.
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
     * Converts a manages group-predicate to a regular predicate.
     *
     * This is done by wrapping the checked exception in a runtime exception.
     *
     * @param predicate the manages group-predicate
     * @return a regular predicate
     */
    private static Predicate<GitLabGroupInfo> convertManagesGroupPredicate(final ManagesGroupPredicate predicate) {
        return new Predicate<GitLabGroupInfo>() {
            public boolean apply(GitLabGroupInfo group) {
                try {
                    return predicate.shouldManageGroup(group);
                } catch (GitLabApiException e) {
                    throw new WrappedGitLabApiException(e);
                }
            }
        };
    }

    /**
     * Converts a manages group-predicate to a regular predicate for group folders.
     *
     * This is done by wrapping the checked exception in a runtime exception.
     *
     * @param predicate the manages group-predicate
     * @return a regular predicate
     */
    private static Predicate<GroupFolderInfo> convertManagesGroupFolderPredicate(
            final ManagesGroupPredicate predicate) {
        return new Predicate<GroupFolderInfo>() {
            public boolean apply(GroupFolderInfo groupFolder) {
                try {
                    return predicate.shouldManageGroup(groupFolder.getGroup());
                } catch (GitLabApiException e) {
                    throw new WrappedGitLabApiException(e);
                }
            }
        };
    }

    /**
     * Predicate for determining whether to manage a certain group.
     */
    public static interface ManagesGroupPredicate {
        /**
         * Checks whether a group should be managed by the folder manager.
         *
         * @param group the group
         * @return true if the group should be managed
         * @throws GitLabApiException
         */
        public boolean shouldManageGroup(GitLabGroupInfo group) throws GitLabApiException;
    }

    /**
     * Runtime exception wrapper for {@link GitLabApiException}.
     */
    private static class WrappedGitLabApiException extends RuntimeException {
        public WrappedGitLabApiException(GitLabApiException cause) {
            super(cause);
        }

        @Override
        public synchronized GitLabApiException getCause() {
            return (GitLabApiException)super.getCause();
        }
    }
}
