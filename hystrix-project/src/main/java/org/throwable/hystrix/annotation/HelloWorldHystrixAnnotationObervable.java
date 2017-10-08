package org.throwable.hystrix.annotation;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import rx.Observable;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/10/8 17:41
 */
public class HelloWorldHystrixAnnotationObervable {

	@HystrixCommand(groupKey = "helloWorldHystrixAnnotation",
			commandKey = "helloWorldHystrixAnnotationObervable", fallbackMethod = "fallbck")
	public Observable<Boolean> run(Integer value) {
		return Observable.create(subscriber -> {
			try {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(value == 0 || value % 2 == 0);
					subscriber.onCompleted();
				}
			} catch (Exception e) {
				subscriber.onError(e);
			}
		});
	}

	public Boolean fallbck() {
		return false;
	}
}
