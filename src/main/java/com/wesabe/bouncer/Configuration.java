package com.wesabe.bouncer;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Map.Entry;

public class Configuration {
	private static final String C3P0_KEY_PREFIX = "c3p0.";
	private static final String HTTP_COMPRESSION_MINIMUM_SIZE_KEY = "bouncer.http.compression.minimum-size";
	private static final String HTTP_COMPRESSION_MIME_TYPES_KEY = "bouncer.http.compression.mime-types";
	private static final String HTTP_COMPRESSION_ENABLE_KEY = "bouncer.http.compression.enable";
	private static final String JDBC_PASSWORD_KEY = "bouncer.jdbc.password";
	private static final String JDBC_USERNAME_KEY = "bouncer.jdbc.username";
	private static final String JDBC_URI_KEY = "bouncer.jdbc.uri";
	private static final String JDBC_DRIVER_KEY = "bouncer.jdbc.driver";
	private static final String AUTHENTICATION_ERROR_MESSAGE_KEY = "bouncer.auth.error-message";
	private static final String AUTHENTICATION_REALM_KEY = "bouncer.auth.realm";
	private static final String SERVER_NAME_KEY = "bouncer.server-name";
	private static final String PORT_KEY = "bouncer.port";
	private static final String BACKEND_URI_KEY = "bouncer.backend.uri";
	
	private final Properties properties;
	
	public Configuration(String filename) throws IOException {
		this.properties = new Properties();
		
		FileReader reader = new FileReader(filename);
		try {
			properties.load(reader);
		} finally {
			reader.close();
		}
	}

	public String getBackendUri() {
		return properties.getProperty(BACKEND_URI_KEY);
	}

	public int getPort() {
		return Integer.valueOf(properties.getProperty(PORT_KEY));
	}

	public String getServerName() {
		return properties.getProperty(SERVER_NAME_KEY);
	}

	public String getAuthenticationRealm() {
		return properties.getProperty(AUTHENTICATION_REALM_KEY);
	}

	public String getAuthenticationErrorMessage() {
		return properties.getProperty(AUTHENTICATION_ERROR_MESSAGE_KEY);
	}

	public String getJdbcDriver() {
		return properties.getProperty(JDBC_DRIVER_KEY);
	}

	public URI getJdbcUri() throws URISyntaxException {
		return new URI(properties.getProperty(JDBC_URI_KEY));
	}

	public String getJdbcUsername() {
		return properties.getProperty(JDBC_USERNAME_KEY);
	}

	public String getJdbcPassword() {
		return properties.getProperty(JDBC_PASSWORD_KEY);
	}

	public boolean isHttpCompressionEnabled() {
		return Boolean.parseBoolean(properties.getProperty(HTTP_COMPRESSION_ENABLE_KEY));
	}

	public String getHttpCompressableMimeTypes() {
		return properties.getProperty(HTTP_COMPRESSION_MIME_TYPES_KEY);
	}

	public int getHttpCompressionMinimumSize() {
		return Integer.valueOf(properties.getProperty(HTTP_COMPRESSION_MINIMUM_SIZE_KEY));
	}

	public Properties getC3P0Properties() {
		final Properties c3p0Properties = new Properties();
		
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String) entry.getKey();
			if (key.startsWith(C3P0_KEY_PREFIX)) {
				key = key.substring(C3P0_KEY_PREFIX.length());
				String value = (String) entry.getValue();
				c3p0Properties.setProperty(key, value);
			}
		}
		
		return c3p0Properties;
	}
	
	
}
