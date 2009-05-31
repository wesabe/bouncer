package com.wesabe.bouncer.auth.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import net.spy.memcached.MemcachedClientIF;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mortbay.jetty.Request;

import com.wesabe.bouncer.auth.BadCredentialsException;
import com.wesabe.bouncer.auth.LockedAccountException;
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
		protected MemcachedClientIF memcached;
		
		public void setup() throws Exception {
			this.resultSet = mock(ResultSet.class);
			
			this.statement = mock(PreparedStatement.class);
			when(statement.executeQuery()).thenReturn(resultSet);
			
			this.connection = mock(Connection.class);
			when(connection.prepareStatement(anyString())).thenReturn(statement);
			
			this.dataSource = mock(DataSource.class);
			when(dataSource.getConnection()).thenReturn(connection);
			
			this.request = mock(Request.class);
			
			this.memcached = mock(MemcachedClientIF.class);
			
			this.authenticator = new WesabeAuthenticator(dataSource, memcached);
			
			Logger.getLogger("com.wesabe").setLevel(Level.OFF);
		}
	}
	
	public static class Authenticating_A_Request_Without_An_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itThrowsABadCredentialsException() throws Exception {
			when(request.getHeader("Authorization")).thenReturn(null);
			
			try {
				authenticator.authenticate(request);
				fail("should have thrown a BadCredentialsException but didn't");
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
		}
	}
	
	public static class Authenticating_A_Request_With_A_Non_Basic_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itThrowsABadCredentialsException() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Digest BLAHBLAH");
			
			try {
				authenticator.authenticate(request);
				fail("should have thrown a BadCredentialsException but didn't");
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
		}
	}
	
	public static class Authenticating_A_Request_With_A_Blank_Basic_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itThrowsABadCredentialsException() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Basic ");
			
			try {
				authenticator.authenticate(request);
				fail("should have thrown a BadCredentialsException but didn't");
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
		}
	}
	
	public static class Authenticating_A_Request_With_A_NonBase64_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itThrowsABadCredentialsException() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Basic aaa");
			
			try {
				authenticator.authenticate(request);
				fail("should have thrown a BadCredentialsException but didn't");
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
		}
	}
	
	public static class Authenticating_A_Request_With_A_Malformed_Authorization_Header extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
		};
		
		@Test
		public void itThrowsABadCredentialsException() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ28=");
			
			try {
				authenticator.authenticate(request);
				fail("should have thrown a BadCredentialsException but didn't");
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
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
		public void itThrowsABadCredentialsException() throws Exception {
			try {
				authenticator.authenticate(request);
				fail("should have thrown a BadCredentialsException but didn't");
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
		}
		
		@Test
		public void itConnectsToTheDatabaseAndSelectsTheUserRecord() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (BadCredentialsException e) {
				
			}
			
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
			inOrder.verify(resultSet).close();
			inOrder.verify(statement).close();
			inOrder.verify(connection).close();
		}
	}
	
	public static class Authenticating_A_Request_With_A_Bad_Password_And_No_Failed_Logins extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("DEADBEEF");
			when(memcached.incr("failed-logins:200", 1)).thenReturn(1L);
		};
		
		@Test
		public void itThrowsABadCredentialsException() throws Exception {
			try {
				authenticator.authenticate(request);
				fail("should have thrown a BadCredentialsException but didn't");
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
		}
		
		@Test
		public void itIncrementsTheFailedLoginCounterInMemcached() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (BadCredentialsException e) {
			}
			
			InOrder inOrder = inOrder(memcached);
			inOrder.verify(memcached).add("failed-logins:200", 60 * 60 * 24, Integer.valueOf(0));
			inOrder.verify(memcached).incr("failed-logins:200", 1);
		}
	}
	
	public static class Authenticating_A_Request_With_A_Bad_Password_And_One_Failed_Login extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("DEADBEEF");
			when(memcached.incr("failed-logins:200", 1)).thenReturn(2L);
		};
		
		@Test
		public void itThrowsABadCredentialsException() throws Exception {
			try {
				authenticator.authenticate(request);
				fail("should have thrown a BadCredentialsException but didn't");
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
		}
		
		@Test
		public void itIncrementsTheFailedLoginCounterInMemcached() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (BadCredentialsException e) {
			}
			
			InOrder inOrder = inOrder(memcached);
			inOrder.verify(memcached).add("failed-logins:200", 60 * 60 * 24, Integer.valueOf(0));
			inOrder.verify(memcached).incr("failed-logins:200", 1);
		}
	}
	
	public static class Authenticating_A_Request_With_A_Bad_Password_And_Two_Failed_Logins extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("DEADBEEF");
			when(memcached.incr("failed-logins:200", 1)).thenReturn(3L);
		};
		
		@Test
		public void itThrowsABadCredentialsException() throws Exception {
			try {
				authenticator.authenticate(request);
				fail("should have thrown a BadCredentialsException but didn't");
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
		}
		
		@Test
		public void itIncrementsTheFailedLoginCounterInMemcached() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (BadCredentialsException e) {
			}
			
			InOrder inOrder = inOrder(memcached);
			inOrder.verify(memcached).add("failed-logins:200", 60 * 60 * 24, Integer.valueOf(0));
			inOrder.verify(memcached).incr("failed-logins:200", 1);
			
		}
	}
	
	public static class Authenticating_A_Request_With_A_Bad_Password_And_Three_Failed_Logins extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("DEADBEEF");
			when(memcached.incr("failed-logins:200", 1)).thenReturn(4L);
		};
		
		@Test
		public void itThrowsALockedAccountException() throws Exception {
			try {
				authenticator.authenticate(request);
				fail("should have thrown a LockedAccountException but didn't");
			} catch (LockedAccountException e) {
				assertTrue(true);
			}
		}
		
		@Test
		public void itIncrementsTheFailedLoginCounterInMemcached() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			InOrder inOrder = inOrder(memcached);
			inOrder.verify(memcached).add("failed-logins:200", 60 * 60 * 24, Integer.valueOf(0));
			inOrder.verify(memcached).incr("failed-logins:200", 1);
			
		}
		
		@Test
		public void itLocksTheAccountFor15s() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			verify(memcached).set("lock-account:200", 15, Integer.valueOf(15));
		}
	}
	
	public static class Authenticating_A_Request_With_A_Bad_Password_And_Four_Failed_Logins extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("DEADBEEF");
			when(memcached.incr("failed-logins:200", 1)).thenReturn(5L);
		};
		
		@Test
		public void itThrowsALockedAccountException() throws Exception {
			try {
				authenticator.authenticate(request);
				fail("should have thrown a LockedAccountException but didn't");
			} catch (LockedAccountException e) {
				assertTrue(true);
			}
		}
		
		@Test
		public void itIncrementsTheFailedLoginCounterInMemcached() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			InOrder inOrder = inOrder(memcached);
			inOrder.verify(memcached).add("failed-logins:200", 60 * 60 * 24, Integer.valueOf(0));
			inOrder.verify(memcached).incr("failed-logins:200", 1);
			
		}
		
		@Test
		public void itLocksTheAccountFor30s() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			verify(memcached).set("lock-account:200", 30, Integer.valueOf(30));
		}
	}
	
	public static class Authenticating_A_Request_With_A_Bad_Password_And_Eight_Failed_Logins extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("DEADBEEF");
			when(memcached.incr("failed-logins:200", 1)).thenReturn(9L);
		};
		
		@Test
		public void itThrowsALockedAccountException() throws Exception {
			try {
				authenticator.authenticate(request);
				fail("should have thrown a LockedAccountException but didn't");
			} catch (LockedAccountException e) {
				assertTrue(true);
			}
		}
		
		@Test
		public void itIncrementsTheFailedLoginCounterInMemcached() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			InOrder inOrder = inOrder(memcached);
			inOrder.verify(memcached).add("failed-logins:200", 60 * 60 * 24, Integer.valueOf(0));
			inOrder.verify(memcached).incr("failed-logins:200", 1);
			
		}
		
		@Test
		public void itLocksTheAccountFor480s() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			verify(memcached).set("lock-account:200", 480, Integer.valueOf(480));
		}
	}
	
	public static class Authenticating_A_Request_With_A_Bad_Password_And_100_Failed_Logins extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("DEADBEEF");
			when(memcached.incr("failed-logins:200", 1)).thenReturn(101L);
		};
		
		@Test
		public void itThrowsALockedAccountException() throws Exception {
			try {
				authenticator.authenticate(request);
				fail("should have thrown a LockedAccountException but didn't");
			} catch (LockedAccountException e) {
				assertTrue(true);
			}
		}
		
		@Test
		public void itIncrementsTheFailedLoginCounterInMemcached() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			InOrder inOrder = inOrder(memcached);
			inOrder.verify(memcached).add("failed-logins:200", 60 * 60 * 24, Integer.valueOf(0));
			inOrder.verify(memcached).incr("failed-logins:200", 1);
			
		}
		
		@Test
		public void itLocksTheAccountFor900s() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			verify(memcached).set("lock-account:200", 900, Integer.valueOf(900));
		}
	}
	
	public static class Authenticating_A_Request_For_A_Locked_Account_With_Four_Failed_Logins extends Context {
		@Override
		@Before
		public void setup() throws Exception {
			super.setup();
			
			when(request.getHeader("Authorization")).thenReturn("Basic ZGluZ286bWF0aA==");
			
			when(resultSet.first()).thenReturn(true);
			when(resultSet.getString("salt")).thenReturn("cVApCcmpECrgRwCo");
			when(resultSet.getInt("id")).thenReturn(200);
			when(resultSet.getString("password_hash")).thenReturn("DEADBEEF");
			when(memcached.incr("failed-logins:200", 1)).thenReturn(5L);
			when(memcached.get("lock-account:200")).thenReturn(Integer.valueOf(15));
		};
		
		@Test
		public void itThrowsALockedAccountException() throws Exception {
			try {
				authenticator.authenticate(request);
				fail("should have thrown a LockedAccountException but didn't");
			} catch (LockedAccountException e) {
				assertTrue(true);
			}
		}
		
		@Test
		public void itIncrementsTheFailedLoginCounterInMemcached() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			InOrder inOrder = inOrder(memcached);
			inOrder.verify(memcached).add("failed-logins:200", 60 * 60 * 24, Integer.valueOf(0));
			inOrder.verify(memcached).incr("failed-logins:200", 1);
			
		}
		
		@Test
		public void itLocksTheAccountFor30s() throws Exception {
			try {
				authenticator.authenticate(request);
			} catch (LockedAccountException e) {
			}
			
			verify(memcached).set("lock-account:200", 30, Integer.valueOf(30));
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
			when(resultSet.getString("username")).thenReturn("FrankyDoo!");
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
			assertEquals("33aef8191baba4c7b836fa3f79a11a9c50a90b73e7bf381d5487aab3946b39ed", creds.getAccountKey());
		}
		
		@Test
		public void itRemovesTheFailedLoginCounterFromMemcached() throws Exception {
			authenticator.authenticate(request);
			
			verify(memcached).delete("failed-logins:200");
		}
	}
}
