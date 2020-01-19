package com.cositea.demo.model;
import java.io.Serializable;


/**
 * TODO:
 * 
 * @author liuyicong
 * @Date 2018年12月8日 下午6:44:22
 */
public class ReturnBody<T> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5128384002751782705L;


    private int code;

    private String msg;

    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <F> ReturnBody<F> fail(int code, String msg) {
        ReturnBody<F> returnBody = new ReturnBody<>();
        returnBody.setCode(code);
        returnBody.setMsg(msg);
        return returnBody;
    }
    
    public static <D> ReturnBody<D> success(D result) {
        ReturnBody<D> returnBody = new ReturnBody<>();
        returnBody.setCode(ErrorCode.OK.getCode());
        returnBody.setMsg(ErrorCode.OK.getMsg());
        returnBody.setData(result);
        return returnBody;
    }

    public static ReturnBody<?> success() {
        ReturnBody<?> returnBody = new ReturnBody<>();
        returnBody.setCode(ErrorCode.OK.getCode());
        returnBody.setMsg(ErrorCode.OK.getMsg());
        return returnBody;
    }
    
    public static <F> ReturnBody<F> success(int code, String msg) {
        ReturnBody<F> returnBody = new ReturnBody<>();
        returnBody.setCode(code);
        returnBody.setMsg(msg);
        return returnBody;
    }

}