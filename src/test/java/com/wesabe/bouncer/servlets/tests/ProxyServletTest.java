package com.wesabe.bouncer.servlets.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpExchange;

import com.google.common.collect.Lists;
import com.wesabe.bouncer.proxy.ProxyHttpExchange;
import com.wesabe.bouncer.proxy.ProxyHttpExchangeFactory;
import com.wesabe.bouncer.servlets.ProxyServlet;

@RunWith(Enclosed.class)
public class ProxyServletTest {
	private static class FakeHttpClient extends HttpClient {
		private final List<HttpExchange> exchanges = Lists.newLinkedList();
		
		@Override
		protected void doStart() throws Exception {
		}
		
		@Override
		protected void doStop() throws Exception {
		}
		
		@Override
		public void send(HttpExchange exchange) throws IOException {
			exchanges.add(exchange);
		}
		
		public List<HttpExchange> getExchanges() {
			return exchanges;
		}
	}
	
	private static abstract class Context {
		protected FakeHttpClient httpClient;
		protected ProxyServlet servlet;
		protected ProxyHttpExchangeFactory factory;
		
		public void setup() throws Exception {
			Logger.getLogger("org.mortbay").setLevel(Level.OFF);
			this.httpClient = new FakeHttpClient();
			this.factory = mock(ProxyHttpExchangeFactory.class);
			this.servlet = new ProxyServlet(httpClient, factory);
		}
	}
	
	public static class A_Proxy_Servlet extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
		}
		
		@Test
		public void itHasAProxyHttpExchangeFactory() throws Exception {
			assertThat(servlet.getExchangeFactory(), is(factory));
		}
		
		@Test
		public void itHasAnHttpClient() throws Exception {
			assertThat(servlet.getHttpClient(), is((HttpClient) httpClient));
		}
	}
	
	public static class Initializing extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
		}
		
		@Test
		public void itStartsTheClient() throws Exception {
			servlet.init();
			
			assertThat(httpClient.isStarted(), is(true));
		}
	}
	
	public static class Destroying extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
			httpClient.start();
		}
		
		@Test
		public void itStopsTheClient() throws Exception {
			servlet.destroy();
			
			assertThat(httpClient.isStopped(), is(true));
		}
	}
	
	public static class Servicing_A_Request extends Context {
		private HttpServletRequest request;
		private HttpServletResponse response;
		private ProxyHttpExchange exchange;
		
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
			this.exchange = mock(ProxyHttpExchange.class);
			this.request = mock(HttpServletRequest.class);
			this.response = mock(HttpServletResponse.class);
			when(factory.build(request, response)).thenReturn(exchange);
			servlet.init();
		}
		
		@Test
		public void itProxiesTheRequestAndWaitsForCompletion() throws Exception {
			servlet.service(request, response);
			
			assertThat(httpClient.getExchanges().size(), is(1));
			assertThat((ProxyHttpExchange) httpClient.getExchanges().get(0), is(this.exchange));
			
			verify(exchange).waitForDone();
		}
	}
}
