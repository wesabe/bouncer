package com.wesabe.bouncer.security.validators.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.security.validators.DateHeaderValueValidator;

@RunWith(Enclosed.class)
public class DateHeaderValueValidatorTest {
	public static class Validing_Header_Field_Values {
		private DateHeaderValueValidator validator = new DateHeaderValueValidator();
		
		@Test
		public void itAcceptsValidDates() throws Exception {
			assertTrue(validator.isValid("Sun, 06 Nov 1994 08:49:37 GMT"));
			assertTrue(validator.isValid("Sunday, 06-Nov-94 08:49:37 GMT"));
			assertTrue(validator.isValid("Sun Nov  6 08:49:37 1994"));
		}
		
		@Test
		public void itDoesNotAcceptInvalidDates() throws Exception {
			assertFalse(validator.isValid(null));
			assertFalse(validator.isValid("Dog, 06 Nov 1994 08:49:37 GMT"));
			assertFalse(validator.isValid("Sun, 06 Dog 1994 08:49:37 GMT"));
			assertFalse(validator.isValid("Sun, 06 Nov XXXX 08:49:37 GMT"));
			assertFalse(validator.isValid("Sun, 06 Nov 1994 0d:49:37 GMT"));
			assertFalse(validator.isValid("Sun, 06 Nov 1994 08:f9:37 GMT"));
			assertFalse(validator.isValid("Sun, 06 Nov 1994 08:49:!! GMT"));
			assertFalse(validator.isValid("Sun, 06 Nov 1994 08:49:37 FFT"));
		}
	}
}
