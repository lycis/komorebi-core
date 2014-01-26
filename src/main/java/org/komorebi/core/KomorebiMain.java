package org.komorebi.core;

import org.komorebi.core.configuration.KomorebiCoreConfig;
import org.komorebi.core.security.UserStore;

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
		if(args.length == 0){
			ServerRunner sr = new ServerRunner();
			sr.run();
		}else{
			KomorebiCoreConfig config = new KomorebiCoreConfig();
			if("--init-user-store".equals(args[0])){
				// initialise user store
				UserStore store = UserStore.getInstance();
				store.init(config.getString("users.store"));
			}
		}
	}

}
