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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;
import com.cloudbees.hudson.plugins.folder.Folder;

import hudson.Extension;
import hudson.model.Item;
import hudson.security.Permission;
import hudson.security.PermissionGroup;

/**
 * Property used for configuring access rights for folders.
 * 
 * @author Andreas Alanko
 */
public class GitLabFolderAuthorization extends FolderProperty<Folder> {
    private Map<String, List<Permission>> grantedFolderPermissions;
    
    public GitLabFolderAuthorization(Map<String, List<Permission>> grantedFolderPermissions) {
        this.grantedFolderPermissions = grantedFolderPermissions;
    }
    
    /**
     * Checks if the given GitLab role has the given permission.
     * 
     * Mainly used to check if a checkbox should be checked or not on the config page.
     * 
     * @param role the role name
     * @param permission the permission
     * @return true if the given role has the given permission
     */
    public boolean isPermissionSet(String role, Permission permission) {
        if(role != null && permission != null) {
            List<Permission> rolePermissions = grantedFolderPermissions.get(role);
            return (rolePermissions != null) ? rolePermissions.contains(permission) : false;
        }
        return false;
    }
    
    @Extension
    public static class DescriptorImpl extends FolderPropertyDescriptor {

        @Override
        public String getDisplayName() {
            return "GitLab Folder Authorization";
        }
        
        @Override
        public GitLabFolderAuthorization newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            HashMap<String, List<Permission>> grantedFolderPermissions = new HashMap<String, List<Permission>>();
            
            Map<String, Object> tableData = formData.getJSONObject("permissionTable");
            
            for(Entry<String, Object> rolePermission : tableData.entrySet()) {
                String role = rolePermission.getKey();
                
                Map<String, Object> value = (JSONObject) rolePermission.getValue();
                
                for (Entry<String, Object> valueSet : value.entrySet()) {
                    if ((Boolean) valueSet.getValue()) {
                        if (!grantedFolderPermissions.containsKey(role)) {
                            grantedFolderPermissions.put(role, new ArrayList<Permission>());
                        }
                        
                        grantedFolderPermissions.get(role).add(Permission.fromId(valueSet.getKey()));
                    }
                }
            }
            
            return new GitLabFolderAuthorization(grantedFolderPermissions);
        }
        
        /**
         * Returns the Permission Group belonging to the Item class.
         * 
         * @return a permission group
         */
        public PermissionGroup getItemPermissionGroup() {
            return PermissionGroup.get(Item.class);
        }
        
        /**
         * Returns a list with all Jenkins roles.
         * 
         * @return a list with all roles
         */
        public List<String> getAllRoles() {
            return new ArrayList<String>(Arrays.asList(GitLabACL.jenkinsAccessLevels));
        }
        
        @Override
        public boolean isApplicable(Class<? extends Folder> containerType) {
            try {
                //return Jenkins.getInstance().getAuthorizationStrategy() instanceof GitLabAuthorization;
                return true;
            } catch (NoClassDefFoundError e) {
                return false;
            }
        }
    }
}
