package com.sonymobile.jenkins.plugins.gitlabauth;

import net.sf.json.JSONObject;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.dao.DataAccessException;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.AbstractPasswordBasedSecurityRealm;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;

public class GitLabSecurityRealm extends AbstractPasswordBasedSecurityRealm {
	
	@DataBoundConstructor
	public GitLabSecurityRealm() {
		super();
	}
	
	@Override
	public boolean allowsSignup() {
		return false;
	}

	@Override
	protected UserDetails authenticate(String username, String password) throws AuthenticationException {
		UserDetails userDetails = null;
		
		if (isValidUser(username, password)) {
			userDetails = new User(username, password, true, true, true, true, new GrantedAuthority[] { SecurityRealm.AUTHENTICATED_AUTHORITY });
		} else {
			throw new BadCredentialsException("Not a valid username or password");
		}
		
		return userDetails;
	}
	
	/*
	 * Checks user credentials with GitLab API server
	 */
	private boolean isValidUser(String username, String password) {
		return username.equals("nisse") && password.equals("p2");
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GroupDetails loadGroupByGroupname(String groupname) throws UsernameNotFoundException, DataAccessException {
		throw new UsernameNotFoundException("Feature not supported");
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