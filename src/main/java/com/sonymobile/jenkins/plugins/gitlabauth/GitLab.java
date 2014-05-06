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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sonymobile.gitlab.api.GitLabApiClient;
import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.exceptions.GroupNotFoundException;
import com.sonymobile.gitlab.exceptions.UserNotFoundException;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.gitlab.model.GitLabGroupMemberInfo;
import com.sonymobile.gitlab.model.GitLabUserInfo;
import com.sonymobile.jenkins.plugins.gitlabapi.GitLabConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * An interface to a GitLab server.
 *
 * @author Emil Nilsson
 */
public class GitLab {
    /** The singleton implementation instance. */
    private static Implementation instance = new Implementation();

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
        return instance.getUser(userId);
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
        return instance.getGroupMember(userId, groupId);
    }

    /**
     * Gets information about a member in a group.
     *
     * @param userId    ID of the user
     * @param groupPath the group path
     * @return group membership information or null user or group doesn't exist or user isn't member of group
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static GitLabGroupMemberInfo getGroupMember(int userId, String groupPath) throws GitLabApiException {
        GitLabGroupInfo group = getGroupByPath(groupPath);

        if (group == null) {
            // group doesn't exist
            return null;
        } else {
            return instance.getGroupMember(userId, group.getId());
        }
    }

    /**
     * Gets all groups.
     *
     * @return a list of all groups
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static List<GitLabGroupInfo> getGroups() throws GitLabApiException {
        return instance.getGroups();
    }

    /**
     * Gets the group for a specified path.
     *
     * @param groupPath the group path
     * @return the group or null if the group doesn't exist
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static GitLabGroupInfo getGroupByPath(String groupPath) throws GitLabApiException {
        return instance.getGroupByPath(groupPath);
    }

    /**
     * Checks whether a user is an administrator.
     *
     * @param userId ID of the user
     * @return true if the user is and administrator
     * @throws GitLabApiException if the connection against GitLab failed
     */
    public static boolean isAdmin(int userId) throws GitLabApiException {
        GitLabUserInfo user = getUser(userId);
        // not administrator if the user wasn't found
        return user != null && user.isAdmin();
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
        GitLabGroupMemberInfo member = getGroupMember(userId, groupId);
        // no access if user isn't a member of the group
        return member == null ? GitLabAccessLevel.NONE : member.getAccessLevel();
    }

    /**
     * Returns the singleton implementation.
     *
     * @return the instance
     */
    private static synchronized Implementation getInstance() {
        return instance;
    }

    /**
     * Changes the singleton implementation instance.
     *
     * @param instance a new instance
     */
    private static synchronized void setInstance(Implementation instance) {
        GitLab.instance = instance;
    }

    /**
     * Singleton implementation.
     */
    private static class Implementation {
        /** A cache storing users. */
        private final LoadingCache<Integer, GitLabUserInfo> cachedUsers;

        /** A cache for storing memberships for groups. */
        private final LoadingCache<Integer, Map<Integer, GitLabGroupMemberInfo>> cachedGroupMemberships;

        /** A cache for storing groups. */
        private final LoadingCache<Any, Map<String, GitLabGroupInfo>> cachedGroups;

        /**
         * Creates a new standard implementation.
         */
        public Implementation() {
            // use a standard cache builder
            this(CacheBuilder.newBuilder());
        }

        /**
         * Creates a new implementation with a custom cache builder.
         *
         * @param cacheBuilder the cache builder to use.
         */
        public Implementation(CacheBuilder cacheBuilder) {
            // cache for 1 minute
            cacheBuilder.expireAfterWrite(1, TimeUnit.MINUTES);

            // cache users with userId -> user
            cachedUsers = cacheBuilder.build(new UserCacheLoader());

            // cache group members with groupId -> map of userId -> user
            cachedGroupMemberships = cacheBuilder.build(new GroupMembershipsCacheLoader());

            // cache groups with * -> list of groups (uses Any for keys since only one value is cached)
            cachedGroups = cacheBuilder.initialCapacity(1).build(new GroupsCacheLoader());
        }

        /**
         * @see GitLab#getUser(int)
         */
        public GitLabUserInfo getUser(int userId) throws GitLabApiException {
            try {
                // throws UserNotFoundException if user is missing
                return cachedUsers.get(userId);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof UserNotFoundException) {
                    return null;
                } else if (e.getCause() instanceof GitLabApiException) {
                    // throw any GitLabApiExceptions
                    throw (GitLabApiException)e.getCause();
                } else {
                    // throw any other unexpected exceptions
                    throw new RuntimeException(e.getCause());
                }
            }
        }

        /**
         * @see GitLab#getGroupMember(int, int)
         */
        public GitLabGroupMemberInfo getGroupMember(int userId, int groupId) throws GitLabApiException {
            try {
                // throws GroupNotFoundException if group is missing, returns null if user is missing from group
                return cachedGroupMemberships.get(groupId).get(userId);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof GroupNotFoundException) {
                    return null;
                } else if (e.getCause() instanceof GitLabApiException) {
                    // throw any GitLabApiExceptions
                    throw (GitLabApiException)e.getCause();
                } else {
                    // throw any other unexpected exceptions
                    throw new RuntimeException(e.getCause());
                }
            }
        }

        /**
         * @see GitLab#getGroups()
         */
        public List<GitLabGroupInfo> getGroups() throws GitLabApiException {
            try {
                return new ArrayList<GitLabGroupInfo>(cachedGroups.get(Any.anyValue).values());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof GitLabApiException) {
                    // throw any GitLabApiExceptions
                    throw (GitLabApiException)e.getCause();
                } else {
                    // throw any other unexpected exceptions
                    throw new RuntimeException(e.getCause());
                }
            }
        }

        /**
         * @see GitLab#getGroupByPath(String)
         */
        public GitLabGroupInfo getGroupByPath(String path) throws GitLabApiException {
            try {
                return cachedGroups.get(Any.anyValue).get(path);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof GitLabApiException) {
                    // throw any GitLabApiExceptions
                    throw (GitLabApiException)e.getCause();
                } else {
                    // throw any other unexpected exceptions
                    throw new RuntimeException(e.getCause());
                }
            }
        }

        /**
         * Cache loader for getting users from the API.
         */
        private class UserCacheLoader extends CacheLoader<Integer, GitLabUserInfo> {
            @Override
            public GitLabUserInfo load(Integer userId) throws Exception {
                return getApiClient().getUser(userId);
            }
        }

        /**
         * Cache loader for getting memberships of groups from the API.
         */
        private class GroupMembershipsCacheLoader extends CacheLoader<Integer, Map<Integer, GitLabGroupMemberInfo>> {
            @Override
            public Map<Integer, GitLabGroupMemberInfo> load(Integer groupId)
                    throws Exception {
                // get all members as a list
                List<GitLabGroupMemberInfo> memberList = getApiClient().getGroupMembers(groupId);

                // create and return map with userId -> member
                Map<Integer, GitLabGroupMemberInfo> members = new HashMap<Integer,
                        GitLabGroupMemberInfo>(memberList.size());
                for (final GitLabGroupMemberInfo member : memberList) {
                    members.put(member.getId(), member);
                }
                return members;
            }
        }

        /**
         * Cache loader for getting groups from the API.
         */
        private class GroupsCacheLoader extends CacheLoader<Any, Map<String, GitLabGroupInfo>> {
            @Override
            public Map<String, GitLabGroupInfo> load(Any key) throws Exception {
                List<GitLabGroupInfo> groupList = getApiClient().getGroups();

                // create and return map with groupPath -> group
                Map<String, GitLabGroupInfo> groups = new TreeMap<String, GitLabGroupInfo>();
                for (GitLabGroupInfo group : groupList) {
                    groups.put(group.getPath(), group);
                }
                return groups;
            }
        }

        /**
         * Returns the API client.
         *
         * @return an API client
         */
        private GitLabApiClient getApiClient() {
            return GitLabConfiguration.getApiClient();
        }

        /**
         * Used as a generic type where the value doesn't matter.
         */
        private static class Any {
            /** The only instance of the type. */
            public static final Any anyValue = new Any();

            private Any() { /* empty */ }

            @Override
            public int hashCode() { return 1; }

            @Override
            public boolean equals(Object other) { return other instanceof Any; }
        }
    }
}
