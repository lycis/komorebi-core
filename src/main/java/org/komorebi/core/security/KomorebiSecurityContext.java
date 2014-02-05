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
		return false;
	}

	public boolean isSecure() {
		return false;
	}

	public String getAuthenticationScheme() {
		return null;
	}

}
