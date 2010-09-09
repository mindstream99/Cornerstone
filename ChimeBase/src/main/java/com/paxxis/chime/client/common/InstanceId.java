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

package com.paxxis.chime.client.common;

import java.io.Serializable;

/**
 * This is a simple wrapper around a string value that represents an instance id.
 * This is meant to provide compile time type checking for APIs where there are
 * methods that take an ID, and others that take a name, both being strings.  This
 * is a potential source of bugs, so it's best to let the compiler help us out.
 *
 * TBD use of this class is a work in progress.  It should be considered a medium
 * priority refactoring task.
 *
 * @author Robert Englander
 */
public class InstanceId implements Serializable {

    public static final InstanceId UNKNOWN = InstanceId.create("-1");
    
    private String id;

    private InstanceId() {
    }

    private InstanceId(String value) {
        this.id = value;
    }

    public static InstanceId create(String value) {
        return new InstanceId(value);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other.getClass() != getClass()) {
            return false;
        }

        InstanceId instId = (InstanceId)other;
        if (id == null) {
            if (instId.id != null) {
                return false;
            }
        } else {
            if (!id.equals(instId.id)) {
                return false;
            }
        }

        return true;
    }

    public String getValue() {
        return id;
    }
}
