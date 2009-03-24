package com.wesabe.bouncer.http;

import java.util.Enumeration;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;

/**
 * A factory class for building {@link ProxyRequest}s based on the information
 * in {@link GrizzlyRequest}s.
 * 
 * @author coda
 */
public class ProxyRequestFactory {
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
			if (requestHeaders.contains(headerName)) {
				proxyRequest.setHeader(headerName, request.getHeader(headerName));
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
