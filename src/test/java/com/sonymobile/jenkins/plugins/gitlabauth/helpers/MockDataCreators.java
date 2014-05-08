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

package com.sonymobile.jenkins.plugins.gitlabauth.helpers;

import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Helper methods for creating mock data for tests.
 *
 * @author Emil Nilsson
 */
public class MockDataCreators {
    private MockDataCreators() { /* empty */}

    /**
     * Mocks a folder authorization property object.
     *
     * @param groupId the group ID for the GitLab group
     * @return a folder property object
     */
    public static GitLabFolderAuthorization mockFolderAuthorization(int groupId) {
        GitLabFolderAuthorization folderAuthorization = new GitLabFolderAuthorization(null);
        folderAuthorization.setGroupId(groupId);

        return folderAuthorization;
    }

    /**
     * Mocks a GitLab group info object.
     *
     * @param groupId the group ID
     * @param name    the group name
     * @param path    the path for the group
     * @return a group info object
     */
    public static GitLabGroupInfo mockGroupInfo(int groupId, String name, String path) {
        GitLabGroupInfo groupInfo = createNiceMock(GitLabGroupInfo.class);
        expect(groupInfo.getId()).andReturn(groupId);
        expect(groupInfo.getName()).andReturn(name);
        expect(groupInfo.getPath()).andReturn(path);
        replay(groupInfo);

        return groupInfo;
    }
}
