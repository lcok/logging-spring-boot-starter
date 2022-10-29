package cn.vious.starter.logging.aop;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.vious.starter.logging.annotation.Logging;
import cn.vious.starter.logging.context.BeanResolverRegistrar;
import cn.vious.starter.logging.context.LoggingContext;
import cn.vious.starter.logging.model.LogDTO;
import cn.vious.starter.logging.model.base.HttpLogDTO;
import cn.vious.starter.logging.prop.ViousLoggingProperties;
import cn.vious.starter.logging.service.LoggingConsumer;
import cn.vious.starter.logging.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志切面
 *
 * @author lcok
 * @date 2022/8/3 11:01
 */
@Slf4j
@Aspect
@Component
public class ViousLoggingAspect {

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");

    @Resource
    private List<LoggingConsumer> loggingConsumerList;

    @Resource(name = ViousLoggingProperties.LOGGING_ASYNC_THREAD_POOL_NAME)
    private ThreadPoolTaskExecutor executor;

    @Resource
    private BeanResolverRegistrar beanResolverRegistrar;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(cn.vious.starter.logging.annotation.Logging) || @annotation(cn.vious.starter.logging.annotation.LoggingList)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        Method method;
        // 日志注解
        Logging[] annotations;
        // 待记录日志
        Map<Logging, LogDTO> logMap = new LinkedHashMap<>();
        long startTs = System.currentTimeMillis();

        // 解析日志注解
        method = getMethod(joinPoint);
        annotations = parseAnnotations(method);
        if (annotations.length == 0) {
            // 日志注解解析失败，中断后续逻辑，执行原方法后返回
            return joinPoint.proceed();
        }

        // http request
        HttpLogDTO httpLogDTO = generateRequestLog();

        try {
            // before
            logMap.putAll(collectLogs(joinPoint, annotations, method, true));
            // 原方法执行
            result = joinPoint.proceed();
            // after
            LoggingContext.put(LoggingContext.KEY_OF_METHOD_RESULT, result);
            logMap.putAll(collectLogs(joinPoint, annotations, method, false));
            completeLogs(logMap, method, httpLogDTO, startTs, result, null);
        } catch (Throwable throwable) {
            // 原方法执行抛出异常
            LoggingContext.put(LoggingContext.KEY_OF_METHOD_EXCEPTION, throwable.getMessage());
            logMap.putAll(collectLogs(joinPoint, annotations, method, false));
            completeLogs(logMap, method, httpLogDTO, startTs, null, throwable);
            // 抛出原方法抛出的异常
            throw throwable;
        } finally {
            // 开始记录日志
            loggingConsume(logMap);
            // 清除上下文
            LoggingContext.clear();
        }

