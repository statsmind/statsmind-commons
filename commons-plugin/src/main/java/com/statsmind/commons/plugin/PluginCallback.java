package com.statsmind.commons.plugin;

public interface PluginCallback {

    void onPluginRemoved(AbstractPlugin plugin);

    void onPluginStarted(AbstractPlugin plugin);
}
