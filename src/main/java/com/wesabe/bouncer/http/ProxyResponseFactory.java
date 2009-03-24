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

/**
 * A factory class for building {@link GrizzlyRequest}s based on the information
 * in {@link HttpResponse}s.
 * 
 * @author coda
 */
public class ProxyResponseFactory {
	private static final int BUFFER_SIZE = 16 * 1024;
	private final String serverName;
	private final ResponseHeaderSet headers;

	/**
	 * Given a set of valid headers, and a server name, create a new
	 * {@link ProxyRequestFactory}.
	 * 
	 * @param serverName the name of the server
	 * @param headers a set of valid headers
	 */
	public ProxyResponseFactory(String serverName, ResponseHeaderSet headers) {
		this.serverName = serverName;
		this.headers = headers;
	}

	/**
	 * Given an outgoing {@link HttpResponse}, sends a corresponding {@link
	 * GrizzlyResponse}.
	 * 
	 * @param proxyResponse the response from the proxied server
	 * @param response the response to send to the client
	 * @throws IOException if something goes wrong
	 */
	@SuppressWarnings("unchecked")
	public void buildFromHttpResponse(HttpResponse proxyResponse, GrizzlyResponse response)
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
			if (headers.contains(header.getName())) {
				response.setHeader(header.getName(), header.getValue());
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
			if (proxyResponse.getEntity().getContentLength() > 0) {
				response.setContentType(proxyResponse.getEntity().getContentType().getValue());
			}
		}
	}

	private void copyStream(InputStream input, OutputStream output) throws IOException {
		// from O'Reilly's Java NIO, pg. 60
		final ReadableByteChannel inputChannel = Channels.newChannel(input);
		final WritableByteChannel outputChannel = Channels.newChannel(output);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		while (inputChannel.read(buffer) != -1) {
			// Prepare the buffer to be drained
			buffer.flip();

			// Write to the channel; may block
			outputChannel.write(buffer);

			// If partial transfer, shift remainder down
			// If buffer is empty, same as doing clear( )
			buffer.compact();
		}

		// EOF will leave buffer in fill state
		buffer.flip();

		// Make sure that the buffer is fully drained
		while (buffer.hasRemaining()) {
			outputChannel.write(buffer);
		}
		
		inputChannel.close();
		outputChannel.close();
	}
}
