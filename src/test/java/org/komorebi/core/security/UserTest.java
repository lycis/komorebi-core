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
	
	
	// TODO test credentials with multiple values
	
	// TODO test privileges
	@Test
	public void testPrivileges(){
		User user = new User();
		
		assertFalse("administrator privilege was set on new user", user.hasPrivilege(Privilege.ADMINISTRATOR));
		
		user.setPrivilege(Privilege.ADMINISTRATOR, true);
		assertTrue("administrator privilege was not granted", user.hasPrivilege(Privilege.ADMINISTRATOR));
		
		user.setPrivilege(Privilege.ADMINISTRATOR, false);
		assertFalse("administrator privilege was not revoked", user.hasPrivilege(Privilege.ADMINISTRATOR));
	}
}
