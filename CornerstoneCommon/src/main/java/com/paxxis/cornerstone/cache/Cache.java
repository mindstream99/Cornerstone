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

import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Matthew Pflueger
 */
public interface Cache<K, V> {

    V peek(K key);
    
    V put(K key, V content);

    V put(K key, V content, long lifespan, TimeUnit lifespanUnits);

    V put(K key, V content, long lifespan, TimeUnit lifespanUnits, long maxIdle, TimeUnit maxIdleUnits);
    
    V putIfAbsent(K key, V content);

    V putIfAbsent(K key, V content, long lifespan, TimeUnit lifespanUnits);

    V putIfAbsent(K key, V content, long lifespan, TimeUnit lifespanUnits, long maxIdle, TimeUnit maxIdleUnits);

    V get(K key);

    void addListener(CacheListener listener);
    
}
