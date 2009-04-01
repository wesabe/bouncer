package com.wesabe.bouncer.security.normalizers;

/**
 * A class which takes input values and normalizes them to output values.
 * 
 * @author coda
 * 
 * @param <Input>
 *            the type of values the normalizer takes as input
 * @param <Output>
 *            the type of values the normalizer produces as output
 */
public interface Normalizer<Input, Output> {

	/**
	 * Returns the normalized form of {@code input}.
	 * 
	 * @param input
	 *            an un-normalized value
	 * @return the normalized form of {@code input}
	 * @throws MalformedValueException
	 *             if {@code input} cannot be normalized
	 */
	public abstract Output normalize(Input input) throws MalformedValueException;
}
