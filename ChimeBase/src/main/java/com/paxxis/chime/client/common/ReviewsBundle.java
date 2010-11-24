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
import java.util.ArrayList;
import java.util.List;

import com.paxxis.cornerstone.base.Cursor;

/**
 *
 * @author Robert Englander
 */
public class ReviewsBundle implements Serializable
{
    private List<Review> _reviews = null;
    private Cursor _cursor = null;

    public ReviewsBundle()
    {

    }

    public ReviewsBundle(List<Review> reviews, Cursor cursor)
    {
        _reviews = new ArrayList<Review>();
        _reviews.addAll(reviews);
        _cursor = cursor;
    }

    public List<Review> getReviews()
    {
        return _reviews;
    }

    public Cursor getCursor()
    {
        return _cursor;
    }
}
