package cn.vious.starter.logging.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import cn.vious.starter.logging.prop.ViousLoggingProperties;
import cn.vious.starter.logging.model.LogDTO;
import cn.vious.starter.logging.service.LoggingConsumer;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 默认的日志结果处理<p>
 *
 * @author lcok
 * @date 2022/8/5 14:47
 */
@Slf4j
public class LoggingConsumerHttp implements LoggingConsumer {

    @Resource
    private ViousLoggingProperties loggingProperties;

    @Override
    public void accept(LogDTO logDTO) {
        if (logDTO != null) {
            String logStr = JSONUtil.toJsonStr(logDTO);
            log.debug("日志提交至 [{}] <=  {}", loggingProperties.getToHttp().getUrl(), logStr);
            String logResult = HttpRequest.post(loggingProperties.getToHttp().getUrl()).body(logStr).execute().body();
            log.debug("日志提交返回结果 =>  {}", logResult);
        } else {
            log.warn("[ vious logging ] : 未取到日志");
        }
    }

    @Override
    public String getName() {
        return "LoggingConsumerHttp";
    }
}
