package com.wesabe.bouncer.http;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A set of valid response headers.
 * 
 * @author coda
 */
public class ResponseHeaderSet extends AbstractHeaderSet {
	
	/* (non-Javadoc)
	 * @see com.wesabe.bouncer2.http.AbstractHeaderSet#getHeaders()
	 */
	@Override
	protected Iterable<String> getHeaders() {
		final List<String> headers = Lists.newLinkedList();
		
		headers.addAll(GENERAL_HEADERS);
		headers.addAll(ENTITY_HEADERS);
		headers.addAll(RESPONSE_HEADERS);
		
		headers.removeAll(UNPROXYABLE_HEADERS);
		
		return headers;
	}

}
