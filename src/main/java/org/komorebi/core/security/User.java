package org.komorebi.core.security;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents the data of a user (name, permissions, groups, accounts etc.). Passwords are not stored
 * in the object but in the user store only.
 * 
 * @author lycis
 *
 */
public class User {
	private String username = "";
	private int position = 0;
	
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
	public int getPosition() {
		return position;
	}
	
	/**
	 * Set the position of the record in the user store.
	 * @param position
	 */
	public void setPosition(int position) {
		this.position = position;
	}
}
