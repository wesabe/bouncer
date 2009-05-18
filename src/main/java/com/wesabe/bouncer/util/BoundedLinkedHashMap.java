package com.wesabe.bouncer.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe, bounded map backed by a {@link LinkedHashMap}. Thread safety
 * is guaranteed via the use of read/write locks.
 * 
 * @author coda
 *
 * @param <K> the type of keys the map has
 * @param <V> the type of values the map has
 * @see <a href="http://www.javaconcurrencyinpractice.com/listings.html">Section 13.7 of <em>Java Concurrency in Practice</em></a>
 */
public class BoundedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 9150041586350550714L;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
	private final int maxCapacity;
	
	/**
	 * Creates a new {@link BoundedLinkedHashMap} with a given capacity.
	 * 
	 * @param maxCapacity the maximum number of items the map can have
	 * @return a new {@link BoundedLinkedHashMap}
	 */
	public static <K, V> BoundedLinkedHashMap<K, V> create(int maxCapacity) {
		return new BoundedLinkedHashMap<K, V>(maxCapacity);
	}
	
	private BoundedLinkedHashMap(int capacity) {
		super(capacity);
		this.maxCapacity = capacity;
	}
	
    @Override
	public V put(K key, V value) {
        writeLock.lock();
        try {
            return super.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
	public V remove(Object key) {
        writeLock.lock();
        try {
            return super.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
	public void putAll(Map<? extends K, ? extends V> m) {
        writeLock.lock();
        try {
        	super.putAll(m);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
	public void clear() {
        writeLock.lock();
        try {
        	super.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
	public V get(Object key) {
        readLock.lock();
        try {
            return super.get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
	public int size() {
        readLock.lock();
        try {
            return super.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
	public boolean isEmpty() {
        readLock.lock();
        try {
            return super.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
	public boolean containsKey(Object key) {
        readLock.lock();
        try {
            return super.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
	public boolean containsValue(Object value) {
        readLock.lock();
        try {
            return super.containsValue(value);
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Returns the map's maximum capacity.
     */
    public int getMaxCapacity() {
		return maxCapacity;
	}
	
	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return size() > maxCapacity;
	}
}
