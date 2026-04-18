package org.zeroagent.common.utils.concurrent;


import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final static AtomicInteger POOL_NUMBER = new AtomicInteger(1);

    private final        AtomicInteger threadNumber;
    private final        String        namePrefix;
    private final        boolean       isDaemon;

    public NamedThreadFactory(@NotNull String prefix, boolean isDaemon) {
        this.threadNumber = new AtomicInteger(1);
        this.namePrefix = prefix +  "-" + POOL_NUMBER.getAndIncrement() + "-thread-";
        this.isDaemon = isDaemon;
    }
    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread t = new Thread(r, this.namePrefix + this.threadNumber.getAndIncrement());
        t.setDaemon(isDaemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
