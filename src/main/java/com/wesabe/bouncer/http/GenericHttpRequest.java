package com.wesabe.bouncer.http;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.InputStreamEntity;

/**
 * An {@link HttpRequest} with an HTTP method, a {@link URI} and an optional
 * {@link HttpEntity}.
 * 
 * @author coda
 */
public class GenericHttpRequest extends HttpEntityEnclosingRequestBase {
	private final String method;
	private final boolean hasEntity;
	
	/**
	 * Creates a new generic HTTP request.
	 * 
	 * @param method an HTTP method
	 * @param uri a relative URI
	 * @param entity an input stream of the entity contents, or null if no entity
	 * @param entityLength the length of {@code entity}, in bytes
	 */
	public GenericHttpRequest(String method, String uri, InputStream entity, int entityLength) {
		super();
		this.method = method;
		setURI(URI.create(uri));
		this.hasEntity = entity != null;
		if (hasEntity) {
			setEntity(new InputStreamEntity(entity, entityLength));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.http.client.methods.HttpRequestBase#getMethod()
	 */
	@Override
	public String getMethod() {
		return method;
	}
	
	/**
	 * Returns whether or not the request has an entity.
	 * 
	 * @return {@code true} if the request has an entity
	 */
	public boolean hasEntity() {
		return hasEntity;
	}

}
