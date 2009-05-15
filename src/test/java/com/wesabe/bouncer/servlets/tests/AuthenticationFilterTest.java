package com.wesabe.bouncer.servlets.tests;

import static org.mockito.Mockito.*;

import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

import com.wesabe.bouncer.auth.Authenticator;
import com.wesabe.bouncer.auth.LockedAccountException;
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
			this.filter = new AuthenticationFilter(authenticator, "Test API");
			
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
		private HttpServletResponse response;
		private FilterChain chain;
		private Authenticator authenticator;
		private AuthenticationFilter filter;
		
		@Before
		public void setup() throws Exception {
			this.authenticator = mock(Authenticator.class);
			this.filter = new AuthenticationFilter(authenticator, "Test API");
			this.request = mock(Request.class);
			this.response = mock(HttpServletResponse.class);
			this.chain = mock(FilterChain.class);
		}
		
		@Test
		public void itReturnsABasicAuthChallenge() throws Exception {
			when(authenticator.authenticate(request)).thenReturn(null);
			
			final SafeRequest safeRequest = new SafeRequest(request);
			filter.doFilter(safeRequest, response, chain);
			
			verify(chain, never()).doFilter(safeRequest, response);
			
			InOrder inOrder = inOrder(authenticator, response);
			inOrder.verify(authenticator).authenticate(request);
			inOrder.verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Test API\"");
			inOrder.verify(response).sendError(401);
		}
	}
	
	public static class Filtering_A_Request_For_A_Locked_Account {
		private Request request;
		private HttpServletResponse response;
		private FilterChain chain;
		private Authenticator authenticator;
		private AuthenticationFilter filter;

		@Before
		public void setup() throws Exception {
			this.authenticator = mock(Authenticator.class);
			this.filter = new AuthenticationFilter(authenticator, "Test API");
			this.request = mock(Request.class);
			this.response = mock(HttpServletResponse.class);
			this.chain = mock(FilterChain.class);
			
			when(authenticator.authenticate(request)).thenThrow(new LockedAccountException(200));
		}
		
		@Test
		public void itReturnsAServiceUnavailableResponse() throws Exception {
			final SafeRequest safeRequest = new SafeRequest(request);
			filter.doFilter(safeRequest, response, chain);
			
			verify(chain, never()).doFilter(safeRequest, response);
			
			InOrder inOrder = inOrder(authenticator, response);
			inOrder.verify(authenticator).authenticate(request);
			inOrder.verify(response).setIntHeader("Retry-After", 200);
			inOrder.verify(response).sendError(503);
		}
	}
	
	public static class Initializing_And_Destroying {
		private Authenticator authenticator;
		private AuthenticationFilter filter;
		
		@Before
		public void setup() throws Exception {
			this.authenticator = mock(Authenticator.class);
			this.filter = new AuthenticationFilter(authenticator, "Test API");
		}
		
		@Test
		public void itDoesntDoAnything() throws Exception {
			filter.destroy();
			filter.init(mock(FilterConfig.class));
		}
	}
}
