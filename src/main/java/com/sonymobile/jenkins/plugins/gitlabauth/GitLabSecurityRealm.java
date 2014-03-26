package com.sonymobile.jenkins.plugins.gitlabauth;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.SecurityRealm;

public class GitLabSecurityRealm extends SecurityRealm {
	
	@DataBoundConstructor
	public GitLabSecurityRealm() {
		super();
	}

	@Override
	public SecurityComponents createSecurityComponents() {
		return new SecurityComponents(new GitLabAuthenticationManager());
	}
	
	@Override
	public boolean allowsSignup() {
		return false;
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<SecurityRealm> {
		
		@Override
		public SecurityRealm newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return new GitLabSecurityRealm();
		}

		public String getDisplayName() {
			return "GitLab Authentication";
		}
	}
}