package org.throwable.support;

import javax.servlet.AsyncContext;
import java.util.concurrent.Callable;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/26 14:46
 */
public class AsyncContextCallable implements Callable<Object> {

    private AsyncContext asyncContext;

    public AsyncContextCallable() {
    }

    @Override
    public Object call() throws Exception {

        return null;
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    public void setAsyncContext(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }
}
