package com.wesabe.bouncer.security.validators;

import com.google.common.collect.ImmutableSet;

/**
 * An HTTP method validator.
 * 
 * @author coda
 *
 */
public class MethodValidator implements Validator<String> {
	protected static final ImmutableSet<Character> VALID_CHARACTERS = ImmutableSet.of(
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B',
		'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
	);
	

	@Override
	public boolean isValid(String method) {
		for (int i = 0; i < method.length(); i++) {
			final Character character = Character.valueOf(method.charAt(i));
			if (!VALID_CHARACTERS.contains(character)) {
				return false;
			}
		}
		
		return true;
	}

}
