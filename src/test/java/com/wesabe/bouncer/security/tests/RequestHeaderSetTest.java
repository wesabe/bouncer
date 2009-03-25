package com.wesabe.bouncer.security.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.security.RequestHeaderSet;

@RunWith(Enclosed.class)
public class RequestHeaderSetTest {
	
	public static class Checking_Valid_Headers {
		private final RequestHeaderSet set = new RequestHeaderSet();
		
		@Test
		public void itContainsGeneralHeaders() throws Exception {
			assertTrue(set.contains("Cache-Control", "no-cache"));
		}
		
		@Test
		public void itDoesNotContainHeadersWithMalformedValues() throws Exception {
			assertFalse(set.contains("Cache-Control", "smuggle \n attack"));
		}
		
		@Test
		public void itContainsEntityHeaders() throws Exception {
			assertTrue(set.contains("Allow", "GET"));
		}
		
		@Test
		public void itContainsRequestHeaders() throws Exception {
			assertTrue(set.contains("Accept", "application/json"));
		}
	}
	

	public static class Checking_Invalid_Headers {
		private final RequestHeaderSet set = new RequestHeaderSet();
		
		@Test
		public void itIsCaseInsensitive() throws Exception {
			assertTrue(set.contains("cache-control", "no-cache"));
			assertTrue(set.contains("CACHE-CONTROL", "no-cache"));
		}
		
		@Test
		public void itDoesNotContainResponseHeaders() throws Exception {
			assertFalse(set.contains("Location", "http://api.wesabe.com/thing/"));
		}
		
		@Test
		public void itDoesNotContainExtensionHeaders() throws Exception {
			assertFalse(set.contains("X-Date", "woo!"));
		}
		
		@Test
		public void itDoesNotContainTheConnectionHeader() throws Exception {
			assertFalse(set.contains("Connection", "close"));
		}
		
		@Test
		public void itDoesNotContainTheProxyConnectionHeader() throws Exception {
			assertFalse(set.contains("Proxy-Connection", "close"));
		}
		
		@Test
		public void itDoesNotContainTheKeepAliveHeader() throws Exception {
			assertFalse(set.contains("Keep-Alive", "always"));
		}
		
		@Test
		public void itDoesNotContainTheTransferEncodingHeader() throws Exception {
			assertFalse(set.contains("Transfer-Encoding", "chunked"));
		}
		
		@Test
		public void itDoesNotContainTheTEHeader() throws Exception {
			assertFalse(set.contains("TE", "chunked"));
		}
		
		@Test
		public void itDoesNotContainTheTrailerHeader() throws Exception {
			assertFalse(set.contains("Trailer", "YAY"));
		}
		
		@Test
		public void itDoesNotContainTheProxyAuthorizationHeader() throws Exception {
			assertFalse(set.contains("Proxy-Authorization", "none"));
		}
		
		@Test
		public void itDoesNotContainTheProxyAuthenticateHeader() throws Exception {
			assertFalse(set.contains("Proxy-Authenticate", "none"));
		}
		
		@Test
		public void itDoesNotContainTheUpgradeHeader() throws Exception {
			assertFalse(set.contains("Upgrade", "1.1"));
		}
	}
	
	public static class Checking_The_Validity_Of_Date_Headers {
		private final RequestHeaderSet set = new RequestHeaderSet();
		
		@Test
		public void itAcceptsValidDateHeaders() throws Exception {
			assertTrue(set.contains("Date", "Sun, 06 Nov 1994 08:49:37 GMT"));
			assertTrue(set.contains("Date", "Sunday, 06-Nov-94 08:49:37 GMT"));
			assertTrue(set.contains("Date", "Sun Nov  6 08:49:37 1994"));
		}
		
		@Test
		public void itDoesNotAcceptInvalidDateHeaders() throws Exception {
			assertFalse(set.contains("Date", "Sdn, 06 Nov 1994 08:49:37 GMT"));
			assertFalse(set.contains("Date", "fdsfsaddfsa"));
			assertFalse(set.contains("Date", "\0\0\0\0\0\0 YAY"));
		}
	}
}
