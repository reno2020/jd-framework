package org.throwable.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/28 15:32
 */
public class HelloWorldRequestCacheCommand extends HystrixCommand<Boolean> {

	private final Integer value;
	private final String name;

	public HelloWorldRequestCacheCommand(Integer value,String name) {
		//最小配置,指定groupKey
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("helloWorldGroup"))
				//commonKey
				.andCommandKey(HystrixCommandKey.Factory.asKey("helloWorldRequestCache")));
		this.value = value;
		this.name  = name;
	}

	@Override
	protected Boolean run() throws Exception {
		return value == 0 || value %2 == 0;
	}

	@Override
	protected String getCacheKey() {
		return name + value;
	}

	public static void main(String[] args) throws Exception {
		HystrixRequestContext context = HystrixRequestContext.initializeContext();
		try {
			HelloWorldRequestCacheCommand command1 = new HelloWorldRequestCacheCommand(1,"doge");
			HelloWorldRequestCacheCommand command2 = new HelloWorldRequestCacheCommand(1,"doge");
			HelloWorldRequestCacheCommand command3 = new HelloWorldRequestCacheCommand(1,"doge-ex");
			System.out.println("command1 result --> " + command1.execute());
			System.out.println("command1 isResponseFromCache --> " + command1.isResponseFromCache());

			System.out.println("command2 result --> " + command2.execute());
			System.out.println("command2 isResponseFromCache --> " + command2.isResponseFromCache());

			System.out.println("command3 result --> " + command3.execute());
			System.out.println("command3 isResponseFromCache --> " + command3.isResponseFromCache());
		}finally {
			context.shutdown();
		}

		Thread.sleep(Integer.MAX_VALUE);
	}
}
