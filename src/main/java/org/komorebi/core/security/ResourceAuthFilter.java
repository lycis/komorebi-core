package org.komorebi.core.security;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.internal.util.Base64;
import org.glassfish.jersey.server.ContainerRequest;

/**
 * Incorporates basic authentication for requests
 * @author lycis
 *
 */
public class ResourceAuthFilter implements ContainerRequestFilter {

	public void filter(ContainerRequestContext requestContext)
			throws IOException {
		 Response unauthorizedResponse =
				   Response.status(Status.UNAUTHORIZED)
		           .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"realm\"")
		           .entity("service requires login").build();
		 
		// Extract authentication credentials
		String auth = requestContext.getHeaderString(ContainerRequest.AUTHORIZATION);
		if (auth == null) {
			requestContext.abortWith(unauthorizedResponse);
			return;
		}
		
		// decode bas64 password
		auth = auth.replaceFirst("[Bb]asic ", "");
		String userpass = Base64.decodeAsString(auth);
		if(!userpass.contains(":")  || userpass.split(":").length < 2){
			requestContext.abortWith(unauthorizedResponse);
			return;
		}
		
		// check given password vs. configured password
		UserStore ustore = UserStore.getInstance();
		String user = userpass.split(":")[0];
		User u = ustore.getUser(user);
		if(u == null){
			requestContext.abortWith(unauthorizedResponse);
			return;
		}
		
		String pass = userpass.split(":")[1];
		if(!ustore.checkPassword(u, pass.toCharArray())){
			requestContext.abortWith(unauthorizedResponse);
			return;
		}
		
		user     = null;
		pass     = null;
		userpass = null;
		
		// when user is authenticated set security context
		requestContext.setSecurityContext(new KomorebiSecurityContext(u));
		return;
	}

}
