package com.wesabe.bouncer.adapters;

import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.wesabe.bouncer.auth.Authenticator;

/**
 * A filtering adapter which sends authenticated requests to a passthrough
 * adapter and unauthenticated requests to a challenge adapter.
 * 
 * @author coda
 */
public class AuthenticationAdapter extends GrizzlyAdapter {
	private static final String AUTHENTICATION_HEADER = "Authorization";
	private static final Logger LOGGER = Logger.getLogger(AuthenticationAdapter.class.getName());

	private final Authenticator authenticator;
	private final GrizzlyAdapter challengeAdapter, passthroughAdapter, healthAdapter;
	
	/**
	 * Creates a new AuthenticationAdapter.
	 * 
	 * @param authenticator the {@link Authenticator} against which requests
	 *                      will be authenticated
	 * @param challengeAdapter the adapter to which unauthenticated requests are
	 *                         sent
	 * @param passthroughAdapter the adapter to which authenticated requests are
	 *                           sent
	 */
	public AuthenticationAdapter(Authenticator authenticator, GrizzlyAdapter challengeAdapter,
			GrizzlyAdapter passthroughAdapter, GrizzlyAdapter healthAdapter) {
		super();
		this.authenticator = authenticator;
		this.challengeAdapter = challengeAdapter;
		this.passthroughAdapter = passthroughAdapter;
		this.healthAdapter = healthAdapter;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.grizzly.tcp.http11.GrizzlyAdapter#service(com.sun.grizzly.tcp.http11.GrizzlyRequest, com.sun.grizzly.tcp.http11.GrizzlyResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void service(GrizzlyRequest request, GrizzlyResponse response) {
		System.err.println(request.getRequestURI());
		try {
			if (request.getMethod().equalsIgnoreCase("GET") && request.getRequestURI().equals("/health/")) {
				healthAdapter.service(request, response);
			} else {
				final Principal principal = authenticator.authenticate(request);
				if (principal != null) {
					markAsAuthenticated(request, principal);
					passthroughAdapter.service(request, response);
				} else {
					challengeAdapter.service(request, response);
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error authenticating request", e);
		}
	}

	private void markAsAuthenticated(GrizzlyRequest request, final Principal principal) {
		request.getRequest().getMimeHeaders().setValue(AUTHENTICATION_HEADER).setString(principal.toString());
	}

}
