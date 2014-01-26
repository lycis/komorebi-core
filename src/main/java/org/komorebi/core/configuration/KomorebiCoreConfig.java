package org.komorebi.core.configuration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import com.google.common.io.Files;

/**
 * Provides convenient access to the config file. Whenever a setting is read from the configuration
 * it will be taken from the file itself. This means that changes to the configuration are immediately
 * applied (except startup information).
 * 
 * @author lycis
 *
 */
public class KomorebiCoreConfig extends XMLConfiguration{	
	private static final String CONFIG_FILENAME = "komorebi-core-conf.xml";
	
	/**
	 * default constructor
	 */
	public KomorebiCoreConfig(){
		load();
	}
	
	/**
	 * load data from configuration file
	 */
	public void load(){
		try{
			super.load(CONFIG_FILENAME);
		}catch(ConfigurationException e){
			Logger.getLogger("config")
				.info("Configuration file missing or damaged. Generating default komorebi-core-conf.xml");
			generateDefaultConfig();
		}
	}
	
	/**
	 * Writes a new config file in case the existing one is corrupted or does not exist.
	 */
	private void generateDefaultConfig(){
		super.clear();
		super.setRootElementName("komorebi-core");
		
		// connection information
		super.setProperty("connection.port", "8080"); // used port for webserver
		super.setProperty("connection.keystore", "keystore"); // name of the keystore file
		super.setProperty("connection.keystorepass", "pass"); // password for the keystore
		
		// user store
		super.setProperty("users.store", "user.str"); // filename of the user store
		super.setProperty("users.hashmethod", "SHA-2"); // used hash method (supported: PLAIN (not recommended!!), SHA-2)
		super.setProperty("users.encrypted", false); // tells if the user store is encrypted (password required on start) 
		
		// backup corrupted copy if it exists
		File cfile = new File(CONFIG_FILENAME);
		if(cfile.exists()){
			try{
				Files.copy(cfile, new File(CONFIG_FILENAME+".corrupt"));
			}catch(IOException e){
				Logger.getLogger("config").warning("Backup of existing configuration failed. New one won't be written.");
				return;
			}
		}
		
		// save to file
		try{
			super.save(cfile);
		}catch(ConfigurationException e){
			Logger.getLogger("config").warning("Writing clean configuration file failed: "+e.getMessage());
		}
	}
}
