package com.cositea.demo.netty.handler;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import com.cositea.demo.model.BraceletData;
import com.cositea.demo.model.ErrorCode;
import com.cositea.demo.model.ReturnBody;
import com.cositea.demo.util.Constants;
import com.cositea.gateway.message.ErrorMsg;
import com.cositea.gateway.message.MessageParent;
import com.cositea.gateway.message.entity.DeviceData;
import com.cositea.gateway.message.type.DeviceAck;
import com.cositea.gateway.message.type.GatewayOnline;
import com.cositea.gateway.message.type.QueryDeviceStatus;
import com.cositea.gateway.message.type.QueryDevices;
import com.cositea.gateway.message.type.QueryGwVersion;
import com.cositea.gateway.message.type.ReportedData;
import com.cositea.gateway.service.CositeaMethod;
import com.cositea.gateway.util.ByteOps;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * TODO: 接收处理示例
 * 
 * @author liuyicong
 * @Date 2018年11月26日 上午10:07:46
 */
@Component
@Qualifier("serverHandler")
@Sharable
public class ServerHandler extends SimpleChannelInboundHandler<byte[]> {

    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);

  //实例化方法
    CositeaMethod cositeaMethod=CositeaMethod.getInstance();

    // 新建一个对象 存业务代码 --- ChannelHandlerContext
    public static Map<String, ChannelHandlerContext> map = new HashMap<String, ChannelHandlerContext>();

    /**
     * 接收数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] in) throws Exception {
        try {
            log.info("接收数据channel_Id : " + ctx.channel().id() + "");
            // byte[] re;
            String msg = ByteOps.bytesToHexString(in).replace(" ", "");
            log.info("接受的数据:" + msg);
            // 接到数据首先要去检查数据是否符合
            ErrorMsg errorMsg = cositeaMethod.CheckData(msg);
            if (errorMsg.getError() != 0) {
                // (如果接收做了粘包处理,可以无视)
                return;
                // 或者 强制踢出连接(如果接收做了粘包处理建议踢出)
                // ctx.close();
                // return;
            }
            // 将接收到数据转成对象
            MessageParent imp = cositeaMethod.InDataConversion(msg);
            log.info("接收的数据,接收数据的时间：" + imp.getDataTimeStamp() + ",状态：" + imp.getAckMsg() + ",MAC地址：" + imp.getMac()
                    + ",数据命令位：" + imp.getDataCommand() + ",是否是应答：" + imp.getReply() + "" + "是否需要应答：" + imp.getIsReply()
                    + ",异常：" + imp.getError() + ",设备类型：" + imp.getDeviceType() + ",数据类型：" + imp.getType());
//            String responseKey = mac + "_" + type + "_" + imp.getDataTimeStamp();
//            DeferredResult<Object> result = Constants.responseMap.get(responseKey);// 拿到请求缓存
            log.info(imp.toString());
            if (!imp.getError().equals("0")) {// 数据是否异常
                // 不正常的数据自己做业务处理
                return;
            }
            String type = imp.getType();
            String mac = imp.getMac();
            if (imp.getDataCommand().equals("1")) {// 1数据为命令(即 跟网关交互) 为0即是设备交互指令
                    if (imp.getReply().equals("0")) {// 网关发送的指令数据
                        switch (type) {
                            case "GatewayOnline": //网关上线通知
                                GatewayOnline gatewayOnline = (GatewayOnline) imp;
                                gatewayOnline.getDataTimeStamp();//上线时间戳自行转换处理
                                log.info("接收的GatewayOnline数据：" + gatewayOnline.toString());
                                // 把这个TCP socket连接放入缓存  
                                Constants.ctxMap.put("tcp_" + mac, ctx);
                                Constants.macMap.put(ctx.channel().id().asShortText(), mac);
                                //执行完后应答 
                                gatewayOnline.setReply("1");//应答标志位 1为应答 0发送                                                         
                                gatewayOnline.setIsReply("0");//是否需要应答 0不需要 1需要                                       
                                String  sendMsg=cositeaMethod.OutDataConversion(gatewayOnline);//普通用户调用，数据转换
                                log.info("转换后数据:"+sendMsg);
                                byte[] re = ByteOps.hexStringToBytes(sendMsg);
                                    ctx.channel().writeAndFlush(re);//发送
                                // 或者直接执行网关时间更新 参考GWTestController 即可
                                break;
                            case "ReportedData":// 设备数据上报
                                ReportedData reportedData = (ReportedData) imp;
                                log.info("接收的ReportedData数据：" + reportedData.toString());
                                List<DeviceData> deviceDataList=reportedData.getDeviceDataList();
                                if (deviceDataList.size()>0) {//设备数据List没有不做处理
                                	for (DeviceData deviceData : deviceDataList) {
    										log.info("接收的"+deviceData.getDevMac()+"原始数据:"+deviceData.toString());
    										switch (deviceData.getDeviceType()+""+deviceData.getDeviceModel()) {//如用户自己的设备，可以另行解析
                                                case "1111"://测试自定义数据
                                                  //如用户自己的设备，可以另行解析
                                                	log.info("接收"+deviceData.getDevMac()+"的自定义数据"+deviceData.getHexString());
                                                    break;
                                                default://默认汇诚和设备
                                                    BraceletData bd=new BraceletData();
                                                    bd.setDevMac(deviceData.getDevMac());
                                                    bd.setDeviceType(deviceData.getDeviceType());
                                                    bd.setDeviceModel(deviceData.getDeviceModel());
                                                    bd.setRssi(deviceData.getRssi());
                                                    String hexString=deviceData.getHexString();//b8广播的hexString
                                                    if(hexString.length()<=4){
                                                        log.info("接收"+deviceData.getDevMac()+"的非法数据"+deviceData.getHexString());
                                                    	 continue;
                                                    }
                                                    log.info("接收"+deviceData.getDevMac()+"的自定义数据"+deviceData.getHexString());
                                                    Integer datalength= hexString.length(); 
                                                    String crc = hexString.substring(datalength  - 2, datalength);//截取CRC
//                                                    bd.setCrc(crc);//异域
//                                                    String crcCherk=ByteOps.intToHexString(0xff&ByteOps.getXor(
//                                                            ByteOps.hexStringToBytes(hexString.substring(0, datalength-2))),2);//校验异域CRC
//                                                    if (!crc.equals(crcCherk)) {//判断CRC
//                                                        log.info("接收的"+deviceData.getDevMac()+"数据CRC校验错误:");
//                                                        continue;
//                                                    }
                                                    bd.setHeartate(Integer.parseInt(hexString.substring(0, 2), 16));
                                                    bd.setStep(Integer.parseInt(hexString.substring(6, 8) + hexString.substring(4, 6)
                                                            + hexString.substring(2, 4), 16));
                                                    if (!hexString.substring(8, 10).equals("FF")) {// 默认运动模式  关闭， 动静状态  默认无效
                                                        String bitString = ByteOps.hexStringTobit(hexString.substring(8, 10));
                                                        bd.setMovement(bitString.charAt(0) - '0'); //运动模式 
                                                        bd.setDynamic(bitString.charAt(1) - '0');   // 动静状态
                                                    }
                                                    if (!hexString.substring(10, 12).equals("FF")) {// 默认未检测
                                                        bd.setSleep(Integer.parseInt(hexString.substring(10, 12), 16));
                                                    }
                                                    bd.setDiastolic(Integer.parseInt(hexString.substring(12, 14), 16));//血压舒张压
                                                    bd.setSystolic(Integer.parseInt(hexString.substring(14, 16), 16));//血压收缩压
                                                    bd.setOxygen(Integer.parseInt(hexString.substring(16, 18), 16));// 血氧
                                                    bd.setHrv(Integer.parseInt(hexString.substring(18, 20), 16));//hrv
                                                    bd.setVersion(Integer.parseInt(hexString.substring(20, 22), 16));//软件版本
                                                    bd.setTimeStamp(
                                                            Long.parseLong(
                                                                    hexString.substring(28, 30) + hexString.substring(26, 28)
                                                                            + hexString.substring(24, 26) + hexString.substring(22, 24),
                                                                    16));//时间戳
                                                    bd.setStaticHeartate(Integer.parseInt(hexString.substring(30, 32), 16));
                                                    bd.setElectricity(Integer.parseInt(
                                                            hexString.substring(datalength - 4, datalength - 2), 16));//电量
                                                    //执行存数据操作， 数据上报比较频繁，建议存入nosql 内存数据库
                                                    log.info("接收:"+deviceData.getDevMac()+"的数据:"+bd.toString());
                                                    break;
                                            }
    								}
    							}
                                break;
                            default:
                                break;
                        }
                }else if (imp.getReply().equals("1")) {// 设备应答的数据
                        String responseKey = mac + "_" + type + "_" + imp.getDataTimeStamp();
                        DeferredResult<Object> result = Constants.responseMap.get(responseKey);// 拿到请求缓存
                        ReturnBody<?> rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(), ErrorCode.ACK_FAIL.getMsg());// 非成功就返回失败
                        switch (type) {
                            case "UpdateTime":// 网关时间更新应答
                                if (imp.getStatus() == 0) {// 设置成功
                                    rb = ReturnBody.success();
                                }
                                else {
                                    log.error(imp.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(),imp.getAckMsg());
                                }
                                break;
                            case "ConnectDevShort":// 连接指定设备应答
                                if (imp.getStatus() == 0) {// 连接成功
                                    rb = ReturnBody.success();
                                }
                                else {
                                    log.error(imp.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(),imp.getAckMsg());
                                }
                                break;
                            case "ConnectDevLong":// 连接指定设备应答
                                if (imp.getStatus() == 0) {// 连接成功
                                    rb = ReturnBody.success();
                                }
                                else {
                                    log.error(imp.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(),imp.getAckMsg());
                                }
                                break;
                                 
                                
                            case "DisconnectDevice":// 断开指定设备应答
                                if (imp.getStatus() == 0) {// 断开成功
                                    rb = ReturnBody.success();
                                }
                                else {
                                    log.error(imp.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(),imp.getAckMsg());
                                }
                                break;
                            case "QueryDeviceStatus":// 查询指定设备的连接状态 应答
                            	QueryDeviceStatus  qds =(QueryDeviceStatus)imp;
                                if (qds.getStatus() == 0) {// 连接状态
                                    rb = ReturnBody.success(ErrorCode.OK.getCode(),qds.getDevMac()+"连接状态");
                                }  else {// 断开状态
                                    log.error("断开状态:"+qds.getDevMac());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(),qds.getDevMac()+"断开状态");
                                }
                                break;
                            case "QueryDevices":// 查询当前已连接设备应答
                                QueryDevices qd = (QueryDevices) imp;
                                if (qd.getStatus() == 0) {// 查询成功
                                    rb = ReturnBody.success(qd.getDevMacList());// 返回List到前端
                                                                                // 根据自己需求返回对象也可以
                                }
                                else {// 查询失败
                                    log.error(qd.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(), qd.getAckMsg());
                                }
                                break;
                            case "OpenScanning":// 开启扫描应答
                                if (imp.getStatus() == 0) {// 开启成功
                                    rb = ReturnBody.success();
                                } else {// 失败
                                    log.error(imp.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(), imp.getAckMsg());
                                }
                                break;
                            case "CloseScanning":// 停止扫描应答
                                if (imp.getStatus() == 0) {// 成功
                                    rb = ReturnBody.success();
                                }else {// 失败
                                    log.error(imp.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(), imp.getAckMsg());
                                }
                                break;
                            case "QueryGwVersion":// 查询网关固件版本应答
                                QueryGwVersion qgv = (QueryGwVersion) imp;
                                if (qgv.getStatus() == 0) {// 成功
                                    rb = ReturnBody.success(qgv);
                                    // 如网关固件版本号比服务器上的低，可以送发起固件升级指令
    
                                }
                                else {// 失败
                                    log.error(qgv.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(), qgv.getAckMsg());
                                }
                                break; 
                            case "UpgradeGateway":// 发起网关固件升级
                                if (imp.getStatus() == 0) {// 成功
                                    rb = ReturnBody.success();
                                }else {//失败
                                    log.error(imp.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(), imp.getAckMsg());
                                }
                                break;  
                            case "UpgradeDevice":// 发起设备固件升级
                                if (imp.getStatus() == 0) {// 成功
                                    rb = ReturnBody.success();
                                }else {//失败
                                    log.error(imp.getAckMsg());
                                    rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(), imp.getAckMsg());
                                }
                                break;  
                            default:
                                break;
                        }
                        Constants.responseMap.remove(responseKey);
                        result.setResult(rb);// 返回前端
                    } else {
                        log.error("错误数据");
                    }
            } else if (imp.getDataCommand().equals("0")) {// 为0 数据透传  设备交互指令
                    String responseKey = mac + "_PassthrougData_" + imp.getDataTimeStamp();
                    DeferredResult<Object> result = Constants.responseMap.get(responseKey);// 拿到请求缓存
                    ReturnBody<?> rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(), ErrorCode.ACK_FAIL.getMsg());// 非成功就返回失败
                    switch (type) {
                        case "PassthrougData"://网关的应答
                            if (imp.getStatus() == 0) {// 应答状态 0
                                //透传数据会应答两次，第一次是网关的应答，  网关应答0成功 不做处理
                            }  else {
                                log.error(imp.getAckMsg());
                                rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(),imp.getAckMsg());
                            }
                            return;
                        case "DeviceAck"://设备应答的应答
                        	DeviceAck da = (DeviceAck) imp;
                            if (da.getStatus() == 0) {// 应答状态 0
                            	da.getMac();//应答的设备MAC  自行处理
                            	da.getDeviceType() ;//数据类型
                                //透传数据会应答两次，第二次是设备应答，   返回前端
                                rb = ReturnBody.success();
                            }  else {
                                log.error(da.getAckMsg());
                                rb = ReturnBody.fail(ErrorCode.ACK_FAIL.getCode(), da.getAckMsg());
                            }
                            Constants.responseMap.remove(responseKey);
                            result.setResult(rb);// 返回前端
                            break; 
                        default:
                            break;
                    }
            }  else {
                log.error("错误数据");
            }
            // re = ByteOps.hexStringToBytes("00000001");
            // ctx.channel().writeAndFlush(re);
            // ctx.channel().close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端连接
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel_Id : " + ctx.channel().id().asShortText());
//        Constants.ctxMap.put("tcp_" + "E9F8DF01C7EC", ctx);
//        Constants.macMap.put(ctx.channel().id().asShortText(), "E9F8DF01C7EC");
        Constants.ctxMap.put("tcp_" + "8CD4950004F5", ctx);
        Constants.macMap.put(ctx.channel().id().asShortText(), "8CD4950004F5");
        // 把channelId放入缓存
        // Constants.ctxMap.put("channelId_"+ctx.channel().id(), ctx);
        ctx.channel().writeAndFlush("Welcome to " + InetAddress.getLocalHost().getHostName() + " service!\n");
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 断开
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        String channelId = ctx.channel().id().asShortText();
        String mac = Constants.macMap.get(channelId);
        // 清理缓存
        if (mac != null) {
            log.info("\n 设备 :" + mac + " 主 动  离 线");
            Constants.ctxMap.remove("tcp_" + mac);
            Constants.macMap.remove(channelId);
        }
        ctx.close();
        log.info("\n地址 :" + ctx.channel().remoteAddress() + " 的设备离 线");
    }

}
