/*
 * Copyright 2010 the original author or authors.
 * Copyright 2009 Paxxis Technology LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paxxis.cornerstone.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.container.entries.InternalCacheEntry;

/**
 * @author Rob Englander
 */
public class NamedCache<K, V> {
    private static final Logger logger = Logger.getLogger(NamedCache.class);
        
    /** the name of the cache */
    private String cacheName = null;

    /** the associated cache manager */
    private CacheManager cacheManager = null;

    /** the cache itself */
    private Cache<K, V> cache = null;

	private List<CacheListener> listeners = new ArrayList<CacheListener>();

    public void setCacheName(String name) {
        cacheName = name;
    }
    
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
	public void setListeners(List<CacheListener> listeners) {
		this.listeners.clear();
		this.listeners.addAll(listeners);
	}

	@SuppressWarnings("unchecked")
    public void initialize() {
        try {
            cache = cacheManager.getCache(cacheName);
			for (CacheListener listener : listeners) {
				cache.addListener(listener);
			}
        } catch (Exception e) {
            String msg = e.getLocalizedMessage();
            logger.error(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Retrieves a cache entry in the same way as get() except that it 
     * does not update or reorder any of the internal constructs.
     */
    @SuppressWarnings("unchecked")
	public V peek(K key) {
        V result = null;
        InternalCacheEntry entry = cache.getAdvancedCache().getDataContainer().peek(key);
        if (entry != null) {
        	result = (V)entry.getValue();
        }
        
        return result;
    }
    
    public V put(K key, V content) {
    	return cache.put(key, content);
    }

    public V put(K key, V content, long lifespan, TimeUnit lifespanUnits) {
    	return cache.put(key, content, lifespan, lifespanUnits);
    }

    public V put(K key, V content, long lifespan, TimeUnit lifespanUnits, long maxIdle, TimeUnit maxIdleUnits) {
    	return cache.put(key, content, lifespan, lifespanUnits, maxIdle, maxIdleUnits);
    }
    
    public V putIfAbsent(K key, V content) {
    	return cache.putIfAbsent(key, content);
    }

    public V putIfAbsent(K key, V content, long lifespan, TimeUnit lifespanUnits) {
    	return cache.putIfAbsent(key, content, lifespan, lifespanUnits);
    }

    public V putIfAbsent(K key, V content, long lifespan, TimeUnit lifespanUnits, long maxIdle, TimeUnit maxIdleUnits) {
    	return cache.putIfAbsent(key, content, lifespan, lifespanUnits, maxIdle, maxIdleUnits);
    }

    public V get(K key) {
        if (key == null) {
            return null;
        }
        
        return cache.get(key);
    }
    
    public V remove(K key) {
        return cache.remove(key);
    }
    
}
