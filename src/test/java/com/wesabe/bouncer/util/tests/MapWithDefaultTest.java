package com.wesabe.bouncer.util.tests;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableMap;
import com.wesabe.bouncer.util.MapWithDefault;

@RunWith(Enclosed.class)
public class MapWithDefaultTest {
	public static class A_Map_With_A_Default_Value {
		private final Map<String, String> names = ImmutableMap.of("Coda", "Hale");
		private final Map<String, String> namesWithDefault = MapWithDefault.of(names, "McButt");
		
		@Test
		public void itHasADefaultValue() throws Exception {
			assertEquals("McButt", namesWithDefault.get("Blah"));
		}
		
		@Test
		public void itHasSetValues() throws Exception {
			assertEquals("Hale", namesWithDefault.get("Coda"));
		}
	}
}
