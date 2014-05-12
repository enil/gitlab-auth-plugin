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
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;
import com.sonymobile.jenkins.plugins.gitlabauth.exceptions.ItemNameCollisionException;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.ModifiableTopLevelItemGroup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A class for creating
 *
 * @author Emil Nilsson
 */
public class GitLabFolderCreator implements GitLabFolderSynchronizer.FolderCreator {
    /** The target item group to create folders in. */
    private final ModifiableTopLevelItemGroup itemGroup;

    /** The descriptor of the Folder item type. */
    private final TopLevelItemDescriptor folderDescriptor;

    /**
     * Create a folder creator for an item group.
     *
     * @param itemGroup        the item group
     * @param folderDescriptor the descriptor used to create folders
     */
    public GitLabFolderCreator(ModifiableTopLevelItemGroup itemGroup, TopLevelItemDescriptor folderDescriptor) {
        this.itemGroup = itemGroup;
        this.folderDescriptor = folderDescriptor;
    }

    public Folder createOrGetGitLabGroupFolder(GitLabGroupInfo group, Map<Integer, Folder> existingFolders)
            throws ItemNameCollisionException, IOException {
        Folder folder;

        // check if folder needs to be created
        if ((folder = existingFolders.get(group.getId())) == null) {
            folder = createGitLabGroupFolder(group);
        }

        return folder;
    }

    public Map<Integer, Folder> getExistingGitLabGroupFolders() {
        Map<Integer, Folder> existingFolders = new HashMap<Integer, Folder>();

        for (final TopLevelItem item : itemGroup.getItems()) {
            if (item instanceof Folder) {
                Folder folder = (Folder)item;
                GitLabFolderAuthorization property = folder.getProperties().get(GitLabFolderAuthorization.class);

                // make sure the GitLab authorization property is set folder
                if (property != null) {
                    existingFolders.put(property.getGroupId(), folder);
                }
            }
        }

        return existingFolders;
    }

    /**
     * Creates a new folder for a GitLab group.
     *
     * @param group the GitLab group
     * @return the folder
     * @throws ItemNameCollisionException if an item with the name already existed
     * @throws IOException                if saving to persistent storage failed
     */
    private Folder createGitLabGroupFolder(GitLabGroupInfo group) throws ItemNameCollisionException, IOException {
        try {
            Folder folder = (Folder)itemGroup.createProject(folderDescriptor, group.getPath(), true);
            folder.addProperty(new GitLabFolderAuthorization(group.getId()));

            return folder;
        } catch (IllegalArgumentException e) {
            throw new ItemNameCollisionException("Cannot create folder because an item with the name "
                    + group.getPath() + " already exists");
        }
    }
}
