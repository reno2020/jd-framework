package org.throwable.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import rx.Observable;
import rx.Observer;

import java.util.concurrent.Future;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/28 15:32
 */
public class HelloWorldCommand extends HystrixCommand<String> {

    private final String name;

    public HelloWorldCommand(String name) {
        //最小配置,指定groupKey
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("helloWorldGroup"))
                //commonKey代表一个依赖抽象,相同的依赖要用相同的commonKey,依赖隔离的根本就是依据commonKey进行隔离
        .andCommandKey(HystrixCommandKey.Factory.asKey("helloWorld")));
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
        return "Hello " + name + ",current thread:" + Thread.currentThread().getName();
    }

    public static void main(String[] args) throws Exception {
        HelloWorldCommand command = new HelloWorldCommand("doge");
        //1、同步调用
        String result = command.execute();
        System.out.println("Sync call result --> " + result);

        //2、异步调用
        command = new HelloWorldCommand("doge async");
        Future<String> future = command.queue();
        result = future.get();
        System.out.println("Async call result --> " + result);

        //3.1、注册观察者事件订阅 -- 事件注册前执行
        Observable<String> observable = new HelloWorldCommand("doge observable").observe();

        observable.subscribe(result1 -> System.out.println("Observable call result --> " + result1));

        //3.2、注册完整执行生命周期事件 -- 事件注册前执行
        observable.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                //onNext/onError完成之后最后回调
                System.out.println("Execute onCompleted");
            }

            @Override
            public void onError(Throwable throwable) {
                // 当产生异常时回调
                System.out.println("Execute error");
                throwable.printStackTrace();
            }

            @Override
            public void onNext(String s) {
                // 获取结果后回调
                System.out.println("Execute onNext --> " + s);
            }
        });

        //4、注册观察者事件订阅 -- 事件注册后执行
		command = new HelloWorldCommand("doge toObservable");
		Observable<String> toObservable = command.toObservable();
		toObservable.subscribe(new Observer<String>() {
			@Override
			public void onCompleted() {
				//onNext/onError完成之后最后回调
				System.out.println("Execute onCompleted");
			}

			@Override
			public void onError(Throwable throwable) {
				// 当产生异常时回调
				System.out.println("Execute error");
				throwable.printStackTrace();
			}

			@Override
			public void onNext(String s) {
				// 获取结果后回调
				System.out.println("Execute onNext --> " + s);
			}
		});

		//异步执行需要时间，先阻塞主线程
		Thread.sleep(5000);
	}
}
