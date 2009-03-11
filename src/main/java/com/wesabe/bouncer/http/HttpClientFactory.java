package com.wesabe.bouncer.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * A factory class for building {@link HttpClient} instances which are
 * thread-safe, don't follow redirects, and don't retry requests.
 * 
 * @author coda
 *
 */
public class HttpClientFactory {
	
	private static class NonredirectHandler extends DefaultRedirectHandler {
		@Override
		public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
			return false;
		}
	}
	
	private static class AlwaysKeepAliveStrategy implements ConnectionKeepAliveStrategy {
		private static final int ONE_MINUTE = 60000; // ms = 1 minute

		@Override
		public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
			return ONE_MINUTE;
		}
	}
	
	private static class NeverRetryHandler implements HttpRequestRetryHandler {
		@Override
		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
			return false;
		}
	}
	
	private final HttpParams params;
	private final SchemeRegistry schemeRegistry;
	private final ThreadSafeClientConnManager connectionManager;
	
	public HttpClientFactory() {
		this.params = buildParams();
		this.schemeRegistry = buildSchemeRegistry();
		this.connectionManager = buildConnectionManager();
	}
	
	public HttpClient buildClient() {
		final DefaultHttpClient client = new DefaultHttpClient(connectionManager, params);
		client.setRedirectHandler(new NonredirectHandler());
		client.setKeepAliveStrategy(new AlwaysKeepAliveStrategy());
		client.setHttpRequestRetryHandler(new NeverRetryHandler());
		return client;
	}

	private ThreadSafeClientConnManager buildConnectionManager() {
		return new ThreadSafeClientConnManager(params, schemeRegistry);
	}

	private SchemeRegistry buildSchemeRegistry() {
		final SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", new PlainSocketFactory(), 80));
		return schemeRegistry;
	}

	private BasicHttpParams buildParams() {
		return new BasicHttpParams();
	}

}
