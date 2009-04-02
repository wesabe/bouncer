package com.wesabe.bouncer.security;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.wesabe.bouncer.security.normalizers.MalformedValueException;
import com.wesabe.bouncer.security.normalizers.UriNormalizer;
import com.wesabe.bouncer.security.validators.DateHeaderValueValidator;
import com.wesabe.bouncer.security.validators.HeaderValueValidator;
import com.wesabe.bouncer.security.validators.MethodValidator;
import com.wesabe.bouncer.security.validators.Validator;
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
	private static final Map<String, Validator<String>> HEADER_VALIDATORS = MapWithDefault.of(
		ImmutableMap.of(
			"date", (Validator<String>) new DateHeaderValueValidator(),
			"expires", (Validator<String>) new DateHeaderValueValidator(),
			"last-modified", (Validator<String>) new DateHeaderValueValidator()
		),
		new HeaderValueValidator()
	);
	private static final MethodValidator METHOD_VALIDATOR = new MethodValidator();
	private static final UriNormalizer URI_NORMALIZER = new UriNormalizer();
	private static final Logger LOGGER = Logger.getLogger(SafeRequest.class.getCanonicalName());
	private static final RequestHeaderSet VALID_HEADERS = new RequestHeaderSet();
	private final GrizzlyRequest request;
	
	
	/**
	 * Creates a new {@link SafeRequest} with a backing {@link GrizzlyRequest}.
	 * 
	 * @param request an unsafe request
	 */
	public SafeRequest(GrizzlyRequest request) {
		this.request = request;
	}
	
	/**
	 * Returns a safe, decoded, UTF-8, hostless request URI.
	 * 
	 * @return the request URI
	 * @throws BadRequestException if the request's URI is invalid
	 */
	public String getURI() throws BadRequestException {
		try {
			return URI_NORMALIZER.normalize(request.getRequestURI());
		} catch (MalformedValueException e) {
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
			
			if (VALID_HEADERS.contains(headerName)) {
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
	public String getMethod() throws BadRequestException {
		final String method = request.getMethod();
		if (!METHOD_VALIDATOR.isValid(method)) {
			throw new BadRequestException(
				request,
				new IllegalArgumentException(method + " is not a valid method")
			);
		}
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
	
	public GrizzlyRequest getRequest() {
		return request;
	}
}
