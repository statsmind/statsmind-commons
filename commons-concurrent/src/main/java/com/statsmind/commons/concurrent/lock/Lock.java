package com.statsmind.commons.concurrent.lock;

/**
 * Created by kl on 2017/12/29.
 */
public interface Lock extends AutoCloseable {

    boolean acquire();

    void release();
}
