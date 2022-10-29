package cn.vious.starter.logging;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.vious.starter.logging.prop.ViousLoggingProperties;
import cn.vious.starter.logging.service.LoggingConsumer;
import cn.vious.starter.logging.service.impl.LoggingConsumerHttp;
import cn.vious.starter.logging.service.impl.LoggingConsumerStdout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 配置类
 *
 * @author lcok
 * @date 2022/8/3 10:32
 */
@Slf4j
@ComponentScan("cn.vious.starter.logging")
@ServletComponentScan("cn.vious.starter.logging.filter")
@EnableConfigurationProperties({ViousLoggingProperties.class})
@ConditionalOnProperty(prefix = ViousLoggingProperties.PREFIX, name = "enable", havingValue = "true")
public class ViousLoggingAutoConfiguration {


    @Resource
    private ViousLoggingProperties properties;

    // --------------------------- 日志输出目标配置 ---------------------------

    @Bean
    @ConditionalOnMissingBean
    public List<LoggingConsumer> loggingConsumerList() {
        List<LoggingConsumer> list = new ArrayList<>();
        if (properties.getToStdout().isEnable()) {
            LoggingConsumerStdout consumerStdout = new LoggingConsumerStdout();
            list.add(consumerStdout);
            log.info("[ vious logging ] : 加载日志消费者 {}", consumerStdout.getName());
        }
        if (properties.getToHttp().isEnable()) {
            // config check
            String url = properties.getToHttp().getUrl();
            if (StrUtil.isNotBlank(url)) {
                url = URLUtil.normalize(url);
                properties.getToHttp().setUrl(url);
                // create consumer
                LoggingConsumerHttp consumerHttp = new LoggingConsumerHttp();
                list.add(consumerHttp);
                log.info("[ vious logging ] : 加载日志http消费者 {} , target url: {}", consumerHttp.getName(), url);
            }
            log.error("[ vious logging ] : toHttp url不可为空");
        }
        return list;
    }


    // --------------------------- 日志输出线程池配置 ---------------------------

    @Bean(ViousLoggingProperties.LOGGING_ASYNC_THREAD_POOL_NAME)
    @ConditionalOnMissingBean
    public Executor viousLoggingThreadPool(ViousLoggingProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(properties.getThreadPool().getNamePrefix());
        executor.setQueueCapacity(properties.getThreadPool().getQueueCapacity());
        executor.setCorePoolSize(properties.getThreadPool().getCore());
        executor.setMaxPoolSize(properties.getThreadPool().getCoreMax());
        //拒绝策略默认抛异常
        executor.initialize();
        log.info("[ vious logging ] : LoggingConsumer 线程池加载完毕");
        return executor;
    }
}
