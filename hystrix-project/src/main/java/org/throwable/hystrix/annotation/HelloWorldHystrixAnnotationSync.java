package org.throwable.hystrix.annotation;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/10/8 17:41
 */
public class HelloWorldHystrixAnnotationSync {

	@HystrixCommand(groupKey = "helloWorldHystrixAnnotation",
			commandKey = "helloWorldHystrixAnnotationSync", fallbackMethod = "fallbck")
	public Boolean run(Integer value) {
		return 0 == value || value % 2 == 0;
	}

	public Boolean fallbck() {
		return false;
	}
}
