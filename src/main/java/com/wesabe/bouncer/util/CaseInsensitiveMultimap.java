package com.wesabe.bouncer.util;

import java.util.Collection;
import java.util.Locale;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class CaseInsensitiveMultimap extends ForwardingMultimap<String, String> {
	private final ImmutableMultimap<String, String> headers;
	
	public static class Builder {
		private final Multimap<String, String> headers;
		
		private Builder() {
			this.headers = Multimaps.newArrayListMultimap();
		}
		
		public Builder put(String key, String value) {
			headers.put(downcase(key), value);
			return this;
		}
		
		public CaseInsensitiveMultimap build() {
			return new CaseInsensitiveMultimap(ImmutableMultimap.copyOf(headers));
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
