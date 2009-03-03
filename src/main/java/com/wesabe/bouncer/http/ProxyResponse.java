package com.wesabe.bouncer.http;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;

/**
 * An {@link HttpResponse}. Only exists to provide a sense of symmetry with
 * {@link ProxyRequest}.
 * 
 * @author coda
 */
public class ProxyResponse extends BasicHttpResponse {

	public ProxyResponse(ProtocolVersion ver, int code, String reason) {
		super(ver, code, reason);
	}

}
