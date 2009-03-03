package com.wesabe.bouncer.http.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.ProtocolVersion;
import org.apache.http.entity.InputStreamEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.sun.grizzly.tcp.OutputBuffer;
import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.grizzly.util.buf.ByteChunk;
import com.wesabe.bouncer.HttpHeaders;
import com.wesabe.bouncer.http.ProxyResponse;
import com.wesabe.bouncer.http.ProxyResponseFactory;

@RunWith(Enclosed.class)
public class ProxyResponseFactoryTest {
	private static class StringOutputBuffer implements OutputBuffer {
		private String buffer = "";
		
		@SuppressWarnings("unchecked")
		@Override
		public int doWrite(ByteChunk chunk, Response response) throws IOException {
			String chunkAsString = new String(chunk.getBytes());
			this.buffer += chunkAsString;
			return chunkAsString.length();
		}
		
	}
	
	public static class Building_A_Response_From_A_Proxy_Response_Without_An_Entity {
		private HttpHeaders httpHeaders = new HttpHeaders();
		private ProxyResponseFactory factory = new ProxyResponseFactory("Wesabe", httpHeaders);
		private ProxyResponse proxyResponse;
		@SuppressWarnings("unchecked")
		private GrizzlyResponse response;
		
		
		@Before
		@SuppressWarnings("unchecked")
		public void setup() throws Exception {
			this.proxyResponse = new ProxyResponse(
				new ProtocolVersion("http", 1, 1), 301, "Permanently Moved"
			);
			proxyResponse.setHeader("Date", "RIGHT NOW");
			proxyResponse.setHeader("Location", "https://api.wesabe.com/v2/blah");
			proxyResponse.setHeader("Server", "Snooper Secret");
			proxyResponse.setHeader("Accept", "STUFF");
			proxyResponse.setHeader("X-Left-Field", "All babies are ugly.");
			
			Response connectionResponse = new Response();
			this.response = new GrizzlyResponse();
			response.setResponse(connectionResponse);
			
			factory.buildFromHttpResponse(proxyResponse, response);
		}
		
		private String getHeaderValues(String headerName) {
			List<String> headers = Lists.newArrayList();
			for (String headerValue : response.getHeaderValues(headerName)) {
				headers.add(headerName + ": " + headerValue);
			}
			return headers.toString();
		}
		
		@Test
		public void itCopiesTheStatusLine() throws Exception {
			assertEquals(301, response.getStatus());
		}
		
		@Test
		public void itCopiesValidResponseHeaders() throws Exception {
			assertEquals("[Location: https://api.wesabe.com/v2/blah]", getHeaderValues("Location"));
		}
		
		@Test
		public void itCopiesValidGeneralHeaders() throws Exception {
			assertEquals("[Date: RIGHT NOW]", getHeaderValues("Date"));
		}
		
		@Test
		public void itDoesNotCopyValidRequestHeaders() throws Exception {
			assertEquals("[]", getHeaderValues("Accept"));
		}
		
		@Test
		public void itDoesNotCopyInvalidRequestHeaders() throws Exception {
			assertEquals("[]", getHeaderValues("X-Left-Field"));
		}
		
		@Test
		public void itOverwritesTheServerHeader() throws Exception {
			assertEquals("[Server: Wesabe]", getHeaderValues("Server"));
		}
		
		@Test
		public void itCommitsTheResponse() throws Exception {
			assertTrue(response.isCommitted());
		}
	}
	
	public static class Building_A_Response_From_A_Proxy_Response_With_An_Entity {
		private HttpHeaders httpHeaders = new HttpHeaders();
		private ProxyResponseFactory factory = new ProxyResponseFactory("Wesabe", httpHeaders);
		private ProxyResponse proxyResponse;
		@SuppressWarnings("unchecked")
		private GrizzlyResponse response;
		
		
		@Before
		@SuppressWarnings("unchecked")
		public void setup() throws Exception {
			this.proxyResponse = new ProxyResponse(
				new ProtocolVersion("http", 1, 1), 200, "THAT WAS AWESOME"
			);
			proxyResponse.setHeader("Date", "RIGHT NOW");
			proxyResponse.setHeader("Location", "https://api.wesabe.com/v2/blah");
			proxyResponse.setHeader("Server", "Snooper Secret");
			proxyResponse.setHeader("Accept", "STUFF");
			proxyResponse.setHeader("X-Left-Field", "All babies are ugly.");
			
			InputStreamEntity entity = new InputStreamEntity(new ByteArrayInputStream("blah blah blah".getBytes()), 14);
			entity.setContentType("text/plain");
			proxyResponse.setEntity(entity);
			
			Response connectionResponse = new Response();
			connectionResponse.setOutputBuffer(new StringOutputBuffer());
			this.response = new GrizzlyResponse();
			response.setResponse(connectionResponse);
			
			factory.buildFromHttpResponse(proxyResponse, response);
		}
		
		@Test
		public void itCopiesTheStatusLine() throws Exception {
			assertEquals(200, response.getStatus());
		}
		
		@Test
		public void itCopiesTheContentType() throws Exception {
			assertEquals("text/plain", response.getContentType());
		}
		
		@Test
		public void itCopiesTheContentLength() throws Exception {
			assertEquals(14, response.getContentLength());
		}
	}
}
