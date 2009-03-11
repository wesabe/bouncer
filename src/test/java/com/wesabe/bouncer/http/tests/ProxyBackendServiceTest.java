package com.wesabe.bouncer.http.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import com.wesabe.bouncer.http.HttpClientFactory;
import com.wesabe.bouncer.http.ProxyBackendService;
import com.wesabe.bouncer.http.ProxyRequest;

@RunWith(Enclosed.class)
public class ProxyBackendServiceTest {
	public static class Initializing {
		@Test
		public void itBuildsAnHTTPClient() throws Exception {
			HttpClientFactory factory = mock(HttpClientFactory.class);
			
			new ProxyBackendService(URI.create("http://0.0.0.0"), factory);
			
			verify(factory).buildClient();
		}
	}
	
	public static class Executing_An_HTTP_Request {
		private HttpClientFactory factory;
		private HttpClient client;
		private ProxyBackendService service;
		private ProxyRequest request;
		private HttpResponse response;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(ProxyRequest.class);
			when(request.getURI()).thenReturn(URI.create("/woo"));
			
			this.response = mock(HttpResponse.class);
			
			this.client = mock(HttpClient.class);
			when(client.execute(request)).thenReturn(response);
			
			this.factory = mock(HttpClientFactory.class);
			when(factory.buildClient()).thenReturn(client);
			
			this.service = new ProxyBackendService(URI.create("http://0.0.0.0"), factory);
		}
		
		@Test
		public void itResolvesTheRequestURIForTheBackendURIBeforeExecution() throws Exception {
			service.execute(request);
			
			InOrder inOrder = inOrder(client, request);
			inOrder.verify(request).setURI(URI.create("http://0.0.0.0/woo"));
			inOrder.verify(client).execute(request);
		}
		
		@Test
		public void itExecutesTheRequestAndReturnsTheResponse() throws Exception {
			assertEquals(response, service.execute(request));
		}
	}
}
