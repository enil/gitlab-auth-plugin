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
import com.sonymobile.gitlab.model.FullGitLabUserInfo;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.gitlab.model.GitLabGroupMemberInfo;
import com.sonymobile.gitlab.model.GitLabUserInfo;
import com.sonymobile.jenkins.plugins.gitlabapi.GitLabConfig;
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
    public void getUser() throws Exception {
        // user 1 exists, user 1000 does not
        expect(mockApiClient.getUser(1)).andReturn(loadUser());
        expect(mockApiClient.getUser(1000)).andReturn(null);
        replay(mockApiClient);

        GitLabUserInfo goodUser = GitLab.getUser(1);
        GitLabUserInfo badUser = GitLab.getUser(1000);

        assertThat("user 1 should exist", goodUser, is(notNullValue()));
        assertThat(1, is(goodUser.getId()));

        assertThat("user 1000 should not exist", badUser, is(nullValue()));

        verify(mockApiClient);
    }

    @Test
    public void getGroupMember() throws Exception {
        expect(mockApiClient.getGroupMembers(1)).andReturn(loadGroupMembers(1)).times(2);
        replay(mockApiClient);

        GitLabGroupMemberInfo goodMember = GitLab.getGroupMember(/* userId */ 1, /* groupId */ 1);
        GitLabGroupMemberInfo badMember = GitLab.getGroupMember(/* userId */ 1000, /* groupId */ 1);

        assertThat("user 1 should be a member of the group", goodMember, is(notNullValue()));
        assertThat(1, is(goodMember.getId()));
        assertThat(1, is(goodMember.getGroupId()));

        assertThat("user 1000 should not be a member of the group", badMember, is(nullValue()));

        verify(mockApiClient);
    }

    @Test
    public void getGroups() throws Exception {
        expect(mockApiClient.getGroups()).andReturn(loadGroups());
        replay(mockApiClient);

        List<GitLabGroupInfo> groups = GitLab.getGroups();

        assertThat(groups, hasSize(1));

        GitLabGroupInfo group = groups.get(0);
        assertThat(1, is(group.getId()));

        verify(mockApiClient);
    }

    @Test
    public void isAdmin() throws Exception {
        // user 1 is an admin, user 2 is not
        expect(mockApiClient.getUser(1)).andReturn(loadAdminUser());
        expect(mockApiClient.getUser(2)).andReturn(loadUser());
        replay(mockApiClient);

        assertThat(GitLab.isAdmin(1), is(true));
        assertThat(GitLab.isAdmin(2), is(false));

        verify(mockApiClient);
    }

    @Test
    public void getAccessLevelInGroup() throws Exception {
        expect(mockApiClient.getGroupMembers(1)).andReturn(loadGroupMembers(1)).times(2);
        replay(mockApiClient);

        // user 1 is an developer, user 1000 isn't member of the group
        assertThat(GitLab.getAccessLevelInGroup(/* userId */ 1, /* groupId */ 1), is(GitLabAccessLevel.DEVELOPER));
        assertThat(GitLab.getAccessLevelInGroup(/* userId */ 1000, /* groupId */ 1), is(GitLabAccessLevel.NONE));

        verify(mockApiClient);
    }

    /**
     * Loads a JSON file with a user.
     *
     * @param variant the variant name
     * @return a user
     * @throws Exception if loading the file failed
     */
    private GitLabUserInfo loadUser(String variant) throws Exception {
        return jsonFile("api/v3/users/1")
                .withVariant(variant)
                .withType(FullGitLabUserInfo.class)
                .loadAsObject();
    }

    /**
     * Load a JSON file with a normal user.
     *
     * @see #loadUser(String)
     */
    private GitLabUserInfo loadUser() throws Exception {
        return loadUser(null);
    }

    /**
     * Load a JSON file with an admin user.
     *
     * @see #loadUser(String)
     */
    private GitLabUserInfo loadAdminUser() throws Exception {
        return loadUser("admin");
    }

    /**
     * Loads the JSON file with group members.
     *
     * @param groupId the group ID
     * @return a list of group members
     * @throws Exception if loading the file failed
     */
    private List<GitLabGroupMemberInfo> loadGroupMembers(int groupId) throws Exception {
        return jsonFile("api/v3/groups/1/members")
                .withType(GitLabGroupMemberInfo.class)
                .andParameters(groupId)
                .loadAsArray();
    }

    /**
     * Loads the JSON file with all groups.
     *
     * @return a list of groups
     * @throws Exception if loading the file failed
     */
    private List<GitLabGroupInfo> loadGroups() throws Exception {
        return jsonFile("api/v3/groups")
                .withType(GitLabGroupInfo.class)
                .loadAsArray();
    }
}
