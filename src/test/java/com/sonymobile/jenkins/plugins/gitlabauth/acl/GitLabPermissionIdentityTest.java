package com.sonymobile.jenkins.plugins.gitlabauth.acl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity;

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
    }
    
    @Test
    public void testUser() {
        //TODO:
    }
    
    @Test
    public void testGroup() {
        //TODO:
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
        allIdentitiesShuffled.add(GitLabPermissionIdentity.GITLAB_DEVELOPER);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.GITLAB_OWNER);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.JENKINS_ADMIN);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.GITLAB_GUEST);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.JENKINS_LOGGED_IN);
        allIdentitiesShuffled.add(GitLabPermissionIdentity.GITLAB_MASTER);
        
        Collections.sort(allIdentitiesShuffled);
        
        assertEquals(allIdentitiesSorted, allIdentitiesShuffled);
    }
    
    @Test
    public void testEquals() {
        assertTrue(GitLabPermissionIdentity.GITLAB_OWNER.equals(GitLabPermissionIdentity.GITLAB_OWNER));
        assertFalse(GitLabPermissionIdentity.GITLAB_MASTER.equals(GitLabPermissionIdentity.JENKINS_ADMIN));
    }
    
    @Test
    public void testToString() {
        assertEquals(GitLabPermissionIdentity.GITLAB_DEVELOPER.toString(), "GITLAB:DEVELOPER");
        assertNotSame(GitLabPermissionIdentity.GITLAB_DEVELOPER.toString(), "GITLAB:DEVELOPERs");
    }
}
