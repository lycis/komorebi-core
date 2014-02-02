package org.komorebi.core.security;


/**
 * Represents the data of a user (name, permissions, groups, accounts etc.). Passwords are not stored
 * in the object but in the user store only.
 * 
 * @author lycis
 *
 */
public class User {
	private String username = "";
	private long position = 0;
	
	// TODO handling storage locations
	
	/**
	 * @return login name of the user
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Set the username
	 * @param username
	 */
	public void setUsername(String username) {
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
}
