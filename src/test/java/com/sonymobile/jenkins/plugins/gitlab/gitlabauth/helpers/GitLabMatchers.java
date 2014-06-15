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

package com.sonymobile.jenkins.plugins.gitlab.gitlabauth.helpers;

import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.authorization.GitLabFolderAuthorization;
import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.folder.GroupFolderInfo;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Hamcrest matchers used in the tests.
 *
 * @author Emil Nilsson
 */
public final class GitLabMatchers {
    private GitLabMatchers() { /* empty */ }

    /**
     * Creates a matcher matching the group ID of a group or group folder.
     *
     * @param groupId the expected group ID
     * @return a matcher
     */
    public static GroupIdMatcher hasGroupId(int groupId) {
        return new GroupIdMatcher(groupId);
    }

    /**
     * Matcher for matching the group ID of a {@link GroupFolderInfo}, {@link GitLabGroupInfo} or
     * {@link GitLabFolderAuthorization} object.
     */
    public final static class GroupIdMatcher extends BaseMatcher {
        /** The expected group ID. */
        private final int expectedGroupId;

        /**
         * Creates a group ID matcher.
         *
         * @param expectedGroupId the expected group ID
         */
        public GroupIdMatcher(int expectedGroupId) {
            this.expectedGroupId = expectedGroupId;
        }

        public boolean matches(Object item) {
            return getGroupId(item) == expectedGroupId;
        }

        public void describeTo(Description description) {
            // describe the expected group ID
            description.appendText("with group ID ").appendValue(expectedGroupId);
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            description.appendText("has group ID ").appendValue(getGroupId(item));
        }

        /**
         * Gets the group ID of an object.
         *
         * Works on {@link GroupFolderInfo}, {@link GitLabGroupInfo} and {@link GitLabFolderAuthorization} objects, all
         * other objects will result in an exception being thrown.
         *
         * @param item the object
         * @return the group ID of the object
         */
        private int getGroupId(Object item) {
            if (item instanceof GroupFolderInfo) {
                return ((GroupFolderInfo)item).getGroupId();
            } else if (item instanceof GitLabGroupInfo) {
                return ((GitLabGroupInfo)item).getId();
            } else if (item instanceof GitLabFolderAuthorization) {
                return ((GitLabFolderAuthorization)item).getGroupId();
            } else {
                throw new IllegalArgumentException("Unexpected type of matched object");
            }
        }
    }
}
