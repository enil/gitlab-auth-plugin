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

package com.sonymobile.jenkins.plugins.gitlabauth.acl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity.IdentityType;

/**
 * Test class for {@link GitLabPermissionIdentity}.
 *
 * @author Andreas Alanko
 */
public class GitLabPermissionIdentityTest {
    private List<GitLabPermissionIdentity> allIdentitiesSorted;
    
    @Before
    public void setUp() {
        allIdentitiesSorted = new ArrayList<GitLabPermissionIdentity>();
        allIdentitiesSorted.add(GitLabPermissionIdentity.JENKINS_ADMIN);
        allIdentitiesSorted.add(GitLabPermissionIdentity.JENKINS_LOGGED_IN);
        allIdentitiesSorted.add(GitLabPermissionIdentity.JENKINS_ANONYMOUS);

        allIdentitiesSorted.add(GitLabPermissionIdentity.GITLAB_OWNER);
        allIdentitiesSorted.add(GitLabPermissionIdentity.GITLAB_MASTER);
        allIdentitiesSorted.add(GitLabPermissionIdentity.GITLAB_DEVELOPER);
        allIdentitiesSorted.add(GitLabPermissionIdentity.GITLAB_REPORTER);
        allIdentitiesSorted.add(GitLabPermissionIdentity.GITLAB_GUEST);

        allIdentitiesSorted.add(GitLabPermissionIdentity.user("AuserName"));
        allIdentitiesSorted.add(GitLabPermissionIdentity.user("userName"));

        allIdentitiesSorted.add(GitLabPermissionIdentity.group("AgroupName"));
        allIdentitiesSorted.add(GitLabPermissionIdentity.group("groupName"));
    }

    @Test
    public void testJenkins() {
        assertEquals(IdentityType.JENKINS, GitLabPermissionIdentity.JENKINS_ADMIN.type);
        assertEquals("ADMIN", GitLabPermissionIdentity.JENKINS_ADMIN.id);
        assertEquals("Admin", GitLabPermissionIdentity.JENKINS_ADMIN.displayName);
    }

    @Test
    public void testGitLab() {
        assertEquals(IdentityType.GITLAB, GitLabPermissionIdentity.GITLAB_MASTER.type);
        assertEquals("MASTER", GitLabPermissionIdentity.GITLAB_MASTER.id);
        assertEquals("Master", GitLabPermissionIdentity.GITLAB_MASTER.displayName);
    }

    @Test
    public void testUser() {
        GitLabPermissionIdentity u1 = GitLabPermissionIdentity.user("u1");
        GitLabPermissionIdentity u2 = GitLabPermissionIdentity.user("u2");

        assertEquals(IdentityType.USER, u1.type);
        assertEquals("u1", u1.id);
        assertEquals("u1", u1.displayName);

        assertEquals(IdentityType.USER, u2.type);
        assertEquals("u2", u2.id);
        assertEquals("u2", u2.displayName);

        assertNotEquals(u1, u2);
    }

    @Test
    public void testGroup() {
        GitLabPermissionIdentity g1 = GitLabPermissionIdentity.group("g1");
        GitLabPermissionIdentity g2 = GitLabPermissionIdentity.group("g2");

        assertEquals(IdentityType.GROUP, g1.type);
        assertEquals("g1", g1.id);
        assertEquals("g1", g1.displayName);

        assertEquals(IdentityType.GROUP, g2.type);
        assertEquals("g2", g2.id);
        assertEquals("g2", g2.displayName);

        assertNotEquals(g1, g2);
    }

    @Test
    public void testGetGitLabPermissionIdentityWithAccessLevel() {
        assertEquals(GitLabPermissionIdentity.getGitLabIdentityFromAccessLevel(GitLabAccessLevel.OWNER), GitLabPermissionIdentity.GITLAB_OWNER);
        assertEquals(GitLabPermissionIdentity.getGitLabIdentityFromAccessLevel(GitLabAccessLevel.MASTER), GitLabPermissionIdentity.GITLAB_MASTER);
        assertEquals(GitLabPermissionIdentity.getGitLabIdentityFromAccessLevel(GitLabAccessLevel.DEVELOPER), GitLabPermissionIdentity.GITLAB_DEVELOPER);
        assertEquals(GitLabPermissionIdentity.getGitLabIdentityFromAccessLevel(GitLabAccessLevel.REPORTER), GitLabPermissionIdentity.GITLAB_REPORTER);
        assertEquals(GitLabPermissionIdentity.getGitLabIdentityFromAccessLevel(GitLabAccessLevel.GUEST), GitLabPermissionIdentity.GITLAB_GUEST);
    }

    @Test
    public void testGetJenkinsPermissionIdentityWithAccessLevel() {
        assertEquals(GitLabPermissionIdentity.getJenkinsIdentityFromAccessLevel(JenkinsAccessLevel.ADMIN), GitLabPermissionIdentity.JENKINS_ADMIN);
        assertEquals(GitLabPermissionIdentity.getJenkinsIdentityFromAccessLevel(JenkinsAccessLevel.LOGGED_IN), GitLabPermissionIdentity.JENKINS_LOGGED_IN);
        assertEquals(GitLabPermissionIdentity.getJenkinsIdentityFromAccessLevel(JenkinsAccessLevel.ANONYMOUS), GitLabPermissionIdentity.JENKINS_ANONYMOUS);
    }

