package com.sonymobile.jenkins.plugins.gitlabauth.acl;

import hudson.security.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GitLabGrantedPermissions {
    private Map<GitLabIdentity, List<Permission>> usersGrantedPermissions;
    private Map<GitLabIdentity, List<Permission>> groupsGrantedPermissions;
    private Map<GitLabIdentity, List<Permission>> jenkinsAccessLevelsGrantedPermissions;
    private Map<GitLabIdentity, List<Permission>> gitLabAccessLevelsGrantedPermissions;

    public GitLabGrantedPermissions() {
        usersGrantedPermissions = new TreeMap<GitLabIdentity, List<Permission>>();
        groupsGrantedPermissions = new TreeMap<GitLabIdentity, List<Permission>>();
        jenkinsAccessLevelsGrantedPermissions = new TreeMap<GitLabIdentity, List<Permission>>();
        gitLabAccessLevelsGrantedPermissions = new TreeMap<GitLabIdentity, List<Permission>>();
    }
    
    /**
     * Checks if the given identity has the given permission.
     * 
     * @param identity   the identity
     * @param permission the permission
     * @return true if the identity has permission
     */
    public boolean isPermissionSet(GitLabIdentity identity, Permission permission) {
        if (identity != null && permission != null) {
            if (jenkinsAccessLevelsGrantedPermissions.containsKey(identity)) {
                if (jenkinsAccessLevelsGrantedPermissions.get(identity).contains(permission)) {
                    return true;
                }
            }
            
            if (gitLabAccessLevelsGrantedPermissions.containsKey(identity)) {
                if (gitLabAccessLevelsGrantedPermissions.get(identity).contains(permission)) {
                    return true;
                }
            }
            
            if (groupsGrantedPermissions.containsKey(identity)) {
                if (groupsGrantedPermissions.get(identity).contains(permission)) {
                    return true;
                }
            }
            
            if (usersGrantedPermissions.containsKey(identity)) {
                if (usersGrantedPermissions.get(identity).contains(permission)) {
                    return true;
                }
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
    public void addPermission(GitLabIdentity identity, Permission permission) {
        if (identity != null && permission != null) {
            switch (identity.type) {
            case JENKINS:
                addPermission(identity, permission, jenkinsAccessLevelsGrantedPermissions);
                break;
                
            case GITLAB:
                addPermission(identity, permission, gitLabAccessLevelsGrantedPermissions);
                break;
                
            case GROUP:
                addPermission(identity, permission, groupsGrantedPermissions);
                break;
                
            case USER:
                addPermission(identity, permission, usersGrantedPermissions);
                break;
            }
        }
    }
    
    // Helper method for addPermission(GitLabIdentity, Permission)
    private void addPermission(GitLabIdentity identity, Permission permission, Map<GitLabIdentity, List<Permission>> map) {
        if (!map.containsKey(identity)) {
            map.put(identity, new ArrayList<Permission>());
        }
        map.get(identity).add(permission);
    }
}
