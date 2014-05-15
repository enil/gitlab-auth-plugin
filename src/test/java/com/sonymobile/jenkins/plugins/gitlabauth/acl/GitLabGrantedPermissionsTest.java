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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import hudson.security.Permission;

/**
 * Test class for {@link GitLabFolderCreator}. a
 * 
 * @author Andreas Alanko
 */
public class GitLabGrantedPermissionsTest {
    private GitLabGrantedPermissions grantedPermissions = new GitLabGrantedPermissions();
    
    @Before
    public void setUp() {
        grantedPermissions.addPermission(GitLabPermissionIdentity.GITLAB_DEVELOPER, Permission.CREATE);
        grantedPermissions.addPermission(GitLabPermissionIdentity.GITLAB_DEVELOPER, Permission.UPDATE);
        grantedPermissions.addPermission(GitLabPermissionIdentity.GITLAB_GUEST, Permission.WRITE);
        grantedPermissions.addPermission(GitLabPermissionIdentity.user("User"), Permission.DELETE);
        grantedPermissions.addPermission(GitLabPermissionIdentity.group("Group"), Permission.READ);
    }
    
    @Test
    public void testIsPermissionSet() {
        assertTrue(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.GITLAB_DEVELOPER, Permission.CREATE));
        assertTrue(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.GITLAB_DEVELOPER, Permission.UPDATE));
        assertTrue(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.GITLAB_GUEST, Permission.WRITE));
        
        assertFalse(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.GITLAB_DEVELOPER, Permission.WRITE));
        assertFalse(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.GITLAB_OWNER, Permission.WRITE));
        
        assertTrue(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.user("User"), Permission.DELETE));
        assertTrue(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.group("Group"), Permission.READ));
        
        assertFalse(grantedPermissions.isPermissionSet(null, null));
        assertFalse(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.GITLAB_GUEST, null));
        assertFalse(grantedPermissions.isPermissionSet(null, Permission.WRITE));
        
        assertFalse(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.user("User"), Permission.READ));
        assertFalse(grantedPermissions.isPermissionSet(GitLabPermissionIdentity.group("Group"), Permission.DELETE));
    }
    
    @Test
    public void testGetPermissionIdentitiesWithGitLabIdentities() {
        List<GitLabPermissionIdentity> identities = grantedPermissions.getPermissionIdentities(true);
        
        assertTrue(identities.contains(GitLabPermissionIdentity.GITLAB_OWNER));
        assertTrue(identities.contains(GitLabPermissionIdentity.GITLAB_MASTER));
        assertTrue(identities.contains(GitLabPermissionIdentity.GITLAB_DEVELOPER));
        assertTrue(identities.contains(GitLabPermissionIdentity.GITLAB_REPORTER));
        assertTrue(identities.contains(GitLabPermissionIdentity.GITLAB_GUEST));
        
        assertTrue(identities.contains(GitLabPermissionIdentity.JENKINS_ADMIN));
        assertTrue(identities.contains(GitLabPermissionIdentity.JENKINS_LOGGED_IN));
        assertTrue(identities.contains(GitLabPermissionIdentity.JENKINS_ANONYMOUS));
        
        assertTrue(identities.contains(GitLabPermissionIdentity.user("User")));
        assertTrue(identities.contains(GitLabPermissionIdentity.group("Group")));
        
        assertFalse(identities.contains(GitLabPermissionIdentity.group("NotAMember")));
        assertFalse(identities.contains(GitLabPermissionIdentity.user("NotAMember")));
    }
    
    @Test
    public void testGetPermissionIdentites() {
        List<GitLabPermissionIdentity> identities = grantedPermissions.getPermissionIdentities(false);
        
        assertFalse(identities.contains(GitLabPermissionIdentity.GITLAB_DEVELOPER));
        assertFalse(identities.contains(GitLabPermissionIdentity.GITLAB_GUEST));
        
        assertTrue(identities.contains(GitLabPermissionIdentity.group("Group")));
        assertTrue(identities.contains(GitLabPermissionIdentity.user("User")));
    }
    
    @Test
    public void testGetGroupPermissionIdentities() {
        List<GitLabPermissionIdentity> identities = grantedPermissions.getGroupPermissionIdentities();
        
        assertTrue(identities.contains(GitLabPermissionIdentity.group("Group")));
        assertFalse(identities.contains(GitLabPermissionIdentity.user("User")));
    }

}