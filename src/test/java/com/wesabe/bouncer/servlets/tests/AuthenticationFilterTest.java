package com.wesabe.bouncer.servlets.tests;

import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

import com.wesabe.bouncer.auth.Authenticator;
import com.wesabe.bouncer.servlets.AuthenticationFilter;
import com.wesabe.servlet.SafeRequest;

@RunWith(Enclosed.class)
public class AuthenticationFilterTest {
	public static class Filtering_An_Authenticated_Request {
		private Request request;
		private Response response;
		private FilterChain chain;
		private Authenticator authenticator;
		private AuthenticationFilter filter;
		private Principal principal;
		
		@Before
		public void setup() throws Exception {
			this.authenticator = mock(Authenticator.class);
			this.filter = new AuthenticationFilter(authenticator, "Test API", "Gotta have a password.");
			
			this.request = mock(Request.class);
			this.response = mock(Response.class);
			this.chain = mock(FilterChain.class);
			this.principal = mock(Principal.class);
		}
		
		@Test
		public void itSetsTheUserPrincipalAndPassesTheRequestOn() throws Exception {
			when(authenticator.authenticate(request)).thenReturn(principal);
			
			filter.doFilter(new SafeRequest(request), response, chain);
			
			InOrder inOrder = inOrder(authenticator, request, chain);
			inOrder.verify(authenticator).authenticate(request);
			inOrder.verify(request).setUserPrincipal(principal);
			inOrder.verify(request).setAuthType("BASIC");
			inOrder.verify(chain).doFilter(request, response);
		}
	}
	
	public static class Filtering_An_Unauthenticated_Request {
		private Request request;
		private Response response;
		private FilterChain chain;
		private Authenticator authenticator;
		private AuthenticationFilter filter;
		private PrintWriter writer;
		
		@Before
		public void setup() throws Exception {
			this.authenticator = mock(Authenticator.class);
			this.filter = new AuthenticationFilter(authenticator, "Test API", "Gotta have a password.");
			
			this.request = mock(Request.class);
			
			this.writer = mock(PrintWriter.class);
			
			this.response = mock(Response.class);
			when(response.getWriter()).thenReturn(writer);
			
			this.chain = mock(FilterChain.class);
		}
		
		@Test
		public void itReturnsABasicAuthChallenge() throws Exception {
			when(authenticator.authenticate(request)).thenReturn(null);
			
			filter.doFilter(new SafeRequest(request), response, chain);
			
			InOrder inOrder = inOrder(authenticator, response, writer);
			inOrder.verify(authenticator).authenticate(request);
			inOrder.verify(response).setStatus(401);
			inOrder.verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Test API\"");
			inOrder.verify(writer).println("Gotta have a password.");
			inOrder.verify(writer).close();
		}
	}
	
	public static class Initializing_And_Destroying {
		private Authenticator authenticator;
		private AuthenticationFilter filter;
		
		@Before
		public void setup() throws Exception {
			this.authenticator = mock(Authenticator.class);
			this.filter = new AuthenticationFilter(authenticator, "Test API", "Gotta have a password.");
		}
		
		@Test
		public void itDoesntDoAnything() throws Exception {
			filter.destroy();
			filter.init(mock(FilterConfig.class));
		}
	}
}
