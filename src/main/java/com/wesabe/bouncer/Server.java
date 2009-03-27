package com.wesabe.bouncer;

import java.util.logging.Logger;

import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PooledDataSource;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.wesabe.bouncer.adapters.AuthenticationAdapter;
import com.wesabe.bouncer.adapters.BasicAuthChallengeAdapter;
import com.wesabe.bouncer.adapters.HealthAdapter;
import com.wesabe.bouncer.adapters.ProxyAdapter;
import com.wesabe.bouncer.auth.Authenticator;
import com.wesabe.bouncer.auth.WesabeAuthenticator;
import com.wesabe.bouncer.client.BackendService;
import com.wesabe.bouncer.client.HttpClientFactory;
import com.wesabe.bouncer.client.ProxyBackendService;
import com.wesabe.bouncer.client.ProxyRequestFactory;
import com.wesabe.bouncer.client.ProxyResponseFactory;
import com.wesabe.bouncer.security.ResponseHeaderSet;

public class Server {
	private static final Logger LOGGER = Logger.getLogger(Server.class.getCanonicalName());
	private static final int CONFIG_FILE_IDX = 0;
	private static final int PORT_IDX = 1;
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: java -jar <bouncer.jar> <config file> <port>");
		}
		
		Configuration config = new Configuration(args[CONFIG_FILE_IDX]);
		
		BasicAuthChallengeAdapter challengeAdapter = new BasicAuthChallengeAdapter(
				config.getAuthenticationRealm(),
				config.getAuthenticationErrorMessage()
		);
		
		BackendService backendService = new ProxyBackendService(
				config.getBackendUri(),
				new HttpClientFactory()
		);
		
		ResponseHeaderSet responseHeaders = new ResponseHeaderSet();
		
		ProxyRequestFactory requestFactory = new ProxyRequestFactory();
		
		ProxyResponseFactory responseFactory = new ProxyResponseFactory(
				config.getServerName(),
				responseHeaders
			);
		
		ProxyAdapter proxyAdapter = new ProxyAdapter(
				backendService,
				requestFactory,
				responseFactory
		);
		
		LOGGER.info("Initializing data source");
		
		PooledDataSource dataSource = (PooledDataSource) DataSources.pooledDataSource(
				DataSources.unpooledDataSource(
						config.getJdbcUri().toString(),
						config.getJdbcUsername(),
						config.getJdbcPassword()
				),
				config.getC3P0Properties()
		);

		HealthAdapter healthAdapter = new HealthAdapter(dataSource);
		
		Authenticator authenticator = new WesabeAuthenticator(dataSource);
		
		AuthenticationAdapter authenticationAdapter = new AuthenticationAdapter(
				authenticator,
				challengeAdapter,
				proxyAdapter,
				healthAdapter
		);
		
		int port = Integer.valueOf(args[PORT_IDX]);
		
		GrizzlyWebServer ws = new GrizzlyWebServer(port);
		ws.addGrizzlyAdapter(authenticationAdapter, new String[] {});
		
		if (config.isHttpCompressionEnabled()) {
			LOGGER.config("gzip-encoding enabled for: " + config.getHttpCompressableMimeTypes());
			ws.getSelectorThread().setCompression("on");
			ws.getSelectorThread().setCompressableMimeTypes(config.getHttpCompressableMimeTypes());
			ws.getSelectorThread().setCompressionMinSize(config.getHttpCompressionMinimumSize());
		} else {
			ws.getSelectorThread().setCompression("off");
		}
		
		LOGGER.info("Starting bouncer at http://0.0.0.0:" + ws.getSelectorThread().getPort());
		ws.start();
	}
}
