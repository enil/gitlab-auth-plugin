package com.sonymobile.jenkins.plugins.gitlabauth;

import hudson.Extension;
import hudson.security.SecurityRealm;

@Extension
public class GitLabSecurityRealm extends SecurityRealm {
	public GitLabSecurityRealm() {
		//TODO
	}

	@Override
	public SecurityComponents createSecurityComponents() {
		// TODO
		return null;
	}
	
	/*
	 * No users should be allowed to sign up.
	 */
	@Override
	public boolean allowsSignup() {
		return false;
	}
}