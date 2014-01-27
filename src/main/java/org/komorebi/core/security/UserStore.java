package org.komorebi.core.security;

import java.io.Console;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.komorebi.core.configuration.KomorebiCoreConfig;

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
	
	// constants
	private static final int VERSION = 1; // most recent version of the file format
	private static final int PASSWORD_BLOCK_LEN = 1024; // length of the password block

	// singleton instance
	private static UserStore store = new UserStore();
	
	// members
	private Map<String, User> userMap = null;

	private UserStore() {
		userMap = new HashMap<String, User>();
	}

	/**
	 * Get current instance of the user store.
	 */
	synchronized public static UserStore getInstance() {
		return store;
	}

	public boolean load(char[] password) {
		KomorebiCoreConfig config = new KomorebiCoreConfig();

		if (config.getBoolean("users.encrypted")) {
			// TODO decrypt user store file
		}

		userMap.clear(); // initialise user cache

		File storefile = new File(config.getString("users.store"));
		if (!storefile.exists()) {
			Logger.getLogger("userstore").severe("User store file ("+storefile.getAbsolutePath()+") not found.");
			return false;
		}

		// here is where the actual user store is read in its binary format
		// IMPORTANT: this has to be in versions, meaning that whenever the file
		// structure changes
		// it must be able to use a newer core version with an older file
		// version.
		// the version of the file is stored in the first 4 byte (= integer)
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(storefile, "r");
			
			// read version
			int version = raf.readInt();
			if (version != 1) {
				throw new IOException("Not supported user store version");
			}

			// read one user block after another
			while(raf.getFilePointer() < raf.length()){
				User u = restoreUser(raf);
				userMap.put(u.getUsername(), u);
			}

		} catch (IOException e) {
			Logger.getLogger("userstore")
					.severe("The user store can not be accessed (reason: "
							+ e.getMessage()
							+ "). It may be corrputed! You may need to delete and renew it...");
			return false;
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					Logger.getLogger("userstore").warning(
							"Potential resource leak: could not close user store RAF (reason: "
									+ e.getMessage() + ")");
				}
			}
		}
		
		Logger.getLogger("userstore").info("Loaded User Store with "+userMap.size()+" registered users.");
		return true;
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
		
		RandomAccessFile raf = null;
		try{
			 raf = new RandomAccessFile(storefile, "rw");
			
			// write version
			raf.writeInt(VERSION);
			
			// write admin user
			adminUser.setPosition(raf.getFilePointer()); // admin user starts on position zero
			storeUser(adminUser, raf, true);
		}catch(IOException e){
			System.out.println("Error: "+e.getMessage());
			System.exit(1);
		}finally{
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					Logger.getLogger("userstore").warning(
							"Potential resource leak: could not close user store RAF (reason: "
									+ e.getMessage() + ")");
				}
			}
		}
		
		// set password
		if(!setPassword(adminUser.getPosition(), adminpass)){
			// password set failed :(
			clearPassword(adminpass);
			System.out.println("Initialisation failed. Could not set administrative password.");
			System.exit(1);
		}
		
		clearPassword(adminpass);
	}
	
	/**
	 * Store this user (except password) in the user store
	 * @param ds data stream to write to
	 * @param position position of the user record in user store
	 * @param unew indicates that it is a new user
	 */
	public void storeUser(User user, RandomAccessFile raf, boolean unew) throws IOException{
		
		// password - either skip or delete
		if(unew){
			byte[] pass = new byte[PASSWORD_BLOCK_LEN]; // 1K password block
			raf.write(pass);
		}else{
			raf.seek(user.getPosition()+PASSWORD_BLOCK_LEN);
		}
		
		// user name
		raf.writeInt(user.getUsername().length());
		byte[] uname = user.getUsername().getBytes("UTF-8");
		raf.write(uname);
	}
	
	private User restoreUser(RandomAccessFile raf) throws IOException{
		if(raf.length()-raf.getFilePointer() <= PASSWORD_BLOCK_LEN){
			throw new IOException("Corrupted user record detected. Password block is too small. ("+(raf.length()-raf.getFilePointer())+" instead of "
					              +PASSWORD_BLOCK_LEN+")");
		}
		
		// read over pass word block
		raf.seek(raf.getFilePointer()+PASSWORD_BLOCK_LEN);
		
		// read user name
		int unameLen = raf.readInt();
		byte[] uname = new byte[unameLen];
		if(raf.read(uname) != unameLen){
			throw new IOException("Corrupted user store. User record does not contain user name!");
		}
		
		// build user object
		User u = new User();
		u.setUsername(new String(uname, "UTF-8"));
		return u;
	}
	
	/**
	 * Checks if the given password matches the password of the user. Used for e.g. login.
	 * The given password will be hashed according to the configured method and checked afterwards. So if
	 * the hashing method is changed all currently stored passwords will become invalid.
	 * 
	 * @param user user to check
	 * @param password password to check
	 * @return <code>true</code> if the password matches
	 */
	public boolean checkPassword(User user, char[] password){
		KomorebiCoreConfig conf = new KomorebiCoreConfig();
		
		// hash and clear given password
		byte[] hashedPw = hashPassword(password, conf.getString("users.hashmethod"));
		if(hashedPw.length != PASSWORD_BLOCK_LEN){
			return false; // the password block is always the same size
		}
		
		// read configured password from file
		File usfile = new File(conf.getString("users.store"));
		if(!usfile.exists()){
			Logger.getLogger("userstore").severe("User store file does not exist!");
			return false;
		}
		
		byte[] filePw = new byte[PASSWORD_BLOCK_LEN];
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(usfile, "r");
			raf.seek(user.getPosition());
			if(raf.read(filePw) != PASSWORD_BLOCK_LEN){
				throw new IOException("User store corrupted: invalid password block length");
			}
		}catch(IOException e){
			Logger.getLogger("userstore").severe(e.getMessage());
			return false;
		}finally{
			if(raf != null){
				try{
					raf.close();
				}catch(Exception e){
					Logger.getLogger("userstore").warning("Potential ressource leak: could not close user store RAF (reason: "+e.getMessage()+")");
				}
			}
		}
		
		// check if hashed passwords do match
		for(int i=0; i<PASSWORD_BLOCK_LEN; ++i){
			if(hashedPw[i] != filePw[i]){
				return false; // passwords do not match
			}
		}
		
		return true;
	}
	
	/**
	 * Applies the given hash algorithm to the password. The password will be cleard afterwards.
	 * 
	 * @param password password string to be hashed
	 * @param method hashing method (currently supported: PLAIN)
	 * @return hashed password
	 */
	private byte[] hashPassword(char[] password, String method){
		byte[] hashed = new byte[PASSWORD_BLOCK_LEN];
		
		method = method.trim().toUpperCase();
		if("PLAIN".equals(method)){
			// warn about plaintext
			Logger.getLogger("userstore").warning("You are using PLANTEXT passwords. This is not recommended!");
			for(int i=0; i<(hashed.length<password.length?hashed.length:password.length); ++i){
				hashed[i] = (byte) password[i]; // ONLY ASCII
			}
		}else{
			Logger.getLogger("userstore").severe("Can not set password - Unknown hash method.");
			return hashed;
		}
		
		clearPassword(password);
		return hashed;
	}
	
	/**
	 * Sets and hashes a user password.
	 * 
	 * @param position position of the user record in the store
	 * @param password password (plain text)
	 * @return <code>true</code> if the password was set
	 */
	public boolean setPassword(long position, char[] password){
		KomorebiCoreConfig config = new KomorebiCoreConfig();
		
		byte[] filePass = hashPassword(password, config.getString("users.hashmethod"));
		
		// TODO implement SHA-2
		
		// RAF access
		try{
			RandomAccessFile usfile = new RandomAccessFile(config.getString("users.store"), "rw");
			usfile.seek(position);
			usfile.write(filePass);
			usfile.close();
		}catch(FileNotFoundException e){
			Logger.getLogger("userstore").severe("Password error: user store file not found");
			return false;
		}catch(IOException e){
			Logger.getLogger("userstore").severe("Can not set password: "+e.getMessage());
			return false;
		}
		
		clearPassword(password);
		return true;
	}
	
	private void clearPassword(char[] pw){
		String alphabet = "123xyz";
		Random r = new Random();
		for(int i=0; i<pw.length; ++i){
			pw[i] = alphabet.charAt(r.nextInt(alphabet.length()));
		}
	}
}
