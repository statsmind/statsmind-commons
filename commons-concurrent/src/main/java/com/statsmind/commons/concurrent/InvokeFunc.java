package com.statsmind.commons.concurrent;

public interface InvokeFunc<T, U> {
    public U accept(T param);
}
