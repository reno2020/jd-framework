package org.throwable.support;

import org.springframework.beans.factory.InitializingBean;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/26 14:45
 */
@SuppressWarnings("unchecked")
public class DynamicAsyncContextThreadPool implements InitializingBean {

    private static final String NAME = "dynamicAsyncContextThreadPool";
    private static final int DEFAULT_CORE_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int DEFAULT_QUEUE_CAPACITY = 1000;
    private static final long DEFAULT_KEEP_ALIVE_SECONDS = 60;
    private static final long DEFAULT_ASYNC_TIMEOUT_SECONDS = 5;
    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final RejectedExecutionHandler HANDLER = (r, executor) -> {
        RejectedExecutionException rejectedExecutionException = new RejectedExecutionException("Task " + r.toString() +
                " rejected from " +
                executor.toString());
        if (r instanceof AsyncContextCallable) {
            AsyncContextCallable callable = (AsyncContextCallable) r;
            AsyncContext asyncContext = callable.getAsyncContext();
            ServletRequest servletRequest = asyncContext.getRequest();
            AsyncContextUtils.writeErrorResponse(asyncContext, servletRequest.getAttribute("uri"),
                    servletRequest.getAttribute("params"), rejectedExecutionException);
        } else {
            throw rejectedExecutionException;
        }
    };

    private String name = NAME;
    private Integer coreSize = DEFAULT_CORE_SIZE;
    private Integer maxSize = DEFAULT_CORE_SIZE;
    private Integer queueCapacity = DEFAULT_QUEUE_CAPACITY;
    private Long keepAliveSeconds = DEFAULT_KEEP_ALIVE_SECONDS;
    private Long asyncTimeoutSeconds = DEFAULT_ASYNC_TIMEOUT_SECONDS;
    private ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> taskQueue;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.executor = new ThreadPoolExecutor(coreSize, maxSize, keepAliveSeconds, TimeUnit.SECONDS, taskQueue,
                new ThreadFactory() {
                    private final AtomicInteger COUNTER = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setDaemon(true);
                        thread.setName(getName() + "-thread-" + COUNTER.getAndIncrement());
                        return thread;
                    }
                }, HANDLER);
    }

    public void submitTask(final HttpServletRequest request, final AsyncContextCallable asyncContextCallable) {
        executor.submit((Callable<Void>) () -> {
            AsyncContext asyncContext = request.startAsync();
            ServletRequest servletRequest = asyncContext.getRequest();
            String uri = request.getRequestURI();
            Map<String, String[]> params = request.getParameterMap();
            servletRequest.setAttribute("uri", uri);
            servletRequest.setAttribute("params", params);
            asyncContext.addListener(AsyncContextUtils.setupAsyncListener());
            asyncContext.setTimeout(getAsyncTimeoutSeconds() * 1000);
            asyncContextCallable.setAsyncContext(asyncContext);
            Object target = asyncContextCallable.call();
            if (null == target) {
                AsyncContextUtils.callback("", asyncContext, uri, params);
            } else if (target instanceof CompletableFuture) {
                CompletableFuture<Object> future = (CompletableFuture<Object>) target;
                future.thenAccept(resultValue -> AsyncContextUtils.callback(resultValue, asyncContext, uri, params))
                        .exceptionally(ex -> AsyncContextUtils.writeErrorResponse(asyncContext, uri, params, ex));
            } else {
                AsyncContextUtils.callback(target, asyncContext, uri, params);
            }
            return null;
        });
    }

    public Integer getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(Integer coreSize) {
        this.coreSize = coreSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(Integer queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public Long getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(Long keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAsyncTimeoutSeconds() {
        return asyncTimeoutSeconds;
    }

    public void setAsyncTimeoutSeconds(Long asyncTimeoutSeconds) {
        this.asyncTimeoutSeconds = asyncTimeoutSeconds;
    }

    public Integer getActiveCount(){
        return executor.getActiveCount();
    }

    public Long getTaskCount(){
        return executor.getTaskCount();
    }

    public Long getCompletedTaskCount(){
        return executor.getCompletedTaskCount();
    }

    public void clearTaskQueue() {
        LOCK.lock();
        try {
            taskQueue.clear();
        } finally {
            LOCK.unlock();
        }
    }
}
