package com.statsmind.commons;

public class TimeRecorder {

    /**
     * 记录初始的时间
     */
    private long baseTimeMillis = System.currentTimeMillis();

    private long lastTimeMillis = System.currentTimeMillis();

    private String name;

    public TimeRecorder(String name) {
        this.name = name;
    }

    public void record(String format, Object... params) {
        long currTimeMillis = System.currentTimeMillis();
        System.out.println(String.format("[%s] 消耗[%d毫秒] 总消耗[%d毫秒] %s", name, currTimeMillis - lastTimeMillis, currTimeMillis - baseTimeMillis, String.format(format, params)));
        lastTimeMillis = currTimeMillis;
    }
}
