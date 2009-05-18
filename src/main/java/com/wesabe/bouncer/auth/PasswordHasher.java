package com.wesabe.bouncer.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;

import com.wesabe.bouncer.util.BoundedLinkedHashMap;

/**
 * A utility class to produce password hashes and account keys. Memoized for
 * speed.
 * 
 * @author coda
 */
public class PasswordHasher {
	private static final String HASH_ALGORITHM = "SHA-256";
	private final Map<String, String> cache = BoundedLinkedHashMap.create(1000);
	
	public String getPasswordHash(String password, String salt) {
		StringBuilder builder = new StringBuilder();
		builder.append(salt);
		builder.append(password);
		final String s = builder.toString();
		final String cachedValue = cache.get(s);
		if (cachedValue != null) {
			return cachedValue;
		}
		
		final String newHash = hash(s);
		cache.put(s, newHash);
		return newHash;
	}
	
	public String getAccountKey(String username, String password) {
		StringBuilder builder = new StringBuilder();
		builder.append(username);
		builder.append(password);
		final String s = builder.toString();
		
		final String cachedValue = cache.get(s);
		if (cachedValue != null) {
			return cachedValue;
		}
		
		builder = new StringBuilder();
		builder.append(hash(s));
		builder.append(password);
		
		final String newHash = hash(builder.toString());
		cache.put(s, newHash);
		return newHash;
	}

	private String hash(final String s) {
		try {
			MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
			return String.valueOf(Hex.encodeHex(sha.digest(s.getBytes())));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public int getCachedValuesCount() {
		return cache.size();
	}
}
