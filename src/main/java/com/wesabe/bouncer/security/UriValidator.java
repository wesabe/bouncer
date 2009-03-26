package com.wesabe.bouncer.security;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A validator for URIs.
 * 
 * @author coda
 */
public class UriValidator implements Validator<String> {
	
	// REVIEW coda@wesabe.com -- Mar 26, 2009: Deal with double-encoded URIs.
	// see http://www.owasp.org/index.php/Double_Encoding
	// We should decode a URI until it no longer changes (or until we get bored)
	
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
