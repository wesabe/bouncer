package com.wesabe.bouncer.auth.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mortbay.jetty.Request;

import com.wesabe.bouncer.auth.WesabeAuthenticator;
import com.wesabe.bouncer.auth.WesabeCredentials;

@RunWith(Enclosed.class)
public class WesabeAuthenticatorTest {
	private static abstract class Context {
		protected DataSource dataSource;
		protected Connection connection;
		protected PreparedStatement statement;
		protected ResultSet resultSet;
		protected WesabeAuthenticator authenticator;
		protected Request request;
		
		public void setup() throws Exception {
			this.resultSet = mock(ResultSet.class);
			
			this.statement = mock(PreparedStatement.class);
			when(statement.executeQuery()).thenReturn(resultSet);
			
			this.connection = mock(Connection.class);
			when(connection.prepareStatement(anyString())).thenReturn(statement);
			
			this.dataSource = mock(DataSource.class);
			when(dataSource.getConnection()).thenReturn(connection);
			
			this.request = mock(Request.class);
			
			this.authenticator = new WesabeAuthenticator(dataSource);
		}
	}
	
	public static class Authenticating_A_Request_Without_An_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itReturnsNull() throws Exception {
			when(request.getHeader("Authorization")).thenReturn(null);
			
			assertNull(authenticator.authenticate(request));
		}
	}
	
	public static class Authenticating_A_Request_With_A_Non_Basic_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itReturnsNull() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Digest BLAHBLAH");
			
			assertNull(authenticator.authenticate(request));
		}
	}
	
	public static class Authenticating_A_Request_With_A_Blank_Basic_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itReturnsNull() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Basic ");
			
			assertNull(authenticator.authenticate(request));
		}
	}
	
	public static class Authenticating_A_Request_With_A_NonBase64_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itReturnsNull() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Basic aaa");
			
			assertNull(authenticator.authenticate(request));
		}
	}
	
	public static class Authenticating_A_Request_With_A_Malformed_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itReturnsNull() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ28=");
			
			assertNull(authenticator.authenticate(request));
		}
	}
	
	public static class Authenticating_A_Request_With_An_Unknown_Username extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(false);
		};
		
		@Test
		public void itReturnsNull() throws Exception {
			assertNull(authenticator.authenticate(request));
		}
		
		@Test
		public void itConnectsToTheDatabaseAndSelectsTheUserRecord() throws Exception {
			authenticator.authenticate(request);
			
			InOrder inOrder = inOrder(dataSource, connection, statement, resultSet);
			inOrder.verify(dataSource).getConnection();
			inOrder.verify(connection).prepareStatement(
				"SELECT * FROM (" +
						"SELECT id, username, salt, password_hash, last_web_login " +
						"FROM users " +
						"WHERE (username = ?) AND status IN (0, 6) " +
					"UNION " +
						"SELECT id, username, salt, password_hash, last_web_login " +
						"FROM users " +
						"WHERE (email = ?) AND status IN (0, 6)" +
				") AS t " +
				"ORDER BY last_web_login DESC " +
				"LIMIT 1"
			);
			inOrder.verify(statement).setString(1, "dingo");
			inOrder.verify(statement).setString(2, "dingo");
			inOrder.verify(statement).executeQuery();
			inOrder.verify(resultSet).first();
			inOrder.verify(connection).close();
		}
	}
	
	public static class Authenticating_A_Request_With_A_Bad_Password extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("DEADBEEF");
		};
		
		@Test
		public void itReturnsNull() throws Exception {
			assertNull(authenticator.authenticate(request));
		}
	}
	
	public static class Authenticating_A_Request_With_A_Good_Password extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("6b56e2021b411940d70b0208693e51bab97cf93c03dc92d7a810f21e1b6faf7f");
		};
		
		@Test
		public void itDoesNotReturnNull() throws Exception {
			assertNotNull(authenticator.authenticate(request));
		}
		
		@Test
		public void itReturnsASetOfCredentials() throws Exception {
			WesabeCredentials creds = (WesabeCredentials) authenticator.authenticate(request);
			
			assertEquals(200, creds.getUserId());
			assertEquals("36d990582959b77f0ec117fa5a703bea8df12ea744b8208a4e13de4f5ec088a1", creds.getAccountKey());
		}
	}
}
