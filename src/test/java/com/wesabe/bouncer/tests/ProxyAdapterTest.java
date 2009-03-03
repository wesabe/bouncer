package com.wesabe.bouncer.tests;

import static org.mockito.Mockito.*;

import org.apache.http.HttpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.wesabe.bouncer.BackendService;
import com.wesabe.bouncer.ProxyAdapter;
import com.wesabe.bouncer.http.ProxyRequest;
import com.wesabe.bouncer.http.ProxyRequestFactory;
import com.wesabe.bouncer.http.ProxyResponse;
import com.wesabe.bouncer.http.ProxyResponseFactory;

@RunWith(Enclosed.class)
public class ProxyAdapterTest {
	
	private static class Proxy_Context {
		protected BackendService backendService;
		protected GrizzlyRequest request;
		@SuppressWarnings("unchecked")
		protected GrizzlyResponse response;
		protected ProxyResponse proxyResponse;
		protected ProxyRequest proxyRequest;
		protected ProxyRequestFactory requestFactory;
		protected ProxyResponseFactory responseFactory;
		protected ProxyAdapter adapter;

		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			this.response = mock(GrizzlyResponse.class);
			this.proxyRequest = mock(ProxyRequest.class);
			this.proxyResponse = mock(ProxyResponse.class);
			this.requestFactory = mock(ProxyRequestFactory.class);
			when(requestFactory.buildFromGrizzlyRequest(request)).thenReturn(proxyRequest);
			this.responseFactory = mock(ProxyResponseFactory.class);
			this.backendService = mock(BackendService.class);
			this.adapter = new ProxyAdapter(backendService, requestFactory, responseFactory);
		}
	}
	
	public static class Proxying_A_Request_Successfully extends Proxy_Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			when(backendService.execute(proxyRequest)).thenReturn(proxyResponse);
		}
		
		@Test
		public void itBuildsAProxyRequestFromTheIncomingRequest() throws Exception {
			adapter.service(request, response);
			
			verify(requestFactory).buildFromGrizzlyRequest(request);
		}
		
		@Test
		public void itSendsTheProxyRequestToTheBackendService() throws Exception {
			adapter.service(request, response);
			
			verify(backendService).execute(proxyRequest);
		}
		
		@Test
		public void itBuildsAnOutgoingResponseFromTheProxyResponse() throws Exception {
			adapter.service(request, response);
			
			verify(responseFactory).buildFromHttpResponse(proxyResponse, response);
		}
	}
	
	public static class Proxying_A_Request_With_An_Http_Error extends Proxy_Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			when(backendService.execute(proxyRequest)).thenThrow(new HttpException("something horrible has happened"));
		}
		
		@Test
		public void itRendersA502Error() throws Exception {
			adapter.service(request, response);
			
			verify(response).sendError(502);
		}
	}
	
	public static class Proxying_A_Request_With_An_Internal_Error extends Proxy_Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			when(backendService.execute(proxyRequest)).thenThrow(new RuntimeException("something horrible has happened"));
		}
		
		@Test
		public void itRendersA500Error() throws Exception {
			adapter.service(request, response);
			
			verify(response).sendError(500);
		}
	}
}
