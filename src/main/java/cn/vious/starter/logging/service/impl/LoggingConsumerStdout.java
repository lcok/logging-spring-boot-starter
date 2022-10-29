package cn.vious.starter.logging.service.impl;

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
public class LoggingConsumerStdout implements LoggingConsumer {

    @Resource
    private ViousLoggingProperties loggingProperties;

    @Override
    public void accept(LogDTO logDTO) {
        if (logDTO != null) {
            log.info(JSONUtil.toJsonStr(logDTO));
        } else {
            log.warn("[ vious logging ] : 未取到日志");
        }
    }

    @Override
    public String getName() {
        return "LoggingConsumerStdout";
    }
}
