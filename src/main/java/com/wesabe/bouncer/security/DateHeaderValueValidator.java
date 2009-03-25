package com.wesabe.bouncer.security;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

/**
 * A header field value validator for HTTP dates. Checks to make sure the date
 * is parseable.
 * 
 * @author coda
 */
public class DateHeaderValueValidator extends HeaderValueValidator {
	@Override
	public boolean isValid(String value) {
		try {
			return (value != null) && (DateUtils.parseDate(value) != null);
		} catch (DateParseException e) {
			return false;
		}
	}
}
