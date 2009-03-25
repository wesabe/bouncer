package com.wesabe.bouncer.client.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.client.ProxyRequest;

@RunWith(Enclosed.class)
public class ProxyRequestTest {
	public static class Without_An_Entity {
		private final ProxyRequest request = new ProxyRequest("GET", "/hello", new ByteArrayInputStream("".getBytes()), 0);
		
		@Test
		public void itHasAMethod() throws Exception {
			assertEquals("GET", request.getMethod());
		}
		
		@Test
		public void itHasARequestURI() throws Exception {
			assertEquals("/hello", request.getURI().toString());
		}
		
		@Test
		public void itDoesNotHaveAnEntity() throws Exception {
			assertFalse(request.hasEntity());
			assertNull(request.getEntity());
		}
	}
	
	public static class With_An_Entity {
		private ProxyRequest request;
		
		@Before
		public void setup() throws Exception {
			ByteArrayInputStream inputStream = new ByteArrayInputStream("blah blah blah".getBytes());
			
			this.request = new ProxyRequest("GET", "/hello", inputStream, 14);
		}
		
		@Test
		public void itHasAMethod() throws Exception {
			assertEquals("GET", request.getMethod());
		}
		
		@Test
		public void itHasARequestURI() throws Exception {
			assertEquals("/hello", request.getURI().toString());
		}
		
		@Test
		public void itHasAnEntity() throws Exception {
			assertTrue(request.hasEntity());
			StringBuffer out = new StringBuffer();
		    byte[] b = new byte[4096];
		    for (int n; (n = request.getEntity().getContent().read(b)) != -1;) {
		        out.append(new String(b, 0, n));
		    }
		    assertEquals("blah blah blah", out.toString());
		}
	}
}
