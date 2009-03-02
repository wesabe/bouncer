package com.wesabe.bouncer;

import java.security.Principal;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;

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
	public abstract Principal authenticate(GrizzlyRequest request);
}
