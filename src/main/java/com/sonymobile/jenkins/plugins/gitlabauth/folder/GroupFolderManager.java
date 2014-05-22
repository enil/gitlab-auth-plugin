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
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.GroupFolderInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;
import com.sonymobile.jenkins.plugins.gitlabauth.exceptions.ItemNameCollisionException;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * Singleton class for managing GitLab group folders.
 *
 * @author Emil Nilsson
 */
public class GroupFolderManager {
    /** The singleton instance. */
    private static final GroupFolderManager INSTANCE = new GroupFolderManager();

    /** The item group for the folders. */
    private final ModifiableTopLevelItemGroup itemGroup;

    /** The descriptor of the folder item. */
    private final TopLevelItemDescriptor folderDescriptor;

    /**
     * Creates a GitLab group folder manager.
     */
    private GroupFolderManager() {
        this(getJenkinsInstance(), getFolderDescriptor());
    }

    /**
     * Creates a GitLab group folder manager with a custom item group and folder descriptor.
     *
     * @param itemGroup        the item group
     * @param folderDescriptor the folder descriptor
     */
    private GroupFolderManager(ModifiableTopLevelItemGroup itemGroup, TopLevelItemDescriptor folderDescriptor) {
        this.itemGroup = itemGroup;
        this.folderDescriptor = folderDescriptor;
    }

    /**
     * Returns the singleton instance of the class.
     *
     * @return the singleton instance
     */
    public GroupFolderManager getInstance() {
        return INSTANCE;
    }

    /**
     * Returns all GitLab folders.
     *
     * @return a map of group IDs and folders
     */
    public synchronized Map<Integer, GroupFolderInfo> getFolders() {
        Map<Integer, GroupFolderInfo> existingFolders = new TreeMap<Integer, GroupFolderInfo>();

        for (final TopLevelItem item : itemGroup.getItems()) {
            if (item instanceof Folder) {
                Folder folder = (Folder)item;
                GitLabFolderAuthorization property = folder.getProperties().get(GitLabFolderAuthorization.class);

                // make sure the GitLab authorization property is set folder
                if (property != null) {
                    existingFolders.put(property.getGroupId(), new GroupFolderInfo(property));
                }
            }
        }

        return existingFolders;
    }

    /**
     * Creates folders for a collection of GitLab groups.
     *
     * Checks if folders already exists before attempting to create a new folder.
     *
     * @param groups the groups
     * @throws ItemNameCollisionException if an item names for new folders already were in use
     * @throws IOException                if saving to persistent storage failed
     */
    public synchronized void createFolders(Iterable<GitLabGroupInfo> groups)
            throws ItemNameCollisionException, IOException {
        // get existing group folders
        Map<Integer, GroupFolderInfo> existingFolders = getFolders();

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
     * Checks if a folder for a GitLab group exists.
     *
     * @param group the group
     * @return true if the folder exists
     */
    public synchronized boolean folderExists(GitLabGroupInfo group) {
        int groupId = group.getId();

        for (final TopLevelItem item : itemGroup.getItems()) {
            if (item instanceof Folder) {
                Folder folder = (Folder)item;
                GitLabFolderAuthorization property = folder.getProperties().get(GitLabFolderAuthorization.class);

                if (property != null && property.getGroupId() == groupId) {
                    // folder matches the group
                    return true;
                }
            }
        }

        // not found
        return false;
    }

    /**
     * Creates a folder for a GitLab group.
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
}
