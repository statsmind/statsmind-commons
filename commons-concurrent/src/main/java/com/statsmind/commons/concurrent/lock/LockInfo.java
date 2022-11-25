package com.statsmind.commons.concurrent.lock;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LockInfo {
    private LockType type;
    private String name;
    private long waitTime;
    private long leaseTime;
}
