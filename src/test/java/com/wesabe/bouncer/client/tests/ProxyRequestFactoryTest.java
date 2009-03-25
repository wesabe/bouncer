package com.wesabe.bouncer.client.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.sun.grizzly.tcp.InputBuffer;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.util.buf.ByteChunk;
import com.wesabe.bouncer.client.ProxyRequest;
import com.wesabe.bouncer.client.ProxyRequestFactory;
import com.wesabe.bouncer.security.RequestHeaderSet;

@RunWith(Enclosed.class)
public class ProxyRequestFactoryTest {
	private static class StringInputBuffer implements InputBuffer {
		private final String content;
		private boolean alreadyRead = false;
		
		public StringInputBuffer(String content) {
			this.content = content;
		}
		
		@Override
		public int doRead(ByteChunk chunk, Request request) throws IOException {
			chunk.setBytes(content.getBytes(), 0, content.length());
			if (alreadyRead) {
				return -1;
			}
			
			this.alreadyRead = true;
			return 0;
		}
		
	}
	
	public static class Building_A_Request_From_A_Grizzly_Request_Without_An_Entity {
		private RequestHeaderSet headers;
		private ProxyRequestFactory factory;
		private GrizzlyRequest request;
		
		@Before
		public void setup() throws Exception {
			this.headers = new RequestHeaderSet();
			
			Request connectionRequest = new Request();
			connectionRequest.method().setString("GET");
			connectionRequest.requestURI().setString("/hello");
			connectionRequest.remoteAddr().setString("123.45.67.89");
			connectionRequest.getMimeHeaders().setValue("Date").setString("RIGHT NOW");
			connectionRequest.getMimeHeaders().setValue("Content-Type").setString("text/xml");
			connectionRequest.getMimeHeaders().setValue("Accept").setString("text/xml");
			connectionRequest.getMimeHeaders().setValue("X-Death").setString("FUEGO");
			connectionRequest.getMimeHeaders().setValue("Server").setString("ALSO FUEGO");
			
			this.request = new GrizzlyRequest();
			request.setRequest(connectionRequest);
			
			this.factory = new ProxyRequestFactory(headers);
		}
		
		private ProxyRequest getRequest() {
			final ProxyRequest proxyRequest = factory.buildFromGrizzlyRequest(request);
			return proxyRequest;
		}
		
		private String getRequestHeader(String headerName) {
			return Lists.newArrayList(getRequest().getHeaders(headerName)).toString();
		}
		
		@Test
		public void itHasTheOriginalMethod() throws Exception {
			assertEquals("GET", getRequest().getMethod());
		}
		
		@Test
		public void itHasTheOriginalRequestURI() throws Exception {
			assertEquals("/hello", getRequest().getURI().toString());
		}
		
		@Test
		public void itCopiesOverAllValidRequestHeaders() throws Exception {
			assertEquals("[Accept: text/xml]", getRequestHeader("Accept"));
		}
		
		@Test
		public void itCopiesOverAllValidGeneralHeaders() throws Exception {
			assertEquals("[Date: RIGHT NOW]", getRequestHeader("Date"));
		}
		
		@Test
		public void itCopiesOverAllValidEntityHeaders() throws Exception {
			assertEquals("[Content-Type: text/xml]", getRequestHeader("Content-Type"));
		}
		
		@Test
		public void itDoesNotCopyOverValidResponseHeaders() throws Exception {
			assertEquals("[]", getRequestHeader("Server"));
		}
		
		@Test
		public void itDoesNotCopyOverAnyOtherHeaders() throws Exception {
			assertEquals("[]", getRequestHeader("X-Death"));
		}
		
		@Test
		public void itSetsTheXForwardedForHeader() throws Exception {
			assertEquals("[X-Forwarded-For: 123.45.67.89]", getRequestHeader("X-Forwarded-For"));
		}
		
		@Test
		public void itHasNoEntity() throws Exception {
			assertFalse(getRequest().hasEntity());
		}
	}
	
	public static class Building_A_Request_From_Grizzly_Request_With_An_Entity {
		private RequestHeaderSet headers;
		private ProxyRequestFactory factory;
		private GrizzlyRequest request;
		
		@Before
		public void setup() throws Exception {
			this.headers = new RequestHeaderSet();
			
			Request connectionRequest = new Request();
			connectionRequest.method().setString("POST");
			connectionRequest.requestURI().setString("/hello");
			connectionRequest.remoteAddr().setString("123.45.67.89");
			connectionRequest.setInputBuffer(new StringInputBuffer("blah blah blah"));
			connectionRequest.setContentLength(14);
			
			this.request = new GrizzlyRequest();
			request.setRequest(connectionRequest);
			
			this.factory = new ProxyRequestFactory(headers);
		}
		
		@Test
		public void itHasAnEntity() throws Exception {
			final ProxyRequest proxyRequest = factory.buildFromGrizzlyRequest(request);
			assertTrue(proxyRequest.hasEntity());
			StringBuffer out = new StringBuffer();
		    byte[] b = new byte[4096];
		    for (int n; (n = proxyRequest.getEntity().getContent().read(b)) != -1;) {
		        out.append(new String(b, 0, n));
		    }
		    assertEquals("blah blah blah", out.toString());
		}
	}
}
