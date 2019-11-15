package com.bookmark.analysis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author mjm
 * @createtime 2018/7/3 12:09
 */
@Configuration
@EnableAsync
public class TaskExecutorConfig {

	@Bean
	public Executor executor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 设置核心线程数
		executor.setCorePoolSize(10);
		// 设置最大线程数
		executor.setMaxPoolSize(100);
		// 设置队列容量
		executor.setQueueCapacity(1000);
		// 设置线程活跃时间（秒）
		executor.setKeepAliveSeconds(60);
		// 设置默认线程名称
		executor.setThreadNamePrefix("taskExecutor-");
		// 设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
		executor.setWaitForTasksToCompleteOnShutdown(true);
		// 设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁
		executor.setAwaitTerminationSeconds(60);
		// 设置拒绝策略
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return executor;
	}
}
