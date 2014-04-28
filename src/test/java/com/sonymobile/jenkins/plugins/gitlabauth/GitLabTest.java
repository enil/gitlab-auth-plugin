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
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.gitlab.model.GitLabGroupMemberInfo;
import com.sonymobile.jenkins.plugins.gitlabapi.GitLabConfig;
import org.easymock.IExpectationSetters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static com.sonymobile.gitlab.helpers.JsonFileLoader.jsonFile;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;

/**
 * Tests for the {@link GitLab} class.
 *
 * @author Emil Nilsson
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(GitLabConfig.class)
public class GitLabTest {
    /** A mock for the GitLab API client returned by GitLabConfig. */
    private GitLabApiClient mockApiClient;

    /**
     * Prepares tests by adding a mock for the GitLab API client.
     */
    @Before
    public void setUp() {
        // create mock for the GitLab API client
        mockApiClient = createMock(GitLabApiClient.class);

        // mock GitLabConfig to return the mocked GitLab API Client
        mockStaticPartial(GitLabConfig.class, "getApiClient");
        expect(GitLabConfig.getApiClient()).andReturn(mockApiClient).anyTimes();
        PowerMock.replay(GitLabConfig.class);
    }

    @Test
    public void getGroupMember() throws Exception {
        expectGetGroupMembers(/* groupId = */ 1);
        replay(mockApiClient);

        GitLabGroupMemberInfo groupMember = GitLab.getGroupMember(/* userId = */ 1, /* groupId = */ 1);

        assertThat("user should be member of group", groupMember, is(notNullValue()));
        assertThat(1, is(groupMember.getId()));
        assertThat(1, is(groupMember.getGroupId()));

        verify(mockApiClient);
    }

    /**
     * Attempts to get a group member not belonging to a group.
     */
    @Test
    public void getNonexistentGroupMember() throws Exception {
        expectGetGroupMembers(1);
        replay(mockApiClient);

        GitLabGroupMemberInfo groupMember = GitLab.getGroupMember(/* userId = */ 1000, /* groupId = */ 1);

        assertThat("user should not be member of group", groupMember, is(nullValue()));

        verify(mockApiClient);
    }

    @Test
    public void getGroups() throws Exception {
        expectGetGroups();
        replay(mockApiClient);

        List<GitLabGroupInfo> groups = GitLab.getGroups();

        assertThat(groups, hasSize(1));

        GitLabGroupInfo group = groups.get(0);
        assertThat(1, is(group.getId()));
        assertThat("Group Name", is(group.getName()));
        assertThat("groupname", is(group.getPath()));

        verify(mockApiClient);
    }

    /**
     * Adds mock for {@link GitLabApiClient#getGroupMembers(int)} to return a list of group members loaded from a
     * JSON file.
     *
     * @param groupId the group ID to expect
     * @return an expectation setter for chaining
     */
    private IExpectationSetters<?> expectGetGroupMembers(int groupId) {
        try {
            // make getGroupMembers() return group members loaded from the JSON file
            return expect(mockApiClient.getGroupMembers(groupId)).andReturn(jsonFile("api/v3/groups/1/members")
                    .withType(GitLabGroupMemberInfo.class)
                    .andParameters(groupId) // group ID
                    .loadAsArray());
        } catch (Exception e) {
            /* method cannot throw any exceptions in expect() for a mock */
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds mock for {@link GitLabApiClient#getGroups()}} to return a list of groups loaded from a JSON file.
     *
     * @return an expectation setter for chaining
     */
    private IExpectationSetters<?> expectGetGroups() {
        try {
            // make getGroups() return groups loaded from the JSON file
            return expect(mockApiClient.getGroups()).andReturn(jsonFile("api/v3/groups")
                    .withType(GitLabGroupInfo.class)
                    .loadAsArray());
        } catch (Exception e) {
            /* method cannot throw any exceptions in expect() for a mock */
            throw new RuntimeException(e);
        }
    }
}
