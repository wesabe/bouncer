package com.wesabe.bouncer.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.wesabe.bouncer.HttpHeaders;

/**
 * A factory class for building {@link GrizzlyRequest}s based on the information
 * in {@link HttpResponse}s.
 * 
 * @author coda
 */
public class ProxyResponseFactory {
	private static final int BUFFER_SIZE = 16 * 1024;
	private final String serverName;
	private final HttpHeaders httpHeaders;

	/**
	 * Given a set of valid {@link HttpHeaders}, and a server name, create a new
	 * {@link ProxyRequestFactory}.
	 * 
	 * @param serverName the name of the server
	 * @param httpHeaders a set of valid {@link HttpHeaders}
	 */
	public ProxyResponseFactory(String serverName, HttpHeaders httpHeaders) {
		this.serverName = serverName;
		this.httpHeaders = httpHeaders;
	}

	/**
	 * Given an outgoing {@link HttpResponse}, configures a
	 * corresponding {@link GrizzlyResponse}.
	 * 
	 * @param proxyResponse the response from the proxied server
	 * @param response the response to send to the client
	 * @throws IOException if something goes wrong
	 */
	@SuppressWarnings("unchecked")
	public void buildFromHttpResponse(ProxyResponse proxyResponse, GrizzlyResponse response)
			throws IOException {
		response.setStatus(proxyResponse.getStatusLine().getStatusCode());
		copyHeaders(proxyResponse, response);
		squashServerHeader(response);
		copyEntity(proxyResponse, response);
		response.finishResponse();
	}

	@SuppressWarnings("unchecked")
	private void copyHeaders(HttpResponse proxyResponse, GrizzlyResponse response) {
		for (Header header : proxyResponse.getAllHeaders()) {
			if (httpHeaders.isValidResponseHeader(header.getName())) {
				response.addHeader(header.getName(), header.getValue());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void squashServerHeader(GrizzlyResponse response) {
		response.setHeader("Server", serverName);
	}

	@SuppressWarnings("unchecked")
	private void copyEntity(HttpResponse proxyResponse, GrizzlyResponse response)
			throws IOException {
		if (proxyResponse.getEntity() != null) {
			copyStream(proxyResponse.getEntity().getContent(), response.getOutputStream());
			response.setContentType(proxyResponse.getEntity().getContentType().getValue());
		}
	}
	
	private void copyStream(InputStream input, OutputStream output) throws IOException {
		// from O'Reilly's Java NIO, pg. 60
		final ReadableByteChannel inputChannel = Channels.newChannel(input);
		final WritableByteChannel outputChannel = Channels.newChannel(output);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		while (inputChannel.read(buffer) != -1) {
			buffer.flip();
			while (buffer.hasRemaining()) {
				outputChannel.write(buffer);
			}
			buffer.clear();
		}
		inputChannel.close();
		outputChannel.close();
	}
}
