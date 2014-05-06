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

import hudson.security.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.acegisecurity.Authentication;

import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlabauth.JenkinsAccessLevels;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabAuthorization;
import com.sonymobile.jenkins.plugins.gitlabauth.security.GitLabUserDetails;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Folder ACL for GitLab.
 * 
 * @author Andreas Alanko
 */
public class GitLabFolderACL extends GitLabAbstactACL {
    /** The group id associated with this ACL */
    private int groupId;
    
    /** Logger for this class. */
    private final Logger LOGGER = Logger.getLogger(GitLabFolderACL.class.getName());
    
    /**
     * Creates a folder ACL to use for GitLabFolderAuthorization.
     * 
     * @param grantedPermissions map of roles and their respective granted permissions
     */
    public GitLabFolderACL(Map<String, List<Permission>> grantedPermissions) {
        super(grantedPermissions);
    }
    
    /**
     * Sets the group id associated with this ACL.
     * 
     * @param groupId the group id
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
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
        if(hasGlobalPermission(auth, permission)) {
            return true;
        }
        
        if (isLoggedIn(auth)) {
            GitLabUserDetails user = (GitLabUserDetails) auth.getPrincipal();
            
            try {
                GitLabAccessLevel accessLevel = GitLab.getAccessLevelInGroup(user.getId(), groupId);
                
                if (isPermissionSet(accessLevel.name(), permission)) {
                    return true;
                }
            } catch (GitLabApiException e) {
                LOGGER.warning("Connection to the GitLab API failed.");
            }
            
            if (isAdmin(user)) {
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
     * Checks if the user has global access rights.
     * 
     * @param auth       the authentication
     * @param permission the permission
     * @return true if the given user has the given permission
     */
    private boolean hasGlobalPermission(Authentication auth, Permission permission) {
        if(Jenkins.getInstance().getAuthorizationStrategy() instanceof GitLabAuthorization) {
            GitLabAuthorization authorization = (GitLabAuthorization) Jenkins.getInstance().getAuthorizationStrategy();
            
            return authorization.getRootACL().hasPermission(auth, permission);
        }
        return false;
    }
    
    /**
     * Checks if the given user has admin access on the jenkins server.
     * 
     * @param user the user
     * @return true is the user has admin access else false
     */
    private boolean isAdmin(GitLabUserDetails user) {
        if(Jenkins.getInstance().getAuthorizationStrategy() instanceof GitLabAuthorization) {
            GitLabAuthorization authorization = (GitLabAuthorization) Jenkins.getInstance().getAuthorizationStrategy();
            
            return ((GitLabFolderACL) authorization.getRootACL()).isAdmin(user);
        }
        return false;
    }

    /**
     * Used to store the permission id instead of the reference to the 
     * permission objects to the config.xml file.
     */
    public static class ConverterImpl implements Converter {
        private static final String XML_FIELD_PERMISSION = "permission";
        private static final String XML_FIELD_GROUPID = "groupId";
        
        /** Logger for this class. */
        private final Logger LOGGER = Logger.getLogger(GitLabFolderACL.class.getName());
        
        public boolean canConvert(Class clazz) {
            return clazz.equals(GitLabFolderACL.class);
        }

        /**
         * Used to write the internal data of the GitLabFolderACL object 
         * to config.xml file.
         */
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            GitLabFolderACL acl = (GitLabFolderACL) value;
            
            writer.startNode(XML_FIELD_GROUPID);
            writer.setValue(String.valueOf(acl.groupId));
            writer.endNode();
            
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
         * in the GitLabFolderACL object.
         */
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            HashMap<String, List<Permission>> grantedPermissions = new HashMap<String, List<Permission>>();
            int groupId = -1;
            
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
                        } else {
                            LOGGER.warning("Unknown permission id " + value[1]);
                        }
                    }
                } else if (XML_FIELD_GROUPID.equals(reader.getNodeName())) {
                        try {
                            groupId = Integer.parseInt(reader.getValue());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                }
                reader.moveUp();
            }
            
            GitLabFolderACL acl = new GitLabFolderACL(grantedPermissions);
            acl.setGroupId(groupId);
            return acl;
        }
    }
}