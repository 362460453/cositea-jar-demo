package com.cositea.demo.model;

public enum ErrorCode {
    
      OK                            (0,     "成功！") 
     ,ACK_FAIL                            (100,     "操作失败！")
     ,AUTHOR_FAIL                            (101,     "请求参数不能为空！")
     ,DEVICE_OFFLINE                            (102,     "网关不在线")
     ,REQUEST_TIMEOUT                (103,       "请求超时,请检查设备是否在线！");
    
    
    private int code;
    private String msg;
    
    private ErrorCode(int c, String m) {
        this.code = c;
        this.msg = m;
    }
    
    public boolean isOk() {
        return (code == OK.code);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
