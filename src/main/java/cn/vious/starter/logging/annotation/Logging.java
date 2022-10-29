package cn.vious.starter.logging.annotation;

import java.lang.annotation.*;

/**
 * 日志注解
 *
 * @author lcok
 * @date 2022/8/3 10:39
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(LoggingList.class)
public @interface Logging {

    /**
     * 必填 ,  值类型：字符串，支持SpEL表达式模板 <p>
     * 操作模块 <p>
     *
     * <p>
     * SpEL表达式模板示例： <p>
     * "xxx{{SpEL表达式1}}xxx{{SpEL表达式2}}xxx" <p>
     * 说明： <p>
     * 双花括号模板内的内容解析为SpEL表达式 <p>
     * 支持0或多个模板 <p>
     * 示例中会分别解析"SpEL表达式1"和"SpEL表达式2" <p>
     * <p>
     *
     * @return String
     */
    String optModule() default "";

    /**
     * 必填 ,  值类型：字符串，支持SpEL表达式模板  <p>
     * 操作名称 <p>
     *
     * <p>
     * SpEL表达式模板示例： <p>
     * "xxx{{SpEL表达式1}}xxx{{SpEL表达式2}}xxx" <p>
     * 说明： <p>
     * 双花括号模板内的内容解析为SpEL表达式 <p>
     * 支持0或多个模板 <p>
     * 示例中会分别解析"SpEL表达式1"和"SpEL表达式2" <p>
     * <p>
     *
     * @return String
     */
    String optType() default "";

    /**
     * 必填 ,  值类型：字符串，支持SpEL表达式模板  <p>
     * 操作内容 <p>
     *
     * <p>
     * SpEL表达式模板示例： <p>
     * "xxx{{SpEL表达式1}}xxx{{SpEL表达式2}}xxx" <p>
     * 说明： <p>
     * 双花括号模板内的内容解析为SpEL表达式 <p>
     * 支持0或多个模板 <p>
     * 示例中会分别解析"SpEL表达式1"和"SpEL表达式2" <p>
     * <p>
     *
     * @return String
     */
    String optDesc() default "";

    /**
     * 可选 , 值类型：SpEL表达式 , 预期SpEL表达式解析类型：Boolean <p>
     * 触发本日志的条件 <p>
     * 默认触发。
     *
     * @return SpEL表达式预期返回Boolean
     */
    String condition() default "'true'";

    /**
     * 可选 , 值类型：SpEL表达式 , 预期SpEL表达式解析类型：Boolean <p>
     * 设置方法执行是否成功 <p>
     * 默认：自动判定 <p>
     * <p>
     * 日志记录的方法执行是否成功： <p>
     * 1.注解使用时，给success属性设置SpEL表达式。（优先级高） <p>
     * 2.由方法的返回结果自动判定。（优先级低） <p>
     * <p>
     * 方法的返回结果会如何自动判定？ <p>
     * 1.当方法未抛出异常时，则认为方法执行成功；反之则认为执行失败 <p>
     *
     * @return SpEL表达式预期返回Boolean
     */
    String success() default "";


    /**
     * 可选  , 值类型：boolean <p>
     * <p>
     * 是否在执行方法前执行解析 <p>
     * true: 在执行方法前执行解析 <p>
     * false: 在执行方法后执行解析 <p>
     * 默认值为：false <p>
     *
     * @return true/false
     */
    boolean loggingBefore() default false;

}
