package cn.vious.starter.logging.annotation;

import java.lang.annotation.*;

/**
 * 重复日志注解的值记录<p>
 * 用于重复使用@Longging注解
 *
 * @author lcok
 * @date 2022/8/3 10:46
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggingList {
    Logging[] value();
}
