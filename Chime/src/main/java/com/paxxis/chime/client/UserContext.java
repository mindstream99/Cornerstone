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

package com.paxxis.chime.client;

import java.util.ArrayList;
import java.util.List;

import com.paxxis.chime.client.common.Community;

/**
 *
 * @author Robert Englander
 */
public class UserContext {
    public interface UserContextListener {
        public void onPortalPageChange(Community community);
        public void onSearchFavoritesChange();
    }

    private static List<UserContextListener> listeners = new ArrayList<UserContextListener>();

    private UserContext() {
    }

    public static void addListener(UserContextListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(UserContextListener listener) {
        listeners.remove(listener);
    }

    public static void notifySearchFavoritesChange() {
        for (UserContextListener listener : listeners) {
            listener.onSearchFavoritesChange();
        }
    }
    
    public static void notifyPortalPageChange(Community community) {
        for (UserContextListener listener : listeners) {
            listener.onPortalPageChange(community);
        }
    }
}
