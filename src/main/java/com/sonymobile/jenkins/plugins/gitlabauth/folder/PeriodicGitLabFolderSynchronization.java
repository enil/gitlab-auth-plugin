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
import com.sonymobile.jenkins.plugins.gitlabapi.GitLabConfiguration;
import com.sonymobile.jenkins.plugins.gitlabauth.time.Interval;
import hudson.Extension;
import hudson.model.AperiodicWork;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.Jenkins;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A periodic work regularly synchronizing folders for GitLab groups.
 *
 * @author Emil Nilsson
 */
@Extension
public final class PeriodicGitLabFolderSynchronization extends AperiodicWork {
    /** The default duration between synchronizations. */
    private static final Interval DEFAULT_PERIOD_DURATION = new Interval(10, TimeUnit.MINUTES);

    /** The synchronizer creating folders from GitLab groups. */
    private final GitLabFolderSynchronizer synchronizer;

    /** The duration between synchronizations. */
    private Interval periodDuration;

    /** The logger for the class. */
    private final Logger LOGGER = Logger.getLogger(GitLabFolderSynchronizer.class.getName());

    /**
     * Creates a periodic folder synchronization with the default duration.
     */
    public PeriodicGitLabFolderSynchronization() {
        this(DEFAULT_PERIOD_DURATION);
    }

    /**
     * Creates a periodic folder synchronization.
     *
     * @param periodDuration the duration between synchronizations
     */
    public PeriodicGitLabFolderSynchronization(Interval periodDuration) {
        this.periodDuration = periodDuration;

        // Jenkins instance to create folders in
        Jenkins jenkins = Jenkins.getInstance();
        // descriptor of Folder to be used for folder creation
        TopLevelItemDescriptor folderDescriptor = jenkins.getDescriptorByType(Folder.DescriptorImpl.class);

        synchronizer = new GitLabFolderSynchronizer(new GitLabFolderCreator(jenkins, folderDescriptor));
    }

    /**
     * Synchronizes the GitLab folders.
     *
     * Synchronization is only attempted if GitLab is configured.
     *
     * @return true if synchronization was attempted
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public boolean synchronize() throws GitLabApiException {
        // only try to synchronize if GitLab is configured
        if (GitLabConfiguration.isApiConfigured()) {
            synchronizer.synchronizeGroupFolders();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long getRecurrencePeriod() {
        // duration has to be in ms
        return periodDuration.toMilliseconds();
    }

    @Override
    public AperiodicWork getNewInstance() {
        return new PeriodicGitLabFolderSynchronization(getPeriodDuration());
    }

    @Override
    protected void doAperiodicRun() {
        try {
            if (!synchronize()) {
                LOGGER.warning("Cannot synchronize GitLab folders: GitLab not configured. Retrying in " +
                        periodDuration);
            }
        } catch (GitLabApiException e) {
            LOGGER.severe("Synchronization of GitLab folders failed: " + e.getMessage());
        }
    }

    /**
     * Gets the duration between synchronizations.
     *
     * @return the duration
     */
    private synchronized Interval getPeriodDuration() {
        return periodDuration;
    }
}
