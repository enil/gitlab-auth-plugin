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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;

import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlabauth.JenkinsAccessLevels;
import com.sonymobile.jenkins.plugins.gitlabauth.security.GitLabUserDetails;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import hudson.security.Permission;

/**
 * Global ACL for GitLab
 * 
 * @author Andreas Alanko
 */
public class GitLabGlobalACL extends GitLabAbstactACL {
    /** GitLab usernames with admin rights on Jenkins. */
    private List<String> adminUsernames;
    /** If we want all GitLab admins to be Jenkins admins aswell. */
    private boolean useGitLabAdmins;
    
    /**
     * Creates a global ACL to use for GitLabAuthorization.
     * 
     * @param adminUsernames     the admin usernames seperated by a comma
     * @param useGitLabAdmins    if GitLab admins should also be Jenkins admins
     * @param grantedPermissions map of roles and their respective granted permissions
     */
    public GitLabGlobalACL(String adminUsernames, boolean useGitLabAdmins, Map<String, List<Permission>> grantedPermissions) {
        super(grantedPermissions);
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
     * @param auth       the authentication object
     * @param permission the permission
     * @return true if the given principal has permission
     */
    @Override
    public boolean hasPermission(Authentication auth, Permission permission) {
        if(isLoggedIn(auth)) {
            GitLabUserDetails user = (GitLabUserDetails) auth.getPrincipal();
            
            if(isAdmin(user)) {
                if (isPermissionSet(JenkinsAccessLevels.ADMIN, permission)) {
                    return true;
                }
            }
            
            if (isPermissionSet(JenkinsAccessLevels.LOGGED_IN, permission)) {
                return true;
            }
        }
        return isPermissionSet(JenkinsAccessLevels.ANONYMOUS, permission);
    }
    
    /**
     * Checks if the given user has admin access on the jenkins server.
     * 
     * @param user the user
     * @return true is the user has admin access else false
     */
    public boolean isAdmin(GitLabUserDetails user) {
        try {
            return adminUsernames.contains(user.getUsername()) || (useGitLabAdmins && GitLab.isAdmin(user.getId()));
        } catch (GitLabApiException e) {
            //TODO: Logger: connection failed.
        }
        return false;
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
     * Used to store the permission id instead of the reference to the 
     * permission objects to the config.xml file.
     */
    public static class ConverterImpl implements Converter {
    	private static final String XML_FIELD_PERMISSION = "permission";
    	private static final String XML_FIELD_ADMIN = "admin";
    	private static final String XML_FIELD_USEGITLABADMINS = "useGitLabAdmins";
    	
        public boolean canConvert(Class clazz) {
            return clazz.equals(GitLabGlobalACL.class);
        }

        /**
         * Used to write the internal data of the GitLabGlobalACL object 
         * to config.xml file.
         */
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            GitLabGlobalACL acl = (GitLabGlobalACL) value;
            
            writer.startNode(XML_FIELD_USEGITLABADMINS);
            writer.setValue(String.valueOf(acl.useGitLabAdmins));
            writer.endNode();
            
            for (int i = 0; i < acl.adminUsernames.size(); i++) {
                writer.startNode(XML_FIELD_ADMIN);
                writer.setValue(acl.adminUsernames.get(i));
                writer.endNode();
            }
            
            for (String role : acl.getGrantedPermissions().keySet()) {
                List<Permission> permissions = acl.getGrantedPermissions().get(role);
                
                for (int i = 0; i < permissions.size(); i++) {
                    writer.startNode(XML_FIELD_PERMISSION);
                    writer.setValue(role + ":" + permissions.get(i).getId());
                    writer.endNode();
                }
            }
        }

        /**
         * Used to parse data stored in the config.xml file to be used 
         * in the GitLabGlobalACL object.
         */
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            HashMap<String, List<Permission>> grantedPermissions = new HashMap<String, List<Permission>>();
            ArrayList<String> adminUsernames = new ArrayList<String>();
            boolean useGitLabAdmins = false;
            
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                
                if (XML_FIELD_PERMISSION.equals(reader.getNodeName())) {
                    String[] value = reader.getValue().split(":");
                    
                    if (value.length == 2) {
                        if (!grantedPermissions.containsKey(value[0])) {
                            grantedPermissions.put(value[0], new ArrayList<Permission>());
                        }
                        Permission p = Permission.fromId(value[1]);
                        if (p != null) {
                            grantedPermissions.get(value[0]).add(p);
                        }
                    }
                } else if (XML_FIELD_USEGITLABADMINS.equals(reader.getNodeName())) {
                    useGitLabAdmins = Boolean.valueOf(reader.getValue());
                } else if (XML_FIELD_ADMIN.equals(reader.getNodeName())) {
                    adminUsernames.add(reader.getValue());
                }
                
                reader.moveUp();
            }
            return new GitLabGlobalACL(StringUtils.join(adminUsernames.iterator(), ", "), useGitLabAdmins, grantedPermissions);
        }
    }
}
