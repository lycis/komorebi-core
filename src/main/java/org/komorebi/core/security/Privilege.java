package org.komorebi.core.security;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides literals for defined privileges a user may have.
 * 
 * @author lycis
 *
 */
public class Privilege {

	// constants
	public final static String ADMINISTRATOR = "Admin";          // administrator, has access to settings and configs and other things
	public final static String ADD_LOCATION = "AddLocation";     // user may add new locations to his credentials list (if not only administrator may do)
	public final static String GRANT_LOCATION = "GrantLocation"; // user may add a new location to the profile of an other user
	
	// tells which privilege is stored in which bit of the privilege mask of a user object
	private static String[] bitPositions = {
		ADMINISTRATOR,
		ADD_LOCATION,
		GRANT_LOCATION,
	};
	
	/**
	 * Gives the bit of the according privilege that corresponds to the bit of the privilege
	 * mask in the user object.
	 * 
	 * @param privilege name of the privilege (use constants provided)
	 * @return number of the mask bit
	 */
	public static int getPrivilegeBit(String privilege){
		for(int i=0; i<bitPositions.length; ++i){
			if(bitPositions[i].equals(privilege)){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Gives all currently supported privileges
	 * @return all supported privileges
	 */
	public static String[] getPrivileges() {
		return bitPositions;
	}
}
