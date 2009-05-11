package com.wesabe.bouncer.proxy;

/**
 * A runtime exception class thrown by {@link ProxyHttpExchange} when a
 * connection to a backend server fails.
 * 
 * @author coda
 */
public class BackendFailureException extends RuntimeException {
	private static final long serialVersionUID = 5601311438579383786L;
	private static final String MSG_TEMPLATE = "Backend connection failed on %s";
	private final ProxyHttpExchange exchange;
	
	/**
	 * Creates a new {@link BackendFailureException}.
	 * 
	 * @param exchange an exchange for which the backend connection failed
	 * @param e the cause of the failure
	 */
	public BackendFailureException(ProxyHttpExchange exchange, Throwable e) {
		super(String.format(MSG_TEMPLATE, exchange), e);
		this.exchange = exchange;
	}
	
	/**
	 * Returns the failed exchange.
	 * 
	 * @return the failed exchange
	 */
	public ProxyHttpExchange getExchange() {
		return exchange;
	}
}
