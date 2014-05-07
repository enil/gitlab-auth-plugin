package com.sonymobile.jenkins.plugins.gitlabauth.acl;

public class GitLabIdentity {
    public final String name;
    public final IdentityType type;
    
    public GitLabIdentity(String name, IdentityType type) {
        this.name = name;
        this.type = type;
    }
    
    public enum IdentityType {
        GITLAB,
        JENKINS,
        USER,
        GROUP;
        
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
