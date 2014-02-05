package org.komorebi.core.security;

/**
 * This class provides literals for defined privileges a user may have.
 * 
 * @author lycis
 *
 */
public class Privilege {

	public final static String ADD_LOCATION = "AddLocation";     // user may add new locations to his credentials list (if not only administrator may do)
	public final static String GRANT_LOCATION = "GrantLocation"; // user may add a new location to the profile of an other user
}
