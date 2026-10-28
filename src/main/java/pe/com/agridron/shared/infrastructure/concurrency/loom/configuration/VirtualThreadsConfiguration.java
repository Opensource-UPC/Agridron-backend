package pe.com.agridron.shared.infrastructure.concurrency.loom.configuration;

import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configures asynchronous task execution with Project Loom virtual threads.
 */
@Configuration
@EnableAsync
@NullMarked
public class VirtualThreadsConfiguration {

    @Bean(name = "applicationTaskExecutor")
    public TaskExecutor applicationTaskExecutor() {
        var executor = new SimpleAsyncTaskExecutor("agridron-loom-");
        executor.setVirtualThreads(true);
        return executor;
    }
}
