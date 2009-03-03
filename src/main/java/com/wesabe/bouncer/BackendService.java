package com.wesabe.bouncer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.wesabe.bouncer.http.ProxyRequest;
import com.wesabe.bouncer.http.ProxyResponse;

/**
 * An internal service which, when given an {@link HttpUriRequest}, returns a
 * {@link HttpResponse}.
 * 
 * @author coda
 *
 */
public interface BackendService {
	public abstract ProxyResponse execute(ProxyRequest request);
}
