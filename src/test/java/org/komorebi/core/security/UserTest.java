package org.komorebi.core.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class UserTest {

	/**
	 * Check if setCredentialValue is working correctly with no previous values in the user.
	 */
	@Test
	public void testCredentialsLocationSingle() {
		User user = new User();
		user.setCredentialValue("local", "foo", "bar");
		
		Set<String> locations = user.getLocations();
		assertTrue("location was not inserted", locations.size() == 1);
		assertTrue("location is not 'local'", "local".equals(locations.iterator().next()));
		
		Set<String> keys = user.getCredentials("local");
		assertTrue("key count for location 'local' does not match", keys.size() == 1);
		assertTrue("key 'foo' not found for location 'local'", "foo".equals(keys.iterator().next()));
		
		assertTrue("key value incorrect", "bar".equals(user.getCredentialValue("local", "foo")));
	}
	
	
	/**
	 * Check if granting and revoking privileges works for all supported privileges.
	 */
	@Test
	public void testGrantRevokePrivileges(){
		User user = new User();
		
		for(String priv: Privilege.getPrivileges()){
			assertFalse("privilege '"+priv+"' was set on new user", user.hasPrivilege(priv));
			
			// grant
			user.setPrivilege(priv, true);
			assertTrue("privilege '"+priv+"' was not granted", user.hasPrivilege(priv));
			
			// revoke
			user.setPrivilege(priv, false);
			assertFalse("privilege '"+priv+"' was not revoked", user.hasPrivilege(priv));
		}
	}
	
	/**
	 * Try adding a single location credential to a user.
	 */
	@Test
	public void testAddLocationCredentialSingle(){
		User user = new User();
		user.setCredentialValue("testlocation", "testkey", "1234");
		assertTrue("test credentials were not added", "1234".equals(user.getCredentialValue("testlocation", "testkey")));
	}
	
	/**
	 * Try adding multiple location credential to a user.
	 */
	@Test
	public void testAddLocationCredentials(){
		User user = new User();
		user.setCredentialValue("testlocation0", "k0", "0");
		user.setCredentialValue("testlocation0", "k1", "A");
		user.setCredentialValue("testlocation1", "kA", "0");
		user.setCredentialValue("testlocation1", "kB", "1");
		
		assertTrue("test credentials #0 were not added", "0".equals(user.getCredentialValue("testlocation0", "k0")));
		assertTrue("test credentials #1 were not added", "A".equals(user.getCredentialValue("testlocation0", "k1")));
		assertTrue("test credentials #2 were not added", "0".equals(user.getCredentialValue("testlocation1", "kA")));
		assertTrue("test credentials #3 were not added", "1".equals(user.getCredentialValue("testlocation1", "kB")));
	}
}
