package com.paxxis.cornerstone.cache;

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

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * 
 * @author Rob Englander
 *
 */
public class CacheManager {
    private static final Logger logger = Logger.getLogger(CacheManager.class);

    /** the cache config file location */
    private String cacheConfigLocation = null;

    private EmbeddedCacheManager cacheManager = null;

    public void initialize() {
        try {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(cacheConfigLocation);
            cacheManager = new DefaultCacheManager(stream);             
        } catch (Exception e) {
            String msg = e.getLocalizedMessage();
            logger.error(msg);
            throw new RuntimeException(msg);
        }
    }

    public String getCacheConfigLocation() {
        return cacheConfigLocation;
    }

    public void setCacheConfigLocation(String cacheConfigLocation) {
        this.cacheConfigLocation = cacheConfigLocation;
    }
    
    @SuppressWarnings("rawtypes")
    public Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }
}
