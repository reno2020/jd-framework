package org.throwable.hystrix.annotation;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;

import java.util.concurrent.Future;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/10/8 17:41
 */
public class HelloWorldHystrixAnnotationAsync {

	@HystrixCommand(groupKey = "helloWorldHystrixAnnotation",
			commandKey = "helloWorldHystrixAnnotationAsync", fallbackMethod = "fallbck")
	public Future<Boolean> run(Integer value) {
		return new AsyncResult<Boolean>() {
			@Override
			public Boolean invoke() {
				return 0 == value || value % 2 == 0;
			}
		};
	}

	public Boolean fallbck() {
		return false;
	}
}
