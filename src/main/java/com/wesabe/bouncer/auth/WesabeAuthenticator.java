package com.wesabe.bouncer.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.mortbay.jetty.Request;

/**
 * An {@link Authenticator} which, given a {@link DataSource} for the PFC
 * database, authenticated Basic HTTP Authorization requests against the PFC
 * user records and returns {@link WesabeCredentials}.
 * 
 * @author coda
 *
 */
public class WesabeAuthenticator implements Authenticator {
	private static class AuthHeader {
		private static final String BASIC_AUTHENTICATION_PREFIX = "Basic ";
		private final String username, password;
		
		public static AuthHeader parse(String authHeader) {
			if ((authHeader != null) && authHeader.startsWith(BASIC_AUTHENTICATION_PREFIX)) {
				final String encodedCreds = authHeader.substring(BASIC_AUTHENTICATION_PREFIX.length(), authHeader.length());
				final String creds = new String(Base64.decodeBase64(encodedCreds.getBytes()));
				int separator = creds.indexOf(':');
				if (separator > 0) {
					final String username = creds.substring(0, separator);
					final String password = creds.substring(separator + 1);
					
					return new AuthHeader(username, password);
				}
			}
			
			return null;
		}
		
		public AuthHeader(String username, String password) {
			this.username = username;
			this.password = password;
		}
		
		public String getUsername() {
			return username;
		}
		
		public String getPassword() {
			return password;
		}
	}
	
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String USER_ID_FIELD = "id";
	private static final String USERNAME_FIELD = "username";
	private static final String SALT_FIELD = "salt";
	private static final String PASSWORD_HASH_FIELD = "password_hash";
	private static final String USER_SELECT_SQL =
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
		"LIMIT 1";
	private static final String HASH_ALGORITHM = "SHA-256";
	private final DataSource dataSource;
	
	public WesabeAuthenticator(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Override
	public Principal authenticate(Request request) throws LockedAccountException, BadCredentialsException {
		final AuthHeader header = AuthHeader.parse(request.getHeader(AUTHORIZATION_HEADER));
		if (header != null) {
			try {
				final Connection connection = dataSource.getConnection();
				try {
					final ResultSet resultSet = getResults(connection, header);
					if (resultSet.first()) {
						return buildCredentials(header, resultSet);
					}
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				} finally {
					connection.close();
				}

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		throw new BadCredentialsException();
	}

	private Principal buildCredentials(AuthHeader header, ResultSet resultSet)
			throws SQLException, NoSuchAlgorithmException {
		final String salt = resultSet.getString(SALT_FIELD);
		final int userId = resultSet.getInt(USER_ID_FIELD);
		final String username = resultSet.getString(USERNAME_FIELD);
		final String passwordHash = resultSet.getString(PASSWORD_HASH_FIELD);
		
		final String candidatePasswordHash = concatenateAndHash(salt, header.getPassword());
		
		if (candidatePasswordHash.equals(passwordHash)) {
			return new WesabeCredentials(
					userId,
					concatenateAndHash(
							concatenateAndHash(username, header.getPassword()),
							header.getPassword()
					)
			);
		}
		
		return null;
	}

	private ResultSet getResults(Connection connection, AuthHeader header)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement(USER_SELECT_SQL);
		statement.setString(1, header.getUsername());
		statement.setString(2, header.getUsername());

		ResultSet resultSet = statement.executeQuery();
		return resultSet;
	}

	private String concatenateAndHash(String a, String b)
			throws NoSuchAlgorithmException {
		MessageDigest sha512 = MessageDigest.getInstance(HASH_ALGORITHM);
		
		StringBuilder builder = new StringBuilder();
		builder.append(a);
		builder.append(b);
		
		return new String(Hex.encodeHex(sha512.digest(builder.toString().getBytes())));
	}
}
