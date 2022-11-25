package com.statsmind.commons.plugin;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

@Configurable
public class PluginManager {
    private final Map<String, AbstractPlugin> plugins = new HashMap<>();
    private final Map<Path, Date> pluginQueue = new HashMap<>();

    private final PluginContext pluginContext;
    private final Path pluginPath;
    private final String rootPackage;
    private final ExecutorService executors;
    private final PluginCallback pluginCallback;
    private final AutowireCapableBeanFactory beanFactory;

    public PluginManager(PluginContext pluginContext,
                         Path pluginPath,
                         String rootPackage,
                         int threads,
                         PluginCallback pluginCallback) {
        this.pluginContext = pluginContext;
        this.beanFactory = pluginContext.getApplicationContext().getAutowireCapableBeanFactory();
        this.pluginPath = pluginPath;
        this.rootPackage = rootPackage;
        this.pluginCallback = pluginCallback;
        this.executors = Executors.newFixedThreadPool(threads);

        if (!this.pluginPath.toFile().exists()) {
            this.pluginPath.toFile().mkdirs();
        }

        this.start();
    }

    public void onEventObject(EventObject event) {
        /**
         * Event 的执行应该是同步的，发起者要等待返回结果
         */
        plugins.values().stream()
            .sorted(Comparator.comparingInt(AbstractPlugin::getPriority))
            .forEach(plugin -> plugin.onEvent(event));
    }

    public synchronized void startPlugin(Class<?> pluginClass, Path pluginJar) {
        try {
            /**
             * PluginContext 应该在某一个Plugin的多个实例间共享，前一个instance删除后，应该带给新建的instance
             * 如果是 Plugin 新的实例，需要 pluginContext.clone()， 而不是直接使用系统缺省 pluginContext
             */
            PluginInterface pluginInterface = pluginClass.getAnnotation(PluginInterface.class);
            if (pluginInterface == null || StringUtils.isBlank(pluginInterface.value())) {
                throw new RuntimeException("plugin does not have PluginInterface annotation");
            }

            if (!pluginInterface.enabled()) {
                return;
            }

            final PluginContext oldContext = stopPlugin(pluginClass);
            final PluginContext newContext = oldContext != null ? oldContext : pluginContext.clone();

            final AbstractPlugin plugin = (AbstractPlugin) pluginClass.getConstructor(PluginContext.class).newInstance(newContext);
            // 删除时用
            plugin.getContext().put("pluginJar", pluginJar);

            beanFactory.autowireBean(plugin);
            beanFactory.initializeBean(plugin, pluginInterface.value());

            plugin.onStarted();

            plugins.put(pluginInterface.value(), plugin);

            if (pluginCallback != null) {
                pluginCallback.onPluginStarted(plugin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized PluginContext stopPlugin(Class<?> pluginClass) {
        try {
            PluginInterface pluginInterface = pluginClass.getAnnotation(PluginInterface.class);
            if (pluginInterface == null || StringUtils.isBlank(pluginInterface.value())) {
                throw new RuntimeException("plugin does not have PluginInterface annotation");
            }

            final AbstractPlugin oldPlugin = plugins.getOrDefault(pluginInterface.value(), null);
            if (oldPlugin == null) {
                return null;
            }
            beanFactory.destroyBean(oldPlugin);

            oldPlugin.onStopped();
            plugins.remove(pluginInterface.value());

            if (pluginCallback != null) {
                pluginCallback.onPluginRemoved(oldPlugin);
            }

            return oldPlugin.getContext();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized PluginContext stopPlugin(Path pluginJar) {
        final Optional<Map.Entry<String, AbstractPlugin>> plugin = plugins.entrySet().stream().filter(entry ->
            pluginJar.equals(entry.getValue().getContext().get("pluginJar"))
        ).findFirst();

        if (plugin.isPresent()) {
            return stopPlugin(plugin.get().getValue().getClass());
        } else {
            return null;
        }
    }

    public synchronized void startPlugin(final Path pluginJar) {
        URLClassLoader loader = null;
        try {
            loader = new URLClassLoader(new URL[]{pluginJar.toUri().toURL()}, getClass().getClassLoader());

            final URL resource = loader.findResource("plugin.properties");
            if (resource == null) {
                throw new Exception("Failed to find plugin.properties in plugin jar");
            }

            final Properties properties = new Properties();
            properties.load(resource.openStream());

            final String pluginClassName = properties.getProperty("plugin.class");
            final Class<?> pluginClass = loader.loadClass(pluginClassName);
            startPlugin((Class<AbstractPlugin>) pluginClass, pluginJar);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (loader != null) {
                try {
                    loader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void start() {
        /**
         * 第一步，先看看系统内有哪些缺省的Plugin
         */
        final Reflections reflections = new Reflections(this.rootPackage);
        final Set<Class<?>> pluginsMap = reflections.getTypesAnnotatedWith(PluginInterface.class);
        pluginsMap.forEach(clazz -> {
            executors.submit(() -> startPlugin(clazz, null));
        });

        if (this.pluginPath.toFile().exists() && this.pluginPath.toFile().listFiles() != null) {
            for (final File file : this.pluginPath.toFile().listFiles()) {
                if (file.getName().endsWith(".jar")) {
                    executors.submit(() -> startPlugin(file.toPath()));
                }
            }
        }

        executors.submit(() -> run());
    }

    public void stop() {
        try {
            executors.shutdown();
            executors.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (Exception e) {
        }
    }

    protected void registerPluginsInQueue() {
        synchronized (pluginQueue) {
            final List<Map.Entry<Path, Timestamp>> entries = new ArrayList(pluginQueue.entrySet());
            for (final Map.Entry<Path, Timestamp> entry : entries) {
                if (entry.getValue().before(DateUtils.addDays(new Date(), -10))) {
                    pluginQueue.remove(entry.getKey());
                    executors.submit(() -> startPlugin(entry.getKey()));
                }
            }
        }
    }

    @SneakyThrows
    protected void run() {
        final WatchService watcher = FileSystems.getDefault().newWatchService();
        this.pluginPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        for (; ; ) {
            WatchKey watchKey;

            try {
                watchKey = watcher.poll(5, TimeUnit.SECONDS);
                if (watchKey == null) {
                    registerPluginsInQueue();
                    continue;
                }
            } catch (InterruptedException ex) {
                break;
            }

            final Set<Path> watchedFiles = new HashSet();
            for (final WatchEvent<?> event : watchKey.pollEvents()) {
                final WatchEvent.Kind<?> kind = event.kind();
                if (kind == OVERFLOW) {
                    continue;
                }

                final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                final Path filename = this.pluginPath.resolve(ev.context());
                if (!filename.toFile().getName().endsWith(".jar")) {
                    continue;
                }

                if (kind == ENTRY_DELETE) {
                    executors.submit(() -> stopPlugin(filename));
                } else if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                    watchedFiles.add(filename);
                }
            }

            watchKey.reset();

            synchronized (pluginQueue) {
                for (final Path watchedFile : watchedFiles) {
                    pluginQueue.put(watchedFile, new Date());
                }
            }
        }
    }
}
