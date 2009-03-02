package com.wesabe.bouncer;

import java.security.Principal;

import com.sun.grizzly.util.buf.Base64Utils;

/**
 * A set of credentials for a Wesabe user, to be passed to internal services
 * with authenticated requests.
 * 
 * @author coda
 *
 */
public class WesabeCredentials implements Principal {
	private static final String FORMAT = "%s:%s";
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
		return Base64Utils.encodeToString(
			String.format(
				FORMAT,
				Integer.valueOf(userId),
				accountKey
			).getBytes(),
			false
		);
	}
}
