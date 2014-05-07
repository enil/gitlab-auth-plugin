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

import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.GitLabModelDataCreator.createGroupInfo;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockTopLevelItemCreator.createMockFolder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.powermock.api.easymock.PowerMock.mockStatic;

/**
 * Tests for {@link GitLabFolderSynchronizer}.
 *
 * @author Emil Nilsson
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(GitLab.class)
public class GitLabFolderSynchronizerTest {
    /** A list of groups that the mock GitLab cache returns. */
    private List<GitLabGroupInfo> groups;

    /** A mock for the folder creator responsible for creating the groups. */
    private GitLabFolderCreator mockFolderCreator;

    /** The folder synchronizer. */
    private GitLabFolderSynchronizer synchronizer;

    @Before
    public void setUp() throws Exception {
        mockStatic(GitLab.class);
        // GitLab mock returns the group list when GitLab#getGroups() is called
        expect(GitLab.getGroups()).andAnswer(new IAnswer<List<GitLabGroupInfo>>() {
            public List<GitLabGroupInfo> answer() throws Throwable { return groups; }
        }).anyTimes();
        PowerMock.replay(GitLab.class);

        mockFolderCreator = createMock(GitLabFolderCreator.class);

        // create a synchronizer using the mock folder creator
        synchronizer = new GitLabFolderSynchronizer(mockFolderCreator);
    }

    /**
     * Tests {@link GitLabFolderSynchronizer#synchronizeGroupFolders()} without any existing groups or items.
     */
    @Test
    public void synchronizesGroupFolders() throws Exception {
        GitLabGroupInfo firstGroup = createGroupInfo(1, "Group Name", "first");
        GitLabGroupInfo secondGroup = createGroupInfo(2, "Second Group", "second");
        groups = newArrayList(firstGroup, secondGroup);

        expect(mockFolderCreator.createOrGetGitLabGroup(firstGroup)).
                andReturn(createMockFolder("first"));
        expect(mockFolderCreator.createOrGetGitLabGroup(secondGroup)).
                andReturn(createMockFolder("second"));
        replay(mockFolderCreator);

        // synchronize the groups from GitLab and expect it to try to create each of them
        synchronizer.synchronizeGroupFolders();

        verify(mockFolderCreator);
    }
}
