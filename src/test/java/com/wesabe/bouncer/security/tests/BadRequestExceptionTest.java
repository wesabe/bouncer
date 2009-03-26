package com.wesabe.bouncer.security.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.wesabe.bouncer.security.BadRequestException;

@RunWith(Enclosed.class)
public class BadRequestExceptionTest {
	public static class An_Exception_About_A_Bad_Request {
		private GrizzlyRequest request;
		private Exception e;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			when(request.getRequestURI()).thenReturn("/dingo");
			when(request.getMethod()).thenReturn("GET");
			
			this.e = mock(Exception.class);
		}
		
		@Test
		public void itHasARequest() throws Exception {
			BadRequestException exception = new BadRequestException(request, e);
			assertSame(request, exception.getBadRequest());
		}
		
		@Test
		public void itHasAnUnderlyingCause() throws Exception {
			BadRequestException exception = new BadRequestException(request, e);
			assertSame(e, exception.getCause());
		}
		
		@Test
		public void itHasAnInformativeMessage() throws Exception {
			BadRequestException exception = new BadRequestException(request, e);
			assertEquals("GET request to /dingo rejected", exception.getMessage());
		}
	}
}
