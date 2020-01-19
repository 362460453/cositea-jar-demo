package com.cositea.demo.controller;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.cositea.demo.controller.timeout.TimeOutWork;
import com.cositea.demo.model.ErrorCode;
import com.cositea.demo.model.ReturnBody;
import com.cositea.demo.util.Constants;
import com.cositea.gateway.message.type.CloseScanning;
import com.cositea.gateway.message.type.ConnectDevLong;
import com.cositea.gateway.message.type.ConnectDevShort;
import com.cositea.gateway.message.type.DisconnectDevice;
import com.cositea.gateway.message.type.OpenScanning;
import com.cositea.gateway.message.type.QueryDeviceStatus;
import com.cositea.gateway.message.type.QueryDevices;
import com.cositea.gateway.message.type.QueryGwVersion;
import com.cositea.gateway.message.type.UpdateTime;
import com.cositea.gateway.message.type.UpgradeDevice;
import com.cositea.gateway.message.type.UpgradeGateway;
import com.cositea.gateway.service.CositeaMethod;
import com.cositea.gateway.util.ByteOps;

import io.netty.channel.ChannelHandlerContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="网关指令测试接口")
@RestController
@RequestMapping(value="gwTest")  
public class GWTestController {
    
    private static final Logger log = LoggerFactory.getLogger(GWTestController.class);
    
    //实例化方法
    CositeaMethod cositeaMethod=CositeaMethod.getInstance();
    
    
    @ApiOperation(value="网关时间更新",  response =  ReturnBody.class,
            notes="填入要更新的网关MAC，  timeStamp 需要更新的UTC时间戳（秒数）为null默认当前系统时间  \r\n\n " )
    @PostMapping("updateTime")
    public DeferredResult<Object>  updateTime(String mac,Long timeStamp) {
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(mac)){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+mac);//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        UpdateTime up =new UpdateTime();
        if(null!=timeStamp){//设置要更新的时间
        	up.setTimeStamp(timeStamp);//可以不设置 默认当前CCC
        }
        Long dataTimeStamp=new Date().getTime()/1000;//时间戳秒   注意是秒   
//        up.setDataTimeStamp(dataTimeStamp);//数据时间戳                        可以不设置 默认当前UTC时间戳
        up.setDataCommand("1");// 数据命令位 ，0表示数据，1表示命令                可以不设置 默认1命令
        up.setReply("0");//应答标志位 1为应答 0发送                                                        可以不设置 默认 0 发送
        up.setIsReply("1");//是否需要应答 0不需要 1需要                                              可以不设置  默认1需要应答
        up.setDeviceType("0000");//设备类型                                                                可以不设置 默认0000 网关
        up.setMac(mac);
        System.out.println(up.toString());
        String responseKey=mac+"_"+up.getType()+"_"+up.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
//      String  sendMsg=cositeaMethod.OutDataConversion(up,"00");//非定制的用户不传协议版本号 ，不然引起数据错乱
        String  sendMsg=cositeaMethod.OutDataConversion(up);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
            ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    
    
