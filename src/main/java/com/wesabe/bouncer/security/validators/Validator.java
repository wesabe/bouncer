package com.wesabe.bouncer.security.validators;

/**
 * A simple validator interface.
 * 
 * @author coda
 *
 * @param <E> the type of input to be validated
 */
public interface Validator<E> {
	/**
	 * Returns {@code true} if {@code input} is valid, {@code false} otherwise.
	 * 
	 * @param input a potentially valid piece of input
	 * @return {@code true} if {@code input} is valid
	 */
	public abstract boolean isValid(E input);
}
