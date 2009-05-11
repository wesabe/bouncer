package com.wesabe.bouncer.proxy.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.proxy.BackendFailureException;
import com.wesabe.bouncer.proxy.ProxyHttpExchange;

@RunWith(Enclosed.class)
public class BackendFailureExceptionTest {
	public static class A_Backend_Failure {
		private Throwable cause;
		private ProxyHttpExchange exchange;
		private BackendFailureException e;
		
		@Before
		public void setup() throws Exception {
			this.cause = mock(Throwable.class);
			this.exchange = mock(ProxyHttpExchange.class);
			when(exchange.toString()).thenReturn("GET BLAH");
			
			this.e = new BackendFailureException(exchange, cause);
		}
		
		@Test
		public void itHasADescriptiveMessage() throws Exception {
			assertThat(e.getMessage(), is("Backend connection failed on GET BLAH"));
		}
		
		@Test
		public void itHasACause() throws Exception {
			assertThat(e.getCause(), is(cause));
		}
		
		@Test
		public void itHasAnExchange() throws Exception {
			assertThat(e.getExchange(), is(exchange));
		}
	}
}
