package com.wesabe.bouncer.security.validators.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.security.validators.HeaderValueValidator;

@RunWith(Enclosed.class)
public class HeaderValueValidatorTest {
	public static class Validating_Header_Values {
		private HeaderValueValidator validator = new HeaderValueValidator();
		
		@Test
		public void itAcceptsValidHeaders() throws Exception {
			assertTrue(validator.isValid("close"));
			assertTrue(validator.isValid("application/json"));
		}
		
		@Test
		public void itDoesNotAcceptInvalidHeaders() throws Exception {
			assertFalse(validator.isValid("smuggle \n attack"));
			assertFalse(validator.isValid("null bytes \0\0\0"));
		}
	}
}
