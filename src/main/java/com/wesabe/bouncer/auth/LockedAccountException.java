package com.wesabe.bouncer.auth;

/**
 * An exception raised by {@link Authenticator} when a request is sent for an
 * account which is locked due to too many failed authentication requests.
 * 
 * @author coda
 */
public class LockedAccountException extends Exception {
	private static final long serialVersionUID = 9157441049688890691L;
	private final int penaltyDuration;
	
	/**
	 * Creates a new {@link LockedAccountException}.
	 * 
	 * @param penaltyDuration the number of seconds the account is locked for
	 */
	public LockedAccountException(int penaltyDuration) {
		super();
		this.penaltyDuration = penaltyDuration;
	}
	
	/**
	 * Returns the number of seconds the account is being locked for.
	 * 
	 * @return the number of seconds the account is being locked for
	 */
	public int getPenaltyDuration() {
		return penaltyDuration;
	}
}
