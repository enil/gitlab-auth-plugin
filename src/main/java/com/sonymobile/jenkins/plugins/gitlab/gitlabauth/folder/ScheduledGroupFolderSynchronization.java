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

import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.jenkins.plugins.gitlabapi.GitLabConfiguration;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.configuration.GitLabAuthConfiguration;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.time.Interval;
import hudson.Extension;
import hudson.model.AperiodicWork;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * An extension regularly synchronizing GitLab group folders.
 *
 * @author Emil Nilsson
 */
@Extension
public final class ScheduledGroupFolderSynchronization extends AperiodicWork {
    /** The interval between checking the configuration when not active. */
    private static final Interval CONFIG_POLLING_INTERVAL = new Interval(10, TimeUnit.MINUTES);

    /** The synchronizer creating folders from GitLab groups. */
    private static final Synchronizer synchronizer = new Synchronizer();

    /** The logger for the class. */
    private static final Logger LOGGER = Logger.getLogger(ScheduledGroupFolderSynchronization.class.getName());

    /**
     * Checks whether synchronization should be performed.
     *
     * @return true if synchronization should be performed
     */
    public boolean isActive() {
        return GitLabAuthConfiguration.getAutoCreateFolders();
    }

    /**
     * Synchronizes the GitLab folders.
     *
     * Synchronization is only attempted if GitLab is configured.
     *
     * @return true if synchronization was attempted
     * @throws GitLabApiException if the connection against GitLab failed
     */
    private boolean synchronize() throws GitLabApiException {
        // only try to synchronize if GitLab is configured
        if (GitLabConfiguration.isApiConfigured()) {
            synchronizer.synchronize();
            return true;
        }
        return false;
    }

    @Override
    public long getRecurrencePeriod() {
        // duration has to be in ms
        return getPeriodDuration().toMilliseconds();
    }

    @Override
    public AperiodicWork getNewInstance() {
        return new ScheduledGroupFolderSynchronization();
    }

    @Override
    protected void doAperiodicRun() {
        try {
            if (isActive()) {
                if (synchronize()) {
                    LOGGER.fine("Performed GitLab folder synchronization performed");
                } else {
                    LOGGER.warning("Cannot synchronize GitLab folders: GitLab not configured. Retrying in " +
                            getPeriodDuration());
                }
            } else {
                LOGGER.fine("Scheduled GitLab folder synchronization not active");
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
    private Interval getPeriodDuration() {
        if (isActive()) {
            return GitLabAuthConfiguration.getPeriodDuration();
        }
        return CONFIG_POLLING_INTERVAL;
    }

    /**
     * Implementation of the group folder synchronization mechanism.
     */
    private static class Synchronizer extends GroupFolderSynchronizer {
        /**
         * Synchronizes folders for all GitLab groups.
         *
         * @throws GitLabApiException if the connection against GitLab failed
         */
        public void synchronize() throws GitLabApiException {
            // synchronize all available groups
            synchronizeGroupFolders(GitLab.getGroups());
        }
    }
}
