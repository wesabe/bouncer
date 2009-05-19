package com.wesabe.bouncer.servlets.tests;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.spy.memcached.MemcachedClientIF;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import com.google.common.collect.ImmutableMap;
import com.wesabe.bouncer.servlets.HealthServlet;

@RunWith(Enclosed.class)
public class HealthServletTest {
	private static abstract class Context {

		protected DataSource dataSource;
		protected MemcachedClientIF memcached;
		protected HealthServlet servlet;
		protected HttpServletRequest request;
		protected HttpServletResponse response;
		protected Connection connection;
		protected PreparedStatement query;
		protected ResultSet results;
		protected Map<SocketAddress, Map<String, String>> stats;

		
		public void setup() throws Exception {
			this.results = mock(ResultSet.class);
			
			this.query = mock(PreparedStatement.class);
			when(query.executeQuery()).thenReturn(results);
			
			this.connection = mock(Connection.class);
			when(connection.prepareStatement(anyString())).thenReturn(query);
			
			this.dataSource = mock(DataSource.class);
			when(dataSource.getConnection()).thenReturn(connection);
			
			this.memcached = mock(MemcachedClientIF.class);
			
			this.servlet = new HealthServlet(dataSource, memcached);
			
			this.request = mock(HttpServletRequest.class);
			when(request.getMethod()).thenReturn("GET");
			this.response = mock(HttpServletResponse.class);
		}
		
	}
	
	public static class Handling_A_Health_Request_When_Healthy extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
			
			when(results.first()).thenReturn(true);
			when(results.getInt(anyInt())).thenReturn(1);
			
			this.stats = ImmutableMap.of((SocketAddress) new InetSocketAddress("example", 11211), (Map<String, String>) ImmutableMap.of("one", "two"));
			when(memcached.getStats()).thenReturn(stats);
		}
		
		@Test
		public void itReturns200OK() throws Exception {
			servlet.service(request, response);
			
			verify(response).sendError(200);
		}
		
		@Test
		public void itChecksMemcacheForLiveness() throws Exception {
			servlet.service(request, response);
			
			verify(memcached).getStats();
		}
		
		@Test
		public void itChecksTheDatabaseForLiveness() throws Exception {
			servlet.service(request, response);
			
			InOrder inOrder = inOrder(dataSource, connection, query, results);
			inOrder.verify(dataSource).getConnection();
			inOrder.verify(connection).prepareStatement("SELECT 1");
			inOrder.verify(query).executeQuery();
			inOrder.verify(results).first();
			inOrder.verify(results).getInt(1);
		}
	}
	
	public static class Handling_A_Health_Request_When_Memcached_Is_Down extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
			
			when(results.first()).thenReturn(true);
			when(results.getInt(anyInt())).thenReturn(1);
			
			this.stats = ImmutableMap.of();
			when(memcached.getStats()).thenReturn(stats);
		}
		
		@Test
		public void itReturns500() throws Exception {
			servlet.service(request, response);
			
			verify(response).sendError(500);
		}
		
		@Test
		public void itChecksMemcacheForLiveness() throws Exception {
			servlet.service(request, response);
			
			verify(memcached).getStats();
		}
	}
	
	public static class Handling_A_Health_Request_When_Database_Is_Down extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
			
			when(dataSource.getConnection()).thenThrow(new SQLException("AUGH FIRE"));
		}
		
		@Test
		public void itReturns500() throws Exception {
			servlet.service(request, response);
			
			verify(response).sendError(500);
		}
	}
	
	public static class Handling_A_Health_Request_When_Database_Is_Not_Returning_Results extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
			
			when(results.first()).thenReturn(false);
		}
		
		@Test
		public void itReturns500() throws Exception {
			servlet.service(request, response);
			
			verify(response).sendError(500);
		}
	}
	
	public static class Handling_A_Health_Request_When_Database_Is_Returning_Bad_Results extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
			
			when(results.first()).thenReturn(true);
			when(results.getInt(anyInt())).thenReturn(2);
		}
		
		@Test
		public void itReturns500() throws Exception {
			servlet.service(request, response);
			
			verify(response).sendError(500);
		}
	}
}
