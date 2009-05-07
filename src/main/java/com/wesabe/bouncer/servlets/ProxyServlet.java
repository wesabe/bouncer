package com.wesabe.bouncer.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyServlet extends HttpServlet {
	private static final long serialVersionUID = -3276400775243866667L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		final PrintWriter writer = resp.getWriter();
		writer.println("Hello, world.");
		writer.close();
	}
}
