package com.wesabe.bouncer.security;

import java.util.Locale;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * A set of HTTP header field names.
 * 
 * @author coda
 */
public abstract class AbstractHeaderSet {
	
	/**
	 * Valid HTTP 1.1 general header fields.
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.5">RFC 2616 Section 4.5</a>
	 */
	protected static final Set<String> GENERAL_HEADERS = ImmutableSet.of(
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
	protected static final Set<String> ENTITY_HEADERS = ImmutableSet.of(
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
	 * Valid HTTP 1.1 request header fields.
	 * 
	 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.3">RFC 2616, Section 5.3</a>
	 */
	protected static final Set<String>  REQUEST_HEADERS = ImmutableSet.of(
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
	protected static final Set<String>  RESPONSE_HEADERS = ImmutableSet.of(
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
	 * Unproxyable headers.
	 */
	protected static final Set<String>  UNPROXYABLE_HEADERS = ImmutableSet.of(
		"Proxy-Connection",
		"Connection",
		"Keep-Alive",
		"Transfer-Encoding",
		"TE",
		"Trailer",
		"Proxy-Authorization",
		"Proxy-Authenticate",
		"Upgrade"
	);
	
	private static final ImmutableSet<Character> VALID_HEADER_VALUE_CHARACTERS = ImmutableSet.of(
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B',
		'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
		'4', '5', '6', '7', '8', '9', '(', ')', '-', '=', '*', '.', '?', ';',
		',', '+', '/', ':', '&', '_', ' '
	);
	
	private final ImmutableSet<String> headers;
	
	/**
	 * Create a new set of headers.
	 * 
	 */
	public AbstractHeaderSet() {
		Builder<String> builder = new Builder<String>();
		for (String header : getHeaders()) {
			builder.add(downcase(header));
		}
		this.headers = builder.build();
	}
	
	/**
	 * Return a list of headers which subclasses should contain.
	 * 
	 * @return a list of headers
	 */
	protected abstract Iterable<String> getHeaders();
	
	// REVIEW coda@wesabe.com -- Mar 24, 2009: Extend header value sanitization.
	// Right now we're just checking for martian characters, not malformed dates
	// and such.
	
	/**
	 * Returns true if {@code header} is contained in the set and {@code value}
	 * is a valid value for the header.
	 * 
	 * @param name
	 *            an HTTP header name
	 * @param value
	 * 			  an HTTP header value
	 * @return {@code true} if {@code header} is contained in the set and
	 * 			{@code value} is valid
	 */
	public boolean contains(String name, String value) {
		return headers.contains(downcase(name)) && isValidHeaderValue(value);
	}
	
	/**
	 * Returns {@code true} if {@code value} is a valid HTTP header value.
	 * 
	 * @param value
	 * @return {@code true} if {@code value} is a valid HTTP header value
	 */
	private boolean isValidHeaderValue(String value) {
		for (int i = 0; i < value.length(); i++) {
			if (!VALID_HEADER_VALUE_CHARACTERS.contains(Character.valueOf(value.charAt(i)))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Downcase a header field name using the US {@link Locale}.
	 * 
	 * @param header an HTTP header field name
	 * @return {@code header} in lowercase
	 */
	private String downcase(String header) {
		return header.toLowerCase(Locale.US);
	}
}
