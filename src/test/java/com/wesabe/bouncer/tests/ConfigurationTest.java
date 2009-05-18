package com.wesabe.bouncer.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.Configuration;

@RunWith(Enclosed.class)
public class ConfigurationTest {
	public static class Reading_Configuration_From_Disk {
		private Configuration configuration;
		
		@Before
		public void setup() throws Exception {
			final String configLocation = ClassLoader.getSystemResource("sample-config.properties").getFile();
			
			this.configuration = new Configuration(configLocation);
		}
		
		@Test
		public void itHasABackendURI() throws Exception {
			assertEquals(URI.create("http://0.0.0.0:8081"), configuration.getBackendUri());
		}
		
		@Test
		public void itHasAnAuthenticationRealm() throws Exception {
			assertEquals("Wesabe API", configuration.getAuthenticationRealm());
		}
		
		@Test
		public void itHasAJdbcDriver() throws Exception {
			assertEquals("com.mysql.jdbc.Driver", configuration.getJdbcDriver());
		}
		
		@Test
		public void itHasAJdbcUri() throws Exception {
			assertEquals(URI.create("jdbc:mysql://localhost/pfc_development"), configuration.getJdbcUri());
		}
		
		@Test
		public void itHasAJdbcUsername() throws Exception {
			assertEquals("pfc", configuration.getJdbcUsername());
		}
		
		@Test
		public void itHasAJdbcPassword() throws Exception {
			assertEquals("blah", configuration.getJdbcPassword());
		}
		
		@Test
		public void itHasOptionalGzipCompression() throws Exception {
			assertTrue(configuration.isHttpCompressionEnabled());
		}
		
		@Test
		public void itHasGzippableMimeTypes() throws Exception {
			assertEquals("application/xml,application/json", configuration.getHttpCompressableMimeTypes());
		}
		
		@Test
		public void itHasAMinimumGzippableEntitySize() throws Exception {
			assertEquals(Integer.valueOf(100), configuration.getHttpCompressionMinimumSize());
		}
		
		@Test
		public void itHasC3P0Properties() throws Exception {
			final Properties c3p0Properties = configuration.getC3P0Properties();
			assertEquals(1, c3p0Properties.size());
			assertEquals("1800", c3p0Properties.getProperty("maxIdleTime"));
		}
		
		@Test
		public void itHasMemcachedServers() throws Exception {
			final List<InetSocketAddress> servers = configuration.getMemcachedServers();
			
			final InetSocketAddress first = servers.get(0);
			assertThat(first.getHostName(), is("memcache1"));
			assertThat(first.getPort(), is(11211));
			
			final InetSocketAddress second = servers.get(1);
			assertThat(second.getHostName(), is("memcache2"));
			assertThat(second.getPort(), is(11212));
		}
		
		@Test
		public void itIsDebug() throws Exception {
			assertThat(configuration.isDebug(), is(true));
		}
		
		@Test
		public void itHasAClientThreadPoolSize() throws Exception {
			assertThat(configuration.getHttpClientThreadPoolSize(), is(40));
		}
		
		@Test
		public void itHasAClientMaxConnectionsLimit() throws Exception {
			assertThat(configuration.getHttpClientMaxConnections(), is(1000));
		}
		
		@Test
		public void itHasAGracefulShutdownPeriod() throws Exception {
			assertThat(configuration.getHttpGracefulShutdownPeriod(), is(5000));
		}
	}
}