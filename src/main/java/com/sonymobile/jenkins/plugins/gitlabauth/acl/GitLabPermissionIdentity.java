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

import java.util.ArrayList;
import java.util.List;

import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.JenkinsAccessLevel;

/**
 * Used to create a permission identity.
 * 
 * @author Andreas Alanko
 */
public class GitLabPermissionIdentity implements Comparable<GitLabPermissionIdentity> {
    /** The visual name of this identity. */
    public final String displayName;
    
    /** The identifier of this identity. */
    public final String id;
    
    /** The type of this identity. */
    public final IdentityType type;
    
    private GitLabPermissionIdentity(String displayName, String id, IdentityType type) {
        this.displayName = displayName;
        this.id = id;
        this.type = type;
    }
    
    private GitLabPermissionIdentity(GitLabAccessLevel accessLevel) {
        this(accessLevel.toString(), accessLevel.name(), IdentityType.GITLAB);
    }
    
    private GitLabPermissionIdentity(JenkinsAccessLevel accessLevel) {
        this(accessLevel.displayName, accessLevel.name(), IdentityType.JENKINS);
    }
    
    /**
     * Creates a permission identity for a user with the given username.
     * 
     * @param username the username
     * @return the permission identity
     */
    public static GitLabPermissionIdentity user(String username) {
        return new GitLabPermissionIdentity(username, username, IdentityType.USER);
    }
    
    /**
     * Creates a permission identity for a group with the given group name
     * 
     * @param groupName the group name
     * @return the permission identity
     */
    public static GitLabPermissionIdentity group(String groupName) {
        return new GitLabPermissionIdentity(groupName, groupName, IdentityType.GROUP);
    }
    
    @Override
    public String toString() {
        return type + ":" + id;
    }
    
    /**
     * Checks if this object is equal to the given.
     * 
     * Will return true if type and id of both objects are equals.
     * 
     * @return true if this and object is equals
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof GitLabPermissionIdentity) {
            if (object != null) {
                if (type.equals(((GitLabPermissionIdentity) object).type)) {
                    if (id.equals(((GitLabPermissionIdentity) object).id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Compares this permission identity object with the given.
     * 
     * Return 1 if this is considered greater than the given.
     * Return 0 if objects are equals.
     * Return -1 if this is considered lesser than the given.
     * 
     * @param object the object to be compared
     * @return an integer with the result (1, 0, -1)
     */
    public int compareTo(GitLabPermissionIdentity object) {
        int thisValue = type.ordinal();
        int objectValue = object.type.ordinal();
        
        if (thisValue < objectValue) {
            return 1;
        } else if (thisValue > objectValue) {
            return -1;
        } else {
            if (type == IdentityType.GITLAB) {
                int thisAccessLevel = GitLabAccessLevel.getAccessLevelWithName(id).ordinal();
                int objectAccessLevel = GitLabAccessLevel.getAccessLevelWithName(object.id).ordinal();
                
                if (thisAccessLevel < objectAccessLevel) {
                    return 1;
                } else if (thisAccessLevel > objectAccessLevel) {
                    return -1;
                } else {
                    return 0;
                }
            } else if (type == IdentityType.JENKINS) {
                int thisAccessLevel = JenkinsAccessLevel.getAccessLevelWithName(id).ordinal();
                int objectAccessLevel = JenkinsAccessLevel.getAccessLevelWithName(object.id).ordinal();
                
                if (thisAccessLevel < objectAccessLevel) {
                    return 1;
                } else if (thisAccessLevel > objectAccessLevel) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return id.compareTo(object.id);
            }
        }
    }
    
    /**
     * Gets all static identities such as Jenkins:admin and GitLab:Owner.
     * 
     * The boolean getGitLab specifies if the static GitLab identities should
     * be included.
     * 
     * No user or group identities will be included.
     * 
     * @param getGitLabIdentities if GitLab identities should be included
     * @return a list with permission identities
     */
    public static List<GitLabPermissionIdentity> getGlobalStaticPermissionIdentities(boolean getGitLabIdentities) {
        List<GitLabPermissionIdentity> list = new ArrayList<GitLabPermissionIdentity>();
        
        if (getGitLabIdentities) {
            for (GitLabAccessLevel accessLevel : GitLabAccessLevel.values()) {
                if (accessLevel != GitLabAccessLevel.NONE) {
                    list.add(0, getGitLabIdentityFromAccessLevel(accessLevel));
                }
            }
        }
        
        for (JenkinsAccessLevel accessLevel : JenkinsAccessLevel.values()) {
            list.add(0, getJenkinsIdentityFromAccessLevel(accessLevel));
        }
        return list;
    }
    
    /**
     * Gets the permission identity for the given GitLab access level.
     * 
     * @param accessLevel the access level
     * @return the permission identity
     */
    public static GitLabPermissionIdentity getGitLabIdentityFromAccessLevel(GitLabAccessLevel accessLevel) {
        switch (accessLevel) {
        case OWNER:
            return GITLAB_OWNER;
        case MASTER:
            return GITLAB_MASTER;
        case DEVELOPER:
            return GITLAB_DEVELOPER;
        case REPORTER:
            return GITLAB_REPORTER;
        case GUEST:
            return GITLAB_GUEST;
        default:
            return new GitLabPermissionIdentity(GitLabAccessLevel.NONE);
        }
    }
    
    /**
     * Gets the permission identity for the given Jenkins access level.
     * 
     * @param accessLevel the access level
     * @return the permission identity
     */
    public static GitLabPermissionIdentity getJenkinsIdentityFromAccessLevel(JenkinsAccessLevel accessLevel) {
        switch (accessLevel) {
        case ADMIN:
            return JENKINS_ADMIN;
        case LOGGED_IN:
            return JENKINS_LOGGED_IN;
        default:
            return JENKINS_ANONYMOUS;
        }
    }
    
    public static final GitLabPermissionIdentity JENKINS_ADMIN = 
            new GitLabPermissionIdentity(JenkinsAccessLevel.ADMIN);
    
    public static final GitLabPermissionIdentity JENKINS_LOGGED_IN = 
            new GitLabPermissionIdentity(JenkinsAccessLevel.LOGGED_IN);
    
    public static final GitLabPermissionIdentity JENKINS_ANONYMOUS = 
            new GitLabPermissionIdentity(JenkinsAccessLevel.ANONYMOUS);
    
    public static final GitLabPermissionIdentity GITLAB_OWNER = 
            new GitLabPermissionIdentity(GitLabAccessLevel.OWNER);
    
    public static final GitLabPermissionIdentity GITLAB_MASTER = 
            new GitLabPermissionIdentity(GitLabAccessLevel.MASTER);
    
    public static final GitLabPermissionIdentity GITLAB_DEVELOPER = 
            new GitLabPermissionIdentity(GitLabAccessLevel.DEVELOPER);
    
    public static final GitLabPermissionIdentity GITLAB_REPORTER = 
            new GitLabPermissionIdentity(GitLabAccessLevel.REPORTER);
    
    public static final GitLabPermissionIdentity GITLAB_GUEST = 
            new GitLabPermissionIdentity(GitLabAccessLevel.GUEST);
    
    /** Enum for different identity types. */
    public enum IdentityType {
        GROUP("Group"),
        USER("User"),
        GITLAB("GitLab"),
        JENKINS("Jenkins");
        
        /** The display name of the enum. */
        public final String displayName;
        
        private IdentityType(String displayName) {
            this.displayName = displayName;
        }
    }
}
