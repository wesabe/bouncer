package com.wesabe.bouncer.security.normalizers;

public class MalformedValueException extends Exception {
	private static final long serialVersionUID = -1045492595684854887L;
	private final Object value;
	
	public MalformedValueException(String message, Object value) {
		super(message);
		this.value = value;
	}
	
	public MalformedValueException(String message, Object value, Throwable cause) {
		super(message, cause);
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}
}
