package com.wesabe.bouncer.adapters;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;

/**
 * An endpoint adapter which issues an HTTP Basic Authentication challenge.
 * 
 * @author coda
 *
 */
public class BasicAuthChallengeAdapter extends GrizzlyAdapter {
	private static final int UNAUTHORIZED_STATUS = 401;
	private static final String CHALLENGE_HEADER = "WWW-Authenticate";
	private static final String CHALLENGE_HEADER_TEMPLATE = "Basic realm=\"%s\"";
	private static final Logger LOGGER = Logger.getLogger(BasicAuthChallengeAdapter.class.getName());
	private final String realm, errorMessage;
	
	/**
	 * Creates a new BasicAuthChallengeAdapter.
	 * 
	 * @param realm the Basic Auth realm
	 * @param errorMessage the error message to be presented to the client
	 */
	public BasicAuthChallengeAdapter(String realm, String errorMessage) {
		this.realm = realm;
		this.errorMessage = errorMessage;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.grizzly.tcp.http11.GrizzlyAdapter#service(com.sun.grizzly.tcp.http11.GrizzlyRequest, com.sun.grizzly.tcp.http11.GrizzlyResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void service(GrizzlyRequest request, GrizzlyResponse response) {
		try {
			response.setHeader(CHALLENGE_HEADER, String.format(CHALLENGE_HEADER_TEMPLATE, realm));
			response.sendError(UNAUTHORIZED_STATUS, errorMessage);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error issuing basic auth challenge", e);
		}
		
	}

}
