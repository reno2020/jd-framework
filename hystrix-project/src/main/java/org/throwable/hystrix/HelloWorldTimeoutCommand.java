package org.throwable.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/28 15:32
 */
public class HelloWorldTimeoutCommand extends HystrixCommand<String> {

    private final String name;

    public HelloWorldTimeoutCommand(String name) {
        //最小配置,指定groupKey
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("helloWorldGroup"))
                //指定超时时间为500ms
        .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(500))
                //commonKey
        .andCommandKey(HystrixCommandKey.Factory.asKey("helloWorldTimeout")));
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
		System.out.println("HelloWorldTimeoutCommand --> "+ Thread.currentThread().getName());
		Thread.sleep(1000);
        return "Hello " + name + ",current thread:" + Thread.currentThread().getName();
    }

    @Override
    protected String getFallback() {
        return "fallback!";
    }

    public static void main(String[] args) throws Exception {
        HelloWorldTimeoutCommand command = new HelloWorldTimeoutCommand("doge");
        //超时执行getFallback
        System.out.println(command.execute());
    }
}
