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

package com.paxxis.chime.client.common.portal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Englander
 */
public class PortalColumn implements Serializable
{
    private double _width;
    private List<PortletSpecification> _portlets = new ArrayList<PortletSpecification>();

    public PortalColumn()
    {
    }

    public PortalColumn(double width)
    {
        _width = width;
    }

    public PortalColumn copy() {
        PortalColumn target = new PortalColumn();
        target.setWidth(_width);
        for (PortletSpecification spec : _portlets) {
            target.add(spec.copy());
        }
        return target;
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

        PortalColumn inst = (PortalColumn)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (_portlets == null) {
            if (inst._portlets != null) {
                return false;
            }
        } else {
            if (!_portlets.equals(inst._portlets)) {
                return false;
            }
        }

        if (_width != inst._width) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this._portlets != null ? this._portlets.hashCode() : 0);
        return hash;
    }

    public void setWidth(double w) {
        _width = w;
    }

    public double getWidth()
    {
        return _width;
    }

    public void add(PortletSpecification spec)
    {
        _portlets.add(spec);
    }

    public List<PortletSpecification> getPortletSpecifications()
    {
        return _portlets;
    }
}

