package com.sonymobile.jenkins.plugins.gitlabauth;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

public class GitLabUserInformation extends UserProperty {
	private User mUser;
	
	public GitLabUserInformation(User user) {
		/*
		 * Should be a GitLabUser instead
		 */
		this.mUser = user;
	}
	
	public String getUsername() {
		return (mUser != null) ? mUser.getDisplayName() : "Anon";
	}
	
	public String getFullname() {
		return (mUser != null) ? mUser.getFullName() : this.getUsername();
	}
	
	public String getEmail() {
		return "Not available";
	}
	
	@Extension
	public static final class DescriptorImpl extends UserPropertyDescriptor {

		public String getDisplayName() {
			return "GitLab user information";
		}

		@Override
		public UserProperty newInstance(User user) {
			return new GitLabUserInformation(user);
		}
		
		public boolean isEnabled() {
			return false;
		}
	}
}
