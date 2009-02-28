package com.wesabe.bouncer;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;

/**
 * An authenticator of incoming requests.
 * 
 * @author coda
 */
public interface Authenticator {
	
	/**
	 * If {@code request} is authenticated, marks it as such and returns
	 * {@code true}. Otherwise, leaves {@code request} unmodified and returns
	 * {@code false}.
	 * 
	 * @param request a potentially authenticated request
	 * @return {@code true} if {@code request} is authenticated, {@code false}
	 *         otherwise
	 */
	public abstract boolean authenticate(GrizzlyRequest request);
}
