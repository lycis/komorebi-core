package org.komorebi.core.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
	private static final String STRING_ENCODING = "UTF-8"; // encoding of stored strings

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

	/**
	 * Loads the data from the configured user store.
	 * @param password password in case the user store is encrypted. (use <code>null</code> if not)
	 * @return
	 */
	synchronized public boolean load(char[] password) {
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
				userMap.put(u.getName(), u);
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
	synchronized public void init(String file) {
		Console con = System.console();
		System.out.print("Administrator user name: ");
		String adminuser = con.readLine();
		System.out.print("Administrator password: ");
		char[] adminpass = con.readPassword();

		User adminUser = new User();
		adminUser.setName(adminuser);

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

		// grant all privileges to admin user
		// TODO fix
		for(String priv: Privilege.getPrivileges()){			
			// grant
			adminUser.setPrivilege(priv, true);
			if(!adminUser.hasPrivilege(priv)){
				System.out.println("WARNING: could not grant privilege \""+priv+"\"");
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
	private void storeUser(User user, RandomAccessFile raf, boolean unew) throws IOException{

		// 1. password - either skip or delete
		if(unew){
			byte[] pass = new byte[PASSWORD_BLOCK_LEN]; // 1K password block
			raf.write(pass);
		}else{
			raf.seek(user.getPosition()+PASSWORD_BLOCK_LEN);
		}

		// 2. user name
		// 2.a length of user name
		raf.writeInt(user.getName().length());
		// 2.b actual user name
		byte[] uname = user.getName().getBytes(STRING_ENCODING);
		raf.write(uname);

		// 3. credentials
		Set<String> locations = user.getLocations();
		// 3.a number of locations
		raf.writeInt(locations.size());
		for(String loc: locations){
			// 3.b location (length + name)
			raf.write(loc.length());
			raf.write(loc.getBytes(STRING_ENCODING));

			// 3.c number of credentials
			Set<String> credentials = user.getCredentials(loc);
			raf.write(credentials.size());

			for(String key: credentials){
				// 3.d key (length + key)
				raf.write(key.length());
				raf.write(key.getBytes(STRING_ENCODING));

				// 3.e value (length + key)
				String val = user.getCredentialValue(loc, key);
				raf.write(val.length());
				raf.write(val.getBytes(STRING_ENCODING));
			}
		}
		
		// TODO store privileges
	}

	private User restoreUser(RandomAccessFile raf) throws IOException{
		User u = new User(); // returned user object

		long startpos = raf.getFilePointer(); // position of user record
		u.setPosition(startpos);

		if(raf.length()-startpos <= PASSWORD_BLOCK_LEN){
			throw new IOException("Corrupted user record detected. Password block is too small. ("+(raf.length()-startpos)+" instead of "
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
		u.setName(new String(uname, STRING_ENCODING));

		// 3. read location credentials
		int locationCount = raf.readInt();
		for(int i=0;i<locationCount;++i){
			// read location name
			int locationStrLen = raf.readInt(); // 3.a length of location name
			byte[] locNameBArr = new byte[locationStrLen];
			if(raf.read(locNameBArr) != locationStrLen){ // 3.b location name
				throw new IOException("Corrupted user store. Credential location record is invalid.");
			}

			String location = new String(locNameBArr, STRING_ENCODING);

			// 3.c read number of credentials for location
			int numCredentials = raf.readInt();
			for(int j=0; j<numCredentials; ++j){
				// 3.d credential key (length + key)
				int keyLen = raf.readInt();
				byte[] keyBArr = new byte[keyLen];
				if(raf.read(keyBArr) != keyLen){
					throw new IOException("Corrupted user store. Credential key record is invalid.");
				}

				// 3.d credential key (length + key)
				int valLen = raf.readInt();
				byte[] valBArr = new byte[valLen];
				if(raf.read(valBArr) != valLen){
					throw new IOException("Corrupted user store. Credential value record is invalid.");
				}

				String credentialKey = new String(keyBArr, STRING_ENCODING);
				String credentialValue = new String(valBArr, STRING_ENCODING);
				u.setCredentialValue(location, credentialKey, credentialValue);
			}
		}
		
		// TODO restore privileges
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
	synchronized public boolean checkPassword(User user, char[] password){
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
			Logger.getLogger("userstore").warning("You are using PLAINTEXT passwords. This is not recommended!");
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
	synchronized public boolean setPassword(long position, char[] password){
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

	/**
	 * Clears a character array that was used to store a password by overwriting it with random
	 * characters.
	 * @param pw array to be cleared
	 */
	private void clearPassword(char[] pw){
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
		Random r = new Random();
		for(int i=0; i<pw.length; ++i){
			pw[i] = alphabet.charAt(r.nextInt(alphabet.length()));
		}
	}

	/**
	 * Returns the User object (permissions, connected services, etc.) associated with the user
	 * name. 
	 * @param username name of the user you wish to get
	 * @return the User object associated to the user or <code>null</code> if the user does not exist
	 */
	synchronized public User getUser(String username){
		return userMap.get(username);
	}
}
