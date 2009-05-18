package com.wesabe.bouncer.proxy;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A factory class which makes {@link ProxyHttpExchange} instances.
 * 
 * @author coda
 *
 */
public class ProxyHttpExchangeFactory {
	private final String backendUri;
	
	public ProxyHttpExchangeFactory(URI backendUri) {
		final String uri = backendUri.toASCIIString();
		if (uri.endsWith("/")) {
			this.backendUri = uri.substring(0, uri.length() - 1);
		} else {
			this.backendUri = uri;
		}
	}
	
	public URI getBackendUri() {
		return URI.create(backendUri);
	}
	
	public ProxyHttpExchange build(HttpServletRequest request, HttpServletResponse response) {
		return new ProxyHttpExchange(backendUri, request, response);
	}
}
