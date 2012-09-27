package com.paxxis.cornerstone.common;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import com.paxxis.cornerstone.common.CornerstoneConfigurable;

/**
 * 
 * This class should be extended to allow log4j to be entirely managed through configuration instead of
 * a log4j.properties file.
 *
 */
public class LogManager extends CornerstoneConfigurable implements ILogManager {
    private static final Logger LOGGER = Logger.getLogger(LogManager.class);
    
    private static final String APPENDERNAME = "validation";
    
    private String conversionPattern = "%d{yyyy-MM-dd HH:mm:ss} %c{1} [%p] %m%n";
    private String file = "./log.log";
    private boolean append = true;
    private int maxFileSize = 250;
    private int maxBackupIndex = 3;
    private Type appenderType = Type.CONSOLE;
    private Level logLevel = Level.INFO;
    private boolean initialized = false;
    
    private enum Type {
        CONSOLE,
        ROLLING,
        DAILYROLLING
    }
    
    public LogManager() {
        
    }

    public String getConversionPattern() {
        return conversionPattern;
    }

    public void setConversionPattern(String conversionPattern) {
        this.conversionPattern = conversionPattern;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getMaxBackupIndex() {
        return maxBackupIndex;
    }

    public void setMaxBackupIndex(int maxBackupIndex) {
        this.maxBackupIndex = maxBackupIndex;
    }

    public void initialize() {
        super.initialize();
        initialized = true;
        setLogLevel(logLevel.toString());
        setAppender(appenderType.name());
    }
    
    public void setLogLevel(String level) {
        level = level.toUpperCase();
        logLevel = Level.toLevel(level, Logger.getRootLogger().getLevel());
        if (initialized) {
            Logger.getRootLogger().setLevel(logLevel);
        }
    }

    public String getLogLevel() {
        return Logger.getRootLogger().getLevel().toString();
    }
    
    public String getAppender() {
        Appender appender = Logger.getRootLogger().getAppender(APPENDERNAME);
        if (appender instanceof ConsoleAppender) {
            return "CONSOLE";
        } else if (appender instanceof RollingFileAppender) {
            return "ROLLING";
        } else if (appender instanceof DailyRollingFileAppender) {
            return "DAILYROLLING";
        }
        
        return "";
    }
    
    public void setAppender(String value) {
        value = value.toUpperCase();
        try {
            appenderType = Type.valueOf(value);
            if (initialized) {
                AppenderSkeleton appender = null;
                switch (appenderType) {
                    case CONSOLE:
                        appender = getConsoleAppender();
                        break;
                    case ROLLING:
                        appender = getRollingAppender();
                        break;
                    case DAILYROLLING:
                        appender = getDailyRollingAppender();
                        break;
                }

                if (appender != null) {
                    appender.setName(APPENDERNAME);
                    appender.activateOptions();
                    Logger.getRootLogger().removeAllAppenders();
                    Logger.getRootLogger().addAppender(appender);
                }
            }
        } catch (Throwable t) {
            LOGGER.warn("Invalid log appender name: " + value + ". Leaving appender as " + appenderType.name() + ".");
        }
    }

    private AppenderSkeleton getConsoleAppender() {
        Layout layout = new PatternLayout(conversionPattern);
        ConsoleAppender appender = new ConsoleAppender(layout);
        return appender;
    }
    
    private AppenderSkeleton getDailyRollingAppender() {
        Layout layout = new PatternLayout(conversionPattern);
        DailyRollingFileAppender appender = new DailyRollingFileAppender();
        appender.setFile(file);
        appender.setAppend(append);
        appender.setLayout(layout);
        appender.setDatePattern("'.'yyy-MM-dd");
        return appender;
    }
    
    private AppenderSkeleton getRollingAppender() {
        Layout layout = new PatternLayout(conversionPattern);
        RollingFileAppender appender = new RollingFileAppender();
        appender.setFile(file);
        appender.setAppend(append);
        appender.setLayout(layout);
        appender.setMaxBackupIndex(maxBackupIndex);
        appender.setMaxFileSize(maxFileSize + "MB");
        return appender;
    }
}


