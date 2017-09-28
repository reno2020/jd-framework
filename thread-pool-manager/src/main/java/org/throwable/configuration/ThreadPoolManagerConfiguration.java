package org.throwable.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.throwable.support.DynamicAsyncContextThreadPool;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/9/26 14:43
 */
@Configuration
public class ThreadPoolManagerConfiguration {

    @Bean(value = "zeroDynamicAsyncContextThreadPool")
    public DynamicAsyncContextThreadPool zeroDynamicAsyncContextThreadPool() {
        DynamicAsyncContextThreadPool threadPool = new DynamicAsyncContextThreadPool();
        threadPool.setName("第零级业务线程池");
        threadPool.setAsyncTimeoutSeconds(15L);
        return threadPool;
    }

    @Bean
    public RestTemplate restTemplate(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(15000);
        factory.setConnectTimeout(15000);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
}
