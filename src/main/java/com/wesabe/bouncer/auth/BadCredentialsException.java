package com.wesabe.bouncer.auth;

/**
 * An exception thrown by {@link Authenticator} when a request is sent with
 * missing, incorrect, or malformed credentials.
 * 
 * @author coda
 */
public class BadCredentialsException extends Exception {
	private static final long serialVersionUID = 2954398757845043679L;
}
