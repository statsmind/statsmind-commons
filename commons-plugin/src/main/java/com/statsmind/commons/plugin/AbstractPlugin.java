package com.statsmind.commons.plugin;

import java.util.EventObject;

public abstract class AbstractPlugin {
    private PluginContext context;

    public AbstractPlugin(PluginContext context) {
        this.context = context;
    }

    public PluginContext getContext() {
        return context;
    }

    public void onStarted() {

    }

    public void onStopped() {

    }

    public void onEvent(EventObject event) {

    }

    /**
     * 越小的优先级越早执行
     *
     * @return
     */
    public int getPriority() {
        return 99;
    }
}
