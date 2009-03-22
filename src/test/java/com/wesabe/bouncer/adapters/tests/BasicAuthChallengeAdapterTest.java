package com.wesabe.bouncer.adapters.tests;

import static org.mockito.Mockito.*;

import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.wesabe.bouncer.adapters.BasicAuthChallengeAdapter;

@RunWith(Enclosed.class)
public class BasicAuthChallengeAdapterTest {
	@SuppressWarnings("unchecked")
	public static class Issuing_A_Challenge {
		private GrizzlyRequest request;
		private GrizzlyResponse response;
		private PrintWriter writer;
		private BasicAuthChallengeAdapter adapter;
		
		@Before
		public void setup() throws Exception {
			this.writer = mock(PrintWriter.class);
			
			this.request = mock(GrizzlyRequest.class);
			
			this.response = mock(GrizzlyResponse.class);
			when(response.getWriter()).thenReturn(writer);
			
			this.adapter = new BasicAuthChallengeAdapter("Wesabe API", "Authentication required.");
		}
		
		@Test
		public void itIssuesABasicAuthChallenge() throws Exception {
			adapter.service(request, response);
			
			InOrder inOrder = inOrder(response, writer);
			inOrder.verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Wesabe API\"");
			inOrder.verify(response).setStatus(401);
			inOrder.verify(response).getWriter();
			inOrder.verify(writer).append("Authentication required.\n\n");
			inOrder.verify(response).finishResponse();
		}
	}
}
