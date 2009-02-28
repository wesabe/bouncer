package com.wesabe.bouncer.tests;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.wesabe.bouncer.BasicAuthChallengeAdapter;

@RunWith(Enclosed.class)
public class BasicAuthChallengeAdapterTest {
	@SuppressWarnings("unchecked")
	public static class Issuing_A_Challenge {
		private GrizzlyRequest request;
		private GrizzlyResponse response;
		private BasicAuthChallengeAdapter adapter;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			this.response = mock(GrizzlyResponse.class);
			
			this.adapter = new BasicAuthChallengeAdapter("Wesabe API", "Authentication required.");
		}
		
		@Test
		public void itIssuesABasicAuthChallenge() throws Exception {
			adapter.service(request, response);
			
			InOrder inOrder = inOrder(response);
			inOrder.verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Wesabe API\"");
			inOrder.verify(response).sendError(401, "Authentication required.");
		}
	}
}
