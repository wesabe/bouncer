package com.wesabe.bouncer.auth.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableSet;
import com.wesabe.bouncer.auth.WesabeCredentials;
import com.wesabe.bouncer.auth.WesabeUserIdentity;

@RunWith(Enclosed.class)
public class WesabeUserIdentityTest {
	public static class A_Wesabe_User_Identity {
		private WesabeCredentials creds;

		@Before
		public void setup() {
			creds = new WesabeCredentials(1, "abcde");
		}
		
		@Test
		public void itHasASubjectContainingTheWesabeCredentialsInstance() {
			assertEquals(new WesabeUserIdentity(creds).getSubject().getPrincipals(), ImmutableSet.of(creds));
		}
		
		@Test
		public void itHasAWesabeCredentialsInstance() {
			assertEquals(new WesabeUserIdentity(creds).getUserPrincipal(), creds);
		}
	}
}
