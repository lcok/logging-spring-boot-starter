package cn.vious.starter.logging.prop;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置类
 *
 * @author lcok
 * @date 2022/8/3 10:53
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(ViousLoggingProperties.PREFIX)
public class ViousLoggingProperties {

    public static final String PREFIX = "vious.logging";

    public static final String LOGGING_ASYNC_THREAD_POOL_NAME = "vious_logging_pool";

    // --------------------------- 日志基础配置 ---------------------------

    /**
     * 是否开启Logging，默认开启
     */
    private boolean enable = true;

    // --------------------------- 日志输出目标配置 ---------------------------

    private ToHttp toHttp = new ToHttp();

    private ToStdout toStdout = new ToStdout();

    // --------------------------- 日志输出线程池配置 ---------------------------

    private LoggingThreadPool threadPool = new LoggingThreadPool();

    // --------------------------- 配置详情 ---------------------------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoggingThreadPool {
        /**
         * 异步任务线程池设置：线程名前缀
         */
        private String namePrefix = "LOGGING-";

        /**
         * 异步任务线程池设置：线程池核心线程数量
         */
        private int core = 8;

        /**
         * 异步任务线程池设置：线程池最大线程数量
         */
        private int coreMax = 16;

        /**
         * 异步任务线程池设置：队列容量
         */
        private int queueCapacity = 128;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToStdout {
        /**
         * 是否发送日志到stdout，默认开启
         */
        private boolean enable = true;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToHttp {
        /**
         * 是否发送日志到http接口，默认关闭
         */
        private boolean enable = false;

        /**
         * 将日志发送到指定接口。<p>
         * 例如：http://example.com/add-log <p>
         * 调用方法为：POST <p>
         * 调用传参方式为：JSON <p>
         */
        private String url = null;
    }


}
