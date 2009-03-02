package com.wesabe.bouncer.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.HttpHeaders;

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
