package com.wesabe.bouncer.tests;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.wesabe.bouncer.AuthenticationAdapter;
import com.wesabe.bouncer.Authenticator;

@RunWith(Enclosed.class)
public class AuthenticationAdapterTest {
	private static abstract class Context {
		protected Authenticator authenticator;
		protected GrizzlyAdapter challengeAdapter;
		protected GrizzlyAdapter passthroughAdapter;
		protected AuthenticationAdapter adapter;
		protected GrizzlyRequest request;
		@SuppressWarnings("unchecked")
		protected GrizzlyResponse response;
		
		protected void setup() throws Exception {
			this.authenticator = mock(Authenticator.class);
			this.challengeAdapter = mock(GrizzlyAdapter.class);
			this.passthroughAdapter = mock(GrizzlyAdapter.class);
			
			this.request = mock(GrizzlyRequest.class);
			this.response = mock(GrizzlyResponse.class);
			
			this.adapter = new AuthenticationAdapter(authenticator, challengeAdapter, passthroughAdapter);
		}
	}
	
	public static class Handling_An_Unauthenticated_Request extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(authenticator.authenticate(request)).thenReturn(false);
		}
		
		@Test
		public void itShouldPassTheRequestToTheChallengeAdapter() throws Exception {
			adapter.service(request, response);
			
			InOrder inOrder = inOrder(authenticator, challengeAdapter);
			inOrder.verify(authenticator).authenticate(request);
			inOrder.verify(challengeAdapter).service(request, response);
		}
	}
	
	public static class Handling_An_Authenticated_Request extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(authenticator.authenticate(request)).thenReturn(true);
		}
		
		@Test
		public void itShouldPassTheRequestToThePassthroughAdapter() throws Exception {
			adapter.service(request, response);
			
			InOrder inOrder = inOrder(authenticator, passthroughAdapter);
			inOrder.verify(authenticator).authenticate(request);
			inOrder.verify(passthroughAdapter).service(request, response);
		}
	}
}
