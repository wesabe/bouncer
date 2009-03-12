package com.wesabe.bouncer;

import java.util.logging.Logger;

import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PooledDataSource;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.wesabe.bouncer.adapters.AuthenticationAdapter;
import com.wesabe.bouncer.adapters.BasicAuthChallengeAdapter;
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
	
	public static void main(String[] args) throws Exception {
		Configuration config = new Configuration(args[0]);
		
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
		
		GrizzlyWebServer ws = new GrizzlyWebServer(config.getPort());
		ws.addGrizzlyAdapter(authenticationAdapter, new String[] { "/" });
		
		if (config.isHttpCompressionEnabled()) {
			LOGGER.config("gzip-encoding enabled for: " + config.getHttpCompressableMimeTypes());
			ws.getSelectorThread().setCompression("on");
			ws.getSelectorThread().setCompressableMimeTypes(config.getHttpCompressableMimeTypes());
			ws.getSelectorThread().setCompressionMinSize(config.getHttpCompressionMinimumSize());
		} else {
			ws.getSelectorThread().setCompression("off");
		}
		
		LOGGER.info("Starting bouncer at http://0.0.0.0:" + config.getPort());
		ws.start();
	}
}
