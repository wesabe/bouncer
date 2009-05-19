package com.wesabe.bouncer;

import javax.sql.DataSource;

import net.spy.memcached.MemcachedClient;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.servlet.GzipFilter;
import org.mortbay.thread.QueuedThreadPool;

import com.mchange.v2.c3p0.DataSources;
import com.wesabe.bouncer.auth.Authenticator;
import com.wesabe.bouncer.auth.WesabeAuthenticator;
import com.wesabe.bouncer.jetty.QuietErrorHandler;
import com.wesabe.bouncer.proxy.ProxyHttpExchangeFactory;
import com.wesabe.bouncer.servlets.AuthenticationFilter;
import com.wesabe.bouncer.servlets.HealthServlet;
import com.wesabe.bouncer.servlets.ProxyServlet;
import com.wesabe.servlet.ErrorReporterFilter;
import com.wesabe.servlet.SafeFilter;
import com.wesabe.servlet.errors.DebugErrorReporter;
import com.wesabe.servlet.errors.ErrorReporter;
import com.wesabe.servlet.errors.SendmailErrorReporter;

/**
 * The main entry point for Bouncer.
 * 
 * @author coda
 */
public class Runner {
	public static void main(String[] args) throws Exception {
		checkArguments(args);
		
		final Configuration config = new Configuration(args[0]);
		final Server server = setupServer(config, Integer.valueOf(args[1]));
		final Context context = setupContext(config, server);
		
		final DataSource dataSource = DataSources.pooledDataSource(
				DataSources.unpooledDataSource(
						config.getJdbcUri().toASCIIString(),
						config.getJdbcUsername(),
						config.getJdbcPassword()
				),
				config.getC3P0Properties()
		);
		
		final MemcachedClient memcached = new MemcachedClient(config.getMemcachedServers());
		setupAuthentication(config, context, dataSource, memcached);
		setupHealth(context, dataSource, memcached);
		setupProxy(config, context);
		
		server.start();
		server.join();
	}

	private static void setupHealth(Context context, DataSource dataSource,
			MemcachedClient memcached) {
		context.addServlet(
			new ServletHolder(new HealthServlet(dataSource, memcached)),
			"/health/"
		);
	}

	private static void setupProxy(Configuration config, Context context)
			throws Exception {
		final HttpClient client = new HttpClient();
		client.setThreadPool(new QueuedThreadPool(config.getHttpClientThreadPoolSize()));
		client.setMaxConnectionsPerAddress(config.getHttpClientMaxConnections());
		final ProxyHttpExchangeFactory factory = new ProxyHttpExchangeFactory(config.getBackendUri());
		final ServletHolder proxyHolder = new ServletHolder(new ProxyServlet(client, factory));
		context.addServlet(proxyHolder, "/*");
	}

	private static void setupAuthentication(Configuration config, Context context, DataSource dataSource, MemcachedClient memcached)
			throws Exception {
		final Authenticator authenticator = new WesabeAuthenticator(dataSource, memcached);
		
		context.addFilter(new FilterHolder(
			new AuthenticationFilter(authenticator, config.getAuthenticationRealm())
		), "/*", 0);
	}

	private static Context setupContext(Configuration config, Server server) throws Exception {
		final Context context = new Context(server, "/");
		context.addFilter(SafeFilter.class, "/*", Handler.DEFAULT);
		
		final ErrorReporter reporter;
		if (config.isDebug()) {
			reporter = new DebugErrorReporter("Exception Notifier <support@wesabe.com>", "you@wesabe.com", "bouncer");
		} else {
			reporter = new SendmailErrorReporter("Exception Notifier <support@wesabe.com>", "eng@wesabe.com", "bouncer", "/usr/sbin/sendmail");
		}
		
		final FilterHolder errorHolder = new FilterHolder(new ErrorReporterFilter(reporter, "Wesabe engineers have been alerted to this error. If you have further questions, please contact <support@wesabe.com>."));
		context.addFilter(errorHolder, "/*", Handler.DEFAULT);
		
		if (config.isHttpCompressionEnabled()) {
			final FilterHolder gzipHolder = new FilterHolder(GzipFilter.class);
			gzipHolder.setInitParameter("minGzipSize", config.getHttpCompressionMinimumSize().toString());
			gzipHolder.setInitParameter("mimeTypes", config.getHttpCompressableMimeTypes());
			context.addFilter(gzipHolder, "/*", Handler.DEFAULT);
		}
		
		context.setErrorHandler(new QuietErrorHandler());
		
		return context;
	}

	private static Server setupServer(Configuration config, int port) {
		final Server server = new Server();
		final Connector connector = new SelectChannelConnector();
		connector.setPort(port);
		server.addConnector(connector);
		server.setGracefulShutdown(config.getHttpGracefulShutdownPeriod());
		server.setSendServerVersion(false);
		server.setStopAtShutdown(true);
		return server;
	}

	private static void checkArguments(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: java -jar <bouncer.jar> <config file> <port>");
			System.exit(-1);
		}
	}
}
