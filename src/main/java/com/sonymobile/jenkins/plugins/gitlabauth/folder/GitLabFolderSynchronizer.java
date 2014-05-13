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
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlabauth.exceptions.ItemNameCollisionException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Class handling synchronization of folders for GitLab groups.
 *
 * @author Emil Nilsson
 */
public class GitLabFolderSynchronizer {
    /** The folder creator responsible for creating folders for GitLab groups. */
    private final FolderCreator folderCreator;

    /** The logger for the class. */
    private final Logger LOGGER = Logger.getLogger(GitLabFolderSynchronizer.class.getName());

    /**
     * Creates a folder synchronizer for an item group.
     *
     * @param folderCreator the folder creator
     */
    public GitLabFolderSynchronizer(FolderCreator folderCreator) {
        this.folderCreator = folderCreator;
    }

    /**
     * Synchronizes group folders for all groups from GitLab.
     *
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public void synchronizeGroupFolders() throws GitLabApiException {
        List<GitLabGroupInfo> groups = GitLab.getGroups();
        Map<Integer, Folder> folders = folderCreator.getExistingGitLabGroupFolders();

        for (GitLabGroupInfo group : groups) {
            try {
                folderCreator.createOrGetGitLabGroupFolder(group, folders);
            } catch (ItemNameCollisionException e) {
                LOGGER.warning("Could not create GitLab folder group \"" + group + "\": item already exists");
            } catch (IOException e) {
                LOGGER.warning("Could not write GitLab folder group \"" + group + "\"");
            }
        }
    }

    /**
     * An interface used to create folders for GitLab groups.
     */
    public interface FolderCreator {
        /**
         * Creates a new folder for a GitLab group or return an already existing one.
         *
         * @param group           the GitLab group
         * @param existingFolders a map of the existing folders (mapped from group IDs to folders)
         * @return the folder
         * @throws ItemNameCollisionException if an item with the name already existed
         * @throws IOException                if saving to persistent storage failed
         */
        public Folder createOrGetGitLabGroupFolder(GitLabGroupInfo group, Map<Integer, Folder> existingFolders)
                throws ItemNameCollisionException, IOException;

        /**
         * Returns a map of existing folders for GitLab groups.
         *
         * @return a map from group IDs to folders
         */
        public Map<Integer, Folder> getExistingGitLabGroupFolders();
    }
}
