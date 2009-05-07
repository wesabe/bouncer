package com.wesabe.bouncer.auth;

import java.security.Principal;

import org.apache.commons.codec.binary.Base64;

/**
 * A set of credentials for a Wesabe user, to be passed to internal services
 * with authenticated requests.
 * 
 * @author coda
 *
 */
public class WesabeCredentials implements Principal {
	private final int userId;
	private final String accountKey;
	
	public WesabeCredentials(int userId, String accountKey) {
		this.userId = userId;
		this.accountKey = accountKey;
	}
	
	@Override
	public String getName() {
		return Integer.toString(userId);
	}
	
	public String getAccountKey() {
		return accountKey;
	}
	
	public int getUserId() {
		return userId;
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Wesabe ");
		builder.append(buildCreds());
		return builder.toString();
	}
	
	private String buildCreds() {
		final StringBuilder builder = new StringBuilder();
		builder.append(userId);
		builder.append(':');
		builder.append(accountKey);
		return new String(Base64.encodeBase64(builder.toString().getBytes()));
	}
}
