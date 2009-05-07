package com.wesabe.bouncer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;

import com.wesabe.bouncer.auth.Authenticator;

public class ProxyServlet extends HttpServlet {
	private static final long serialVersionUID = -3276400775243866667L;
	private final Authenticator authenticator;
	
	public ProxyServlet(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		final Request request = (Request) req;
		final Principal creds = authenticator.authenticate(request);
		if (creds != null) {
			request.setUserPrincipal(creds);
			resp.setStatus(HttpServletResponse.SC_OK);
			final PrintWriter writer = resp.getWriter();
			writer.println("Hello, world.");
			writer.close();
		} else {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			final PrintWriter writer = resp.getWriter();
			writer.println("Go away.");
			writer.close();
		}
		
	}
}
