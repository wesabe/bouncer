package com.wesabe.bouncer.tests;

import static org.mockito.Mockito.*;

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
	
	public static class Proxying_A_Request {
		private BackendService backendService;
		
		private GrizzlyRequest request;
		@SuppressWarnings("unchecked")
		private GrizzlyResponse response;
		
		private ProxyResponse proxyResponse;
		private ProxyRequest proxyRequest;
		
		private ProxyRequestFactory requestFactory;
		private ProxyResponseFactory responseFactory;
		
		private ProxyAdapter adapter;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(GrizzlyRequest.class);
			this.response = mock(GrizzlyResponse.class);
			
			this.proxyRequest = mock(ProxyRequest.class);
			this.proxyResponse = mock(ProxyResponse.class);
			
			this.requestFactory = mock(ProxyRequestFactory.class);
			when(requestFactory.buildFromGrizzlyRequest(request)).thenReturn(proxyRequest);
			this.responseFactory = mock(ProxyResponseFactory.class);
			
			this.backendService = mock(BackendService.class);
			when(backendService.execute(proxyRequest)).thenReturn(proxyResponse);
			
			this.adapter = new ProxyAdapter(backendService, requestFactory, responseFactory);
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
}
