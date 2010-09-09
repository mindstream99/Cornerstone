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

import com.paxxis.chime.client.common.portal.PortalTemplate;

/**
 *
 * @author Robert Englander
 */
public class Dashboard extends DataInstance {

    public static final InstanceId USERDEFAULT = InstanceId.create("100000300");
    public static final InstanceId ABOUT = InstanceId.create("100000500");
    public static final InstanceId HELP = InstanceId.create("100000600");
    
    private PortalTemplate pageTemplate = new PortalTemplate();

    @Override
    public Dashboard copy() {
        Dashboard result = super.copy(new Dashboard());
        result.pageTemplate = pageTemplate.copy();
        return result;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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

        Dashboard inst = (Dashboard)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        if (pageTemplate == null) {
            if (inst.pageTemplate != null) {
                return false;
            }
        } else {
            if (!pageTemplate.equals(inst.pageTemplate)) {
                return false;
            }
        }

        return true;
    }

    public void setPageTemplate(PortalTemplate t) {
        pageTemplate = t;
    }

    public PortalTemplate getPageTemplate() {
        return pageTemplate;
    }
}
