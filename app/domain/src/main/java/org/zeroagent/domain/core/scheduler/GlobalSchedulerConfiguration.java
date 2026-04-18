package org.zeroagent.domain.core.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.boot.task.ThreadPoolTaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;


/**
 * 全局定时调度线程池配置
 * 注：{@link EnableScheduling} 注解仅用于支持方法切面注解，如果不使用声明式调度，而使用编程式调度，则不需要该注解。
 * @author Nuk3m1
 * @version 2026年04月09日  15时40分
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class GlobalSchedulerConfiguration {
    private final String THREAD_POOL_NAME = ScheduledAnnotationBeanPostProcessor.DEFAULT_TASK_SCHEDULER_BEAN_NAME;
    private final TaskSchedulerHelper taskSchedulerHelper;

    @Bean
    public ThreadPoolTaskSchedulerCustomizer taskSchedulerCustomizer() {
        return taskSchedulerHelper.rejectedHandlerCustomizer(THREAD_POOL_NAME);
    }

    @Bean(name = THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = THREAD_POOL_NAME)
    public TaskScheduler taskScheduler(ThreadPoolTaskSchedulerBuilder builder) {
        // 在 Spring Boot 框架中定义的全局调度线程池，会被 spring-messaging 里定义的 messageBrokerTaskScheduler 所覆盖，最佳实践是自己定义这个线程池实例
        return builder.build();
    }
    @Bean
    public SchedulingConfigurer schedulingConfigurer(@Qualifier(THREAD_POOL_NAME) TaskScheduler taskScheduler) {
        return taskRegistrar -> taskRegistrar.setTaskScheduler(taskScheduler);
    }
}
