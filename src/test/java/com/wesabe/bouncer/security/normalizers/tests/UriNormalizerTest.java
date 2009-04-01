package com.wesabe.bouncer.security.normalizers.tests;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.security.normalizers.MalformedValueException;
import com.wesabe.bouncer.security.normalizers.UriNormalizer;

@RunWith(Enclosed.class)
public class UriNormalizerTest {
	
	private static String normalize(String uri) {
		final UriNormalizer normalizer = new UriNormalizer();
		try {
			return normalizer.normalize(uri);
		} catch (MalformedValueException e) {
			StringWriter writer = new StringWriter();
			PrintWriter w = new PrintWriter(writer);
			e.printStackTrace(w);
			throw new AssertionError(uri + " should have been considered valid, but wasn't:\n" + writer.toString());
		}
	}
	
	private static void assertThrowsException(String uri) {
		final UriNormalizer normalizer = new UriNormalizer();
		try {
			normalizer.normalize(uri);
			fail(uri + " should have been considered malformed, but wasn't");
		} catch (MalformedValueException e) {
			// yay it worked
		}
	}
	
	public static class Normalizing_URIs_With_Invalid_Characters {
		@Test
		public void itThrowsAnException() throws Exception {
			assertThrowsException("/^^^^^^¨î!");
		}
	}

	public static class Normalizing_URIs_With_Malformed_Hex_Characters {
		@Test
		public void itThrowsAnException() throws Exception {
			assertThrowsException("/dingo/%FV");
		}
	}
	
	public static class Normalizing_URIs_With_Malformed_Double_Encoded_Hex_Characters {
		@Test
		public void itThrowsAnException() throws Exception {
			assertThrowsException("/dingo/%25FV");
		}
	}

	public static class Normalizing_URIs_With_Hosts {
		@Test
		public void itThrowsAnException() throws Exception {
			assertThrowsException("http://blah.com/whee");
			assertThrowsException("//blah.com/whee");
		}
	}

	public static class Normalizing_URIs_With_Encoded_Paths {
		@Test
		public void itPassesThemThrough() throws Exception {
			assertEquals("/tags/either%2For", normalize("/tags/either%2for"));
		}
	}
	
	public static class Normalizing_URIs {
		@Test
		public void itPassesThemThrough() throws Exception {
			assertEquals("/tags/food", normalize("/tags/food"));
		}
	}
	
	public static class Normalizing_URIs_With_Trailing_Slashes {
		@Test
		public void itPassesThemThrough() throws Exception {
			assertEquals("/tags/food/", normalize("/tags/food/"));
		}
	}
	
	public static class Normalizing_URIs_With_Double_Encoded_Paths {
		@Test
		public void itThrowsAnException() throws Exception {
			assertThrowsException("/tags/either%252for");
		}
	}
	
	public static class Normalizing_URIs_With_Triple_Encoded_Paths {
		@Test
		public void itThrowsAnException() throws Exception {
			assertThrowsException("/tags/either%25252for");
		}
	}
	
	public static class Normalizing_URIs_With_Query_Strings {
		@Test
		public void itPassesThemThrough() throws Exception {
			assertEquals("/tags/?q=food", normalize("/tags/?q=food"));
			assertEquals("/tags/?q=food&g=two", normalize("/tags/?q=food&g=two"));
		}
	}
	
	public static class Normalizing_URIs_With_Encoded_Query_Strings {
		@Test
		public void itPassesThemThrough() throws Exception {
			assertEquals("/tags/?q=either%2For", normalize("/tags/?q=either%2for"));
		}
	}
	
	public static class Normalizing_URIs_With_Double_Encoded_Query_Strings {
		@Test
		public void itThrowsAnException() throws Exception {
			assertThrowsException("/tags/?q=either%252for");
			assertThrowsException("/tags/?q%252fa=either");
		}
	}
	
	public static class Normalizing_URIs_With_Paramless_Query_Strings {
		@Test
		public void itPassesThemThrough() throws Exception {
			assertEquals("/tags/?food", normalize("/tags/?food"));
		}
	}
	
	public static class Normalizing_URIs_With_Encoded_Paramless_Query_Strings {
		@Test
		public void itPassesThemThrough() throws Exception {
			assertEquals("/tags/?either%2For", normalize("/tags/?either%2for"));
		}
	}
	
	public static class Normalizing_URIs_With_Fragments {
		@Test
		public void itPassesThemThrough() throws Exception {
			assertEquals("/tags/#food", normalize("/tags/#food"));
		}
	}
	
	public static class Normalizing_URIs_With_Encoded_Fragments {
		@Test
		public void itPassesThemThrough() throws Exception {
			assertEquals("/tags/#either%2For", normalize("/tags/#either%2for"));
		}
	}
	
	public static class Normalizing_URIs_With_Double_Fragments {
		@Test
		public void itThrowsAnException() throws Exception {
			assertThrowsException("/tags/#either%252for");
		}
	}
	
	public static class Normalizing_URIs_With_Long_UTF8_Characters {
		@Test
		public void itNormalizesThemToStandardEncodings() throws Exception {
			assertEquals("/..%2F..%2F..%2F..%2Fetc/shadow", normalize("/%C0%AE%C0%AE%C0%AF%C0%AE%C0%AE%C0%AF%C0%AE%C0%AE%C0%AF%C0%AE%C0%AE%C0%AFetc/shadow"));
		}
	}
	
	public static class Normalizing_URIs_With_Malformed_UTF8 {
		@Test
		public void itThrowsAnException() throws Exception {
			assertThrowsException("/tags/%dfo%ee1");
		}
	}
}
