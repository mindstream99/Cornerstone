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
public class Scope implements Serializable
{
    public enum Permission
    {
        R,   // read
        RU,  // read and update
        RM,  // read and moderators can update
        RC,  // read and create instances
        RUC, // read, update, and create instances
    }
    
    // the Community data instance
    private Community _community = null;
    
    // the permission
    private Permission _permission = Permission.R;
    
    public Scope()
    {
    }
    
    public Scope(Community community, Permission permission)
    {
        _community = community;
        _permission = permission;
    }
    
    public boolean isGlobalCommunity()
    {
        boolean isGlobal = false;
        if (_community != null)
        {
            isGlobal = _community.getId().equals(Community.Global.getId());
        }
        
        return isGlobal;
    }
    
    public Community getCommunity()
    {
        return _community;
    }
    
    public Permission getPermission()
    {
        return _permission;
    }
}
