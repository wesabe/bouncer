package com.wesabe.bouncer;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;

import com.wesabe.bouncer.http.ProxyRequest;

/**
 * An internal service which, when given an {@link ProxyRequest}, returns a
 * {@link HttpResponse}.
 * 
 * @author coda
 *
 */
public interface BackendService {
	public abstract HttpResponse execute(ProxyRequest request) throws HttpException;
}
