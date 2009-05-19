package com.wesabe.bouncer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.spy.memcached.MemcachedClientIF;

/**
 * Responds to GET requests with 200 OK if the database and memcache connections
 * are live; 500 Internal Service Error otherwise.
 * 
 * @author coda
 *
 */
public class HealthServlet extends HttpServlet {
	private static final long serialVersionUID = -8313510800154929279L;
	private final DataSource dataSource;
	private final MemcachedClientIF memcached;
	
	public HealthServlet(DataSource dataSource, MemcachedClientIF memcached) {
		this.dataSource = dataSource;
		this.memcached = memcached;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		if (databaseWorks() && memcacheWorks()) {
			resp.sendError(HttpServletResponse.SC_OK);
		} else {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private boolean memcacheWorks() {
		return !memcached.getStats().isEmpty();
	}

	private boolean databaseWorks() {
		try {
			final Connection connection = dataSource.getConnection();
			try {
				final PreparedStatement statement = connection.prepareStatement("SELECT 1");
				final ResultSet results = statement.executeQuery();
				return results.first() && (results.getInt(1) == 1);
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			return false;
		}
	}
}
