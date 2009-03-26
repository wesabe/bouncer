package com.wesabe.bouncer.security;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A validator for URIs.
 * 
 * @author coda
 */
public class UriValidator implements Validator<String> {
	@Override
	public boolean isValid(String uri) {
		try {
			new URI(uri);
			return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}
}
