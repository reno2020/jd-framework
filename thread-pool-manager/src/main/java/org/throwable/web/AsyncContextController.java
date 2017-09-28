package org.throwable.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.throwable.support.AsyncContextCallable;
import org.throwable.support.DynamicAsyncContextThreadPool;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/25 11:07
 */
@RestController
public class AsyncContextController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier(value = "zeroDynamicAsyncContextThreadPool")
    private DynamicAsyncContextThreadPool threadPool;

    @GetMapping(value = "/async/context")
    public void asyncRequest(HttpServletRequest request) throws Exception {
        CompletableFuture<Integer> count = CompletableFuture.supplyAsync(
                () -> restTemplate.getForEntity("http://localhost:9092/product/count", Integer.class).getBody());
        CompletableFuture<Integer> amount = CompletableFuture.supplyAsync(
                () -> restTemplate.getForEntity("http://localhost:9092/product/amount", Integer.class).getBody());
        CompletableFuture<Integer> result = count.thenCombineAsync(amount, (a, b) -> a * b);
        threadPool.submitTask(request, new AsyncContextCallable() {
            @Override
            public Object call() throws Exception {
                return result;
            }
        });
        System.out.println("主线程结束!!!!!!!!");
    }

    @GetMapping(value = "/sync/context")
    public Integer syncRequest() throws Exception {
        CompletableFuture<Integer> count = CompletableFuture.supplyAsync(
                () -> restTemplate.getForEntity("http://localhost:9092/product/count", Integer.class).getBody());
        CompletableFuture<Integer> amount = CompletableFuture.supplyAsync(
                () -> restTemplate.getForEntity("http://localhost:9092/product/amount", Integer.class).getBody());
        return count.thenCombineAsync(amount, (a, b) -> a * b).get();
    }
}
