package com.wesabe.bouncer.auth;

import java.security.Principal;

import javax.security.auth.Subject;

import org.eclipse.jetty.server.UserIdentity;

import com.google.common.collect.ImmutableSet;

public class WesabeUserIdentity implements UserIdentity {
	private final WesabeCredentials principal;
	
	public WesabeUserIdentity(WesabeCredentials principal) {
		this.principal = principal;
	}

	@Override
	public Subject getSubject() {
		return new Subject(true, ImmutableSet.of(getUserPrincipal()), ImmutableSet.of(), ImmutableSet.of());
	}

	@Override
	public Principal getUserPrincipal() {
		return principal;
	}

	@Override
	public boolean isUserInRole(String role, Scope scope) {
		return true;
	}
}
