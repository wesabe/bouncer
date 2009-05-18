package com.wesabe.bouncer.auth.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.wesabe.bouncer.auth.PasswordHasher;

@RunWith(Enclosed.class)
public class PasswordHasherTest {
	public static class Hashing_A_Password {
		private PasswordHasher hasher;
		
		@Before
		public void setup() throws Exception {
			this.hasher = new PasswordHasher();
		}
		
		@Test
		public void itProducesAPFCCompatiblePasswordHash() throws Exception {
			assertThat(hasher.getPasswordHash("dingo", "woot"), is("ed5805ce459c5ce145c376a95184b5e0c7360a9c4bdac0a3fcd10c1aa9052082"));
		}
		
		@Test
		public void itCachesHashedValues() throws Exception {
			assertThat(hasher.getCachedValuesCount(), is(0));
			assertThat(hasher.getPasswordHash("dingo", "woot"), is("ed5805ce459c5ce145c376a95184b5e0c7360a9c4bdac0a3fcd10c1aa9052082"));
			assertThat(hasher.getCachedValuesCount(), is(1));
			assertThat(hasher.getPasswordHash("dingo", "woot"), is("ed5805ce459c5ce145c376a95184b5e0c7360a9c4bdac0a3fcd10c1aa9052082"));
			assertThat(hasher.getCachedValuesCount(), is(1));
		}
	}
	
	public static class Hashing_An_Account_Key {
		private PasswordHasher hasher;
		
		@Before
		public void setup() throws Exception {
			this.hasher = new PasswordHasher();
		}
		
		@Test
		public void itProducesAPFCCompatiblePasswordHash() throws Exception {
			assertThat(hasher.getAccountKey("dingo", "woot"), is("fca01136e6e147f6773e7d901335b237dc6e77b22f550ecf9a5293dc5d93a5ba"));
		}
		
		@Test
		public void itCachesHashedValues() throws Exception {
			assertThat(hasher.getCachedValuesCount(), is(0));
			assertThat(hasher.getAccountKey("dingo", "woot"), is("fca01136e6e147f6773e7d901335b237dc6e77b22f550ecf9a5293dc5d93a5ba"));
			assertThat(hasher.getCachedValuesCount(), is(1));
			assertThat(hasher.getAccountKey("dingo", "woot"), is("fca01136e6e147f6773e7d901335b237dc6e77b22f550ecf9a5293dc5d93a5ba"));
			assertThat(hasher.getCachedValuesCount(), is(1));
		}
	}
}
