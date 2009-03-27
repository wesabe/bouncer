package com.wesabe.bouncer.util;

import java.util.Collection;
import java.util.Locale;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public class CaseInsensitiveMultimap extends ForwardingMultimap<String, String> {
	private final ImmutableMultimap<String, String> headers;
	
	public static class Builder {
		private final com.google.common.collect.ImmutableMultimap.Builder<String, String> builder;
		
		private Builder() {
			this.builder = ImmutableMultimap.builder();
		}
		
		public Builder put(String key, String value) {
			builder.put(downcase(key), value);
			return this;
		}
		
		public CaseInsensitiveMultimap build() {
			return new CaseInsensitiveMultimap(builder.build());
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	private CaseInsensitiveMultimap(ImmutableMultimap<String, String> headers) {
		this.headers = headers;
	}
	
	@Override
	protected Multimap<String, String> delegate() {
		return headers;
	}
	
	@Override
	public Collection<String> get(String key) {
		return super.get(downcase(key));
	}

	private static String downcase(String key) {
		return key.toLowerCase(Locale.US);
	}
}
