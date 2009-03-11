package com.wesabe.bouncer.http.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.http.HttpHeaders;

@RunWith(Enclosed.class)
public class HttpHeadersTest {
	public static class Evaluating_Request_Header_Fields {
		private final HttpHeaders headers = new HttpHeaders();
		
		@Test
		public void itShouldIncludeGeneralFields() throws Exception {
			assertTrue(headers.isValidRequestHeader("Date"));
		}
		
		@Test
		public void itShouldIncludeEntityFields() throws Exception {
			assertTrue(headers.isValidRequestHeader("Content-Type"));
		}
		
		@Test
		public void itShouldIncludeRequestFields() throws Exception {
			assertTrue(headers.isValidRequestHeader("Accept"));
		}
		
		@Test
		public void itShouldIncludeFieldsWithDifferentCapitalization() throws Exception {
			assertTrue(headers.isValidRequestHeader("DATE"));
		}
		
		@Test
		public void itShouldNotIncludeContentLength() throws Exception {
			assertFalse(headers.isValidRequestHeader("Content-Length"));
		}
	}
	
	public static class Evaluating_Response_Header_Fields {
		private final HttpHeaders headers = new HttpHeaders();
		
		@Test
		public void itShouldIncludeGeneralFields() throws Exception {
			assertTrue(headers.isValidResponseHeader("Date"));
		}
		
		@Test
		public void itShouldIncludeEntityFields() throws Exception {
			assertTrue(headers.isValidResponseHeader("Content-Type"));
		}
		
		@Test
		public void itShouldIncludeResponseFields() throws Exception {
			assertTrue(headers.isValidResponseHeader("Server"));
		}
		
		@Test
		public void itShouldIncludeFieldsWithDifferentCapitalization() throws Exception {
			assertTrue(headers.isValidResponseHeader("DATE"));
		}
	}
}
