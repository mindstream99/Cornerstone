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


package com.paxxis.chime.service;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.TTCCLayout;

/**
 *
 * @author Robert Englander
 */
public class LogManager extends ChimeConfigurable
{
    private static final Logger _logger = Logger.getLogger(LogManager.class.getName());

    private String _logLevel = "";
    
    public LogManager()
    {
    }
    
    @Override
    public void initialize()
    {
        getCurrentLevel();
    }

    private void getCurrentLevel()
    {
        _logLevel = _logger.getRootLogger().getLevel().toString();
    }
    
    public LogManager(String level)
    {
        ConsoleAppender appender = new ConsoleAppender(new TTCCLayout());
        appender.activateOptions();
        _logger.getRootLogger().addAppender(appender);
        
        setLogLevel(level);
    }
    
    public void setLogLevel(String level)
    {
        String newLevel = level.toUpperCase();
        if (!newLevel.equals(_logLevel))
        {
            _logger.getRootLogger().setLevel(Level.toLevel(newLevel));
            
            getCurrentLevel();
        }
    }
    
    public String getLogLevel()
    {
        return _logLevel;
    }
    
}
