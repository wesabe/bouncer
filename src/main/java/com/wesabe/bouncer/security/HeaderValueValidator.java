package com.wesabe.bouncer.security;

import com.google.common.collect.ImmutableSet;

/**
 * A default HTTP header field value validator. Checks to make sure the value
 * consists of only happy characters.
 * 
 * @author coda
 *
 */
public class HeaderValueValidator implements Validator<String> {
	/**
	 * The set of valid characters for HTTP header field values. Pinched from
	 * OWASP's ESAPI code.
	 * 
	 * @see <a href="http://code.google.com/p/owasp-esapi-java/source/browse/trunk/src/main/resources/.esapi/ESAPI.properties">ESAPI.properties</a>
	 */
	protected static final ImmutableSet<Character> VALID_CHARACTERS = ImmutableSet.of(
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B',
		'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
		'4', '5', '6', '7', '8', '9', '(', ')', '-', '=', '*', '.', '?', ';',
		',', '+', '/', ':', '&', '_', ' '
	);
	
	/**
	 * Returns {@code true} if {@code value} is a valid HTTP header,
	 * {@code false} otherwise.
	 * 
	 * @param value a potentially valid HTTP header
	 * @return whether or not {@code header} is valid
	 */
	@Override
	public boolean isValid(String value) {
		for (int i = 0; i < value.length(); i++) {
			final Character character = Character.valueOf(value.charAt(i));
			if (!VALID_CHARACTERS.contains(character)) {
				return false;
			}
		}
		return true;
	}
}
