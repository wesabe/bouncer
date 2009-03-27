package com.wesabe.bouncer.security.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.security.MethodValidator;

@RunWith(Enclosed.class)
public class MethodValidatorTest {
	public static class Validating_HTTP_Methods {
		private MethodValidator validator = new MethodValidator();
		
		@Test
		public void itAcceptsValidHttpMethods() throws Exception {
			assertTrue(validator.isValid("GET"));
			assertTrue(validator.isValid("get"));
			assertTrue(validator.isValid("post"));
			assertTrue(validator.isValid("put"));
			assertTrue(validator.isValid("delete"));
			assertTrue(validator.isValid("OPTIONS"));
			assertTrue(validator.isValid("HEAD"));
		}
		
		@Test
		public void itAcceptsNonStandardHttpMethods() throws Exception {
			assertTrue(validator.isValid("purge"));
			assertTrue(validator.isValid("DOIT"));
		}
		
		@Test
		public void itRejectsInvalidHttpMethods() throws Exception {
			assertFalse(validator.isValid("\0\0WOO"));
			assertFalse(validator.isValid("GET\nYAY"));
		}
	}
}
