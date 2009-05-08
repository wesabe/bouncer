package com.wesabe.bouncer.proxy.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wesabe.bouncer.auth.WesabeCredentials;
import com.wesabe.bouncer.proxy.ProxyHttpExchange;

@RunWith(Enclosed.class)
public class ProxyHttpExchangeTest {
	public static class Proxying_A_Request {
		private URI backend;
		private HttpServletRequest request;
		private HttpServletResponse response;
		private ServletInputStream inputStream;
		private WesabeCredentials principal;
		private final Map<String, String> requestHeaders = ImmutableMap.of(
			"Date", "Wed, 15 Nov 1995 06:25:24 GMT",
			"Accept", "application/json",
			"Last-Modified", "Wed, 15 Nov 1995 06:25:25 GMT",
			"Keep-Alive", "true",
			"X-Boggle", "Ninja!"
		);
		
		@Before
		public void setup() throws Exception {
			this.principal = new WesabeCredentials(300, "WOOHOO");
			
			this.backend = URI.create("http://example.com:8081/");
			
			this.inputStream = mock(ServletInputStream.class);
			
			this.request = mock(HttpServletRequest.class);
			when(request.getProtocol()).thenReturn("HTTP/1.0");
			when(request.getMethod()).thenReturn("POST");
			when(request.getRequestURI()).thenReturn("/dingofroop");
			when(request.getHeaderNames()).thenReturn(Collections.enumeration(requestHeaders.keySet()));
			when(request.getHeaders(anyString())).thenAnswer(new Answer<Enumeration<?>> () {
				@Override
				public Enumeration<?> answer(InvocationOnMock invocation) throws Throwable {
					final String name = (String) invocation.getArguments()[0];
					return Collections.enumeration(ImmutableList.of(requestHeaders.get(name)));
				}
			});
			when(request.getInputStream()).thenReturn(inputStream);
			when(request.getUserPrincipal()).thenReturn(principal);
			
			this.response = mock(HttpServletResponse.class);
		}
		
		private ProxyHttpExchange exchange() {
			return new ProxyHttpExchange(backend, request, response);
		}
		
		@Test
		public void itHasABackendUri() throws Exception {
			assertThat(exchange().getBackendUri(), is(backend));
		}
		
		@Test
		public void itHasARequest() throws Exception {
			assertThat(exchange().getRequest(), is(request));
		}
		
		@Test
		public void itHasAResponse() throws Exception {
			assertThat(exchange().getResponse(), is(response));
		}
		
		@Test
		public void itSetsTheWesabeAuthorizationHeader() throws Exception {
			assertThat(exchange().getRequestFields().getStringField("Authorization"), is("Wesabe MzAwOldPT0hPTw=="));
		}
		
		@Test
		public void itCopiesTheRequestsProtocolVersion() throws Exception {
			assertThat(exchange().getVersion(), is(10));
			
			verify(request).getProtocol();
		}
		
		@Test
		public void itCopiesTheRequestsMethod() throws Exception {
			assertThat(exchange().getMethod(), is("POST"));
		}
		
		@Test
		public void itCopiesTheRequestsURI() throws Exception {
			assertThat(exchange().getURI(), is("/dingofroop"));
		}
		
		@Test
		public void itCopiesTheRequestsQueryString() throws Exception {
			when(request.getQueryString()).thenReturn("q=1");
			assertThat(exchange().getURI(), is("/dingofroop?q=1"));
		}
		
		@Test
		public void itSetsTheBackendsHost() throws Exception {
			assertThat(exchange().getAddress().getHost(), is("example.com"));
		}
		
		@Test
		public void itSetsTheBackendsPort() throws Exception {
			assertThat(exchange().getAddress().getPort(), is(8081));
		}
		
		@Test
		public void itCopiesGeneralHttpHeadersToTheRequest() throws Exception {
			assertThat(exchange().getRequestFields().getStringField("Date"), is("Wed, 15 Nov 1995 06:25:24 GMT"));
		}
		
		@Test
		public void itCopiesRequestHttpHeadersToTheRequest() throws Exception {
			assertThat(exchange().getRequestFields().getStringField("Accept"), is("application/json"));
		}
		
		@Test
		public void itCopiesEntityHttpHeadersToTheRequest() throws Exception {
			assertThat(exchange().getRequestFields().getStringField("Last-Modified"), is("Wed, 15 Nov 1995 06:25:25 GMT"));
		}
		
