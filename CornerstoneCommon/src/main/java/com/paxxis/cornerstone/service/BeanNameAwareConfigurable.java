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
package com.paxxis.cornerstone.service;

import org.springframework.beans.factory.BeanNameAware;

/**
 * A configurable that automatically adds its bean name to the list of config prefixes for easy
 * targeting of its configuration in the database...
 *  
 * @author Matthew Pflueger
 */
public class BeanNameAwareConfigurable extends CornerstoneConfigurable implements BeanNameAware {

    private String beanName;
    
    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
        addConfigPropertyPrefix(beanName);
    }

    protected String getBeanName() {
        return beanName;
    }
}
