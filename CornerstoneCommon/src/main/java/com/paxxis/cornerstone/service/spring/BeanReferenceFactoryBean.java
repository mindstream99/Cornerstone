package com.paxxis.cornerstone.service.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * FactoryBean that exposes an arbitrary target bean under a different name.
 * 
 * <p>
 * <b>NOTE:</b> This is a direct rip from the 
 * <a href="http://static.springsource.org/spring/docs/current/api/org/springframework/beans/factory/config/BeanReferenceFactoryBean.html">
 * Springframework's BeanReferenceFactoryBean</a>
 * with one major difference - this BeanReferenceFactoryBean is NOT BeanFactoryAware
 * meaning you can set ANY BeanFactory on it and it will alias the bean in that BeanFactory
 * effectively creating a bridge between factories.
 * </p>
 *
 * <p>Usually, the target bean will reside in a different bean definition file,
 * using this FactoryBean to link it in and expose it under a different name.
 * Effectively, this corresponds to an alias for the target bean.
 *
 * <p><b>NOTE:</b> For XML bean definition files, the <code>&lt;alias&gt;</code>
 * tag does NOT achieve the same thing - the <code>&lt;alias&gt;</code> only looks
 * in the current BeanFactory.
 *
 * <p>A special capability of this FactoryBean is enabled through its configuration
 * as bean definition: The "targetBeanName" can be substituted through a placeholder,
 * in combination with Spring's {@link PropertyPlaceholderConfigurer}.
 * Thanks to Marcus Bristav for pointing this out!
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setTargetBeanName
 * @see PropertyPlaceholderConfigurer
 */
@SuppressWarnings("rawtypes")
public class BeanReferenceFactoryBean implements SmartFactoryBean {

    private String targetBeanName;

    private BeanFactory beanFactory;

    /**
     * Set the name of the target bean.
     * <p>This property is required. The value for this property can be
     * substituted through a placeholder, in combination with Spring's
     * PropertyPlaceholderConfigurer.
     * @param targetBeanName the name of the target bean
     * @see PropertyPlaceholderConfigurer
     */
    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void init() {
        if (this.targetBeanName == null) {
            throw new IllegalArgumentException("'targetBeanName' is required");
        }
        if (!this.beanFactory.containsBean(this.targetBeanName)) {
            throw new NoSuchBeanDefinitionException(this.targetBeanName, this.beanFactory.toString());
        }
    }

    public Object getObject() throws BeansException {
        if (this.beanFactory == null) {
            throw new FactoryBeanNotInitializedException();
        }
        return this.beanFactory.getBean(this.targetBeanName);
    }

    public Class getObjectType() {
        if (this.beanFactory == null) {
            return null;
        }
        return this.beanFactory.getType(this.targetBeanName);
    }

    public boolean isSingleton() {
        if (this.beanFactory == null) {
            throw new FactoryBeanNotInitializedException();
        }
        return this.beanFactory.isSingleton(this.targetBeanName);
    }

    public boolean isPrototype() {
        if (this.beanFactory == null) {
            throw new FactoryBeanNotInitializedException();
        }
        return this.beanFactory.isPrototype(this.targetBeanName);
    }

    public boolean isEagerInit() {
        return false;
    }
}