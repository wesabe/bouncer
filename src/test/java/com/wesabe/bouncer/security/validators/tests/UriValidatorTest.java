package com.wesabe.bouncer.security.validators.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.security.validators.UriValidator;

@RunWith(Enclosed.class)
public class UriValidatorTest {
	public static class Validating_Malformed_URIs {
		private UriValidator validator = new UriValidator();
		
		@Test
		public void itRejectsURIsWithInvalidCharacters() throws Exception {
			assertFalse(validator.isValid("\0\n\n"));
		}
	}
	
	public static class Validating_WellFormed_URIs {
		private UriValidator validator = new UriValidator();
		
		@Test
		public void itAcceptsWellFormedURIs() throws Exception {
			assertTrue(validator.isValid("/dingo"));
		}
	}
}
