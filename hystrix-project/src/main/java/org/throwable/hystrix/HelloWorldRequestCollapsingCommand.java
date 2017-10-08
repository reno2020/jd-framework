package org.throwable.hystrix;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/28 15:32
 */
public class HelloWorldRequestCollapsingCommand extends HystrixCollapser<List<Boolean>, Boolean, Integer> {

	private final Integer value;

	public HelloWorldRequestCollapsingCommand(Integer value) {
		this.value = value;
	}

	@Override
	public Integer getRequestArgument() {
		return value;
	}

	@Override
	protected HystrixCommand<List<Boolean>> createCommand(Collection<CollapsedRequest<Boolean, Integer>> collapsedRequests) {
		return new BatchCommand(collapsedRequests);
	}

	private static final class BatchCommand extends HystrixCommand<List<Boolean>> {

		private final Collection<CollapsedRequest<Boolean, Integer>> requests;

		private BatchCommand(Collection<CollapsedRequest<Boolean, Integer>> requests) {
			super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("helloWorldGroup"))
					.andCommandKey(HystrixCommandKey.Factory.asKey("helloWorldRequestCollapsing")));
			this.requests = requests;
		}

		@Override
		protected List<Boolean> run() {
			List<Boolean> response = new ArrayList<>();
			for (CollapsedRequest<Boolean, Integer> request : requests) {
				Integer argument = request.getArgument();
				response.add(0 == argument || argument % 2 == 0);  //这里就是执行单元的逻辑
			}
			return response;
		}
	}

	@Override
	protected void mapResponseToRequests(List<Boolean> batchResponse, Collection<CollapsedRequest<Boolean, Integer>> collapsedRequests) {
		int count = 0;
		for (CollapsedRequest<Boolean, Integer> request : collapsedRequests) {
			request.setResponse(batchResponse.get(count++));
		}
	}


	public static void main(String[] args) throws Exception {
		HystrixRequestContext context = HystrixRequestContext.initializeContext();
		try {
			Future<Boolean> command1 = new HelloWorldRequestCollapsingCommand(1).queue();
			Future<Boolean> command2 = new HelloWorldRequestCollapsingCommand(2).queue();
			Future<Boolean> command3 = new HelloWorldRequestCollapsingCommand(3).queue();
			Future<Boolean> command4 = new HelloWorldRequestCollapsingCommand(4).queue();
			Future<Boolean> command5 = new HelloWorldRequestCollapsingCommand(5).queue();
			//故意sleep超过10ms,第六个命令不会合并到本次批量请求
			TimeUnit.MILLISECONDS.sleep(13);
			Future<Boolean> command6 = new HelloWorldRequestCollapsingCommand(6).queue();

			System.out.println(command1.get());
			System.out.println(command2.get());
			System.out.println(command3.get());
			System.out.println(command4.get());
			System.out.println(command5.get());
			System.out.println(command6.get());
			// note：numExecuted表示共有几个命令执行，1个批量多命令请求算一个，这个实际值可能比代码写的要多，
			// 因为due to non-determinism of scheduler since this example uses the real timer
			int numExecuted = HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().size();
			System.out.println("num executed: " + numExecuted);
			int numLogs = 0;
			for (HystrixInvokableInfo<?> command : HystrixRequestLog.getCurrentRequest().getAllExecutedCommands()) {
				numLogs++;
				System.out.println(command.getCommandKey().name() + " => command.getExecutionEvents(): " + command.getExecutionEvents());
			}
			System.out.println("num logs:" + numLogs);
		} finally {
			context.shutdown();
		}

		Thread.sleep(Integer.MAX_VALUE);
	}
}
