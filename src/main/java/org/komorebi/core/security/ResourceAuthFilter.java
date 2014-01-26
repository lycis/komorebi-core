package org.komorebi.core.security;

import java.io.IOException;

import javax.naming.AuthenticationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
		String authentication = requestContext.getHeaderString(ContainerRequest.AUTHORIZATION);
		System.out.println("auth = "+authentication);
		if (authentication == null) {
			requestContext.abortWith(unauthorizedResponse);
			return;
		}
		
		return;
	}

}
