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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.sonymobile.gitlab.model.GitLabAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.JenkinsAccessLevel;
import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabPermissionIdentity.IdentityType;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Used to store permission identities and their respective granted permissions.
 * 
 * @author Andreas Alanko
 */
public class GitLabGrantedPermissions {
    /** Map of GitLab identities and their respective granted permissions. */
    private Map<GitLabPermissionIdentity, List<Permission>> grantedPermissions;

    /**
     * Creates a TreeMap to store the identities and their respective granted permissions.
     */
    public GitLabGrantedPermissions() {
        grantedPermissions = new TreeMap<GitLabPermissionIdentity, List<Permission>>();
    }
    
    /**
     * Checks if the given identity has the given permission.
     * 
     * @param identity   the identity
     * @param permission the permission
     * @return true if the identity has permission
     */
    public boolean isPermissionSet(GitLabPermissionIdentity identity, Permission permission) {
        if (identity != null && permission != null) {
            if (grantedPermissions.containsKey(identity)) {
                return grantedPermissions.get(identity).contains(permission);
            }
        }
        return false;
    }
    
    /**
     * Adds a permission for the given identity.
     * 
     * @param identity   the identity
     * @param permission the permission
     */
    public void addPermission(GitLabPermissionIdentity identity, Permission permission) {
        if (identity != null && permission != null) {
            if (!grantedPermissions.containsKey(identity)) {
                grantedPermissions.put(identity, new ArrayList<Permission>());
            }
            grantedPermissions.get(identity).add(permission);
        }
    }
    
    /**
     * Gets a list with the configured GitLabPermission identities for this object.
     * 
     * The list will include all static Jenkins identities and if specified
     * also all static GitLab identities.
     * 
     * @param getGitLabIdentities if GitLab identities should be included
     * @return a list of GitLabPermissionIdentities
     */
    public List<GitLabPermissionIdentity> getPermissionIdentities(boolean getGitLabIdentities) {
        List<GitLabPermissionIdentity> list = 
                GitLabPermissionIdentity.getGlobalStaticPermissionIdentities(getGitLabIdentities);
        
        List<GitLabPermissionIdentity> grantedIdentities = 
                new ArrayList<GitLabPermissionIdentity>(grantedPermissions.keySet());
        
        for (GitLabPermissionIdentity pi : grantedIdentities) {
            if (!list.contains(pi)) {
                list.add(pi);
            }
        }
        return list;
    }
    
    public List<GitLabPermissionIdentity> getGroupPermissionIdentities() {
        List<GitLabPermissionIdentity> list = new ArrayList<GitLabPermissionIdentity>();
        
        for (GitLabPermissionIdentity identity : grantedPermissions.keySet()) {
            if (identity.type.equals(IdentityType.GROUP)) {
                list.add(identity);
            }
        }
        return list;
    }
    
    /** Converter class used to store and restore the internal state of this object to a config.xml file. */
    public static class ConverterImpl implements Converter {
        /** The XML field name in the config.xml file. */
        private static final String XML_FIELD_PERMISSION = "permission";
        
        /** Logger for this class. */
        private final transient Logger LOGGER = Logger.getLogger(GitLabGlobalACL.class.getName());
        
        public boolean canConvert(Class clazz) {
            return clazz.equals(GitLabGrantedPermissions.class);
        }

        /** Used to write the internal data to the config.xml file. */
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            GitLabGrantedPermissions grantedPermissions = (GitLabGrantedPermissions) value;
            
            for (GitLabPermissionIdentity pi : grantedPermissions.grantedPermissions.keySet()) {
                List<Permission> permissions = grantedPermissions.grantedPermissions.get(pi);
                
                for (int i = 0; i < permissions.size(); i++) {
                    writer.startNode(XML_FIELD_PERMISSION);
                    writer.setValue(pi.type + ":" + pi.id + ":" + permissions.get(i).getId());
                    writer.endNode();
                }
            }
        }

        /** Used to parse data stored in the config.xml file. */
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            GitLabGrantedPermissions grantedPermissions = new GitLabGrantedPermissions();
            
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                
                if (XML_FIELD_PERMISSION.equals(reader.getNodeName())) {
                    String readerValue = reader.getValue();
                    String[] value = readerValue.split(":");
                    
                    if (value.length == 3) {
                        GitLabPermissionIdentity identity = null;
                        
                        IdentityType type = IdentityType.valueOf(value[0]);
                        String id = value[1];
                                                
                        switch (type) {
                        case GITLAB:
                            identity = GitLabPermissionIdentity.getGitLabIdentityFromAccessLevel(
                                    GitLabAccessLevel.getAccessLevelWithName(id));
                            break;
                        case JENKINS:
                            identity = GitLabPermissionIdentity.getJenkinsIdentityFromAccessLevel(
                                    JenkinsAccessLevel.getAccessLevelWithName(id));
                            break;
                        case GROUP:
                            identity = GitLabPermissionIdentity.group(id);
                            break;
                        case USER:
                            identity = GitLabPermissionIdentity.user(id);
                            break;
                        }
                        
                        Permission permission = Permission.fromId(value[2]);
                        if(permission != null) {
                            grantedPermissions.addPermission(identity, permission);
                        } else {
                            LOGGER.warning("Unknown permission id: " + value[2]);
                        }
                    } else {
                        LOGGER.warning("Couldn't parse identity/permission: " + readerValue);
                    }
                }
                
                reader.moveUp();
            }
            return grantedPermissions;
        }
    }
}