		@Test
		public void itDoesNotCopyUnproxyableHttpHeadersToTheRequest() throws Exception {
			assertThat(exchange().getRequestFields().getStringField("Keep-Alive"), is(nullValue()));
		}
		
		@Test
		public void itDoesNotCopyNonstandardHttpHeadersToTheRequest() throws Exception {
			assertThat(exchange().getRequestFields().getStringField("X-Boggle"), is(nullValue()));
		}
		
		@Test
		public void itCopiesTheInputStreamIfThereIsAContentType() throws Exception {
			when(request.getContentType()).thenReturn("application/json");
			
			assertThat((ServletInputStream) exchange().getRequestContentSource(), is(inputStream));
		}
		
		@Test
		public void itDoesNotCopyTheInputStreamIfThereIsntAContentType() throws Exception {
			when(request.getContentType()).thenReturn(null);
			
			assertThat(exchange().getRequestContentSource(), is(nullValue()));
		}
		
		@Test
		public void itBubblesUpAnyErrorCopyingTheInputStream() throws Exception {
			when(request.getContentType()).thenReturn("application/json");
			when(request.getInputStream()).thenThrow(new IOException("THE BEES THEY'RE IN MY EYES"));
			
			try {
				exchange();
				fail("should have thrown a RuntimeException but didn't");
			} catch (RuntimeException e) {
				assertThat(e.getCause(), is(IOException.class));
			}
		}
	}
	
	public static class Proxying_A_Response {
		private URI backend;
		private HttpServletRequest request;
		private HttpServletResponse response;
		private ServletOutputStream outputStream;
		private ProxyHttpExchange exchange;
		
		@Before
		public void setup() throws Exception {
			this.backend = URI.create("http://example.com:8081/");
			this.request = mock(HttpServletRequest.class);
			when(request.getProtocol()).thenReturn("HTTP/1.0");
			when(request.getMethod()).thenReturn("POST");
			when(request.getRequestURI()).thenReturn("/dingofroop");
			when(request.getHeaderNames()).thenReturn(Collections.enumeration(ImmutableList.of()));
			when(request.getUserPrincipal()).thenReturn(new WesabeCredentials(200, "WOO"));
			this.outputStream = mock(ServletOutputStream.class);
			this.response = mock(HttpServletResponse.class);
			when(response.getOutputStream()).thenReturn(outputStream);
			this.exchange = new ProxyHttpExchange(backend, request, response);
		}
		
		@Test
		public void itCopiesTheResponseEntity() throws Exception {
			final Buffer content = mock(Buffer.class);
			
			exchange.getEventListener().onResponseContent(content);
			
			verify(content).writeTo(outputStream);
		}
		
		@Test
		public void itCopiesTheResponseStatus() throws Exception {
			exchange.getEventListener().onResponseStatus(null, 302, null);
			
			verify(response).setStatus(302);
		}
		
		@Test
		public void itCopiesGeneralHeadersFromTheResponse() throws Exception {
			exchange.getEventListener().onResponseHeader(new ByteArrayBuffer("Date"), new ByteArrayBuffer("blah"));
			
			verify(response).addHeader("Date", "blah");
		}
		
		@Test
		public void itCopiesEntityHeadersFromTheResponse() throws Exception {
			exchange.getEventListener().onResponseHeader(new ByteArrayBuffer("Content-Type"), new ByteArrayBuffer("application/json"));
			
			verify(response).addHeader("Content-Type", "application/json");
		}
		
		@Test
		public void itCopiesResponseHeadersFromTheResponse() throws Exception {
			exchange.getEventListener().onResponseHeader(new ByteArrayBuffer("Age"), new ByteArrayBuffer("viejo"));
			
			verify(response).addHeader("Age", "viejo");
		}
		
		@Test
		public void itDoesNotCopyUnproxyableHeadersFromTheResponse() throws Exception {
			exchange.getEventListener().onResponseHeader(new ByteArrayBuffer("Server"), new ByteArrayBuffer("internal"));
			
			verify(response, never()).addHeader("Server", "internal");
		}
		
		@Test
		public void itDoesNotCopyHeadersWithNullValuesFromTheResponse() throws Exception {
			exchange.getEventListener().onResponseHeader(new ByteArrayBuffer("Age"), null);
			
			verify(response, never()).addHeader("Age", null);
		}
	}
}
