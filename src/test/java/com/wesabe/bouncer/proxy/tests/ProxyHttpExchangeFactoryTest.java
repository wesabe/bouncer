package com.wesabe.bouncer.proxy.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.wesabe.bouncer.auth.WesabeCredentials;
import com.wesabe.bouncer.proxy.ProxyHttpExchange;
import com.wesabe.bouncer.proxy.ProxyHttpExchangeFactory;

@RunWith(Enclosed.class)
public class ProxyHttpExchangeFactoryTest {
	public static class A_Proxy_Http_Exchange_Factory {
		@Test
		public void itHasABackendUri() throws Exception {
			final URI backendUri = new URI("http://example.com");
			final ProxyHttpExchangeFactory factory = new ProxyHttpExchangeFactory(backendUri);
			assertThat(factory.getBackendUri(), is(backendUri));
		}
	}
	
	public static class Building_An_Exchange {
		private HttpServletRequest request;
		private HttpServletResponse response;
		private URI backendUri;
		private ProxyHttpExchangeFactory factory;
		
		@Before
		public void setup() throws Exception {
			this.request = mock(HttpServletRequest.class);
			when(request.getProtocol()).thenReturn("HTTP/1.0");
			when(request.getMethod()).thenReturn("POST");
			when(request.getRequestURI()).thenReturn("/dingofroop");
			when(request.getHeaderNames()).thenReturn(Collections.enumeration(ImmutableList.of()));
			when(request.getUserPrincipal()).thenReturn(new WesabeCredentials(200, "WOO"));
			this.backendUri = new URI("http://example.com");
			this.factory = new ProxyHttpExchangeFactory(backendUri);
		}
		
		@Test
		public void itBuildsAnExchangeWithTheProvidedRequestAndResponse() throws Exception {
			final ProxyHttpExchange exchange = factory.build(request, response);
			assertThat(exchange.getBackendUri(), is(backendUri));
			assertThat(exchange.getRequest(), is(request));
			assertThat(exchange.getResponse(), is(response));
		}
	}
}
