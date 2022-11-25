package com.statsmind.commons.plugin;

import org.springframework.context.ApplicationContext;

import java.util.HashMap;

public class PluginContext {
    private ApplicationContext applicationContext;
    private HashMap<String, Object> properties = new HashMap<>();

    public PluginContext(ApplicationContext context) {
        this.applicationContext = context;
    }

    public PluginContext(PluginContext another) {
        this.applicationContext = another.applicationContext;
        this.properties = (HashMap<String, Object>) another.properties.clone();
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public Object get(String key) {
        return properties.getOrDefault(key, null);
    }

    public Object getOrDefault(String key, Object defaultVal) {
        return properties.getOrDefault(key, defaultVal);
    }

    public void put(String key, Object val) {
        properties.put(key, val);
    }

    public PluginContext clone() {
        return new PluginContext(this);
    }

    public <T> T getBean(Class<T> clazz) {
        return this.applicationContext.getBean(clazz);
    }
}
