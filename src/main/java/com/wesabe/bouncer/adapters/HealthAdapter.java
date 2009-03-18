package com.wesabe.bouncer.adapters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;

public class HealthAdapter extends GrizzlyAdapter {
	private final static Logger LOGGER = Logger.getLogger(HealthAdapter.class.getCanonicalName());
	private final DataSource dataSource;
	
	public HealthAdapter(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void service(GrizzlyRequest request, GrizzlyResponse response) {
		try {
			if (request.getMethod().equals("GET")) {
				if (request.getRequestURI().equals("/health/")) {
					try {
						Connection connection = dataSource.getConnection();
						try {
							PreparedStatement statement = connection.prepareStatement("SELECT 1");
							ResultSet resultSet = statement.executeQuery();
							if (resultSet.first() && resultSet.getInt(1) == 1) {
								response.setStatus(200);
								setBody(response, "OK");
							} else {
								response.setStatus(500);
								setBody(response, "ERROR");
							}
						} finally {
							connection.close();
						}
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Error peforming health check", e);
						response.setStatus(500);
						setBody(response, "ERROR");
					}
				} else {
					response.sendError(404);
					setBody(response, "Not found.");
				}
			} else {
				response.setHeader("Allow", "GET");
				response.sendError(406);
				setBody(response, "GET only");
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error sending response", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setBody(GrizzlyResponse response, String body) throws IOException {
		response.getStream().write(body.getBytes());
		response.getStream().close();
	}

}
