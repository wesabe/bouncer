package com.wesabe.bouncer.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.WesabeCredentials;

@RunWith(Enclosed.class)
public class WesabeCredentialsTest {
	public static class A_Set_Of_Credentials {
		private final WesabeCredentials creds = new WesabeCredentials(30, "woo");

		@Test
		public void itHasAUserId() throws Exception {
			assertEquals(30, creds.getUserId());
		}
		
		@Test
		public void itHasAnAccountKey() throws Exception {
			assertEquals("woo", creds.getAccountKey());
		}
		
		@Test
		public void itHasAName() throws Exception {
			assertEquals("30", creds.getName());
		}
		
		@Test
		public void itEncodesItselfWithBase64() throws Exception {
			assertEquals("Wesabe MzA6d29v", creds.toString());
		}
	}
}
