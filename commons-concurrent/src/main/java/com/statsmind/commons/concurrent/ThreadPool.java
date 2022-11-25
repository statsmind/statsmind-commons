package com.statsmind.commons.concurrent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ThreadPool {
    private static ThreadPool instance;
    private final ScheduledThreadPoolExecutor poolExecutor;

    private ThreadPool(int numThreads) {
        int corePoolSize = Runtime.getRuntime().availableProcessors();

        if (numThreads > 0) {
            corePoolSize = numThreads;
        }

        this.poolExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
//
//        this.poolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
//            Integer.MAX_VALUE, TimeUnit.SECONDS,
//            new LinkedBlockingQueue());
    }

    public static synchronized ThreadPool getInstance() {
        if (instance == null) {
            instance = new ThreadPool(0);
        }

        return instance;
    }

    public ThreadPool corePoolSize(int corePoolSize) {
        this.poolExecutor.setCorePoolSize(corePoolSize);
        return this;
    }

    public ThreadPool maximumPoolSize(int maximumPoolSize) {
        this.poolExecutor.setMaximumPoolSize(maximumPoolSize);
        return this;
    }

    public ScheduledThreadPoolExecutor getExecutor() {
        return poolExecutor;
    }

    public CompletableFuture<Void> invoke(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, getExecutor());
    }

    public ScheduledFuture<?> invoke(Runnable runnable, long delay, TimeUnit unit) {
        return getExecutor().schedule(runnable, delay, unit);
    }

    public <U> ScheduledFuture<U> invoke(Callable<U> callable, long delay, TimeUnit unit) {
        return getExecutor().schedule(callable, delay, unit);
    }

    public <U> ScheduledFuture<U> invoke(Supplier<U> supplier, long delay, TimeUnit unit) {
        return getExecutor().schedule(() -> supplier.get(), delay, unit);
    }

    public <U> CompletableFuture<U> invoke(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, getExecutor());
    }

    public <U> CompletableFuture<U> invoke(Supplier<U> supplier, U defaultValue) {
        return invoke(supplier).exceptionally(throwable -> defaultValue);
    }


    public <T> List<T> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException {
        return getExecutor().invokeAll(tasks, timeout, unit)
            .stream().map(future -> {
                try {
                    return future.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
    }

    public <T> List<T> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return invokeAll(tasks, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws ExecutionException, InterruptedException, TimeoutException {
        return invokeAny(tasks, Integer.MAX_VALUE, TimeUnit.SECONDS);
    }


    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return getExecutor().invokeAny(tasks, timeout, unit);
    }

    /**
     * @param supplierFactory
     * @param capacity        the queue is blocked when queue size exceeds capacity
     * @param <T>
     * @param <U>
     * @return
     */
    public <T, U> Queue<T, U> createQueue(InvokeFunc<T, U> supplierFactory, int capacity) {
        return new Queue<T, U>(supplierFactory, this, capacity);
    }

    /**
     * A thread pool may have multiple queues which controls how many concurrent threads are allowed to execute within a queue context
     *
     * @param <T>
     * @param <U>
     */
    public static class Queue<T, U> {
        /**
         * store all the futures and arguments
         */
        private List<FutureAndParam<T, U>> futureAndParams = new ArrayList<>();
        private InvokeFunc<T, U> invokeFunc;
        private ThreadPool threadPool;
        private LinkedBlockingQueue queue;
        private long timeout;
        private TimeUnit timeUnit;
        private Runnable exceptionHandler;
        private boolean isBlocked = false;

        protected Queue(InvokeFunc<T, U> invokeFunc, ThreadPool threadPool, int capacity) {
            this.invokeFunc = invokeFunc;
            this.threadPool = threadPool;
            this.queue = new LinkedBlockingQueue(capacity);
            this.timeout = Long.MAX_VALUE;
            this.timeUnit = TimeUnit.SECONDS;
            this.exceptionHandler = null;
        }

        public Queue timeout(long timeout, TimeUnit timeUnit) {
            this.timeout = timeout;
            this.timeUnit = timeUnit;

            return this;
        }

        public Queue exceptionHandler(Runnable runnable) {
            this.exceptionHandler = runnable;
            return this;
        }

        @SneakyThrows
        public void enqueue(T param) {
            if (isBlocked) {
                return;
            }

            this.queue.put(param);

            CompletableFuture<U> future = this.threadPool.invoke(() -> invokeFunc.accept(param))
                .orTimeout(timeout, timeUnit)
                .thenApply(u -> {
                    dequeue(param);
                    return u;
                })
                .exceptionally(throwable -> {
                    dequeue(param);

                    if (exceptionHandler != null) {
                        exceptionHandler.run();
                    }

                    throw new RuntimeException(throwable);
                });

            futureAndParams.add(new FutureAndParam<>(future, param));
        }

        public void block(boolean isBlocked) {
            this.isBlocked = isBlocked;
        }

        public void dequeue(T param) {
            this.queue.remove(param);
        }

        public void waitForTermination() {
            waitForResults(false);
        }

        public void waitForTermination(boolean ignoreException) {
            try {
                futureAndParams.forEach(param -> {
                    try {
                        param.getFuture().get();
                    } catch (Exception e) {
                        if (!ignoreException) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } finally {
                this.queue.clear();
                this.queue = null;
            }
        }

        public List<U> waitForResults() {
            return waitForResults(false);
        }

        public List<U> waitForResults(boolean ignoreException) {
            List<U> results = new ArrayList<U>();

            try {
                for (FutureAndParam<T, U> param : futureAndParams) {
                    try {
                        results.add(param.getFuture().get());
                    } catch (Exception e) {
                        if (!ignoreException) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                return results;
            } finally {
                this.queue.clear();
                this.queue = null;
            }
        }

        public void cancelAll() {
            for (FutureAndParam<T, U> param : futureAndParams) {
                param.getFuture().cancel(true);
            }
        }
    }

    @Data
    @AllArgsConstructor
    static class FutureAndParam<T, U> {
        private CompletableFuture<U> future;
        private T param;
    }
}
