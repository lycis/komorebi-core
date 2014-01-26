package org.komorebi.core;

/**
 * Information about the version of the core server.
 * @author lycis
 *
 */
public class Version {
	public static final int MAJOR = 0;
	public static final int MINOR = 1;
	public static final int PATCH = 0;
	
	/**
	 * @return A String-representation of the version number
	 */
	public static String versionString(){
		return MAJOR+"."+MINOR+"."+PATCH;
	}
}
