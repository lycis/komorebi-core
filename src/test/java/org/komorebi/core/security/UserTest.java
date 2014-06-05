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
	
}
