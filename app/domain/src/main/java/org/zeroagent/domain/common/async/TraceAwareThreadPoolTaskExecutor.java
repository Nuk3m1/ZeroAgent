package org.zeroagent.domain.common.async;

import brave.Tracer;
import brave.propagation.TraceContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 支持Tracing的装饰器
 * @author Nuk3m1
 * @version 2026年04月11日  15时48分
 */
public class TraceAwareThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
    private final ThreadPoolTaskExecutor delegate;
    private final Tracer                 tracer;

    public TraceAwareThreadPoolTaskExecutor(ThreadPoolTaskExecutor delegate, Tracer tracer) {
        this.delegate = delegate;
        this.tracer = tracer;
    }

    @Override
    public void execute(Runnable task) {
        delegate.execute(wrapWithTrace(task));
    }
    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(wrapWithTrace(task));
    }
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(wrapWithTrace(task));
    }



    private Runnable wrapWithTrace(Runnable task) {
        TraceContext.Builder builder = tracer.nextSpan().context().toBuilder();
        TraceContext traceContext = builder.build();

        return () -> {
            try (Tracer.SpanInScope ws = tracer.withSpanInScope(tracer.toSpan(traceContext))) {
                task.run();
            }
        };
    }
    private <T> Callable<T> wrapWithTrace(Callable<T> task) {
        TraceContext.Builder builder = tracer.nextSpan().context().toBuilder();
        TraceContext traceContext = builder.build();

        return () -> {
            try (Tracer.SpanInScope ws = tracer.withSpanInScope(tracer.toSpan(traceContext))) {
                return task.call();
            }
        };
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }
    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return delegate.getThreadPoolExecutor();
    }
}
