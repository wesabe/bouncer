package com.wesabe.bouncer.auth;

import java.security.Principal;

import org.mortbay.jetty.Request;

/**
 * An authenticator of incoming requests.
 * 
 * @author coda
 */
public interface Authenticator {
	
	/**
	 * If {@code request} is authenticated, returns the {@link Principal}
	 * associated with the request. Otherwise, returns {@code null}.
	 * 
	 * @param request a potentially authenticated request
	 * @return the {@link Principal} associated with {@code request}
	 */
	public abstract Principal authenticate(Request request);
}
