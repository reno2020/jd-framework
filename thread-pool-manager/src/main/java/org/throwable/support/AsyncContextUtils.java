package org.throwable.support;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/26 15:54
 */
@Slf4j
public abstract class AsyncContextUtils {

    public static AsyncListener setupAsyncListener() {
        return new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                handleAsyncListenerEvent(asyncEvent, AsyncListenerEvent.COMPLETE);
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
                handleAsyncListenerEvent(asyncEvent, AsyncListenerEvent.TIMEOUT);
            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {
                handleAsyncListenerEvent(asyncEvent, AsyncListenerEvent.ERROR);
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
                handleAsyncListenerEvent(asyncEvent, AsyncListenerEvent.START);
            }
        };
    }

    public static void callback(Object result, AsyncContext asyncContext, Object uri, Object params) {
        if (null != asyncContext && asyncContext.getTimeout() > 0) {
            HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
            try {
                writeResponse(result, response);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.error("Handle request failed,uri:{},params:{}", uri, JSON.toJSONString(params), e);
            } finally {
                asyncContext.complete();
            }
        }
    }

    public static Void writeErrorResponse(AsyncContext asyncContext, Object uri, Object params, Throwable e) {
        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error("Handle request failed,uri:{},params:{}", uri, JSON.toJSONString(params), e);
        return null;
    }

    private static void writeResponse(Object result, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try (PrintWriter writer = response.getWriter()) {
            if (result instanceof String) {
                writer.write((String) result);
            } else {
                writer.write(JSON.toJSONString(result));
            }
        }
    }

    private static void handleAsyncListenerEvent(AsyncEvent asyncEvent, AsyncListenerEvent asyncListenerEvent) {
        AsyncContext asyncContext = asyncEvent.getAsyncContext();
        ServletRequest servletRequest = asyncContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        Object uri = servletRequest.getAttribute("uri");
        String params = JSON.toJSONString(servletRequest.getAttribute("params"));
        switch (asyncListenerEvent) {
            case START:
                if (log.isDebugEnabled()) {
                    log.debug("Start handling request,uri:{},params:{}", uri, params);
                }
                break;
            case TIMEOUT:
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.error("Handle request timeout,uri:{},params:{}", uri, params);
                break;
            case ERROR:
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.error("Handle request error,uri:{},params:{}", uri, params);
                break;
            case COMPLETE:
                if (null != servletRequest.getAttribute("timeout") && "true".equals(servletRequest.getAttribute("timeout"))) {
                    log.error("Complete request timeout,uri:{},params:{}", uri, params);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Complete handling request,uri:{},params:{}", uri, params);
                    }
                }
                break;
        }
    }

    private enum AsyncListenerEvent {

        COMPLETE,

        TIMEOUT,

        ERROR,

        START
    }
}
