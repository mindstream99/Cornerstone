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
 *
 * @author Robert Englander
 */
public class Cursor implements Serializable
{
    private int _first;
    private int _count;
    private int _max;
    private int _total;
    private boolean _limited = false;
    
    public Cursor()
    {
        
    }
    
    public Cursor(int max)
    {
        _max = max;
        _first = 0;
        _count = -1;
        _total = -1;
    }
    
    public Cursor(int first, int count, int max, int total, boolean limited)
    {
        _max = max;
        _first = first;
        _count = count;
        _total = total;
        _limited = limited;
    }
    
    public boolean isLimited()
    {
        return _limited;
    }
    
    public boolean prepareFirst()
    {
        _first = 0;
        return true;
    }
    
    public boolean prepareLast()
    {
        _first = (_total / _max) * _max;
        if (_first == _total && _first > 0)
        {
            _first -= _max;
        }
        
        return true;
    }

    public boolean preparePageByIndex(int idx) {
        prepareFirst();
        int last = getFirst() + getCount() - 1;
        while (idx > last) {
            if (!prepareNext()) {
                return false;
            }

            last = getFirst() + getCount() - 1;
        }

        return true;
    }

    public boolean prepareNext()
    {
        boolean pending = false;
        int last = getFirst() + getCount() - 1;
        if (last < (getTotal() - 1))
        {
            _first = last + 1;
            pending = true;
        }
        
        return pending;
    }
    
    public boolean preparePrevious()
    {
        boolean pending = false;
        if (getFirst() > 0)
        {
            _first -= getMax();
            pending = true;
        }
        
        return pending;
    }

    public int getFirst() 
    {
        return _first;
    }

    public int getCount() 
    {
        return _count;
    }

    public int getMax() 
    {
        return _max;
    }

    public int getTotal() 
    {
        return _total;
    }
}
