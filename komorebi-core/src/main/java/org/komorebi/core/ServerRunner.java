package org.komorebi.core;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Runnable that takes care of the server start up.
 * @author lycis
 *
 */
public class ServerRunner implements Runnable {

	/**
	 * Start up server.
	 */
	public void run() {
		// start a grizzly server to serve requests
		final HttpServer server = HttpServer.createSimpleServer("."+File.pathSeparator+"htdocs"+File.pathSeparator, 8080);
		try{
			server.start();
		}catch(IOException e){
			Logger.getGlobal().severe("Can not start HTTP server: "+e.getMessage()+"\nStack Trace:\n"+e.getStackTrace());
			System.exit(1);
		}
		
		// add a shutdown hook so the server will be shutdown controlled
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			public void run() {
				server.shutdownNow();
				
			}
	    }, "shutdownHook"));

		
		// go to waiting mode until the server shuts down
		try{
			Logger.getGlobal().info("Server is up and running. Press Ctrl-C or use the web interface to shut down.");
			Thread.currentThread().join();
		}catch(InterruptedException e){
			
		}

	}

}