    @ApiOperation(value="连接指定设备(短连接)",  response =  ReturnBody.class,
            notes="如果设备与云端有超过30秒时间没有数据交互，则蓝牙网关主动断开与设备的连接。 \r\n\n "
            		+ "mac 操作网关的地址 ，devMac：需要连接的设备设备  \r\n\n " )
    @PostMapping("connectDevShort")
    public DeferredResult<Object>  connectDevShort(String mac,String devMac) {
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
        ConnectDevShort cds =new ConnectDevShort();
        cds.setMac(mac);
        cds.setDevMac(devMac);
        String responseKey=mac+"_"+cds.getType()+"_"+cds.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(cds);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    @ApiOperation(value="连接指定设备(长连接)",  response =  ReturnBody.class,
            notes="测试的时候才用该接口 \r\n\n "
            		+ "mac 操作网关的地址 ，devMac：需要连接的设备设备  \r\n\n " )
    @PostMapping("connectDevLong")
    public DeferredResult<Object>  connectDevLong(String mac,String devMac) {
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
        ConnectDevLong cdl =new ConnectDevLong();
        cdl.setMac(mac);
        cdl.setDevMac(devMac);
        String responseKey=mac+"_"+cdl.getType()+"_"+cdl.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(cdl);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    
    
    
    @ApiOperation(value="与指定设备断开连接",  response =  ReturnBody.class,
            notes="与指定设备断开连接。\r\n\n "
                    + "mac 操作网关的地址 ，devMac 需要断开的设备设备  \r\n\n " )
    @PostMapping("disconnectDevice")
    public DeferredResult<Object>  disconnectDevice(String mac,String devMac) {
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
        DisconnectDevice dd =new DisconnectDevice();
        dd.setMac(mac);
        dd.setDevMac(devMac);
        String responseKey=mac+"_"+dd.getType()+"_"+dd.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(dd);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    @ApiOperation(value="查询指定设备的连接状态",  response =  ReturnBody.class,
            notes="查询指定设备的连接状态。\r\n\n "
                    + "mac 操作网关的地址 ，devMac 需要查询的设备设备  \r\n\n " )
    @PostMapping("queryDeviceStatus")
    public DeferredResult<Object>  queryDeviceStatus(String mac,String devMac) {
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
        QueryDeviceStatus qds =new QueryDeviceStatus();
        qds.setMac(mac);
        qds.setDevMac(devMac);
        String responseKey=mac+"_"+qds.getType()+"_"+qds.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(qds);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    @ApiOperation(value="查询当前已连接设备",  response =  ReturnBody.class,
            notes="查询当前已连接设备。\r\n\n "
                    + "mac 操作网关的地址   \r\n\n " )
    @PostMapping("queryDevices")
    public DeferredResult<Object> queryDevices(String mac) {
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(mac) ){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+mac);//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        QueryDevices qd =new QueryDevices();
        qd.setMac(mac);
        String responseKey=mac+"_"+qd.getType()+"_"+qd.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(qd);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    @ApiOperation(value="开始扫描",  response =  ReturnBody.class,
            notes="开始扫描。\r\n\n "
                    + "mac 操作网关的地址   \r\n\n " )
    @PostMapping("openScanning")
    public DeferredResult<Object>  openScanning(String mac) {
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(mac) ){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+mac);//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        OpenScanning os =new OpenScanning();
        os.setMac(mac);
        String responseKey=mac+"_"+os.getType()+"_"+os.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(os);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    @ApiOperation(value="停止扫描",  response =  ReturnBody.class,
            notes="停止扫描。\r\n\n "
                    + "mac 操作网关的地址   \r\n\n " )
    @PostMapping("closeScanning")
    public DeferredResult<Object>  closeScanning(String mac) {
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(mac) ){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+mac);//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        CloseScanning cs =new CloseScanning();
        cs.setMac(mac);
        String responseKey=mac+"_"+cs.getType()+"_"+cs.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(cs);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    @ApiOperation(value="查询网关固件版本",  response =  ReturnBody.class,
            notes="查询网关固件版本。\r\n\n "
                    + "mac 操作网关的地址   \r\n\n " )
    @PostMapping("queryGwVersion")
    public DeferredResult<Object>  queryGwVersion(String mac) {
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(mac) ){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+mac);//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        QueryGwVersion qfv =new QueryGwVersion();
        qfv.setMac(mac);
        String responseKey=mac+"_"+qfv.getType()+"_"+qfv.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(qfv);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    @ApiOperation(value="发起网关固件升级",  response =  ReturnBody.class,
            notes="发起网关固件升级 \r\n\n "
                    + "mac 操作网关的地址   port服务器应用访问端口号 (提供给网关下载文件)  "
                    + "versionType  固件类型  0、APP  1、bootloader  2、soft device（协议栈，如蓝牙协议栈）"
                    + "filePath 文件路径"
                    + "fileName 固件名称 "
                    + "version 版本号    \r\n\n" )
    @PostMapping("upgradeGateway")
    public DeferredResult<Object>  upgradeGateway(String mac,Integer port,Integer version ,Integer versionType,String filePath,String fileName) {
        //开发时，版本信息自己存数据库查询
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(mac) ||null==version||null==port ){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+mac);//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        UpgradeGateway ug =new UpgradeGateway();
        ug.setMac(mac);
        ug.setPort(port);
        ug.setVersion(version);
        ug.setVersionType(versionType);
        ug.setFilePath(filePath);//文件路径 开发时自己读取
        ug.setFileName(fileName);//文件名称 开发时自己读取    
        String responseKey=mac+"_"+ug.getType()+"_"+ug.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(ug);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        //发送后网关会来服务器下载固件,请提供固件下载接口. 
        // TCP设置的地址为 http://bracelet.cositea.com 
        // 端口号为 8899
        // 固件文件名称： gateway_app_v1.00.bin
        // 下载地址为：   http://bracelet.cositea.com:8899/downLoad/version/?filename=gateway_app_v1.00.bin
        ctx.channel().writeAndFlush(re);//发送
        return  result;
    }
    
    @ApiOperation(value="发起设备固件升级",  response =  ReturnBody.class,
    notes="发起设备固件升级    (先查询设备版本 与服务器比对后 需要升级则 调用改接口)\r\n\n "
            + "mac 操作网关的地址     port服务器应用访问端口号 (提供给网关下载文件)   devMac升级的设备mac  \r\n\n" )
    @PostMapping("upgradeDevice")
    public DeferredResult<Object>  upgradeDevice(String mac,Integer port,String devMac,String fileName) {
        //开发时，版本信息自己存数据库查询
        DeferredResult<Object> result = new DeferredResult<Object>(Constants.DeferredResultTimeOut);//设置超时
        if(StringUtils.isBlank(mac)||StringUtils.isBlank(devMac)||null==port ){
            result.setResult(ReturnBody.fail(ErrorCode.AUTHOR_FAIL.getCode(), ErrorCode.AUTHOR_FAIL.getMsg())) ;
           return result;
        }
        ChannelHandlerContext ctx=Constants.ctxMap.get("tcp_"+mac);//查询设备是否在线
        if(ctx==null){
            result.setResult(ReturnBody.fail(ErrorCode.DEVICE_OFFLINE.getCode(), ErrorCode.DEVICE_OFFLINE.getMsg())) ;
            return result;
        }
        //读取文件
        UpgradeDevice ud =new UpgradeDevice();
        ud.setMac(mac);
        ud.setPort(port);
        ud.setDevMac(devMac);
        ud.setFileName(fileName);//文件名称 开发时自己读取
        String responseKey=mac+"_"+ud.getType()+"_"+ud.getDataTimeStamp();
        Constants.responseMap.put(responseKey, result);//把请求放入缓存 可以用nosql(redis等)
        result.onTimeout(new TimeOutWork(responseKey));//超时调用方法
        String  sendMsg=cositeaMethod.OutDataConversion(ud);//普通用户调用，数据转换
        log.info("转换后数据:"+sendMsg);
        byte[] re = ByteOps.hexStringToBytes(sendMsg);
        ctx.channel().writeAndFlush(re);//发送
        //发送后网关会来服务器下载固件,请提供固件下载接口.  下载地址不设置应用名
        // TCP设置的地址为 http://bracelet.cositea.com 
        // 端口号为 8899
        // 固件文件名称： gateway_bracelet_v1.00.bin
        // 下载地址为：   http://bracelet.cositea.com:8899/downLoad/version/?filename=gateway_bracelet_v1.00.bin
        return  result;
    }
}
