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
	
	 // Exception thrown if user is unauthorized.
   private final static Response unauthorizedResponse =
		   Response.status(Status.UNAUTHORIZED)
           .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"realm\"")
           .entity("service requires login").build();

	public void filter(ContainerRequestContext requestContext)
			throws IOException {
		// Extract authentication credentials
		String auth = requestContext.getHeaderString(ContainerRequest.AUTHORIZATION);
		if (auth == null) {
			requestContext.abortWith(unauthorizedResponse);
			return;
		}
		
		// decode bas64 password
		auth = auth.replaceFirst("[Bb]asic ", "");
		String userpass = Base64.decodeAsString(auth);
		if(!userpass.contains(":")){
			requestContext.abortWith(unauthorizedResponse);
			return;
		}
		
		// check given password vs. configured password
		UserStore ustore = UserStore.getInstance();
		String user = auth.split(":")[0];
		User u = null; // TODO find user
		
		String pass = auth.split(":")[1];
		if(!ustore.checkPassword(u, pass.toCharArray())){
			requestContext.abortWith(unauthorizedResponse);
			return;
		}
		
		user     = null;
		pass     = null;
		userpass = null;
		return;
	}

}