        return result;
    }

    private void loggingConsume(Map<Logging, LogDTO> logMap) {
        logMap.values().forEach(log -> executor.execute(() -> {
            for (LoggingConsumer loggingConsumer : loggingConsumerList) {
                try {
                    loggingConsumer.accept(log);
                } catch (Exception e) {
                    System.out.println(StrUtil.format("[ vious logging ] : consumer [{}]  处理日志发生异常! 日志内容: {}",
                            loggingConsumer.getName(),
                            JSONUtil.toJsonStr(log)));
                    e.printStackTrace();
                }
            }
        }));
    }

    private HttpLogDTO generateRequestLog() {
        String reqStr = null;
        try {
            HttpLogDTO httpLogDTO = new HttpLogDTO();
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            assert requestAttributes != null;
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String reqMethod = request.getMethod();
            httpLogDTO.setRequestMethod(reqMethod);
            httpLogDTO.setRequestIp(RequestUtil.getIp(request));
            httpLogDTO.setRequestUri(request.getRequestURI());

            switch (reqMethod) {
                case "GET":
                case "DELETE":
                    reqStr = RequestUtil.paramsToJsonStr(request.getQueryString());
                    break;
                case "POST":
                case "PUT":
                    if (RequestUtil.isJson(request)) {
                        byte[] contentAsByteArray = ((ContentCachingRequestWrapper) request).getContentAsByteArray();
                        reqStr = new String(contentAsByteArray, StandardCharsets.UTF_8);
                    }
                    break;
                default:
                    log.info("[ vious logging ] : 未提取到HTTP请求参数");
            }
            httpLogDTO.setRequestContent(reqStr);
            return httpLogDTO;
        } catch (Exception e) {
            log.warn("[ vious logging ] : ", e);
        }
        return null;
    }

    private void completeLogs(Map<? extends Logging, ? extends LogDTO> logMap, Method method, HttpLogDTO httpLogDTO, long methodStartTs, Object methodResult, Throwable methodException) {
        long executeTs = System.currentTimeMillis() - methodStartTs;
        // 日志记录的方法执行是否成功：
        // 1.注解使用时，给success属性设置SpEL表达式。（优先级高）
        // 2.由方法的返回结果自动判定。（优先级低）

        // 方法的返回结果会如何自动判定？
        // 1.当方法未抛出异常时，则认为方法执行成功；反之则认为执行失败
        boolean methodResultStatus = methodException != null;
        for (Map.Entry<? extends Logging, ? extends LogDTO> entry : logMap.entrySet()) {
            LogDTO value = entry.getValue();
            // 如已经由SpEL赋值，则无需使用自动判定的结果
            value.setSuccess(value.getSuccess() == null ? methodResultStatus : value.getSuccess());
            value.setResult(methodResult != null ? JSONUtil.toJsonStr(methodResult) : null);
            value.setException(methodException != null ? ExceptionUtil.stacktraceToString(methodException, 1_0000) : null);
            value.setExecuteTs(executeTs);
            value.setHttpLog(httpLogDTO);
        }
    }


    private Map<? extends Logging, ? extends LogDTO> collectLogs(ProceedingJoinPoint joinPoint, Logging[] annotations, Method method, boolean isCollectBeforeProcess) {
        Map<Logging, LogDTO> rtnMap = new LinkedHashMap<>();
        try {
            for (Logging annotation : annotations) {
                if (isCollectBeforeProcess == annotation.loggingBefore()) {
                    LogDTO log = parseLog(joinPoint, method, annotation);
                    if (log != null) {
                        rtnMap.put(annotation, log);
                    }
                }
            }
        } catch (Throwable throwable) {
            log.warn("[ vious logging ] : ", throwable);
        }
        return rtnMap;
    }

    /**
     * 解析日志
     *
     * @param joinPoint  切入点
     * @param method     method
     * @param annotation 日志注解
     * @return 日志实体
     */
    private LogDTO parseLog(ProceedingJoinPoint joinPoint, Method method, Logging annotation) {
        String conditionSpEL = annotation.condition();
        String successSpEL = annotation.success();
        String optModuleSpEL = annotation.optModule();
        String optTypeSpEL = annotation.optType();
        String optDescSpEL = annotation.optDesc();
        try {
            Object[] arguments = joinPoint.getArgs();
            String[] params = discoverer.getParameterNames(method);
            StandardEvaluationContext context = LoggingContext.getContext();
            beanResolverRegistrar.register(context);
            if (params != null) {
                for (int len = 0; len < params.length; len++) {
                    context.setVariable(params[len], arguments[len]);
                }
            }
            LogDTO logDTO = new LogDTO();

            if (StrUtil.isNotBlank(conditionSpEL)) {
                if (!parseSpEL2Bool(context, conditionSpEL)) {
                    return null;
                }
            }

            if (StrUtil.isNotBlank(successSpEL)) {
                logDTO.setSuccess(parseSpEL2Bool(context, successSpEL));
            }


            if (StrUtil.isNotBlank(optModuleSpEL)) {
                logDTO.setOptModule(parseTemplatedSpEL(context, optModuleSpEL));
            }

            if (StrUtil.isNotBlank(optTypeSpEL)) {
                logDTO.setOptType(parseTemplatedSpEL(context, optTypeSpEL));
            }

            if (StrUtil.isNotBlank(optDescSpEL)) {
                logDTO.setOptDesc(parseTemplatedSpEL(context, optDescSpEL));
            }

            return logDTO;
        } catch (Exception e) {
            log.warn("[ vious logging ] : ", e);
            return null;
        }
    }


    /**
     * 解析日志注解
     *
     * @param method method
     * @return 解析出的日志注解数组
     */
    private Logging[] parseAnnotations(Method method) {
        try {
            return method == null ? new Logging[0] : method.getAnnotationsByType(Logging.class);
        } catch (Throwable throwable) {
            log.warn("[ vious logging ] : ", throwable);
            return new Logging[0];
        }
    }


    private Method getMethod(JoinPoint joinPoint) {
        try {
            Signature signature = joinPoint.getSignature();
            MethodSignature ms = (MethodSignature) signature;
            Object target = joinPoint.getTarget();
            return target.getClass().getMethod(ms.getName(), ms.getParameterTypes());
        } catch (NoSuchMethodException e) {
            log.warn("[ vious logging ] : ", e);
            return null;
        }
    }


    private String parseTemplatedSpEL(StandardEvaluationContext context, String templatedEL) {
        if (templatedEL.contains("{{")) {
            Matcher matcher = TEMPLATE_PATTERN.matcher(templatedEL);
            StringBuffer parsedStr = new StringBuffer();
            while (matcher.find()) {
                String express = matcher.group(1);
                express = parseSpEL2String(context, express);
                matcher.appendReplacement(parsedStr, Matcher.quoteReplacement(express));
            }
            matcher.appendTail(parsedStr);
            return parsedStr.toString();
        }
        return templatedEL;
    }


    private String parseSpEL2String(StandardEvaluationContext context, String el) {
        try {
            Expression expression = parser.parseExpression(el);
            Object obj = expression.getValue(context, Object.class);
            if (obj == null) {
                return StrUtil.EMPTY;
            }
            return obj instanceof String ? (String) obj : JSONUtil.toJsonStr(obj);
        } catch (Exception e) {
            log.warn("[ vious logging ] : ", e);
        }
        return StrUtil.EMPTY;
    }

    private boolean parseSpEL2Bool(StandardEvaluationContext context, String el) {
        try {
            Expression expression = parser.parseExpression(el);
            return Boolean.TRUE.equals(expression.getValue(context, Boolean.class));
        } catch (Exception e) {
            log.warn("[ vious logging ] : ", e);
        }
        return false;
    }

}
