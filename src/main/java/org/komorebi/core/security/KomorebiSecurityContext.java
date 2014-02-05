package org.komorebi.core.security;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class KomorebiSecurityContext implements SecurityContext {
	
	private final User user;
	
	KomorebiSecurityContext(User user){
		this.user = user;
	}

	public Principal getUserPrincipal() {
		return user;
	}

	public boolean isUserInRole(String role) {
		// TODO user privileges check
		return false;
	}

	public boolean isSecure() {
		return true; // only HTTPS is permitted
	}

	public String getAuthenticationScheme() {
		return SecurityContext.BASIC_AUTH; // basic authentication
	}

}
