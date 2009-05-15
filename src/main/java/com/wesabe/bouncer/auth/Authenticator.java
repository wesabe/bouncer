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
	 * associated with the request.
	 * 
	 * @param request a potentially authenticated request
	 * @return the {@link Principal} associated with {@code request}
	 * @throws BadCredentialsException if the provided credentials are invalid or missing
	 * @throws LockedAccountException if the principal's account is locked
	 */
	public abstract Principal authenticate(Request request) throws LockedAccountException, BadCredentialsException;
}
