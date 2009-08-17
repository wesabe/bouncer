package com.wesabe.bouncer.jetty.tests;

import static javax.servlet.http.HttpServletResponse.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;

import com.wesabe.bouncer.jetty.QuietErrorHandler;

@RunWith(Enclosed.class)
public class QuietErrorHandlerTest {
	public static class Handling_Errors {
		protected Request request;
		protected Response response;
		protected ErrorHandler handler;
		protected PrintWriter writer;
		protected StringWriter output;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(Request.class);
			this.response = mock(Response.class);
			this.handler = new QuietErrorHandler();
			
			this.output = new StringWriter();
			this.writer = new PrintWriter(output);
			when(response.getWriter()).thenReturn(writer);
		}
		
		@Test
		public void itHandles200s() throws Exception {
			when(response.getStatus()).thenReturn(SC_OK);
			
			assertHandles("\n");
		}
		
		@Test
		public void itHandles400s() throws Exception {
			when(response.getStatus()).thenReturn(SC_BAD_REQUEST);
			
			assertHandles("Your HTTP client sent a request that this server could not understand.\n");
		}
		
		@Test
		public void itHandles401s() throws Exception {
			when(response.getStatus()).thenReturn(SC_UNAUTHORIZED);
			
			assertHandles("This server could not verify that you are authorized to access " +
					"this resource.\n" +
					"You either supplied the wrong credentials (e.g., bad " +
					"password), or your HTTP client doesn't understand how to " +
					"supply the credentials required.\n");
		}
		
		@Test
		public void itHandles403s() throws Exception {
			when(response.getStatus()).thenReturn(SC_FORBIDDEN);
			
			assertHandles("You don't have permission to access the requested resource.\n");
		}
		
		@Test
		public void itHandles404s() throws Exception {
			when(response.getStatus()).thenReturn(SC_NOT_FOUND);
			
			assertHandles("The requested resource could not be found on this server.\n");
		}
		
		@Test
		public void itHandles405s() throws Exception {
			when(response.getStatus()).thenReturn(SC_METHOD_NOT_ALLOWED);
			when(request.getMethod()).thenReturn("PUT");
			
			assertHandles("The PUT method is not allowed for the requested resource.\n");
		}
		
		@Test
		public void itHandles406s() throws Exception {
			when(response.getStatus()).thenReturn(SC_NOT_ACCEPTABLE);
			
			assertHandles("The resource identified by the request is only capable of " +
					"generating response entities which have content " +
					"characteristics not acceptable according to the accept " +
					"headers sent in the request.\n");
		}
		
		@Test
		public void itHandles408s() throws Exception {
			when(response.getStatus()).thenReturn(SC_REQUEST_TIMEOUT);
			
			assertHandles("The server closed the network connection because your HTTP " +
					"client didn't finish the request within the specified time.\n");
		}
		
		@Test
		public void itHandles409s() throws Exception {
			when(response.getStatus()).thenReturn(SC_CONFLICT);
			
			assertHandles("The request could not be completed due to a conflict with " +
					"the current state of the resource.\n");
		}
		
		@Test
		public void itHandles410s() throws Exception {
			when(response.getStatus()).thenReturn(SC_GONE);
			
			assertHandles("The requested resource used to exist but no longer does.\n");
		}
		
		@Test
		public void itHandles411s() throws Exception {
			when(response.getStatus()).thenReturn(SC_LENGTH_REQUIRED);
			when(request.getMethod()).thenReturn("PUT");
			
			assertHandles("A request with the PUT method requires a valid Content-Length header.\n");
		}
		
		@Test
		public void itHandles412s() throws Exception {
			when(response.getStatus()).thenReturn(SC_PRECONDITION_FAILED);
			
			assertHandles("The precondition on the request for the resource failed positive evaluation.\n");
		}
		
		@Test
		public void itHandles413s() throws Exception {
			when(response.getStatus()).thenReturn(SC_REQUEST_ENTITY_TOO_LARGE);
			when(request.getMethod()).thenReturn("PUT");
			
			assertHandles("The PUT method does not allow the data transmitted, " +
					"or the data volume exceeds the capacity limit.\n");
		}
		
		@Test
		public void itHandles414s() throws Exception {
			when(response.getStatus()).thenReturn(SC_REQUEST_URI_TOO_LONG);
			
			assertHandles("The length of the requested URL exceeds the capacity " +
					"limit for this server. The request cannot be processed.\n");
		}
		
		@Test
		public void itHandles415s() throws Exception {
			when(response.getStatus()).thenReturn(SC_UNSUPPORTED_MEDIA_TYPE);
			
			assertHandles("The server does not support the media type transmitted in the request.\n");
		}
		
		@Test
		public void itHandles416s() throws Exception {
			when(response.getStatus()).thenReturn(SC_REQUESTED_RANGE_NOT_SATISFIABLE);
			
			assertHandles("The server cannot serve the requested byte range.\n");
		}
		
		@Test
		public void itHandles417s() throws Exception {
			when(response.getStatus()).thenReturn(SC_EXPECTATION_FAILED);
			
			assertHandles("The server could not meet the expectation given in the Expect request header.\n");
		}
		
		@Test
		public void itHandles500s() throws Exception {
			when(response.getStatus()).thenReturn(SC_INTERNAL_SERVER_ERROR);
			
			assertHandles("The server encountered an internal error and was unable to " +
							"complete your request.\n");
		}
		
		@Test
		public void itHandles501s() throws Exception {
			when(response.getStatus()).thenReturn(SC_NOT_IMPLEMENTED);
			
			assertHandles("The server does not support the action requested.\n");
		}
		
		@Test
		public void itHandles503s() throws Exception {
			when(response.getStatus()).thenReturn(SC_SERVICE_UNAVAILABLE);
			
			assertHandles("The server is temporarily unable to service your " +
							"request due to maintenance downtime or capacity " +
							"problems. Please try again later.\n");
		}
		
		@Test
		public void itHandlesNonSpecificErrors() throws Exception {
			when(response.getStatus()).thenReturn(SC_CONTINUE);
			
			assertHandles("Your request could not be processed: Continue\n");
		}
		
		protected void handle() throws Exception {
			handler.handle("/woo", request, request, response);
		}
		
		protected void assertHandles(String msg) throws Exception {
			handler.handle("/woo", request, request, response);
			
			verify(response).resetBuffer();
			
			assertThat(output.toString(), is(msg));
		}
	}
}
