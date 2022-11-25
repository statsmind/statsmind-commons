package com.statsmind.commons.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginInterface {
    /**
     * name of plugin
     *
     * @return
     */
    String value();

    boolean enabled() default true;
}
