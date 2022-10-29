package cn.vious.starter.logging.service;


import cn.vious.starter.logging.model.LogDTO;

import java.util.function.Consumer;

/**
 * 日志结果的消费者接口 <p>
 * 实现该接口并注入到容器，即可覆盖默认的消费者
 *
 * @author lcok
 * @date 2022/8/5 14:47
 */
public interface LoggingConsumer extends Consumer<LogDTO> {

    /**
     * 返回消费者名称
     *
     * @return 消费者名称
     */
   default String getName(){
       return "Default LoggingConsumer";
   }

}
