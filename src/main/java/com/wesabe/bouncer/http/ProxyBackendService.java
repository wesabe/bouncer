package com.wesabe.bouncer.http;

import java.net.URI;
import java.util.logging.Logger;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;


/**
 * A backend service which sends a {@link ProxyRequest} to a server (identified
 * by a {@link URI}) and returns an {@link HttpResponse}.
 * 
 * @author coda
 *
 */
public class ProxyBackendService implements BackendService {
	private static final Logger LOGGER = Logger.getLogger(ProxyBackendService.class.getSimpleName());
	private final URI uri;
	private final HttpClient httpClient;
	
	public ProxyBackendService(URI uri, HttpClientFactory clientFactory) {
		this.uri = uri;
		this.httpClient = clientFactory.buildClient();
	}
	
	@Override
	public HttpResponse execute(ProxyRequest request) throws HttpException {
		try {
			LOGGER.info(String.format("%s %s", request.getMethod(), request.getURI().toString()));
			return httpClient.execute(resolve(request));
		} catch (Exception e) {
			throw new HttpException("error executing request", e);
		}
	}
	
	private ProxyRequest resolve(ProxyRequest request) {
		request.setURI(uri.resolve(request.getURI()));
		return request;
	}
}
