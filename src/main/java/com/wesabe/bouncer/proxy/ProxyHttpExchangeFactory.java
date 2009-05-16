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
	private final URI backendUri;
	
	public ProxyHttpExchangeFactory(URI backendUri) {
		this.backendUri = backendUri;
	}
	
	public URI getBackendUri() {
		return backendUri;
	}
	
	public ProxyHttpExchange build(HttpServletRequest request, HttpServletResponse response) {
		return new ProxyHttpExchange(backendUri, request, response);
	}
}
