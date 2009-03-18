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
import com.wesabe.bouncer.http.BackendService;
import com.wesabe.bouncer.http.HttpClientFactory;
import com.wesabe.bouncer.http.HttpHeaders;
import com.wesabe.bouncer.http.ProxyBackendService;
import com.wesabe.bouncer.http.ProxyRequestFactory;
import com.wesabe.bouncer.http.ProxyResponseFactory;

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
		
		HttpHeaders httpHeaders = new HttpHeaders();
		
		ProxyRequestFactory requestFactory = new ProxyRequestFactory(httpHeaders);
		
		ProxyResponseFactory responseFactory = new ProxyResponseFactory(
				config.getServerName(),
				httpHeaders
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

		Authenticator authenticator = new WesabeAuthenticator(dataSource);
		
		AuthenticationAdapter authenticationAdapter = new AuthenticationAdapter(
				authenticator,
				challengeAdapter,
				proxyAdapter
		);
		
		int port = Integer.valueOf(args[PORT_IDX]);
		
		GrizzlyWebServer ws = new GrizzlyWebServer(port);
		ws.addGrizzlyAdapter(authenticationAdapter, new String[] { "/" });
		
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
		
		
		int healthPort = port + 1000;
		GrizzlyWebServer healthWs = new GrizzlyWebServer(healthPort);
		HealthAdapter healthAdapter = new HealthAdapter(dataSource);
		healthWs.addGrizzlyAdapter(healthAdapter, new String[] { "/" });
		LOGGER.info("Starting health-check at http://0.0.0.0:" + healthWs.getSelectorThread().getPort());
		healthWs.start();
	}
}
