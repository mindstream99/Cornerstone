package com.paxxis.cornerstone.common;


public interface ILogManager {
    public void setLogLevel(String level);
    public String getLogLevel();
    public void setAppender(String appender);
    public String getAppender();
}
