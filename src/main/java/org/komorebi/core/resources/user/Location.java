package org.komorebi.core.resources.user;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.komorebi.core.security.Privilege;
import org.komorebi.core.security.User;
import org.komorebi.core.security.UserStore;

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
	 * Lists all locations with the according credentials of a user.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String get(@Context SecurityContext context){
		JSONObject json = new JSONObject();
		
		User user = (User) context.getUserPrincipal(); // get associated user
		for(String location: user.getLocations()){
			JSONObject locationJson = new JSONObject();
			for(String key: user.getCredentials(location)){
				locationJson.put(key, user.getCredentialValue(location, key));
			}
			json.put(location, locationJson);
		}
		
		return json.toString();
	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(String data, @Context SecurityContext context){
		// TODO implement - remove location and credentials from user
		return Response.status(Response.Status.OK).build();
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response put(String data, @Context SecurityContext context){
		// unmarshal JSON
		JSONObject json = null;
		try{
			json = new JSONObject(data); // check if json is valid
			
			// check if all required fields are supplied
			if(json.getString("location") == null || json.getJSONArray("credentials") == null){
				throw new JSONException("not all required fields provided");
			}
		}catch(JSONException e){
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		
		String targetUser = null;
		try{
			targetUser = json.getString("user");
		}catch(JSONException e){
			targetUser = null;
		}
		
		if(context.getUserPrincipal().getName().equals(targetUser)){
			targetUser = null; // add location to the own profile
		}
		
		// check for according privilege
		if(!context.isUserInRole(Privilege.ADD_LOCATION) ||  // you need either the privilege to add locations to your self 
		   (targetUser != null && !context.isUserInRole(Privilege.GRANT_LOCATION))){ // ... or to another user
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		
		// get user
		User user;
		if(targetUser == null){
			user = UserStore.getInstance().getUser(context.getUserPrincipal().getName());
		}else{
			user = UserStore.getInstance().getUser(targetUser);
		}
		
		// get location
		String location = json.getString("location");
		
		// set all credentials for location
		JSONArray credentialList = json.getJSONArray("credentials");
		for(int i=0; i<credentialList.length(); ++i){
			JSONObject credJson = credentialList.getJSONObject(i);
			if(credJson == null){
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			
			String key = credJson.getString("key");
			String value = credJson.getString("value");
			if(key == null || value == null){
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			
			user.setCredentialValue(location, key, value);
		}
		
		return Response.status(Response.Status.OK).build();
	}
}
