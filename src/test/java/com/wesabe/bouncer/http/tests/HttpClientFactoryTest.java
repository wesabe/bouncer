package com.wesabe.bouncer.http.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.http.HttpClientFactory;

@RunWith(Enclosed.class)
public class HttpClientFactoryTest {
	public static class Building_An_HTTP_Client {
		private final HttpClientFactory factory = new HttpClientFactory();
		
		@Test
		public void itUsesAThreadsafeConnectionManager() throws Exception {
			HttpClient client = factory.buildClient();
			
			assertTrue(client.getConnectionManager() instanceof ThreadSafeClientConnManager);
		}
		
		@Test
		public void itUsesAPlainSocketFactoryForHTTP() throws Exception {
			HttpClient client = factory.buildClient();
			
			Scheme http = client.getConnectionManager().getSchemeRegistry().get("http");
			
			assertNotNull(http);
			assertTrue(http.getSocketFactory() instanceof PlainSocketFactory);
			assertEquals(80, http.getDefaultPort());
		}
		
		@Test
		public void itNeverFollowsARedirection() throws Exception {
			DefaultHttpClient client = (DefaultHttpClient) factory.buildClient();
			
			HttpResponse response = mock(HttpResponse.class);
			when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 301, "NO ESTA AQUI"));
			
			assertFalse(client.getRedirectHandler().isRedirectRequested(response, null));
		}
		
		@Test
		public void itNeverRetriesARequest() throws Exception {
			DefaultHttpClient client = (DefaultHttpClient) factory.buildClient();
			
			assertFalse(client.getHttpRequestRetryHandler().retryRequest(mock(IOException.class), 1, mock(HttpContext.class)));
		}
		
		@Test
		public void itKeepsAConnectionAliveFor1Minute() throws Exception {
			DefaultHttpClient client = (DefaultHttpClient) factory.buildClient();
			
			assertEquals(60 * 1000, client.getConnectionKeepAliveStrategy().getKeepAliveDuration(null, null));
		}
	}
}
