package org.komorebi.core.requests;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.komorebi.core.Version;

/**
 * This request provides current information about the server.
 * @author lycis
 *
 */
@Path("serverInfo")
public class ServerInfo {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String get(){
		JSONObject json = new JSONObject();
		json.put("version", Version.versionString());
		return json.toString();
	}
}
