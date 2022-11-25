package com.statsmind.commons;

public class IntegerHolder implements Comparable<IntegerHolder> {
    private int value = 0;

    public IntegerHolder() {
        value = 0;
    }

    public IntegerHolder(int value) {
        this.value = value;
    }

    public int increase() {
        return increase(1);
    }

    public synchronized int increase(int count) {
        value = value + count;
        return value;
    }

    public int decrease() {
        return decrease(1);
    }

    public synchronized int decrease(int count) {
        value = value - 1;
        return value;
    }

    public int getValue() {
        return value;
    }

    public synchronized void setValue(int value) {
        this.value = value;
    }

    public synchronized void putMax(Integer cid) {
        if (cid > value) {
            value = cid;
        }
    }

    public synchronized void putMin(Integer cid) {
        if (cid < value) {
            value = cid;
        }
    }

    @Override
    public String toString() {
        return value + "";
    }

    @Override
    public int compareTo(IntegerHolder o) {
        return Integer.compare(value, o.value);
    }
}
