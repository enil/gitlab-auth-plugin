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
import java.util.List;

import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;

import hudson.security.ACL;
import hudson.security.Permission;
import hudson.security.PermissionGroup;

/**
 * ACL for GitLab
 * 
 * @author Andreas Alanko
 */
public class GitLabACL extends ACL {
    /** GitLab usernames with admin rights on Jenkins */
    private List<String> adminUsernames;
    
    private boolean useGitLabAdmins;
    
    private static final String FAL_OWNER = "Owner";
    private static final String FAL_MASTER = "Master";
    private static final String FAL_DEVELOPER = "Developer";
    private static final String FAL_REPORTER = "Reporter";
    private static final String FAL_GUEST = "Guest";
    private static final String FAL_ANONYMOUS = "Anonymous";
    private static final String FAL_LOGGED_IN = "Logged In";
    private static final String[] folderAccessLevels = {FAL_OWNER, FAL_MASTER, FAL_DEVELOPER, FAL_REPORTER, FAL_GUEST, FAL_ANONYMOUS, FAL_LOGGED_IN};
    
    /**
     * Creates an ACL to use for GitLab.
     */
    public GitLabACL(String adminUsernames, boolean useGitLabAdmins) {
        this.useGitLabAdmins = useGitLabAdmins;
        this.adminUsernames = new ArrayList<String>();
        
        if (adminUsernames != null && adminUsernames.length() > 0) {
            adminUsernames = adminUsernames.trim();
            String[] split = adminUsernames.split(",");
            
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
                
                if (!split[i].isEmpty()) {
                    this.adminUsernames.add(split[i]);
                }
            }
        }
    }

    /**
     * Checks if the given principal has permission to use the permission.
     * 
     * @param a the authentication object
     * @param permission the permission
     * @return true if the given principal has permission
     */
    @Override
    public boolean hasPermission(Authentication a, Permission permission) {
        System.out.println("pric: " + a.getPrincipal());
        System.out.println("##############################################");
        for (PermissionGroup pg : PermissionGroup.getAll()) {
            System.out.println("- " + pg.owner + "(" + pg.title + ")");
            
            for (Permission p : pg.getPermissions()) {
                System.out.println("-- " + p.name);
            }
        }
        if(a.getName().equals("jsmith")) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns a string with GitLab usernames who has full admin access in Jenkins.
     * 
     * The usernames are separated by commas.
     * 
     * @return a string with usernames separated by commas
     */
    public String getAdminUsernames() {
        return StringUtils.join(adminUsernames.iterator(), ",");
    }
    
    /**
     * Checks if admins of GitLab also should be admins of Jenkins.
     * 
     * @return true if GitLab admins should be admins of Jenkins, else false
     */
    public boolean getUseGitLabAdmins() {
        return useGitLabAdmins;
    }
}
