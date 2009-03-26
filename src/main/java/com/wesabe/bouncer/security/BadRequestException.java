package com.wesabe.bouncer.security;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;

/**
 * An exception to be raised when a request should be rejected.
 * 
 * @author coda
 */
public class BadRequestException extends Exception {
	private static final long serialVersionUID = 226004964039401873L;
	private final GrizzlyRequest request;
	
	/**
	 * Create a new exception for a request with an underlying cause.
	 * 
	 * @param request a bad request
	 * @param e the reason the request is bad
	 */
	public BadRequestException(GrizzlyRequest request, Throwable e) {
		super(request.getMethod() + " request to " + request.getRequestURI() + " rejected", e);
		this.request = request;
	}
	
	/**
	 * Returns the bad request.
	 * 
	 * @return the bad request
	 */
	public GrizzlyRequest getBadRequest() {
		return request;
	}
}
