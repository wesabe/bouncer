package com.wesabe.bouncer.util.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.wesabe.bouncer.util.BoundedLinkedHashMap;

@RunWith(Enclosed.class)
public class BoundedLinkedHashMapTest {
	public static class An_Empty_Map_With_10_Slots {
		private final BoundedLinkedHashMap<String, String> map = BoundedLinkedHashMap.create(10);
		
		@Test
		public void itHasAMaxCapacityOf10() throws Exception {
			assertThat(map.getMaxCapacity(), is(10));
		}
		
		@Test
		public void itIsEmpty() throws Exception {
			assertThat(map.isEmpty(), is(true));
		}
		
		@Test
		public void itHasAnEmptyEntrySet() throws Exception {
			assertThat(map.entrySet().isEmpty(), is(true));
		}
		
		@Test
		public void itHasAnEmptyValueCollection() throws Exception {
			assertThat(map.values().isEmpty(), is(true));
		}
		
		@Test
		public void itDoesNotContainASpecificKey() throws Exception {
			assertThat(map.containsKey("woo"), is(false));
		}
		
		@Test
		public void itDoesNotContainASpecificValue() throws Exception {
			assertThat(map.containsValue("woo"), is(false));
		}
		
		@Test
		public void itReturnsNullForAGivenKey() throws Exception {
			assertThat(map.get("woo"), is(nullValue()));
		}
		
		@Test
		public void itHasZeroItems() throws Exception {
			assertThat(map.size(), is(0));
		}
	}
	
	public static class A_Map_With_10_Slots_And_Five_Items {
		private final BoundedLinkedHashMap<String, String> map = BoundedLinkedHashMap.create(10);
		
		@Before
		public void setup() throws Exception {
			map.put("one",   "uno");
			map.put("two",   "dos");
			map.put("three", "tres");
			map.put("four",  "quatro");
			map.put("five",  "cinco");
		}
		
		@Test
		public void itIsNotEmpty() throws Exception {
			assertThat(map.isEmpty(), is(false));
		}
		
		@Test
		public void itHasFiveItems() throws Exception {
			assertThat(map.size(), is(5));
		}
		
		@Test
		public void itReturnsAValueForAnExistingKey() throws Exception {
			assertThat(map.get("four"), is("quatro"));
		}
		
		@Test
		public void itReturnsNullForAnNonExistentKey() throws Exception {
			assertThat(map.get("siz"), is(nullValue()));
		}
		
		@Test
		public void itHasFourItemsWhenOneIsRemoved() throws Exception {
			map.remove("three");
			
			assertThat(map.size(), is(4));
		}
		
		@Test
		public void itDoesntHaveRemovedItems() throws Exception {
			map.remove("three");
			
			assertThat(map.get("three"), is(nullValue()));
		}
		
		@Test
		public void itIsEmptyAfterBeingCleared() throws Exception {
			map.clear();
			
			assertThat(map.isEmpty(), is(true));
		}
		
		@Test
		public void itRemovesTheTwoOldestEntriesWhenSevenMoreAreAdded() throws Exception {
			Builder<String, String> builder = ImmutableMap.builder();
			map.putAll(
				builder
				.put("six", "seis")
				.put("seven", "siete")
				.put("eight", "ocho")
				.put("nine", "nueve")
				.put("ten", "diez")
				.put("eleven", "once")
				.put("twelve", "doce")
				.build()
			);
			
			assertThat(map.size(), is(10));
			assertThat(map.containsKey("one"), is(false));
			assertThat(map.containsKey("two"), is(false));
			assertThat(map.containsKey("three"), is(true));
		}
	}
}
