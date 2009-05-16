package com.wesabe.bouncer.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.client.HttpClient;

import com.wesabe.bouncer.proxy.ProxyHttpExchange;
import com.wesabe.bouncer.proxy.ProxyHttpExchangeFactory;

/**
 * A servlet which proxies requests to a backend via an {@link HttpClient}.
 * 
 * @author coda
 *
 */
public class ProxyServlet extends HttpServlet {
	private static final long serialVersionUID = -3276400775243866667L;
	private final HttpClient httpClient;
	private final ProxyHttpExchangeFactory exchangeFactory;
	
	public ProxyServlet(HttpClient httpClient, ProxyHttpExchangeFactory exchangeFactory) {
		this.httpClient = httpClient;
		this.exchangeFactory = exchangeFactory;
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
		final ProxyHttpExchange exchange = exchangeFactory.build(req, resp);
		httpClient.send(exchange);
		try {
			exchange.waitForDone();
		} catch (InterruptedException e) {
			throw new ServletException(e);
		}
	}

	public ProxyHttpExchangeFactory getExchangeFactory() {
		return exchangeFactory;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}
}
