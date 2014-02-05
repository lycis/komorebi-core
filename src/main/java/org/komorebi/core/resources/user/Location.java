package org.komorebi.core.resources.user;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 * This resource controls the storage locations that the user has added 
 * to his or her profile.
 * 
 * @author lycis
 *
 */
@Path("user/location")
public class Location {

	/**
	 * Lists all locations of a user
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String get(@Context SecurityContext context){
		// TODO implement
		return "{}";
	}
}