    @Test
    public void testGetGlobalStaticPermissionIdentitiesWithGitLab() {
        List<GitLabPermissionIdentity> list = GitLabPermissionIdentity.getGlobalStaticPermissionIdentities(true);
        assertTrue(list.contains(GitLabPermissionIdentity.JENKINS_ADMIN));
        assertTrue(list.contains(GitLabPermissionIdentity.JENKINS_LOGGED_IN));
        assertTrue(list.contains(GitLabPermissionIdentity.JENKINS_ANONYMOUS));

        assertTrue(list.contains(GitLabPermissionIdentity.GITLAB_OWNER));
        assertTrue(list.contains(GitLabPermissionIdentity.GITLAB_MASTER));
        assertTrue(list.contains(GitLabPermissionIdentity.GITLAB_DEVELOPER));
        assertTrue(list.contains(GitLabPermissionIdentity.GITLAB_REPORTER));
        assertTrue(list.contains(GitLabPermissionIdentity.GITLAB_GUEST));
    }

    @Test
    public void testGetGlobalStaticPermissionIdentities() {
        List<GitLabPermissionIdentity> list = GitLabPermissionIdentity.getGlobalStaticPermissionIdentities(false);
        assertTrue(list.contains(GitLabPermissionIdentity.JENKINS_ADMIN));
        assertTrue(list.contains(GitLabPermissionIdentity.JENKINS_LOGGED_IN));
        assertTrue(list.contains(GitLabPermissionIdentity.JENKINS_ANONYMOUS));

        assertFalse(list.contains(GitLabPermissionIdentity.GITLAB_OWNER));
        assertFalse(list.contains(GitLabPermissionIdentity.GITLAB_MASTER));
        assertFalse(list.contains(GitLabPermissionIdentity.GITLAB_DEVELOPER));
        assertFalse(list.contains(GitLabPermissionIdentity.GITLAB_REPORTER));
        assertFalse(list.contains(GitLabPermissionIdentity.GITLAB_GUEST));
    }

    @Test
    public void testCompareTo() {
        ArrayList<GitLabPermissionIdentity> allIdentitiesShuffled = new ArrayList<GitLabPermissionIdentity>();
        allIdentitiesShuffled.add(GitLabPermissionIdentity.JENKINS_ANONYMOUS);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.GITLAB_REPORTER);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.user("userName"));
        allIdentitiesShuffled.add(GitLabPermissionIdentity.GITLAB_DEVELOPER);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.GITLAB_OWNER);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.group("groupName"));
        allIdentitiesShuffled.add(GitLabPermissionIdentity.JENKINS_ADMIN);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.user("AuserName"));
        allIdentitiesShuffled.add(GitLabPermissionIdentity.GITLAB_GUEST);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.JENKINS_LOGGED_IN);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.GITLAB_MASTER);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.group("AgroupName"));

        Collections.sort(allIdentitiesShuffled);

        assertEquals(allIdentitiesSorted, allIdentitiesShuffled);
    }

    @Test
    public void testEquals() {
        assertTrue(GitLabPermissionIdentity.GITLAB_OWNER.equals(GitLabPermissionIdentity.GITLAB_OWNER));
        assertTrue(GitLabPermissionIdentity.JENKINS_LOGGED_IN.equals(GitLabPermissionIdentity.JENKINS_LOGGED_IN));
        assertTrue(GitLabPermissionIdentity.group("groupName").equals(GitLabPermissionIdentity.group("groupName")));
        assertTrue(GitLabPermissionIdentity.user("userName").equals(GitLabPermissionIdentity.user("userName")));

        assertFalse(GitLabPermissionIdentity.GITLAB_MASTER.equals(GitLabPermissionIdentity.JENKINS_ADMIN));
        assertFalse(GitLabPermissionIdentity.group("groupName").equals(GitLabPermissionIdentity.user("userName")));
    }

    @Test
    public void testToString() {
        assertEquals(GitLabPermissionIdentity.GITLAB_DEVELOPER.toString(), "GITLAB:DEVELOPER");
        assertNotSame(GitLabPermissionIdentity.GITLAB_DEVELOPER.toString(), "GITLAB:DEVELOPERs");
    }

    @Test
    public void testIdentityTypesOrder() {
        IdentityType[] types = IdentityType.values();

        assertEquals(types[0], IdentityType.GROUP);
        assertEquals(types[1], IdentityType.USER);
        assertEquals(types[2], IdentityType.GITLAB);
        assertEquals(types[3], IdentityType.JENKINS);
    }

    @Test
    public void testIdentityTypesAmount() {
        IdentityType[] types = IdentityType.values();
        assertEquals(4, types.length);
    }

    @Test
    public void testIdentityTypeDisplayNames() {
        assertEquals("Group", IdentityType.GROUP.displayName);
        assertEquals("User", IdentityType.USER.displayName);
        assertEquals("Jenkins", IdentityType.JENKINS.displayName);
        assertEquals("GitLab", IdentityType.GITLAB.displayName);
    }
}