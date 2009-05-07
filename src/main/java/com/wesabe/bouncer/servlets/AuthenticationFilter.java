package com.wesabe.bouncer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

import com.wesabe.bouncer.auth.Authenticator;

public class AuthenticationFilter implements Filter {
	private final Authenticator authenticator;
	private final String challenge;
	private final String errorMessage;
	
	public AuthenticationFilter(Authenticator authenticator, String realm, String errorMessage) {
		this.authenticator = authenticator;
		this.challenge = "Basic realm=\"" + realm + "\"";
		this.errorMessage = errorMessage;
	}
	
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		final Request request = (Request) req;
		final Principal principal = authenticator.authenticate(request);
		if (principal != null) {
			request.setUserPrincipal(principal);
			request.setAuthType(HttpServletRequest.BASIC_AUTH);
			chain.doFilter(request, resp);
		} else {
			final Response response = (Response) resp;
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setHeader("WWW-Authenticate", challenge);
			final PrintWriter writer = resp.getWriter();
			writer.println(errorMessage);
			writer.close();
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

}
