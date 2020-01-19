package com.cositea.demo.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.cositea.demo.controller.timeout.TimeOutWork;
import com.cositea.demo.model.ErrorCode;
import com.cositea.demo.model.Parameters;
import com.cositea.demo.model.ReturnBody;
import com.cositea.demo.util.Constants;
import com.cositea.gateway.message.entity.DeviceParameters;
import com.cositea.gateway.message.entity.DeviceUpdateTime;
import com.cositea.gateway.message.entity.SendDevMessage;
import com.cositea.gateway.message.type.PassthrougData;
import com.cositea.gateway.service.CositeaMethod;
import com.cositea.gateway.util.ByteOps;

import io.netty.channel.ChannelHandlerContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="设备指令测试接口")
@RestController
@RequestMapping(value="DeviceTest")  
public class DeviceGWTestController {

 private static final Logger log = LoggerFactory.getLogger(GWTestController.class);
    //实例化方法
    CositeaMethod cositeaMethod=CositeaMethod.getInstance();
    
    
    @ApiOperation(value="更新设备时间",  response =  ReturnBody.class,
            notes=" 1）连接设备（可先发送查询连接状态指令，如设备已经连接则不需该操作） 参考GWTestController类connectDevShort方法 \r\n\n  "
                    + " 2）发送更新设备时间指令      \r\n\n "
                    + " 3）断开连接（如有其它操作可不进行该操作） 参考GWTestController类disconnectDevice方法 \r\n\n "
                    + " mac 操作网关的地址 ，devMac：需要操作的设备设备 ,timeStamp：需要更新的UTC时间戳（秒数）为null默认当前系统时间 \r\n\n " )
    @PostMapping("deviceUpdateTime")
    public DeferredResult<Object>  deviceUpdateTime(String mac,String devMac,Long timeStamp) {
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(mac)||
                StringUtils.isBlank(devMac)){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+mac);//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        PassthrougData<DeviceUpdateTime>  pdDut =new PassthrougData<DeviceUpdateTime>();//透传数据--设备时间校准
        pdDut.setMac(mac);
        pdDut.setDevMac(devMac);
        pdDut.setDataCommand("0");//0为透传设备指令
        
        DeviceUpdateTime but=new DeviceUpdateTime();
        
        if(null!=timeStamp){//设置要更新的时间
            but.setTimeStamp(timeStamp);//可以不设置 默认当前UTC时间戳
        }
        pdDut.setDeviceDataType(but.getType());//设置设备指令类型
        pdDut.setDeviceData(but);//设置透指令传数据
        
        String responseKey=mac+"_"+pdDut.getType()+"_"+pdDut.getDataTimeStamp();
        System.out.println("------------"+responseKey);
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(pdDut);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    
    @ApiOperation(value="发送消息到设备",  response =  ReturnBody.class,
            notes=" 1）连接设备（可先发送查询连接状态指令，如设备已经连接则不需该操作） 参考GWTestController类connectDevShort方法 \r\n\n  "
                    + " 2）发送短消息指令      \r\n\n "
                    + " 3）断开连接（如有其它操作可不进行该操作） 参考GWTestController类disconnectDevice方法  \r\n\n "
                    + " mac 操作网关的地址 ，devMac：需要操作的设备设备 ,msg：消息内容(字符串)  \r\n\n "
                    + " sendType 消息类型   7: others(默认7   其他)   \r\n\n" )
    @PostMapping("sendDevMessage")
    public DeferredResult<Object>  sendDevMessage(String mac,String devMac,String msg,Integer sendType) {
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(mac)||
                StringUtils.isBlank(devMac)||
                StringUtils.isBlank(msg)){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+mac);//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        PassthrougData<SendDevMessage>  pdSdm =new PassthrougData<SendDevMessage>();//透传数据--设备时间校准
        pdSdm.setMac(mac);
        pdSdm.setDevMac(devMac);
        pdSdm.setDataCommand("0");//0为透传设备指令
        
        SendDevMessage sdm=new SendDevMessage();
        
        if(null!=sendType){//设置发送类型 默认 7 不设置 否则不成功
            sdm.setSendType(sendType);
        }
        sdm.setMsg(msg);
        pdSdm.setDeviceDataType(sdm.getType());//设置设备指令类型
        pdSdm.setDeviceData(sdm);//设置透指令传数据
        
        String responseKey=mac+"_"+pdSdm.getType()+"_"+pdSdm.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(pdSdm);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
//    Set the parameters
    @ApiOperation(value="设置设备参数",  response =  ReturnBody.class,
            notes=" 1）连接设备（可先发送查询连接状态指令，如设备已经连接则不需该操作） 参考GWTestController类connectDevShort方法 \r\n\n  "
                    + " 2）设置设备参数     \r\n\n "
                    + " 3）断开连接（如有其它操作可不进行该操作） 参考GWTestController类disconnectDevice方法  \r\n\n "
                    + " mac 操作网关的地址 ，devMac：需要操作的设备设备 , 其他参数可以为空，可以一个或多个同时设置 参数解析 见实体类  \r\n\n" )
    @PostMapping("setDeviceParameters")
    public DeferredResult<Object>  setDeviceParameters(Parameters params) {
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(params.getMac())||
                StringUtils.isBlank(params.getDevMac())){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+params.getMac());//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        PassthrougData<DeviceParameters>  pdDp =new PassthrougData<DeviceParameters>();//透传数据--设备时间校准
        pdDp.setMac(params.getMac());
        pdDp.setDevMac(params.getDevMac());
        pdDp.setDataCommand("0");//0为透传设备指令
        
        DeviceParameters dp=new DeviceParameters();
        
        if(params.getTimeFormat()!=null){//12/24 小时制
        	dp.setTimeFormat(params.getTimeFormat());
        }
		if(params.getUnit()!=null){//公制/英制
		    dp.setUnit(params.getUnit());   	
		}
		if(params.getHeartRateInterval()!=null){//心率监控间隔
			dp.setHeartRateInterval(params.getHeartRateInterval());
		}
		if(params.getFatigueMonitor()!=null){//疲劳度监控开关
			dp.setFatigueMonitor(params.getFatigueMonitor());
		}
		if(params.getHeartRateAuto()!=null){//心率自动监控开关
			dp.setHeartRateAuto(params.getHeartRateAuto());
		}
		if(params.getDateFormat()!=null){//日期显示格式
			dp.setDateFormat(params.getDateFormat());
		}
		if(params.getLanguage()!=null){//设备语言版本
			dp.setLanguage(params.getLanguage());
		}
        
		pdDp.setDeviceDataType(dp.getType());//设置设备指令类型
		pdDp.setDeviceData(dp);//设置透指令传数据
        
        String responseKey=params.getMac()+"_"+pdDp.getType()+"_"+pdDp.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(pdDp);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
}
