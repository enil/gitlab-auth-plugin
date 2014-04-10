/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Andreas Alanko, Emil Nilsson, Sony Mobile Communications AB. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;
import hudson.security.PermissionGroup;

/**
 * Inherit permissions from a GitLab server.
 * 
 * @author Andreas Alanko
 */
public class GitLabAuthorization extends AuthorizationStrategy {
    /** ACL for GitLab */
    private final GitLabACL rootACL;

    /**
     * Creates an Authorization Strategy for GitLab.
     * 
     * Also creates an ACL for GitLab.
     */
    @DataBoundConstructor
    public GitLabAuthorization(String adminUsernames, boolean useGitLabAdmins) {
        rootACL = new GitLabACL(adminUsernames, useGitLabAdmins);
    }
    
    /**
     * Returns a list of all permission groups except the ones belonging to Item and Permission.
     * 
     * Item permission group is configured separately in each folder.
     * 
     * @return a List of permission groups
     */
    public List<PermissionGroup> getAllGroupsExceptItem() {
        List<PermissionGroup> groups = new ArrayList<PermissionGroup>(PermissionGroup.getAll());
        groups.remove(PermissionGroup.get(Permission.class));
        groups.remove(PermissionGroup.get(Item.class));
        
        return groups;
    }
    
    /**
     * Returns a string with GitLab usernames who has full admin access in Jenkins.
     * 
     * The usernames are separated by commas.
     * 
     * @return a string with usernames separated by commas
     */
    public String getAdminUsernames() {
        return rootACL.getAdminUsernames();
    }
    
    /**
     * Checks if admins of GitLab also should be admins of Jenkins.
     * 
     * @return true if GitLab admins should be admins of Jenkins, else false
     */
    public boolean getUseGitLabAdmins() {
        return rootACL.getUseGitLabAdmins();
    }
    
    /**
     * Returns a collection of all group names.
     * 
     * Will always return an empty list.
     * 
     * @return a collection of groups
     */
    @Override
    public Collection<String> getGroups() {
        return new ArrayList<String>(0);
    }

    /**
     * Gets the root ACL object.
     * 
     * @return an ACL object
     */
    @Override
    public ACL getRootACL() {
        return rootACL;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<AuthorizationStrategy> {

        @Override
        public String getDisplayName() {
            return "GitLab Authorization Strategy";
        }
    }
}