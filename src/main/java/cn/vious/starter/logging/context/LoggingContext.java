package cn.vious.starter.logging.context;

import org.springframework.core.NamedThreadLocal;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 日志上下文
 *
 * @author lcok
 * @date 2022/8/3 14:58
 */
public class LoggingContext {

    public static final String KEY_OF_METHOD_RESULT = "_RESULT_";

    public static final String KEY_OF_METHOD_EXCEPTION = "_EXCEPTION_";

    private static final ThreadLocal<StandardEvaluationContext> CONTEXT_THREAD_LOCAL = new NamedThreadLocal<>("ThreadLocal StandardEvaluationContext");

    public static StandardEvaluationContext getContext() {
        return CONTEXT_THREAD_LOCAL.get() == null ? new StandardEvaluationContext() : CONTEXT_THREAD_LOCAL.get();
    }

    public static void put(String key, Object value) {
        StandardEvaluationContext context = getContext();
        context.setVariable(key, value);
        CONTEXT_THREAD_LOCAL.set(context);
    }

    public static void clear() {
        CONTEXT_THREAD_LOCAL.remove();
    }

}
