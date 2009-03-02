package com.wesabe.bouncer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;

public class ProxyAdapter extends GrizzlyAdapter {
	private static final Logger LOGGER = Logger.getLogger(ProxyAdapter.class.getName());
	private final BackendService backendService;
	private final HttpHeaders httpHeaders;

	public ProxyAdapter(BackendService backendService, HttpHeaders httpHeaders) {
		super();
		this.backendService = backendService;
		this.httpHeaders = httpHeaders;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void service(GrizzlyRequest request, GrizzlyResponse response) {
		try {
			buildResponse(backendService.execute(buildProxyRequest(request)), response);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unhandled internal error", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void buildResponse(HttpResponse proxyResponse, GrizzlyResponse response) throws IOException {
		// set status
		response.setStatus(proxyResponse.getStatusLine().getStatusCode());
		
		// set headers
		for (Header header : proxyResponse.getAllHeaders()) {
			if (httpHeaders.isValidResponseHeader(header.getName())) {
				response.addHeader(header.getName(), header.getValue());
			}
		}
		
		// always overwrite the Server field
		response.setHeader("Server", "Wesabe");
		
		// set entity, if any
		if (proxyResponse.getEntity() != null) {
			copyStream(proxyResponse.getEntity().getContent(), response.getOutputStream());
		}
		
		// send
		response.finishResponse();
	}

	private HttpUriRequest buildProxyRequest(GrizzlyRequest request) {
		final String uri = request.getRequestURI();
		final HttpUriRequest proxyRequest;
		
		if (request.getMethod().equals("GET")) {
			proxyRequest = new HttpGet(uri);
		} else {
			// FIXME coda@wesabe.com -- Feb 27, 2009: buildRequest should handle DELETE, POST, PUT, OPTIONS, and a few others
			throw new IllegalArgumentException("No idea how to handle a " + request.getMethod() + " request");
		}
		
		Enumeration<?> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			if (httpHeaders.isValidRequestHeader(headerName)) {
				proxyRequest.addHeader(headerName, request.getHeader(headerName));
			}
		}
		
		proxyRequest.setHeader("X-Forwarded-For", request.getRemoteAddr());
		
		return proxyRequest;
	}
	
	private void copyStream(InputStream input, OutputStream output) throws IOException {
		final ReadableByteChannel inputChannel = Channels.newChannel(input);
		final WritableByteChannel outputChannel = Channels.newChannel(output);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
		
		while (inputChannel.read(buffer) != -1) {
			buffer.flip();
			outputChannel.write(buffer);
			buffer.compact();
		}
		
		buffer.flip();
		
		while (buffer.hasRemaining()) {
			outputChannel.write(buffer);
		}

		inputChannel.close();
		outputChannel.close();
	}
}
