package com.wesabe.bouncer.security.normalizers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * A URI normalizer. Ensures that all request URIs meet the following critera:
 * <p>
 * <ul>
 * 	<li>
 * 		Syntactically valid according to
 * 		<a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>.
 * 	</li>
 *  <li>
 *  	All URL-encoded characters are represented in the shortest possible way
 *  	(e.g., ASCII if possible, then the shortest UTF-8 sequence).
 *  </li>
 *  <li>
 *  	No double-encoded UTF-8 bytes.
 *  </li>
 *  <li>
 *  	No malformed UTF-8 bytes or URL-encoded pairs.
 *  </li>
 *  <li>
 *  	No host information (e.g., {@code http://example.com/path}).
 *  </li>
 * </ul>
 * <p>
 * If a URI cannot be normalized to meet these criteria (e.g., invalid UTF-8
 * bytes), a {@link MalformedValueException} is thrown.
 * <p>
 * The resulting URI can be considered structurally but not semantically safe.
 * It may reference an invalid or forbidden resource (e.g.,
 * {@code /../../../passwd}) or a contain a malicious payload (e.g.,
 * {@code /search?q=%3Cscript%3Ealert%28%27pwnd%21%27%29%3B%3C%2Fscript%3E}).
 * Semantic normalization is the responsibility of backend services.
 * 
 * 
 * @author coda
 */
public class UriNormalizer implements Normalizer<String, String> {
	
	/**
	 * A normalizer for fragments of a URI.
	 */
	private static class UriFragmentNormalizer {
		private static final int MAX_DECODE_DEPTH = 1;
		private static final String DEFAULT_CHARSET = "UTF-8";
		private static final char REPLACEMENT_CHARACTER = '\uFFFD';
		
		public String normalize(String fragment) throws URISyntaxException {
			if (fragment == null) {
				return null;
			}
			
			try {
				String lastDecoded = fragment;
				for (int i = MAX_DECODE_DEPTH; i >= 0; i--) {
					final String decoded = URLDecoder.decode(lastDecoded, DEFAULT_CHARSET);
					
					if (decoded.indexOf(REPLACEMENT_CHARACTER) != -1) {
						throw new URISyntaxException(fragment, "cannot contain invalid UTF-8 codepoints");
					}
					
					if (lastDecoded.equals(decoded)) {
						return URLEncoder.encode(lastDecoded, DEFAULT_CHARSET);
					}
					
					lastDecoded = decoded;
				}
				
				throw new URISyntaxException(fragment, "was encoded " + MAX_DECODE_DEPTH + " or more times");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new URISyntaxException(fragment, "had un-decodable characters");
			}
		}
	}
	
	/**
	 * A normalizer for the path component of a URI.
	 */
	private static class UriPathNormalizer extends UriFragmentNormalizer {
		private static final char PATH_SEPARATOR = '/';
		
		@Override
		public String normalize(String path) throws URISyntaxException {
			StringBuilder pathBuilder = new StringBuilder(path.length() * 2);
			StringBuilder buffer = new StringBuilder(path.length());
			
			for (int i = 0; i < path.length(); i++) {
				final char c = path.charAt(i);
				if (c == PATH_SEPARATOR) {
					pathBuilder.append(super.normalize(buffer.toString()));
					pathBuilder.append(c);
					buffer = new StringBuilder(path.length());
				} else {
					buffer.append(c);
				}
			}
			
			if (buffer.length() > 0) {
				pathBuilder.append(super.normalize(buffer.toString()));
			}
			
			return pathBuilder.toString();
		}
	}
	
	/**
	 * A normalizer for a query parameter of a URI.
	 */
	private static class UriQueryParamNormalizer extends UriFragmentNormalizer {
		private static final char PARAM_KEY_VALUE_SEPARATOR = '=';
		
		@Override
		public String normalize(String param) throws URISyntaxException {
			final int separatorIndex = param.indexOf(PARAM_KEY_VALUE_SEPARATOR);
			
			if (separatorIndex == -1) {
				return super.normalize(param);
			}
			
			final String key = param.substring(0, separatorIndex);
			final String value = param.substring(separatorIndex + 1);
			
			final StringBuilder builder = new StringBuilder();
			builder.append(super.normalize(key));
			builder.append('=');
			builder.append(super.normalize(value));
			return builder.toString();
		}
	}
	
	/**
	 * A normalizer for the query component of a URI.
	 */
	private static class UriQueryNormalizer extends UriQueryParamNormalizer {
		private static final char PARAM_SEPARATOR = '&';
		
		@Override
		public String normalize(String query) throws URISyntaxException {
			if (query == null) {
				return null;
			}
			
			StringBuilder queryBuilder = new StringBuilder(query.length() * 2);
			StringBuilder buffer = new StringBuilder(query.length());
			
			for (int i = 0; i < query.length(); i++) {
				final char c = query.charAt(i);
				if (c == PARAM_SEPARATOR) {
					queryBuilder.append(super.normalize(buffer.toString()));
					queryBuilder.append(c);
					buffer = new StringBuilder(query.length());
				} else {
					buffer.append(c);
				}
			}
			
			queryBuilder.append(super.normalize(buffer.toString()));
			
			return queryBuilder.toString();
		}
	}
	
	private static final char QUERY_SEPARATOR = '?';
	private static final char FRAGMENT_SEPARATOR = '#';
	private static final UriPathNormalizer PATH_NORMALIZER = new UriPathNormalizer();
	private static final UriQueryNormalizer QUERY_NORMALIZER = new UriQueryNormalizer();
	private static final UriFragmentNormalizer FRAGMENT_NORMALIZER = new UriFragmentNormalizer();
	
	@Override
	public String normalize(String input) throws MalformedValueException {
		try {
			final URI rawURI = new URI(input);
			
			if (rawURI.getHost() != null) {
				throw new URISyntaxException(input, "cannot have host information");
			}
			
			return format(
				PATH_NORMALIZER.normalize(rawURI.getRawPath()),
				QUERY_NORMALIZER.normalize(rawURI.getRawQuery()),
				FRAGMENT_NORMALIZER.normalize(rawURI.getRawFragment())
			);
		} catch (URISyntaxException e) {
			throw new MalformedValueException("malformed URI", input, e);
		}
	}
	
	private String format(String path, String query, String fragment) {
		final StringBuilder uri = new StringBuilder();

		uri.append(path);

		if (query != null) {
			uri.append(QUERY_SEPARATOR);
			uri.append(query);
		}

		if (fragment != null) {
			uri.append(FRAGMENT_SEPARATOR);
			uri.append(fragment);
		}

		return uri.toString();
	}
}
