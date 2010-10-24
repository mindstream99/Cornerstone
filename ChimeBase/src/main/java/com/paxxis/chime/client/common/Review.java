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

/**
 *
 * @author Robert Englander
 */
public class Review extends DataInstance {
	private static final long serialVersionUID = 1L;
	private int _rating = 0;

    public void setRating(int rating) {
        _rating = rating;
    }

    public int getRating() {
        return _rating;
    }

    @Override
    public Review copy() {
        Review result = super.copy(new Review());
        result._rating = _rating;
        return result;
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

        Review inst = (Review)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        if (_rating != inst._rating) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
