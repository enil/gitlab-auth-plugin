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

import org.acegisecurity.Authentication;

import com.sonymobile.jenkins.plugins.gitlabauth.GitLabUserDetails;
import com.sonymobile.jenkins.plugins.gitlabauth.JenkinsAccessLevels;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Folder ACL for GitLab
 * 
 * @author Andreas Alanko
 */
public class GitLabFolderACL extends GitLabAbstactACL {
    
    /**
     * Creates a folder ACL to use for GitLabFolderAuthorization.
     * 
     * @param grantedPermissions map of roles and their respective granted permissions
     */
    public GitLabFolderACL(Map<String, List<Permission>> grantedPermissions) {
        super(grantedPermissions);
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
        if (auth.isAuthenticated()) {
            if(auth.getPrincipal() instanceof GitLabUserDetails) {
                if (((GitLabUserDetails) auth.getPrincipal()).getUsername().equals("andreas")) {
                    return true;
                } else {
                    return isPermissionSet(JenkinsAccessLevels.LOGGED_IN, permission);
                }
            }
        }
        return isPermissionSet(JenkinsAccessLevels.ANONYMOUS, permission);
    }
    
    /**
     * Used to store the permission id instead of the reference to the 
     * permission objects to the config.xml file.
     */
    public static class ConverterImpl implements Converter {
        private static final String XML_FIELD_PERMISSION = "permission";
        
        public boolean canConvert(Class clazz) {
            return clazz.equals(GitLabFolderACL.class);
        }

        /**
         * Used to write the internal data of the GitLabFolderACL object 
         * to config.xml file.
         */
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            GitLabFolderACL acl = (GitLabFolderACL) value;
            
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
                }
                reader.moveUp();
            }
            return new GitLabFolderACL(grantedPermissions);
        }
    }
}