package cn.vious.starter.logging.model;

import cn.vious.starter.logging.model.base.HttpLogDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 日志DTO
 *
 * @author lcok
 * @date 2022/8/3 10:49
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LogDTO extends HttpLogDTO implements Serializable {
    private static final long serialVersionUID = -1563145395846700654L;

    /**
     * 操作模块 <p>
     */
    private String optModule;

    /**
     * 操作名称 <p>
     */
    private String optType;

    /**
     * 操作内容 <p>
     */
    private String optDesc;

    /**
     * 方法的执行结果。true:成功；false:失败
     */
    private Boolean success;

    /**
     * 方法的返回结果。如果无返回或抛出异常，则为空
     */
    private String result;

    /**
     * 方法的异常。如果方法未抛出异常，则为空
     */
    private String exception;

    /**
     * 方法的执行时长。单位：毫秒
     */
    private Long executeTs;

    /**
     * 设置http请求信息
     *
     * @param httpLog http请求信息
     */
    public void setHttpLog(HttpLogDTO httpLog) {
        if (httpLog == null) {
            return;
        }
        setRequestIp(httpLog.getRequestIp());
        setRequestMethod(httpLog.getRequestMethod());
        setRequestUri(httpLog.getRequestUri());
        setRequestContent(httpLog.getRequestContent());
    }
}
