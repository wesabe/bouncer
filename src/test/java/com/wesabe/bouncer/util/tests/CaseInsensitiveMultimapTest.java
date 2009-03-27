package com.wesabe.bouncer.util.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.wesabe.bouncer.util.CaseInsensitiveMultimap;
import com.wesabe.bouncer.util.CaseInsensitiveMultimap.Builder;

@RunWith(Enclosed.class)
public class CaseInsensitiveMultimapTest {
	public static class A_Case_Insensitive_Multimap {
		private CaseInsensitiveMultimap map;
		
		@Before
		public void setup() {
			Builder builder = CaseInsensitiveMultimap.builder();
			builder.put("woo", "yeah");
			builder.put("Woo", "yeah?");
			builder.put("WOO", "yeah!");
			this.map = builder.build();
		}
		
		@Test
		public void itIsCaseInsensitive() throws Exception {
			assertEquals(ImmutableList.of("yeah", "yeah?", "yeah!"), map.get("woo"));
			assertEquals(ImmutableList.of("yeah", "yeah?", "yeah!"), map.get("Woo"));
			assertEquals(ImmutableList.of("yeah", "yeah?", "yeah!"), map.get("woO"));
		}
	}
}
