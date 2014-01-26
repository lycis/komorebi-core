package org.komorebi.core;

/**
 * Executes the server core application.
 * @author lycis
 *
 */
public class KomorebiMain {

	/**
	 * Execute server
	 * @param args command line arguments (currently none supported)
	 */
	public static void main(String[] args) {
		ServerRunner sr = new ServerRunner();
		sr.run();
	}

}
