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

import com.sonymobile.gitlab.api.GitLabApiClient;
import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.gitlab.model.GitLabGroupMemberInfo;
import com.sonymobile.gitlab.model.GitLabUserInfo;
import com.sonymobile.jenkins.plugins.gitlabapi.GitLabConfig;

import java.util.List;

/**
 * An interface to a GitLab server.
 *
 * @author Emil Nilsson
 */
public class GitLab {
    private GitLab() {
        /* no public constructor */
    }

    /**
     * Gets a user.
     *
     * @param userId ID of the user
     * @return a user for the ID or null if the user doesn't exist
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static GitLabUserInfo getUser(int userId) throws GitLabApiException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Gets information about a member in a group.
     *
     * @param userId  ID of the user
     * @param groupId ID of the group
     * @return group membership information or null user or group doesn't exist or user isn't member of group
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static GitLabGroupMemberInfo getGroupMember(int userId, int groupId) throws GitLabApiException {
        GitLabApiClient apiClient = GitLabConfig.getApiClient();

        List<GitLabGroupMemberInfo> groupMembers = apiClient.getGroupMembers(groupId);

        // find user in group
        for (final GitLabGroupMemberInfo groupMember : groupMembers) {
            if (groupMember.getId() == userId) {
                return groupMember;
            }
        }

        // not found
        return null;
    }

    /**
     * Gets all groups.
     *
     * @return a list of all groups
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static List<GitLabGroupInfo> getGroups() throws GitLabApiException {
        return GitLabConfig.getApiClient().getGroups();
    }

    /**
     * Checks whether a user is an administrator.
     *
     * @param userId ID of the user
     * @return true if the user is and administrator
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static boolean isAdmin(int userId) throws GitLabApiException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Get access level for a member in a group.
     *
     * @param userId  ID of the member
     * @param groupId ID of the group
     * @return access level for the member
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static GitLabAccessLevel getAccessLevelInGroup(int userId, int groupId) throws GitLabApiException {
        // todo: add NONE access level and return if user isn't a group member
        throw new UnsupportedOperationException("Not implemented");
    }
}
