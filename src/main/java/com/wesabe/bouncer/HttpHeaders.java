package com.wesabe.bouncer;

import java.util.Locale;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class HttpHeaders {
	/**
	 * An immutable, case-insensitive set of HTTP header field names.
	 */
	private static class HeaderFieldSet {
		private final ImmutableSet<String> headerFields;
		
		public HeaderFieldSet(String... headerFields) {
			Builder<String> builder = ImmutableSet.builder();
			for (String headerField : headerFields) {
				builder.add(headerField.toLowerCase(Locale.US));
			}
			this.headerFields = builder.build();
		}
		
		/**
		 * Returns {@code true} if the set contains the given header field,
		 * {@code false} otherwise.
		 * 
		 * @param headerField an HTTP header field name
		 * @return {@code true} if the set contains the given header field
		 */
		public boolean contains(String headerField) {
			return headerFields.contains(headerField.toLowerCase(Locale.US));
		}
	}
	
	/**
	 * Valid HTTP 1.1 general header fields.
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.5">RFC 2616 Section 4.5</a>
	 */
	private static final HeaderFieldSet GENERAL_HEADERS = new HeaderFieldSet(
		"Cache-Control",
		"Connection",
		"Date",
		"Pragma",
		"Trailer",
		"Transfer-Encoding",
		"Upgrade",
		"Via",
		"Warning"
	);
	
	/**
	 * Valid HTTP 1.1 entity header fields.
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec7.html#sec7.1">RFC 2616, Section 7.1</a>
	 */
	private static final HeaderFieldSet ENTITY_HEADERS = new HeaderFieldSet(
		"Allow",
		"Content-Encoding",
		"Content-Language",
		"Content-Length",
		"Content-Location",
		"Content-MD5",
		"Content-Range",
		"Content-Type",
		"Expires",
		"Last-Modified"
	);
	
	/**
	 * Valid HTTP 1.1. request header fields.
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.3">RFC 2616, Section 5.3</a>
	 */
	private static final HeaderFieldSet REQUEST_HEADERS = new HeaderFieldSet(
		"Accept",
		"Accept-Charset",
		"Accept-Encoding",
		"Accept-Language",
		"Authorization",
		"Expect",
		"From",
		"Host",
		"If-Match",
		"If-Modified-Since",
		"If-None-Match",
		"If-Range",
		"If-Unmodified-Since",
		"Max-Forwards",
		"Proxy-Authorization",
		"Range",
		"Referer",
		"TE",
		"User-Agent"
	);
	
	/**
	 * Valid HTTP 1.1 response header fields.
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.2">RFC 2616, Section 6.2</a>
	 */
	private static final HeaderFieldSet RESPONSE_HEADERS = new HeaderFieldSet(
		"Accept-Ranges",
		"Age",
		"ETag",
		"Location",
		"Proxy-Authenticate",
		"Retry-After",
		"Server",
		"Vary",
		"WWW-Authenticate"
	);
	
	/**
	 * Returns {@code true} if the given header field is valid for HTTP 1.1
	 * requests.
	 * 
	 * @param header an HTTP header field name
	 * @return {@code true} if {@code header} is a valid HTTP 1.1. request
	 * 			header field
	 */
	public boolean isValidRequestHeader(String header) {
		return REQUEST_HEADERS.contains(header)
				|| GENERAL_HEADERS.contains(header)
				|| ENTITY_HEADERS.contains(header);
	}
	
	/**
	 * Returns {@code true} if the given header field is valid for HTTP 1.1
	 * responses.
	 * 
	 * @param header an HTTP header field name
	 * @return {@code true} if {@code header} is a valid HTTP 1.1. response
	 * 			header field
	 */
	public boolean isValidResponseHeader(String header) {
		return RESPONSE_HEADERS.contains(header)
				|| GENERAL_HEADERS.contains(header)
				|| ENTITY_HEADERS.contains(header);
	}
}
