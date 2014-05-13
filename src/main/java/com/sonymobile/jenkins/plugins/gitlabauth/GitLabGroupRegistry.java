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

import com.sonymobile.gitlab.model.GitLabGroupInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * A registry for {@link GitLabGroupInfo} objects for easy access.
 *
 * @author Emil Nilsson
 */
public class GitLabGroupRegistry implements Iterable<GitLabGroupInfo> {
    /** The groups as an ordered list. */
    private final List<GitLabGroupInfo> groupsAsList;

    /** The groups mapped by path. */
    private final Map<String, GitLabGroupInfo> groupsByPath;

    /** The groups mapped by group ID. */
    private final Map<Integer, GitLabGroupInfo> groupsById;

    /**
     * Creates a registry from a list of groups.
     *
     * @param groupsAsList the list of groups
     */
    public GitLabGroupRegistry(List groupsAsList) {
        this.groupsAsList = unmodifiableList(groupsAsList);
        groupsByPath = unmodifiableMap(mapGroupsByPath());
        groupsById = unmodifiableMap(mapGroupsById());
    }

    /**
     * Returns the registry as an ordered list of groups.
     *
     * @return a list of groups
     */
    public List<GitLabGroupInfo> asList() {
        return groupsAsList;
    }

    /**
     * Gets a group by its path.
     *
     * @param path the path
     * @return a group or null if no group was found
     */
    public GitLabGroupInfo getByPath(String path) {
        return groupsByPath.get(path);
    }

    /**
     * Gets a group by its group ID.
     *
     * @param groupId the group ID
     * @return a group or null if no group was found
     */
    public GitLabGroupInfo getById(int groupId) {
        return groupsById.get(groupId);
    }

    /**
     * Returns the number of groups in the registry
     *
     * @return the number of groups
     */
    public int size() {
        return groupsAsList.size();
    }

    public Iterator<GitLabGroupInfo> iterator() {
        return asList().iterator();
    }

    /**
     * Creates a map from the group list mapping groups by path.
     *
     * @return a map from paths to groups
     */
    private Map<String, GitLabGroupInfo> mapGroupsByPath() {
        Map<String, GitLabGroupInfo> groupsByPath = new HashMap<String, GitLabGroupInfo>(size());

        for (final GitLabGroupInfo group : groupsAsList) {
            groupsByPath.put(group.getPath(), group);
        }

        return groupsByPath;
    }

    /**
     * Creates a map from the group list mapping groups by group ID.
     *
     * @return a map from group IDs to groups
     */
    private Map<Integer, GitLabGroupInfo> mapGroupsById() {
        Map<Integer, GitLabGroupInfo> groupsById = new HashMap<Integer, GitLabGroupInfo>(size());

        for (final GitLabGroupInfo group : groupsAsList) {
            groupsById.put(group.getId(), group);
        }

        return groupsById;
    }
}
