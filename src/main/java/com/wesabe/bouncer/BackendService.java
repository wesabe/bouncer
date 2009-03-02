package com.wesabe.bouncer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * An internal service which, when given an {@link HttpUriRequest}, returns a
 * {@link HttpResponse}.
 * 
 * @author coda
 *
 */
public interface BackendService {
	public abstract HttpResponse execute(HttpUriRequest request);
}
