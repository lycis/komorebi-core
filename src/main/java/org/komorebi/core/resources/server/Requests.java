package org.komorebi.core.resources.server;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API documentation and resource list.
 * @author lycis
 *
 */

@Path("server/requests")
public class Requests {

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response get() {
		// TODO automatically read and write out resources documentation
		return Response.status(Response.Status.OK).entity("test").build();
	}
}
