package com.wesabe.bouncer.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.util.buf.Base64Utils;
import com.sun.grizzly.util.buf.HexUtils;

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
			if (authHeader != null && authHeader.startsWith(BASIC_AUTHENTICATION_PREFIX)) {
				final char[] encodedAuthHeader = authHeader.substring(BASIC_AUTHENTICATION_PREFIX.length(), authHeader.length()).toCharArray();
				byte[] decodedAuthHeader = Base64Utils.decode(encodedAuthHeader);
				if (decodedAuthHeader != null) {
					final String creds = new String(decodedAuthHeader);
					
					int separator = creds.indexOf(':');
					if (separator > 0) {
						final String username = creds.substring(0, separator);
						final String password = creds.substring(separator + 1);
						
						return new AuthHeader(username, password);
					}
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
	private final DataSource dataSource;
	
	public WesabeAuthenticator(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/* (non-Javadoc)
	 * @see com.wesabe.bouncer.auth.Authenticator#authenticate(com.sun.grizzly.tcp.http11.GrizzlyRequest)
	 */
	@Override
	public Principal authenticate(GrizzlyRequest request) {
		final AuthHeader header = AuthHeader.parse(request.getHeader(AUTHORIZATION_HEADER));
		if (header != null) {
			try {
				final Connection connection = dataSource.getConnection();
				try {

					PreparedStatement statement = connection.prepareStatement("SELECT id, username, salt, password_hash FROM users WHERE username = ?");
					statement.setString(1, header.getUsername());

					ResultSet resultSet = statement.executeQuery();

					if (resultSet.first()) {
						final String salt = resultSet.getString("salt");
						final int userId = resultSet.getInt("id");
						final String passwordHash = resultSet.getString("password_hash");
						
						final String candidatePasswordHash = concatenateAndHash(salt, header.getPassword());
						
						if (candidatePasswordHash.equals(passwordHash)) {
							return new WesabeCredentials(userId, concatenateAndHash(concatenateAndHash(header.getUsername(), header.getPassword()), header.getPassword()));
						}
						
						return null;
					}
					
					return null;

				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				} finally {
					connection.close();
				}

			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		return null;
	}

	private String concatenateAndHash(String a, String b)
			throws NoSuchAlgorithmException {
		MessageDigest sha512 = MessageDigest.getInstance("SHA-256");
		
		StringBuilder builder = new StringBuilder();
		builder.append(a);
		builder.append(b);
		
		return HexUtils.convert(sha512.digest(builder.toString().getBytes()));
	}
}
