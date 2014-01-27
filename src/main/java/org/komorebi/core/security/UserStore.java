package org.komorebi.core.security;

import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import org.komorebi.core.configuration.KomorebiCoreConfig;

import com.google.common.io.CountingOutputStream;

/**
 * This class allows access to user information. It creates an in-memory user
 * directory based on the user store file. Everything regarding handling of
 * users is going through this class.
 * 
 * The user store is a singleton.
 * 
 * @author lycis
 * 
 */
public class UserStore {
	
	private static final int VERSION = 1;
	private static final int PASSWORD_BLOCK_LEN = 1024;

	// singleton instance
	private static UserStore store = new UserStore();

	private UserStore() {

	}

	/**
	 * Get current instance of the user store.
	 */
	synchronized public static UserStore getInstance() {
		return store;
	}

	public void load(char[] password) {
		KomorebiCoreConfig config = new KomorebiCoreConfig();

		if (config.getBoolean("users.encrypted")) {
			// TODO decrypt user store file
		}

		// TODO initialise empty user directory

		File storefile = new File(config.getString("users.store"));
		if (!storefile.exists()) {
			return;
		}

		// here is where the actual user store is read in its binary format
		// IMPORTANT: this has to be in versions, meaning that whenever the file
		// structure changes
		// it must be able to use a newer core version with an older file
		// version.
		// the version of the file is stored in the first 4 byte (= integer)
		FileInputStream fis = null;
		DataInputStream ds = null;
		try {
			fis = new FileInputStream(storefile);
			ds = new DataInputStream(fis);

			// read version
			int version = ds.readInt();
			if (version != 0) {
				throw new IOException("Not supported user store version");
			}

			// TODO process user store

		} catch (IOException e) {
			Logger.getLogger("userstore")
					.severe("The user store can not be accessed (reason: "
							+ e.getMessage()
							+ "). It may be corrputed! You may need to delete and renew it...");
			System.exit(1);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					Logger.getLogger("userstore").warning(
							"Potential resource leak: could not close user store fis (reason: "
									+ e.getMessage() + ")");
				}
			}

			if (ds != null) {
				try {
					ds.close();
				} catch (IOException e) {
					Logger.getLogger("userstore").warning(
							"Potential resource leak: could not close user store ds (reason: "
									+ e.getMessage() + ")");
				}
			}
		}

	}

	/**
	 * Initialise the user store with a basic administrative user
	 * 
	 * @param file
	 */
	public void init(String file) {
		Console con = System.console();
		System.out.print("Administrator user name: ");
		String adminuser = con.readLine();
		System.out.print("Administrator password: ");
		char[] adminpass = con.readPassword();

		User adminUser = new User();
		adminUser.setUsername(adminuser);		
		FileOutputStream fos = null;
		DataOutputStream ds = null;
		
		KomorebiCoreConfig config = new KomorebiCoreConfig();
		File storefile = new File(config.getString("users.store"));
		if(storefile.exists()){
			System.out.print("This causes the existing user store to be overwritten. Continue? [n]");
			String contchar = con.readLine();
			if(!contchar.startsWith("y") || contchar.isEmpty()){
				System.out.println("Aborted.");
				return;
			}
		}
		
		try{
			fos = new FileOutputStream(storefile);
			CountingOutputStream cs = new CountingOutputStream(fos);
			ds = new DataOutputStream(cs);
			
			// write version
			ds.writeInt(VERSION);
			
			// write admin user
			adminUser.setPosition((int) cs.getCount()); // admin user starts on position zero
			storeUser(adminUser, ds, true);
		}catch(IOException e){
			System.out.println("Error: "+e.getMessage());
			System.exit(1);
		}finally{
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					Logger.getLogger("userstore").warning(
							"Potential resource leak: could not close user store fis (reason: "
									+ e.getMessage() + ")");
				}
			}

			if (ds != null) {
				try {
					ds.close();
				} catch (IOException e) {
					Logger.getLogger("userstore").warning(
							"Potential resource leak: could not close user store ds (reason: "
									+ e.getMessage() + ")");
				}
			}
		}
		
		setPassword(adminUser.getPosition(), adminpass); // set password
	}
	
	/**
	 * Store this user (except password) in the user store
	 * @param ds data stream to write to
	 * @param position position of the user record in user store
	 * @param unew indicates that it is a new user
	 */
	public void storeUser(User user, DataOutputStream os, boolean unew) throws IOException{
		
		// password - either skip or delete
		if(unew){
			byte[] pass = new byte[PASSWORD_BLOCK_LEN]; // 1K password block
			os.write(pass);
		}else{
			//byte[] pass = getPassword(user.getPosition());
			//os.write(pass);
			// TODO implement read & save password
		}
		
		// user name
		os.writeInt(user.getUsername().length());
		byte[] uname = user.getUsername().getBytes("UTF-8");
		os.write(uname);
	}
	
	// TODO password check
	
	/**
	 * Sets and hashes a user password.
	 * 
	 * @param position position of the user record in the store
	 * @param password password (plain text)
	 */
	public void setPassword(long position, char[] password){
		KomorebiCoreConfig config = new KomorebiCoreConfig();
		
		byte[] filePass = new byte[PASSWORD_BLOCK_LEN];
		
		if("PLAIN".equals(config.getString("users.hashmethod").trim().toUpperCase())){
			// warn about plaintext
			Logger.getLogger("userstore").warning("You are using PLANTEXT passwords. This is not recommended!");
			for(int i=0; i<(filePass.length<password.length?filePass.length:password.length); ++i){
				filePass[i] = (byte) password[i]; // ONLY ASCII
			}
		}else{
			Logger.getLogger("userstore").severe("Can not set password - Unknown hash method.");
		}
		
		// TODO implement SHA-2
		
		// RAF access
		try{
			RandomAccessFile usfile = new RandomAccessFile(config.getString("users.store"), "rw");
			usfile.seek(position);
			usfile.write(filePass);
			usfile.close();
		}catch(FileNotFoundException e){
			Logger.getLogger("userstore").severe("Password error: user store file not found");
			return;
		}catch(IOException e){
			Logger.getLogger("userstore").severe("Can not set password: "+e.getMessage());
			return;
		}
		
		password = new char[password.length];
	}
}
