package cn.vious.starter.logging.filter;

import cn.vious.starter.logging.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 对于json格式的请求，包装为ContentCachingRequestWrapper，用于下游重复读取请求体
 *
 * @author lcok
 * @date 2022/8/10 10:47
 */
@Slf4j
@Order(1)
@WebFilter(urlPatterns = "/*")
public class HttpServletRequestContentCachingLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("[ vious logging ] : 加载 HttpServletRequestContentCachingFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            if (RequestUtil.isJson(req) && !(req instanceof ContentCachingRequestWrapper)) {
                HttpServletRequest requestWrapper = new ContentCachingRequestWrapper(req);
                chain.doFilter(requestWrapper, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
