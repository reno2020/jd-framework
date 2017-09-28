package org.throwable.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    @GetMapping(value = "/async/context")
    public String asyncRequest() throws Exception{
        CompletableFuture<Integer> count = CompletableFuture.supplyAsync(
                () -> restTemplate.getForEntity("http://localhost:9092/product/count", Integer.class).getBody());
        CompletableFuture<Integer> amount = CompletableFuture.supplyAsync(
                () -> restTemplate.getForEntity("http://localhost:9092/product/amount", Integer.class).getBody());
        CompletableFuture<Integer> result = count.thenCombine(amount, (a, b) -> a * b);
        System.out.println("result -> " + result.get(10, TimeUnit.SECONDS));
        return "success!";
    }
}
