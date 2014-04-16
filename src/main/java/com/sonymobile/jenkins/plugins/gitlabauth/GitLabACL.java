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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import hudson.security.ACL;
import hudson.security.Permission;

/**
 * ACL for GitLab
 * 
 * @author Andreas Alanko
 */
public class GitLabACL extends ACL {
    /** GitLab usernames with admin rights on Jenkins */
    private List<String> adminUsernames;
    /** If we want all GitLab admins to be Jenkins admins aswell */
    private boolean useGitLabAdmins;
    
    /** Jenkins roles */
    private static final String JAL_ADMIN = "Admin";
    private static final String JAL_LOGGED_IN = "Logged In";
    private static final String JAL_ANONYMOUS = "Anonymous";
    public static final String[] jenkinsAccessLevels = {JAL_ADMIN, JAL_LOGGED_IN, JAL_ANONYMOUS};
    
    /** Map of all Jenkins roles and their respective permissions */
    private Map<String, List<Permission>> grantedJenkinsPermissions;
    
    /**
     * Creates an ACL to use for GitLabAuthorization.
     * 
     * @param adminUsernames
     * @param useGitLabAdmins
     * @param grantedJenkinsPermissions
     */
    public GitLabACL(String adminUsernames, boolean useGitLabAdmins, Map<String, List<Permission>> grantedJenkinsPermissions) {
        this.useGitLabAdmins = useGitLabAdmins;
        this.adminUsernames = new ArrayList<String>();
        this.grantedJenkinsPermissions = grantedJenkinsPermissions;
        
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
        //TODO: Implement
        return true;
    }
    
    /**
     * Returns a string with GitLab usernames who has admin access in Jenkins.
     * 
     * The usernames are separated by commas.
     * 
     * @return a string with usernames separated by commas
     */
    public String getAdminUsernames() {
        return StringUtils.join(adminUsernames.iterator(), ", ");
    }
    
    /**
     * Checks if admins of GitLab also should be admins of Jenkins.
     * 
     * @return true if GitLab admins should be admins of Jenkins, else false
     */
    public boolean getUseGitLabAdmins() {
        return useGitLabAdmins;
    }

    /**
     * Returns a map with the given permissions to the different GitLab roles.
     * 
     * GitLab roles are represented as a String object, which is the key to this map.
     * The value of each key is the permissions granted to the specific role.
     * 
     * @return a map with the granted permissions
     */
    public Map<String, List<Permission>> getGrantedJenkinsPermissions() {
        return grantedJenkinsPermissions;
    }

    /**
     * Checks if the given Jenkins role has the given permission.
     * 
     * @param role the role
     * @param p the permission
     * @return true if the role has permission
     */
    public boolean isPermissionSet(String role, Permission p) {
        if (roleExists(role)) {
            return grantedJenkinsPermissions.get(role).contains(p);
        }
        return false;
    }
    
    /**
     * Checks if the given Jenkins role exists.
     * 
     * @param role the role
     * @return true if role exists
     */
    private boolean roleExists(String role) {
        for (int i = 0; i < jenkinsAccessLevels.length; i++) {
            if (jenkinsAccessLevels[i].equals(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Used to store permission id instead of the reference to the permission objects in grantedPermissions in the config.xml file.
     */
    public static class ConverterImpl implements Converter {
        public boolean canConvert(Class clazz) {
            return clazz.equals(GitLabACL.class);
        }

        /**
         * Used to write the internal data of the GitLabACL class to config.xml file.
         * 
         * Will be written in the following format: userRole:permissionId
         */
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            GitLabACL acl = (GitLabACL) value;
            
            writer.startNode("useGitLabAdmins");
            writer.setValue(String.valueOf(acl.useGitLabAdmins));
            writer.endNode();
            
            for (int i = 0; i < acl.adminUsernames.size(); i++) {
                writer.startNode("admin");
                writer.setValue(acl.adminUsernames.get(i));
                writer.endNode();
            }
            
            for (String role : acl.getGrantedJenkinsPermissions().keySet()) {
                List<Permission> permissions = acl.getGrantedJenkinsPermissions().get(role);
                
                for (int i = 0; i < permissions.size(); i++) {
                    writer.startNode("permission");
                    writer.setValue(role + ":" + permissions.get(i).getId());
                    writer.endNode();
                }
            }
        }

        /**
         * Used to parse data stored in the config.xml file to be used in the GitLabACL object.
         */
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            HashMap<String, List<Permission>> grantedJenkinsPermissions = new HashMap<String, List<Permission>>();
            ArrayList<String> adminUsernames = new ArrayList<String>();
            boolean useGitLabAdmins = false;
            
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                
                if ("permission".equals(reader.getNodeName())) {
                    String[] value = reader.getValue().split(":");
                    
                    if (value.length == 2) {
                        if (!grantedJenkinsPermissions.containsKey(value[0])) {
                            grantedJenkinsPermissions.put(value[0], new ArrayList<Permission>());
                        }
                        Permission p = Permission.fromId(value[1]);
                        if (p != null) {
                            grantedJenkinsPermissions.get(value[0]).add(p);
                        }
                    }
                } else if ("useGitLabAdmins".equals(reader.getNodeName())) {
                    useGitLabAdmins = Boolean.valueOf(reader.getValue());
                } else if ("admin".equals(reader.getNodeName())) {
                    adminUsernames.add(reader.getValue());
                }
                
                reader.moveUp();
            }
            
            return new GitLabACL(StringUtils.join(adminUsernames.iterator(), ", "), useGitLabAdmins, grantedJenkinsPermissions);
        }
        
    }
}
