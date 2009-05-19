package com.wesabe.bouncer.servlets;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.Request;

import com.wesabe.bouncer.auth.Authenticator;
import com.wesabe.bouncer.auth.BadCredentialsException;
import com.wesabe.bouncer.auth.LockedAccountException;
import com.wesabe.bouncer.auth.WesabeCredentials;
import com.wesabe.servlet.SafeRequest;

/**
 * A servlet {@link Filter} which parses Basic Auth {@code Authorization}
 * headers and authenticates them via an {@link Authenticator}. If the request
 * is authenticated, a {@link WesabeCredentials} instance is attached as the
 * request's principal. Otherwise, a {@code 401 Unauthorized} response is
 * sent with a Basic Auth challenge.
 * 
 * @author coda
 *
 */
public class AuthenticationFilter implements Filter {
	private final Authenticator authenticator;
	private final String challenge;
	
	public AuthenticationFilter(Authenticator authenticator, String realm) {
		this.authenticator = authenticator;
		this.challenge = "Basic realm=\"" + realm + "\"";
	}
	
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		final SafeRequest safeRequest = (SafeRequest) req;
		final Request request = (Request) safeRequest.getRequest();
		
		if (request.getRequestURI().equals("/health/")) {
			chain.doFilter(request, resp);
			return;
		}
		
		final HttpServletResponse response = (HttpServletResponse) resp;
		final Principal principal;
		try {
			principal = authenticator.authenticate(request);
			request.setUserPrincipal(principal);
			request.setAuthType(HttpServletRequest.BASIC_AUTH);
			chain.doFilter(request, resp);
		} catch (LockedAccountException e) {
			response.setIntHeader(HttpHeaders.RETRY_AFTER, e.getPenaltyDuration());
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		} catch (BadCredentialsException e) {
			response.setHeader(HttpHeaders.WWW_AUTHENTICATE, challenge);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

}
