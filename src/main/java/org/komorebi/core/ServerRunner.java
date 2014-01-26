package org.komorebi.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.komorebi.core.configuration.KomorebiCoreConfig;
import org.komorebi.core.security.UserStore;

/**
 * Runnable that takes care of the server start up.
 * 
 * @author lycis
 * 
 */
public class ServerRunner implements Runnable {

	/**
	 * Start up server.
	 */
	public void run() {
		// get config
		KomorebiCoreConfig config = new KomorebiCoreConfig();
		
		// load user store
		if(config.getBoolean("users.encrypted")){
			// TODO password query + load with password
		}else{
			UserStore.getInstance().load(null);
		}
		
		// start a grizzly server to serve requests
		ResourceConfig rc = new ResourceConfig();
		rc.packages("org.komorebi.core.requests");
		rc.register(org.komorebi.core.security.ResourceAuthFilter.class);

		URI uri = null;
		try {
			uri = new URI("https://localhost:"+config.getString("connection.port"+"/"));
		} catch (URISyntaxException e) {
			Logger.getGlobal().severe("URI syntax error: " + e.getMessage());
			System.exit(1);
		}

		// Grizzly ssl configuration
		SSLContextConfigurator sslContext = new SSLContextConfigurator();

		// set up security context
		sslContext.setKeyStoreFile(config.getString("connection.keystore"));
		sslContext.setKeyStorePass(config.getString("connection.keystorepass"));

		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
				uri, rc, true, new SSLEngineConfigurator(sslContext)
						.setClientMode(false).setNeedClientAuth(false));
		try {
			server.start();
		} catch (IOException e) {
			Logger.getGlobal().severe(
					"Can not start HTTP server: " + e.getMessage()
							+ "\nStack Trace:\n" + e.getStackTrace());
			System.exit(1);
		}

		// add a shutdown hook so the server will be shutdown controlled
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			public void run() {
				server.stop();

			}
		}, "shutdownHook"));

		// go to waiting mode until the server shuts down
		try {
			Logger.getGlobal()
					.info("Server is up and running. Press Ctrl-C or use the web interface to shut down.");
			Thread.currentThread().join();
		} catch (InterruptedException e) {

		}

	}

}
