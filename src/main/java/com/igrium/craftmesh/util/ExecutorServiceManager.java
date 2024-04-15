package com.igrium.craftmesh.util;

import java.io.Closeable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

public final class ExecutorServiceManager<T extends ExecutorService> {

    public static ExecutorServiceManager<?> createFixed(int threadPoolSize, ThreadFactory factory) {
        return new ExecutorServiceManager<>(() -> Executors.newFixedThreadPool(threadPoolSize, factory));
    }

    private final Supplier<T> executorServiceFactory;

    public ExecutorServiceManager(Supplier<T> executorServiceFactory) {
        this.executorServiceFactory = executorServiceFactory;
    }

    private T executorService;

    private final Set<ExecutorHandle> handles = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public synchronized ExecutorHandle getHandle() {
        if (executorService == null) {
            executorService = executorServiceFactory.get();
        }

        ExecutorHandle handle = new ExecutorHandle(executorService);
        handles.add(handle);
        return handle;
    }

    public class ExecutorHandle implements Closeable {
        private final Executor executor;

        private boolean isClosed;

        ExecutorHandle(Executor executor) {
            this.executor = executor;
        }

        public Executor getExecutor() {
            return executor;
        }

        @Override
        public void close() {
            if (isClosed)
                return;
            isClosed = true;
            handles.remove(this);
            
            synchronized(ExecutorServiceManager.this) {
                if (handles.isEmpty()) {
                    shutdownService();
                }
            }
        }
    }

    public synchronized void shutdownService() {
        if (executorService != null) {
            executorService.shutdown();
        }
        executorService = null;
    }
}
