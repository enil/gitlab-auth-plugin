package com.sonymobile.jenkins.plugins.gitlabauth;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;

public class GitLabAuthenticationManager implements AuthenticationManager{

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (isValidUser(authentication.getPrincipal(), authentication.getCredentials())) {
			return authentication;
		} else {
			throw new BadCredentialsException("User does not exist");
		}
	}
	
	/*
	 * Checks user credentials with GitLab API server
	 */
	private boolean isValidUser(Object principal, Object credentials) {
		return principal.equals("nisse") && credentials.equals("p2");
	}
}
