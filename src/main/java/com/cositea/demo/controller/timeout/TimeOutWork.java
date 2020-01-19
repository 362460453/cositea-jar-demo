package com.cositea.demo.controller.timeout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.cositea.demo.model.ErrorCode;
import com.cositea.demo.model.ReturnBody;
import com.cositea.demo.util.Constants;

/**
 * DeferredResult 超时返回类
 * @author lyc
 *
 */
public class TimeOutWork implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(TimeOutWork.class);
    
    
    private  String responseMapKey;
    
    /**
     * 实例化
     * @param responseMapKey
     */
    public TimeOutWork(String responseMapKey) {
        this.responseMapKey = responseMapKey;
    }

    @Override
    public void run() {
        log.info("请求指令超时key:"+responseMapKey);
        DeferredResult<Object> deferredResult=Constants.responseMap.get(responseMapKey);
        deferredResult.setResult(ReturnBody.fail(ErrorCode.REQUEST_TIMEOUT.getCode(), ErrorCode.REQUEST_TIMEOUT.getMsg()));
        Constants.responseMap.remove(responseMapKey);
    }


}
