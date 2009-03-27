package com.wesabe.bouncer.security.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.wesabe.bouncer.security.BadRequestException;
import com.wesabe.bouncer.security.SafeRequest;

@RunWith(Enclosed.class)
public class SafeRequestTest {
	public static class Rejecting_Invalid_URIs {
		private GrizzlyRequest request;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			when(request.getRequestURI()).thenReturn("\0\0\0\0\n");
		}
		
		@Test
		public void itThrowsABadRequestException() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			try {
				safeRequest.getURI();
				fail("should have rejected the invalid URI");
			} catch (BadRequestException e) {
				assertSame(request, e.getBadRequest());
			}
		}
	}
	
	public static class Rejecting_Absolute_URIs {
		private GrizzlyRequest request;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			when(request.getRequestURI()).thenReturn("http://some.other.server/secret");
		}
		
		@Test
		public void itThrowsABadRequestException() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			try {
				safeRequest.getURI();
				fail("should have rejected the invalid URI");
			} catch (BadRequestException e) {
				assertSame(request, e.getBadRequest());
			}
		}
	}
	
	public static class Accepting_Valid_URIs {
		private GrizzlyRequest request;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			when(request.getRequestURI()).thenReturn("/dingo");
		}
		
		@Test
		public void itReturnsTheValidURI() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			assertEquals(URI.create("/dingo"), safeRequest.getURI());
		}
	}
	
	public static class Accepting_SingleEncoded_URIs {
		private GrizzlyRequest request;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			when(request.getRequestURI()).thenReturn("/%2E%2E%2f");
		}
		
		@Test
		public void itReturnsTheFullyDecodedURI() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			assertEquals(URI.create("/../"), safeRequest.getURI());
		}
	}
	
	public static class Accepting_DoubleEncoded_URIs {
		private GrizzlyRequest request;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			when(request.getRequestURI()).thenReturn("/%252E%252E%252F");
		}
		
		@Test
		public void itReturnsTheFullyDecodedURI() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			assertEquals(URI.create("/../"), safeRequest.getURI());
		}
	}
	
	public static class Accepting_TripleEncoded_URIs {
		private GrizzlyRequest request;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			when(request.getRequestURI()).thenReturn("/%2F%25252E%25252E%25252F");
		}
		
		@Test
		public void itThrowsABadRequestException() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			try {
				safeRequest.getURI();
				fail("should have rejected the invalid URI");
			} catch (BadRequestException e) {
				assertSame(request, e.getBadRequest());
			}
		}
	}
	
	public static class Enumerating_Headers {
		private Request connectionRequest;
		private GrizzlyRequest request;
		
		@Before
		public void setup() throws Exception {
			this.connectionRequest = new Request();
			connectionRequest.getMimeHeaders().addValue("Host").setString("api.wesabe.com");
			connectionRequest.getMimeHeaders().addValue("Accept").setString("application/json");
			connectionRequest.getMimeHeaders().addValue("ETag").setString("woo");
			connectionRequest.getMimeHeaders().addValue("User-Agent").setString("\n\n \0\0 EVIL");
			connectionRequest.getMimeHeaders().addValue("Expires").setString("not a date");
			connectionRequest.getMimeHeaders().addValue("Expires").setString("Sun, 06 Nov 1994 08:49:37 GMT");
			connectionRequest.getMimeHeaders().addValue("Date").setString("not a date");
			connectionRequest.getMimeHeaders().addValue("Date").setString("Sun, 06 Nov 1994 08:49:37 GMT");
			connectionRequest.getMimeHeaders().addValue("X-Non-Standard").setString("Kilts, dude. Kilts.");
			
			this.request = new GrizzlyRequest();
			request.setRequest(connectionRequest);
		}
		
		@Test
		public void itRejectsNonRequestHeaders() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			
			assertEquals(ImmutableList.of(), safeRequest.getHeaders().get("ETag"));
		}
		
		@Test
		public void itRejectsInvalidHeaderFields() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			
			assertEquals(ImmutableList.of(), safeRequest.getHeaders().get("User-Agent"));
		}
		
		@Test
		public void itMalformedDateHeaderFields() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			
			assertEquals(ImmutableList.of("Sun, 06 Nov 1994 08:49:37 GMT"), safeRequest.getHeaders().get("Date"));
		}
		
		@Test
		public void itMalformedExpiresHeaderFields() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			
			assertEquals(ImmutableList.of("Sun, 06 Nov 1994 08:49:37 GMT"), safeRequest.getHeaders().get("Expires"));
		}
		
		@Test
		public void itAcceptsValidHeaders() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			
			assertEquals(ImmutableList.of("api.wesabe.com"), safeRequest.getHeaders().get("host"));
		}
	}
	
	public static class Accessing_The_Request_Entity {
		private GrizzlyRequest request;
		private InputStream stream;
		
		@Before
		public void setup() throws Exception {
			this.stream = mock(InputStream.class);
			
			this.request = mock(GrizzlyRequest.class);
			when(request.getStream()).thenReturn(stream);
		}
		
		@Test
		public void itPassesTheStreamStraightThrough() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			
			assertSame(stream, safeRequest.getEntity());
		}
	}
	
	public static class Accessing_The_Remote_IP {
		private GrizzlyRequest request;
		private String ip;
		
		@Before
		public void setup() throws Exception {
			this.ip = "1.3.11.9";
			
			this.request = mock(GrizzlyRequest.class);
			when(request.getRemoteAddr()).thenReturn(ip);
		}
		
		@Test
		public void itPassesTheIPStraightThrough() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			
			assertSame(ip, safeRequest.getRemoteAddr());
		}
	}
	
	public static class Accessing_The_Method {
		private GrizzlyRequest request;
		private String method;
		
		@Before
		public void setup() throws Exception {
			this.method = "GET";
			
			this.request = mock(GrizzlyRequest.class);
			when(request.getMethod()).thenReturn(method);
		}
		
		@Test
		public void itPassesTheMethodStraightThrough() throws Exception {
			SafeRequest safeRequest = new SafeRequest(request);
			
			assertSame(method, safeRequest.getMethod());
		}
	}
}
