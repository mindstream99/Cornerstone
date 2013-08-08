package com.paxxis.cornerstone.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

import com.paxxis.cornerstone.service.spring.CornerstoneService;

public class AbstractService extends CornerstoneService implements ApplicationContextAware, NotificationPublisherAware {
	private static final Logger LOGGER = Logger.getLogger(AbstractService.class);

	private StrategyContext strategyContext = null;
	private NotificationPublisher jmxPublisher = null;
	private int jmxEventNum = 0;
    private ApplicationContext context = null;
    
	public void initialize() {
	    super.initialize();
        if (strategyContext == null) {
            throw new RuntimeException("strategyContext property can't be null.");
        }
    }
	
    public void setStrategyContext(StrategyContext context) {
		this.strategyContext = context;
	}

	public StrategyContext getStrategyContext() {
	    return strategyContext;
	}
	
	@Override
	public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
		jmxPublisher = notificationPublisher;
	}

    public Map<String, Object> getVersionInfo() {
        Package p = getClass().getPackage();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        String title = p.getImplementationTitle();
        map.put("title", title);

        String versionText = p.getImplementationVersion();
        if (versionText != null) {
            String[] parts = versionText.split("_");
            map.put("version", parts[0]);
            map.put("scm id", parts[1]);
            map.put("build date", parts[2]);
        } else {
            map.put("version", "???");
        }
        return map;
    }

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.context = ctx;
	}

    public Map<String, Object> getConfiguration(String objectName) {
        Map<String, Object> conf = new TreeMap<String, Object>();
        if (objectName == null) {
            conf.putAll(getCornerstoneConfiguration().getPropertyMap());
        } else {
            try {
                Object object = context.getBean(objectName);
                if (object instanceof CornerstoneConfigurable) {
                    CornerstoneConfigurable c = (CornerstoneConfigurable)object;
                    conf.putAll(c.getPrefixedConfigurationValues());
                } else {
                    conf.put("error", objectName + " is not configrable");
                }
            } catch (NoSuchBeanDefinitionException e) {
                conf.put("error", "No such object named " + objectName);
            }
        }
        
        return conf;
    }

}
