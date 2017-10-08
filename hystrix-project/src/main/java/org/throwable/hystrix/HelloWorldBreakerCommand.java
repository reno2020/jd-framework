package org.throwable.hystrix;

import com.netflix.hystrix.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/28 15:32
 */
public class HelloWorldBreakerCommand extends HystrixCommand<String> {

	private final String name;

	public HelloWorldBreakerCommand(String name) {
		//最小配置,指定groupKey
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("helloWorldGroup"))
				.andThreadPoolPropertiesDefaults(
						HystrixThreadPoolProperties.Setter()
								.withCoreSize(500))
				.andCommandPropertiesDefaults(
						HystrixCommandProperties.Setter()
								.withCircuitBreakerEnabled(true)
								.withCircuitBreakerErrorThresholdPercentage(50)
								.withCircuitBreakerRequestVolumeThreshold(3)
								.withExecutionTimeoutInMilliseconds(1000))
				//commonKey
				.andCommandKey(HystrixCommandKey.Factory.asKey("helloWorldBreaker")));
		this.name = name;
	}

	@Override
	protected String run() throws Exception {
		System.out.println("RUNNABLE --> " + name);
		Integer num = Integer.valueOf(name);
		if (num % 2 == 0 && num < 10) {
			return "Hello " + name + ",current thread:" + Thread.currentThread().getName();
		} else {
			Thread.sleep(1500);
			return name;
		}
	}

	@Override
	protected String getFallback() {
		return "FALLBACK --> !";
	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 50; i++) {
			try {
				System.out.println(new HelloWorldBreakerCommand(String.valueOf(i)).execute());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Thread.sleep(Integer.MAX_VALUE);
	}
}
