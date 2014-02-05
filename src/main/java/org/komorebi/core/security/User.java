package org.komorebi.core.security;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Represents the data of a user (name, permissions, groups, accounts etc.). Passwords are not stored
 * in the object but in the user store only.
 * 
 * @author lycis
 *
 */
public class User implements Principal{
	private String username = "";
	private long position = 0;
	private Map<String, Map<String, String>> credentials = new HashMap<String, Map<String, String>>();
	
	/**
	 * @return login name of the user
	 */
	public String getName() {
		return username;
	}
	
	/**
	 * Set the username
	 * @param username
	 */
	public void setName(String username) {
		this.username = username;
	}
	
	/**
	 * @return position of the record in the user store - needed for password retrieval
	 */
	public long getPosition() {
		return position;
	}
	
	/**
	 * Set the position of the record in the user store.
	 * @param position
	 */
	public void setPosition(long position) {
		this.position = position;
	}
	
	/**
	 * Add credentials for the given location.
	 * 
	 * @param location location that these credentials are valid for
	 * @param key key that identifies the credential
	 * @param value actual credential value
	 */
	public void setCredentialValue(String location, String key, String value){
		if(!credentials.containsKey(location)){
			credentials.put(location, new HashMap<String, String>());
		}
		
		credentials.get(location).put(key, value);
	}
	
	/**
	 * Returns credentials for the given location identified by the key.
	 * 
	 * @param location location you want to retrieve credentials for
	 * @param key key to identify the credentials
	 * @return credential values
	 */
	public String getCredentialValue(String location, String key){
		Map<String, String> locationCredentials = credentials.get(location);
		if(credentials == null){
			return null;
		}
		
		return locationCredentials.get(key);
	}
	
	/**
	 * Return all locations the user has credentials for (and that are therey configured)
	 */
	public Set<String> getLocations(){
		return credentials.keySet();
	}
	
	/**
	 * Returns all credential keys for the given location
	 * @param location location to get credentials for
	 */
	public Set<String> getCredentials(String location){
		if(!credentials.containsKey(location)){
			return null;
		}
		
		return credentials.get(location).keySet();
	}
}
