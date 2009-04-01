package com.wesabe.bouncer.security.normalizers.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.security.normalizers.MalformedValueException;

@RunWith(Enclosed.class)
public class MalformedValueExceptionTest {
	public static class Throwing_A_Malformed_Value_Exception {
		@Test
		public void itHasAMessage() throws Exception {
			MalformedValueException exception = new MalformedValueException("broken", 20);
			assertEquals("broken", exception.getMessage());
		}
		
		@Test
		public void itHasAValue() throws Exception {
			MalformedValueException exception = new MalformedValueException("broken", 20);
			assertEquals(Integer.valueOf(20), exception.getValue());
		}
		
		@Test
		public void itHasAnOptionalCause() throws Exception {
			IllegalArgumentException cause = new IllegalArgumentException("no");
			MalformedValueException exception = new MalformedValueException("broken", 20, cause);
			assertEquals(cause, exception.getCause());
		}
	}
}
