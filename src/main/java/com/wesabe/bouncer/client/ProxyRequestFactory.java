package com.wesabe.bouncer.client;

import java.util.Enumeration;
import java.util.logging.Logger;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.wesabe.bouncer.security.RequestHeaderSet;

/**
 * A factory class for building {@link ProxyRequest}s based on the information
 * in {@link GrizzlyRequest}s.
 * 
 * @author coda
 */
public class ProxyRequestFactory {
	private static final Logger LOGGER = Logger.getLogger(ProxyRequestFactory.class.getCanonicalName());
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	private final RequestHeaderSet requestHeaders;
	
	/**
	 * Given a set of valid headers, create a new {@link ProxyRequestFactory}.
	 * 
	 * @param requestHeaders a set of valid response headers
	 */
	public ProxyRequestFactory(RequestHeaderSet requestHeaders) {
		this.requestHeaders = requestHeaders;
	}
	
	/**
	 * Given an incoming {@link GrizzlyRequest}, build a corresponding
	 * {@link ProxyRequest}.
	 * 
	 * @param request an incoming {@link GrizzlyRequest}
	 * @return a corresponding {@link ProxyRequest}
	 */
	public ProxyRequest buildFromGrizzlyRequest(GrizzlyRequest request) {
		final ProxyRequest proxyRequest = buildRequest(request);
		copyHeaders(request, proxyRequest);
		setForwardingHeader(request, proxyRequest);
		return proxyRequest;
	}

	private void setForwardingHeader(GrizzlyRequest request, final ProxyRequest proxyRequest) {
		proxyRequest.setHeader(X_FORWARDED_FOR, request.getRemoteAddr());
	}

	private void copyHeaders(GrizzlyRequest request, final ProxyRequest proxyRequest) {
		Enumeration<?> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			final String headerValue = request.getHeader(headerName);
			if (requestHeaders.contains(headerName, headerValue)) {
				proxyRequest.setHeader(headerName, headerValue);
			} else {
				LOGGER.info("Dropped request header: " + headerName + ":" + headerValue);
			}
		}
	}

	private ProxyRequest buildRequest(GrizzlyRequest request) {
		final ProxyRequest proxyRequest = new ProxyRequest(
			request.getMethod(),
			request.getRequestURI(),
			request.getStream(),
			request.getContentLength()
		);
		return proxyRequest;
	}
}
