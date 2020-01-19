package com.cositea.demo.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.context.request.async.DeferredResult;

import io.netty.channel.ChannelHandlerContext;

public class Constants {

    /**
     * netty ChannelHandlerContext缓存  key：tcp_{mac}   
     */
    public static  Map<String, ChannelHandlerContext> ctxMap=new HashMap<String, ChannelHandlerContext>();  
    
    /**
     * netty mac 缓存 key {channelId} 可以用nosql
     */
    public static  Map<String,String> macMap=new HashMap<String,String>();  
    
    /**
     * 发请求的DeferredResult  key {mac}_{dateTime}   
     */
    public static  Map<String,DeferredResult<Object>> responseMap=new HashMap<String,DeferredResult<Object>>();  
    /**
     * DeferredResult超时时间
     */
    public static final Long DeferredResultTimeOut=15000l; 
}
