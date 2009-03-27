package com.wesabe.bouncer.security;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.wesabe.bouncer.util.CaseInsensitiveMultimap;
import com.wesabe.bouncer.util.MapWithDefault;
import com.wesabe.bouncer.util.CaseInsensitiveMultimap.Builder;

/**
 * A wrapper for an incoming request which either returns sanitized values or,
 * if a value cannot be sanitized, throws a {@link BadRequestException}. Also
 * presents a less-sucky façade on top of what is a pretty hilarious API.
 * 
 * @author coda
 */
public class SafeRequest {
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final int MAX_DECODE_DEPTH = 3;
	private static final Map<String, Validator<String>> HEADER_VALIDATORS = MapWithDefault.of(
		ImmutableMap.of(
			"date", (Validator<String>) new DateHeaderValueValidator(),
			"expires", (Validator<String>) new DateHeaderValueValidator(),
			"last-modified", (Validator<String>) new DateHeaderValueValidator()
		),
		new HeaderValueValidator()
	);
	private static final Logger LOGGER = Logger.getLogger(SafeRequest.class.getCanonicalName());
	private final GrizzlyRequest request;
	private final RequestHeaderSet headerSet;
	
	/**
	 * Creates a new {@link SafeRequest} with a backing {@link GrizzlyRequest}.
	 * 
	 * @param request an unsafe request
	 */
	public SafeRequest(GrizzlyRequest request) {
		this.request = request;
		this.headerSet = new RequestHeaderSet();
	}
	
	/**
	 * Returns a safe, decoded, UTF-8, hostless request URI.
	 * 
	 * @return the request URI
	 * @throws BadRequestException if the request's URI is invalid
	 */
	public URI getURI() throws BadRequestException {
		try {
			final URI uri = new URI(decodeURI(request.getRequestURI()));
			if (uri.getHost() != null) {
				throw new URISyntaxException(uri.toString(), "must not have an associated host");
			}
			
			return uri;
		} catch (URISyntaxException e) {
			throw new BadRequestException(request, e);
		}
	}
	
	/**
	 * Returns a multimap of safe, valid headers and their safe, valid values.
	 * 
	 * @return the request's headers
	 */
	public Multimap<String, String> getHeaders() {
		final Builder builder = CaseInsensitiveMultimap.builder();
		
		final Enumeration<?> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			final String headerName = (String) headerNames.nextElement();
			final String normalizedHeaderName = headerName.toLowerCase(Locale.US);
			
			if (headerSet.contains(headerName)) {
				final Enumeration<?> headerValues = request.getHeaders(headerName);
				while (headerValues.hasMoreElements()) {
					final String headerValue = (String) headerValues.nextElement();
					if (HEADER_VALIDATORS.get(normalizedHeaderName).isValid(headerValue)) {
						builder.put(headerName, headerValue);
					} else {
						LOGGER.info("Dropped header from request, invalid value: " + headerName + "=" + headerValue);
					}
				}
			} else {
				LOGGER.info("Dropped header from request, invalid name: " + headerName);
			}
		}
		
		return builder.build();
	}
	
	/**
	 * Returns the request's entity as an input stream.
	 * 
	 * @return the request's entity
	 */
	public InputStream getEntity() {
		return request.getStream();
	}
	
	/**
	 * Returns the client's IP address.
	 * 
	 * @return the client's IP address
	 */
	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}
	
	/**
	 * Returns the request's method.
	 * 
	 * @return the request's method
	 */
	public String getMethod() {
		return request.getMethod();
	}
	
	/**
	 * Returns the request's content length.
	 * 
	 * @return the request's content length
	 */
	public int getContentLength() {
		return request.getContentLength();
	}
	
	private String decodeURI(String encodedUri) throws URISyntaxException {
		try {
			String uri = encodedUri;
			for (int i = MAX_DECODE_DEPTH; i > 0; i--) {
				String decodedUri = URLDecoder.decode(uri, DEFAULT_CHARSET);
				if (uri.equals(decodedUri)) {
					return uri;
				}
				uri = decodedUri;
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		throw new URISyntaxException(encodedUri, "was encoded more than " + MAX_DECODE_DEPTH + " times");
	}

	public GrizzlyRequest getRequest() {
		return request;
	}
}
