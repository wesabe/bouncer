package com.wesabe.bouncer.http.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.wesabe.bouncer.http.ResponseHeaderSet;

public class ResponseHeaderSetTest {
	private final ResponseHeaderSet set = new ResponseHeaderSet();
	
	@Test
	public void itContainsGeneralHeaders() throws Exception {
		assertTrue(set.contains("Cache-Control"));
	}
	
	@Test
	public void itContainsEntityHeaders() throws Exception {
		assertTrue(set.contains("Allow"));
	}
	
	@Test
	public void itContainsResponseHeaders() throws Exception {
		assertTrue(set.contains("Location"));
	}
	
	@Test
	public void itDoesNotContainRequestHeaders() throws Exception {
		assertFalse(set.contains("Accept"));
	}
	
	@Test
	public void itIsCaseInsensitive() throws Exception {
		assertTrue(set.contains("cache-control"));
		assertTrue(set.contains("CACHE-CONTROL"));
	}
	
	@Test
	public void itDoesNotContainExtensionHeaders() throws Exception {
		assertFalse(set.contains("X-Date"));
	}
	
	@Test
	public void itDoesNotContainTheConnectionHeader() throws Exception {
		assertFalse(set.contains("Connection"));
	}
	
	@Test
	public void itDoesNotContainTheProxyConnectionHeader() throws Exception {
		assertFalse(set.contains("Proxy-Connection"));
	}
	
	@Test
	public void itDoesNotContainTheKeepAliveHeader() throws Exception {
		assertFalse(set.contains("Keep-Alive"));
	}
	
	@Test
	public void itDoesNotContainTheTransferEncodingHeader() throws Exception {
		assertFalse(set.contains("Transfer-Encoding"));
	}
	
	@Test
	public void itDoesNotContainTheTEHeader() throws Exception {
		assertFalse(set.contains("TE"));
	}
	
	@Test
	public void itDoesNotContainTheTrailerHeader() throws Exception {
		assertFalse(set.contains("Trailer"));
	}
	
	@Test
	public void itDoesNotContainTheProxyAuthorizationHeader() throws Exception {
		assertFalse(set.contains("Proxy-Authorization"));
	}
	
	@Test
	public void itDoesNotContainTheProxyAuthenticateHeader() throws Exception {
		assertFalse(set.contains("Proxy-Authenticate"));
	}
	
	@Test
	public void itDoesNotContainTheUpgradeHeader() throws Exception {
		assertFalse(set.contains("Upgrade"));
	}
}
