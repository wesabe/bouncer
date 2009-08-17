package com.wesabe.bouncer;

import javax.sql.DataSource;

import net.spy.memcached.MemcachedClient;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.security.Constraint;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.mchange.v2.c3p0.DataSources;
import com.wesabe.bouncer.auth.WesabeAuthenticator;
import com.wesabe.bouncer.jetty.QuietErrorHandler;
import com.wesabe.bouncer.proxy.ProxyHttpExchangeFactory;
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
		final ServletContextHandler context = setupContext(config, server);
		
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

	private static void setupHealth(ServletContextHandler context, DataSource dataSource,
			MemcachedClient memcached) {
		context.addServlet(
			new ServletHolder(new HealthServlet(dataSource, memcached)),
			"/health/"
		);
	}

	private static void setupProxy(Configuration config, ServletContextHandler context)
			throws Exception {
		final HttpClient client = new HttpClient();
		client.setThreadPool(new QueuedThreadPool(config.getHttpClientThreadPoolSize()));
		client.setMaxConnectionsPerAddress(config.getHttpClientMaxConnections());
		final ProxyHttpExchangeFactory factory = new ProxyHttpExchangeFactory(config.getBackendUri());
		final ServletHolder proxyHolder = new ServletHolder(new ProxyServlet(client, factory));
		context.addServlet(proxyHolder, "/*");
	}

	private static void setupAuthentication(final Configuration config, ServletContextHandler context, DataSource dataSource, MemcachedClient memcached)
			throws Exception {
		final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
		final org.eclipse.jetty.security.Authenticator authenticator = new WesabeAuthenticator(config.getAuthenticationRealm(), dataSource, memcached);
		securityHandler.setAuthenticator(authenticator);
		
		final Constraint requireAuthentication = new Constraint();
		requireAuthentication.setAuthenticate(true);
		requireAuthentication.setRoles(new String[] { "user" });
		
		final ConstraintMapping authenticateAll = new ConstraintMapping();
		authenticateAll.setPathSpec("/*");
		authenticateAll.setConstraint(requireAuthentication);
		
		final Constraint passThrough = new Constraint();
		passThrough.setAuthenticate(false);
		
		final ConstraintMapping healthCheckExemption = new ConstraintMapping();
		healthCheckExemption.setPathSpec("/health/");
		healthCheckExemption.setConstraint(passThrough);
		
		final ConstraintMapping statsExemption = new ConstraintMapping();
		statsExemption.setPathSpec("/stats/*");
		statsExemption.setConstraint(passThrough);
		
		securityHandler.setConstraintMappings(new ConstraintMapping[] { authenticateAll, healthCheckExemption, statsExemption });
		context.setSecurityHandler(securityHandler);
	}

	private static ServletContextHandler setupContext(Configuration config, Server server) throws Exception {
		final ServletContextHandler context = new ServletContextHandler(server, "/");
		context.addFilter(SafeFilter.class, "/*", FilterMapping.DEFAULT);
		
		final ErrorReporter reporter;
		if (config.isDebug()) {
			reporter = new DebugErrorReporter("Exception Notifier <support@wesabe.com>", "you@wesabe.com", "bouncer");
		} else {
			reporter = new SendmailErrorReporter("Exception Notifier <support@wesabe.com>", "eng@wesabe.com", "bouncer", "/usr/sbin/sendmail");
		}
		
		final FilterHolder errorHolder = new FilterHolder(new ErrorReporterFilter(reporter, "Wesabe engineers have been alerted to this error. If you have further questions, please contact <support@wesabe.com>."));
		context.addFilter(errorHolder, "/*", FilterMapping.DEFAULT);
		
		if (config.isHttpCompressionEnabled()) {
			final FilterHolder gzipHolder = new FilterHolder(GzipFilter.class);
			gzipHolder.setInitParameter("minGzipSize", config.getHttpCompressionMinimumSize().toString());
			gzipHolder.setInitParameter("mimeTypes", config.getHttpCompressableMimeTypes());
			context.addFilter(gzipHolder, "/*", FilterMapping.DEFAULT);
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
