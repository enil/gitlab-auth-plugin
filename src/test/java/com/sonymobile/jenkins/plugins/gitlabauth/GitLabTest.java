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

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.sonymobile.gitlab.api.GitLabApiClient;
import com.sonymobile.gitlab.exceptions.GroupNotFoundException;
import com.sonymobile.gitlab.exceptions.UserNotFoundException;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.gitlab.model.GitLabGroupMemberInfo;
import com.sonymobile.gitlab.model.GitLabUserInfo;
import com.sonymobile.jenkins.plugins.gitlabapi.GitLabConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataLoaders.loadAdminUser;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataLoaders.loadGroupMembers;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataLoaders.loadGroups;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataLoaders.loadUser;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.reset;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.powermock.reflect.Whitebox.getInnerClassType;
import static org.powermock.reflect.Whitebox.invokeConstructor;
import static org.powermock.reflect.Whitebox.invokeMethod;

/**
 * Tests for the {@link GitLab} class.
 *
 * @author Emil Nilsson
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({GitLabApiClient.class, GitLabConfiguration.class})
public class GitLabTest {
    /** The number of nanoseconds in a second, for the ticker. */
    private static final long SECONDS = TimeUnit.SECONDS.toNanos(1);

    /** The number of nanoseconds in a minute, for the ticker. */
    private static final long MINUTES = TimeUnit.MINUTES.toNanos(1);

    /** A mock for the GitLab API client returned by GitLabConfiguration. */
    private GitLabApiClient mockApiClient;

    /** A mock ticker for cache tests. */
    private MockTicker mockTicker;

    /**
     * Prepares tests by adding a mock for the GitLab API client.
     */
    @Before
    public void setUp() throws Exception {
        // create mock for the GitLab API client
        mockApiClient = createMock(GitLabApiClient.class);

        // mock GitLabConfiguration to return the mocked GitLab API Client
        mockStatic(GitLabConfiguration.class);
        expect(GitLabConfiguration.getApiClient()).andReturn(mockApiClient).anyTimes();
        PowerMock.replay(GitLabConfiguration.class);

        // create ticker for testing cache
        mockTicker = new MockTicker();

        // create singleton implementation for GitLab using the mock ticker
        Object implementation = invokeConstructor(getInnerClassType(GitLab.class, "Implementation"),
                new Class<?>[] { CacheBuilder.class },
                new Object[] { CacheBuilder.newBuilder().ticker(mockTicker) });

        // replace the singleton implementation instance of GitLab
        invokeMethod(GitLab.class, "setInstance", implementation);
    }

    @Test
    public void getUser() throws Exception {
        // user 1 exists, user 1000 does not
        expect(mockApiClient.getUser(1)).andReturn(loadUser());
        expect(mockApiClient.getUser(1000)).andThrow(new UserNotFoundException(EMPTY));
        replay(mockApiClient);

        GitLabUserInfo goodUser = GitLab.getUser(1);
        GitLabUserInfo badUser = GitLab.getUser(1000);

        assertThat("user 1 should exist", goodUser, is(notNullValue()));
        assertThat(1, is(goodUser.getId()));

        assertThat("user 1000 should not exist", badUser, is(nullValue()));

        verify(mockApiClient);
    }

    /**
     * Tests caching with {@link GitLab#getUser(int)}.
     */
    @Test
    public void cachedGetUser() throws Exception {
        // before cache invalidation
        {
            // should only access API once for both method calls
            expect(mockApiClient.getUser(1)).andReturn(loadUser()).once();
            replay(mockApiClient);

            assertThat("username", is(GitLab.getUser(1).getUsername()));

            // advance time without forcing cache to invalidate
            mockTicker.value += 10 * SECONDS;
            assertThat("username", is(GitLab.getUser(1).getUsername()));

            verify(mockApiClient);
        }

        reset(mockApiClient);

        // advance time to force the cache to invalidate
        mockTicker.value += 1 * MINUTES;

        // after cache invalidation
        {
            // return an updated user object
            expect(mockApiClient.getUser(1)).andReturn(loadUser("newer")).once();
            replay(mockApiClient);

            // should access API again
            assertThat("newusername", is(GitLab.getUser(1).getUsername()));

            verify(mockApiClient);
        }
    }

    @Test
    public void getGroupMember() throws Exception {
        expect(mockApiClient.getGroupMembers(1)).andReturn(loadGroupMembers(1)).anyTimes();
        expect(mockApiClient.getGroupMembers(1000)).andThrow(new GroupNotFoundException(EMPTY));
        replay(mockApiClient);

        GitLabGroupMemberInfo goodMember = GitLab.getGroupMember(/* userId */ 1, /* groupId */ 1);
        GitLabGroupMemberInfo badMember = GitLab.getGroupMember(/* userId */ 1000, /* groupId */ 1);
        GitLabGroupMemberInfo memberOfBadGroup = GitLab.getGroupMember(/* userId */ 1, /* groupId */ 1000);

        assertThat("user 1 should be a member of the group", goodMember, is(notNullValue()));
        assertThat(1, is(goodMember.getId()));
        assertThat(1, is(goodMember.getGroupId()));

        assertThat("user 1000 should not be a member of the group", badMember, is(nullValue()));
        assertThat("group 1000 should not exist", memberOfBadGroup, is(nullValue()));

        verify(mockApiClient);
    }

    @Test
    public void getGroupMemberByPath() throws Exception {
        expect(mockApiClient.getGroupMembers(1)).andReturn(loadGroupMembers(1)).anyTimes();
        // will call getGroups to find the groups
        expect(mockApiClient.getGroups()).andReturn(loadGroups()).anyTimes();
        replay(mockApiClient);

        GitLabGroupMemberInfo goodMember = GitLab.getGroupMember(/* userId */ 1, "groupname");
        GitLabGroupMemberInfo badMember = GitLab.getGroupMember(/* userId */ 1000, "groupname");
        GitLabGroupMemberInfo memberOfBadGroup = GitLab.getGroupMember(/* userId */ 1, "notreal");

        assertThat("user 1 should be a member of the group", goodMember, is(notNullValue()));
        assertThat(1, is(goodMember.getId()));
        assertThat(1, is(goodMember.getGroupId()));

        assertThat("user 1000 should not be a member of the group", badMember, is(nullValue()));
        assertThat("group 1000 should not exist", memberOfBadGroup, is(nullValue()));

        verify(mockApiClient);
    }

    /**
     * Tests caching with {@link GitLab#getGroupMember(int, int)}}.
     */
    @Test
    public void cachedGetGroupMember() throws Exception {
        // before cache invalidation
        {
            // should only access API once for both method calls
            expect(mockApiClient.getGroupMembers(1)).andReturn(loadGroupMembers(1)).once();
            replay(mockApiClient);

            assertThat("username", is(GitLab.getGroupMember(1, 1).getUsername()));

            // advance time without forcing cache to invalidate
            mockTicker.value += 10 * SECONDS;
            assertThat("username", is(GitLab.getGroupMember(1, 1).getUsername()));

            verify(mockApiClient);
        }

        reset(mockApiClient);

        // advance time to force the cache to invalidate
        mockTicker.value += 1 * MINUTES;

        // after cache invalidation
        {
            // return an updated member object
            expect(mockApiClient.getGroupMembers(1)).andReturn(loadGroupMembers(1, "newer")).once();
            replay(mockApiClient);

            // should access API again
            assertThat("newusername", is(GitLab.getGroupMember(1, 1).getUsername()));

            verify(mockApiClient);
        }
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
    public void getGroupsAsUser() throws Exception {
        expect(mockApiClient.asUser(1)).andReturn(mockApiClient);
        expect(mockApiClient.getGroups()).andReturn(loadGroups());
        replay(mockApiClient);

        List<GitLabGroupInfo> groups = GitLab.getGroupsAsUser(1);

        GitLabGroupInfo group = groups.get(0);
        assertThat(1, is(group.getId()));

        verify(mockApiClient);
    }

    @Test
    public void cachedGetGroups() throws Exception {
        // before cache invalidation
        {
            // should only access API once for both method calls
            expect(mockApiClient.getGroups()).andReturn(loadGroups()).once();
            replay(mockApiClient);

            assertThat(GitLab.getGroups(), hasSize(1));

            // advance time without forcing cache to invalidate
            mockTicker.value += 10 * SECONDS;
            assertThat(GitLab.getGroups(), hasSize(1));

            verify(mockApiClient);
        }

        reset(mockApiClient);

        // advance time to force the cache to invalidate
        mockTicker.value += 1 * MINUTES;

        // after cache invalidation
        {
            // return an updated group list
            expect(mockApiClient.getGroups()).andReturn(loadGroups("newer")).once();
            replay(mockApiClient);

            // should access API again
            assertThat(GitLab.getGroups(), hasSize(2));

            verify(mockApiClient);
        }
    }


    @Test
    public void getGroup() throws Exception {
        expect(mockApiClient.getGroups()).andReturn(loadGroups());
        replay(mockApiClient);

        GitLabGroupInfo group = GitLab.getGroup(1);

        assertThat("Group should exist", group, is(notNullValue()));
        assertThat(group.getId(), is(1));

        assertThat("Group should not exist", GitLab.getGroup(1000), is(nullValue()));

        verify(mockApiClient);
    }

    @Test
    public void getGroupByPath() throws Exception {
        expect(mockApiClient.getGroups()).andReturn(loadGroups());
        replay(mockApiClient);

        GitLabGroupInfo group = GitLab.getGroupByPath("groupname");

        assertThat("Group should exist", group, is(notNullValue()));
        assertThat(1, is(group.getId()));

        assertThat("Group should not exist", GitLab.getGroupByPath("notreal"), is(nullValue()));

        verify(mockApiClient);
    }

    @Test
    public void getGroupsOwnedByUser() throws Exception {
        // should impersonate as each of the users
        expect(mockApiClient.asUser(1)).andReturn(mockApiClient);
        expect(mockApiClient.asUser(2)).andReturn(mockApiClient);
        expect(mockApiClient.asUser(3)).andReturn(mockApiClient);
        expect(mockApiClient.getGroups()).andReturn(loadGroups()).anyTimes();
        expect(mockApiClient.getGroupMembers(1)).andReturn(loadGroupMembers(1)).anyTimes();
        replay(mockApiClient);

        // only user 3 is an owner of the group
        assertThat(GitLab.getGroupsOwnedByUser(1), is(empty()));
        assertThat(GitLab.getGroupsOwnedByUser(2), is(empty()));
        assertThat(GitLab.getGroupsOwnedByUser(3), hasSize(1));

        verify(mockApiClient);
    }

    @Test
    public void isAdmin() throws Exception {
        // user 1 is an admin, user 2 is not, user 1000 doesn't exist
        expect(mockApiClient.getUser(1)).andReturn(loadAdminUser());
        expect(mockApiClient.getUser(2)).andReturn(loadUser());
        expect(mockApiClient.getUser(1000)).andThrow(new UserNotFoundException(EMPTY));
        replay(mockApiClient);

        assertThat(GitLab.isAdmin(1), is(true));
        assertThat(GitLab.isAdmin(2), is(false));
        assertThat(GitLab.isAdmin(1000), is(false));

        verify(mockApiClient);
    }

    @Test
    public void isGroupOwner() throws Exception {
        // user 1 is a developer, user 2 is a guest and user 3 is an owner
        expect(mockApiClient.getGroupMembers(1)).andReturn(loadGroupMembers(1)).anyTimes();
        replay(mockApiClient);

        assertThat(GitLab.isGroupOwner(/* userId */ 1, /* groupId */ 1), is(false));
        assertThat(GitLab.isGroupOwner(/* userId */ 2, /* groupId */ 1), is(false));
        assertThat(GitLab.isGroupOwner(/* userId */ 3, /* groupId */ 1), is(true));

        verify(mockApiClient);
    }

    @Test
    public void getAccessLevelInGroup() throws Exception {
        expect(mockApiClient.getGroupMembers(1)).andReturn(loadGroupMembers(1)).anyTimes();
        replay(mockApiClient);

        // user 1 is an developer, user 2 is a guest and user 1000 isn't member of the group
        assertThat(GitLab.getAccessLevelInGroup(/* userId */ 1, /* groupId */ 1), is(GitLabAccessLevel.DEVELOPER));
        assertThat(GitLab.getAccessLevelInGroup(/* userId */ 2, /* groupId */ 1), is(GitLabAccessLevel.GUEST));
        assertThat(GitLab.getAccessLevelInGroup(/* userId */ 1000, /* groupId */ 1), is(GitLabAccessLevel.NONE));

        verify(mockApiClient);
    }

    /**
     * A fake Ticker for cache tests.
     */
    private static class MockTicker extends Ticker {
        /** The ticker value. */
        public long value = 0l;

        @Override
        public long read() {
            return value;
        }
    }
}
