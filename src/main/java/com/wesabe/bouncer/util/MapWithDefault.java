package com.wesabe.bouncer.util;

import java.util.Map;

import com.google.common.collect.ForwardingMap;

/**
 * A map decorator which returns a default value on {@link #get(Object)} if no
 * value exists in the map.
 * 
 * @author coda
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MapWithDefault<K, V> extends ForwardingMap<K, V> {
	private final Map<K, V> map;
	private final V defaultValue;
	
	/**
	 * Wraps a map so that its {@link #get(Object)} method will return
	 * {@code defaultValue} if no value exists for that key in {@code map}.
	 * 
	 * @param <K> key type
	 * @param <V> value type
	 * @param map the underlying map
	 * @param defaultValue the default value
	 * @return a map with a default value
	 */
	public static <K, V> Map<K, V> of(Map<K, V> map, V defaultValue) {
		return new MapWithDefault<K, V>(map, defaultValue);
	}
	
	private MapWithDefault(Map<K, V> map, V defaultValue) {
		this.map = map;
		this.defaultValue = defaultValue;
	}
	
	@Override
	protected Map<K, V> delegate() {
		return map;
	}
	
	@Override
	public V get(Object key) {
		final V value = super.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
}
