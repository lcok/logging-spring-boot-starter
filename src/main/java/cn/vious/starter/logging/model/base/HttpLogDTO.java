package cn.vious.starter.logging.model.base;

import lombok.Data;

import java.io.Serializable;

/**
 * Http 请求信息记录
 *
 * @author lcok
 * @date 2022/8/4 11:32
 */
@Data
public class HttpLogDTO implements Serializable {
    private static final long serialVersionUID = -7982981359101043941L;
    private String requestIp;
    private String requestMethod;
    private String requestUri;
    private String requestContent;
}
