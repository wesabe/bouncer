package com.wesabe.bouncer.client;

import java.util.Map.Entry;

import com.wesabe.bouncer.security.BadRequestException;
import com.wesabe.bouncer.security.SafeRequest;

/**
 * A factory class for building {@link ProxyRequest}s based on the information
 * in {@link SafeRequest}s.
 * 
 * @author coda
 */
public class ProxyRequestFactory {
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	
	/**
	 * Given an incoming {@link SafeRequest}, build a corresponding
	 * {@link ProxyRequest}.
	 * 
	 * @param request an incoming {@link SafeRequest}
	 * @return a corresponding {@link ProxyRequest}
	 * @throws BadRequestException if the request should be rejected
	 */
	public ProxyRequest buildFromGrizzlyRequest(SafeRequest request) throws BadRequestException {
		final ProxyRequest proxyRequest = buildRequest(request);
		copyHeaders(request, proxyRequest);
		setForwardingHeader(request, proxyRequest);
		return proxyRequest;
	}

	private void setForwardingHeader(SafeRequest request, final ProxyRequest proxyRequest) {
		proxyRequest.setHeader(X_FORWARDED_FOR, request.getRemoteAddr());
	}

	private void copyHeaders(SafeRequest request, final ProxyRequest proxyRequest) {
		for (Entry<String, String> header : request.getHeaders().entries()) {
			proxyRequest.setHeader(header.getKey(), header.getValue());
		}
	}

	private ProxyRequest buildRequest(SafeRequest request) throws BadRequestException {
		final ProxyRequest proxyRequest = new ProxyRequest(
			request.getMethod(),
			request.getURI().toString(),
			request.getEntity(),
			request.getContentLength()
		);
		return proxyRequest;
	}
}
