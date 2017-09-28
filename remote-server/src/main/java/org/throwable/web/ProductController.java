package org.throwable.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/25 11:13
 */
@RestController
public class ProductController {

    private static final Random COUNT = new Random(10);
    private static final Random AMOUNT = new Random(100);

    @GetMapping(value = "/product/count")
    public Integer getProductCount() throws Exception {
        Thread.sleep(10);
        Integer result = COUNT.nextInt(1000);
        System.out.println("count --> " + result);
        return result;
    }

    @GetMapping(value = "/product/amount")
    public Integer getProductAmount() throws Exception {
        Thread.sleep(20);
        Integer result = AMOUNT.nextInt(1000);
        System.out.println("amount --> " + result);
        return result;
    }
}
