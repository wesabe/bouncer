package com.wesabe.bouncer.auth.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.auth.LockedAccountException;

@RunWith(Enclosed.class)
public class LockedAccountExceptionTest {
	public static class A_Locked_Account_Exception {
		@Test
		public void itHasAPenaltyDuration() throws Exception {
			LockedAccountException e = new LockedAccountException(20);
			assertThat(e.getPenaltyDuration(), is(20));
		}
	}
}
