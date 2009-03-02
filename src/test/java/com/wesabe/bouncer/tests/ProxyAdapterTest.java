package com.wesabe.bouncer.tests;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;

import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.tcp.http11.GrizzlyOutputBuffer;
import com.sun.grizzly.tcp.http11.GrizzlyOutputStream;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.grizzly.util.buf.ByteChunk;
import com.wesabe.bouncer.BackendService;
import com.wesabe.bouncer.HttpHeaders;
import com.wesabe.bouncer.ProxyAdapter;

@RunWith(Enclosed.class)
public class ProxyAdapterTest {
	
	public static class Proxying_A_GET_Request {
		private BackendService backendService;
		private GrizzlyRequest request;
		private GrizzlyOutputBuffer responseEntity;
		@SuppressWarnings("unchecked")
		private GrizzlyResponse response;
		private HttpResponse proxyResponse;
		private ProxyAdapter adapter;
		private StatusLine status;
		private Header[] proxyResponseHeaders;
		private HttpHeaders httpHeaders;
		private HttpEntity proxyEntity;
		@SuppressWarnings("unchecked")
		private Response connectionResponse;
		
		@Before
		public void setup() throws Exception {
			this.httpHeaders = mock(HttpHeaders.class);
			
			this.proxyResponseHeaders = new Header[] {
				new BasicHeader("Server", "Internal Sensitive"),
				new BasicHeader("Content-Type", "application/json"),
				new BasicHeader("X-Death-Face-McGee", "DO NOT SHOW THIS TO ANYONE"),
			};
			
			this.status = mock(StatusLine.class);
			when(status.getStatusCode()).thenReturn(301);
			
			this.proxyEntity = spy(new StringEntity("I am from the proxy."));
			
			this.proxyResponse = mock(HttpResponse.class);
			when(proxyResponse.getStatusLine()).thenReturn(status);
			when(proxyResponse.getAllHeaders()).thenReturn(proxyResponseHeaders);
			when(proxyResponse.getEntity()).thenReturn(proxyEntity);
			
			this.backendService = mock(BackendService.class);
			when(backendService.execute(any(HttpUriRequest.class))).thenReturn(proxyResponse);
			
			this.request = spy(new GrizzlyRequest());
			Request connectionRequest = new Request();
			connectionRequest.getMimeHeaders().setValue("Accept").setString("application/json");
			connectionRequest.getMimeHeaders().setValue("X-Death-Face-McGee").setString("DO NOT SHOW THIS TO ANYONE");
			request.setRequest(connectionRequest);
			doReturn("1.2.3.4").when(request).getRemoteAddr();
			doReturn("GET").when(request).getMethod();
			doReturn("/hello").when(request).getRequestURI();
			
			
			this.response = mock(GrizzlyResponse.class);
			this.responseEntity = new GrizzlyOutputBuffer();
			this.connectionResponse = mock(Response.class);
			responseEntity.setResponse(connectionResponse);
			when(response.getOutputStream()).thenReturn(new GrizzlyOutputStream(responseEntity));
			
			this.adapter = new ProxyAdapter(backendService, httpHeaders);
		}
		
		@Test
		public void itRespondsWithTheEntityOfTheProxyResponse() throws Exception {
			adapter.service(request, response);
			
			InOrder inOrder = inOrder(connectionResponse);
			inOrder.verify(connectionResponse).isCommitted();
			inOrder.verify(connectionResponse).getContentLength();
			inOrder.verify(connectionResponse).doWrite(argThat(anyObjWithToString("I am from the proxy.")));
			inOrder.verify(connectionResponse).finish();
			verifyNoMoreInteractions(connectionResponse);
		}
		
		@Test
		public void itRespondsWithANullEntityIfTheProxyResponseHasNoEntity() throws Exception {
			doReturn(null).when(proxyResponse).getEntity();
			
			adapter.service(request, response);
			
			verifyNoMoreInteractions(connectionResponse);
		}
		
		@Test
		public void itRespondsWithoutAnEntityIfTheProxyReponseHasNoEntity() throws Exception {
			when(proxyResponse.getEntity()).thenReturn(null);
			
			adapter.service(request, response);
			
			verify(proxyEntity, never()).getContent();
		}

		@Test
		public void itSanitizesTheProxiedRequest() throws Exception {
			when(httpHeaders.isValidRequestHeader("Accept")).thenReturn(true);
			when(httpHeaders.isValidRequestHeader("X-Death-Face-McGee")).thenReturn(false);
			
			adapter.service(request, response);
			
			verify(backendService).execute(argThat(new ArgumentMatcher<HttpUriRequest>() {
				@Override
				public boolean matches(Object argument) {
					HttpUriRequest request = (HttpUriRequest) argument;
					try {
						return request.getMethod().equals("GET") &&
								request.getURI().equals(new URI("/hello")) &&
								request.getFirstHeader("X-Death-Face-McGee") == null &&
								request.getFirstHeader("Accept").getValue().equals("application/json") &&
								request.getFirstHeader("X-Forwarded-For").getValue().equals("1.2.3.4")
								;
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
				}
			}));
		}
		
		@Test
		public void itSanitizesTheProxiedResponse() throws Exception {
			when(httpHeaders.isValidResponseHeader("Server")).thenReturn(true);
			when(httpHeaders.isValidResponseHeader("Content-Type")).thenReturn(true);
			when(httpHeaders.isValidResponseHeader("X-Death-Face-McGee")).thenReturn(false);
			
			adapter.service(request, response);
			
			InOrder inOrder = inOrder(response, httpHeaders);
			inOrder.verify(response).setStatus(301);
			inOrder.verify(httpHeaders).isValidResponseHeader("Server");
			inOrder.verify(response).addHeader("Server", "Internal Sensitive");
			inOrder.verify(httpHeaders).isValidResponseHeader("Content-Type");
			inOrder.verify(response).addHeader("Content-Type", "application/json");
			inOrder.verify(httpHeaders).isValidResponseHeader("X-Death-Face-McGee");
			inOrder.verify(response, never()).setHeader("X-Death-Face-McGee", "DO NOT SHOW THIS TO ANYONE");
			inOrder.verify(response).setHeader("Server", "Wesabe");
			inOrder.verify(response).finishResponse();
		}
		
		private ArgumentMatcher<ByteChunk> anyObjWithToString(final String string) {
			return new ArgumentMatcher<ByteChunk>() {
				@Override
				public boolean matches(Object argument) {
					return argument.toString().equals(string);
				}
			};
		}
	}
}
