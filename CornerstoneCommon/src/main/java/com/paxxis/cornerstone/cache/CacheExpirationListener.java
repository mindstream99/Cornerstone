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

import java.util.Date;
import java.util.List;

/**
 * Listener interface for expiration events.
 * 
 * @author Rob Englander
 */
public interface CacheExpirationListener<V> {
	
	/**
	 * Called when entries are expired from the cache.
	 * 
	 * @param expiredEntries list of expired entries
	 */
	public void onExpiration(List<V> expiredEntries);
	
	/**
	 * called before an entry is expired.
	 * 
	 * @param entry the entry to be expired
	 * @param expirationTime the time that the entry reached expiration
	 * 
	 * @return true if the entry should be expired, false otherwise
	 */
	public boolean allowExpiration(V entry, Date expirationTime);
}
