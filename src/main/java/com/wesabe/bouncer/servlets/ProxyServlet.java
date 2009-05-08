package com.wesabe.bouncer.servlets;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.client.HttpClient;

import com.wesabe.bouncer.proxy.ProxyHttpExchange;

public class ProxyServlet extends HttpServlet {
	private static final long serialVersionUID = -3276400775243866667L;
	private final URI backendUri;
	private final HttpClient httpClient;
	
	public ProxyServlet(URI backendUri, HttpClient httpClient) {
		this.backendUri = backendUri;
		this.httpClient = httpClient;
	}
	
	@Override
	public void init() throws ServletException {
		try {
			httpClient.start();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	public void destroy() {
		try {
			httpClient.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		final ProxyHttpExchange exchange = new ProxyHttpExchange(backendUri, req, resp);
		httpClient.send(exchange);
		try {
			exchange.waitForDone();
		} catch (InterruptedException e) {
			throw new ServletException(e);
		}
	}

	public URI getBackendUri() {
		return backendUri;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}
}
