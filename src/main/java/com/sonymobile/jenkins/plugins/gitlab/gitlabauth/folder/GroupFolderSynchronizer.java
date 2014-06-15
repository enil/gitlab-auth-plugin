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

import com.cloudbees.hudson.plugins.folder.Folder;
import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.exceptions.ItemNameCollisionException;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A generic synchronizer for GitLab group folders.
 *
 * @author Emil Nilsson
 */
public abstract class GroupFolderSynchronizer implements GroupFolderManager.ManagesGroupPredicate {
    /** The folder manager used to fetch and create group folders. */
    protected final GroupFolderManager folderManager;

    /** The logger for the class. */
    protected final Logger LOGGER = Logger.getLogger(GroupFolderSynchronizer.class.getName());

    /**
     * Creates a group folder synchronizer.
     */
    protected GroupFolderSynchronizer() {
        this.folderManager = new GroupFolderManager(this);
    }


    /**
     * Creates a group folder synchronizer with a custom item group and folder descriptor.
     *
     * This is used by tests not using an actual Jenkins item group and folder descriptor.
     *
     * @param itemGroup        the item group to create folders in
     * @param folderDescriptor the descriptor for folder items
     */
    protected GroupFolderSynchronizer(ModifiableTopLevelItemGroup itemGroup, TopLevelItemDescriptor folderDescriptor) {
        this.folderManager = new GroupFolderManager(this, itemGroup, folderDescriptor);
    }

    /**
     * Synchronizes a collection group folders.
     *
     * Should be used by a method in concrete subclasses to perform the actual synchronization.
     *
     * @param groups the groups
     * @throws GitLabApiException if the connection against GitLab failed
     */
    protected void synchronizeGroupFolders(Collection<GitLabGroupInfo> groups) throws GitLabApiException {
        try {
            folderManager.createFolders(groups);
        } catch (ItemNameCollisionException e) {
            LOGGER.warning(e.getMessage());
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * Checks whether a group should be included by the folder manager.
     *
     * Can be overridden in concrete subclasses to limit what groups should exposed.
     *
     * @param group the group
     * @return true if the group should be included
     * @throws GitLabApiException
     */
    public boolean shouldManageGroup(GitLabGroupInfo group) throws GitLabApiException {
        // include all groups
        return true;
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
     * Gets the descriptor of {@link Folder}
     *
     * @return the descriptor
     */
    private static TopLevelItemDescriptor getFolderDescriptor() {
        return getJenkinsInstance().getDescriptorByType(Folder.DescriptorImpl.class);
    }
}
