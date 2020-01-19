package com.cositea.demo.model;

/**
 *手环数据
 * @author lyc
 *
 */
public class BraceletData {
    
    private String deviceType; //设备类型
    private String deviceModel;//设备型号
    private String devMac;//设备地址
    private Integer rssi;//信号强度
    private Integer heartate;//心率值
    private Integer step;//步数
    private Integer movement;//运动模式 0开 1关  默认1
    private Integer dynamic=2;//动静状态 0动  1静  2无效(0XFF)
    private Integer sleep=3;//0:清醒； 1：浅度睡眠； 2：深度睡眠； 3:未检测(0XFF)
    private Integer diastolic;//血压舒张压
    private Integer systolic;//血压收缩压
    private Integer oxygen;//血氧
    private Integer hrv;//HRV值
    private Integer version;//软件版本
    private Long timeStamp;//时间戳
    private Integer staticHeartate;//静止心率值
    private Integer electricity;//电量百分比
    private Integer  status;//数据是否校验通过
    private String  crc;//异或校验值
    
    /**
     * TODO: 设备类型
     * @return
     */
    public String getDeviceType() {
        return deviceType;
    }
    /**
     * TODO: 设备类型
     * @param deviceType
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    /**
     * TODO: 设备型号
     * @return
     */
    public String getDeviceModel() {
        return deviceModel;
    }
    /**
     * TODO: 设备型号
     * @param viceModel
     */
    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }
    /**
     * 
     * TODO: 设备mac
     * @return
     */
    public String getDevMac() {
        return devMac;
    }
    /**
     * TODO:  设备mac
     * @param devMac
     */
    public void setDevMac(String devMac) {
        this.devMac = devMac;
    }
    
    /**
     * 信号强度
     * @return
     */
    public Integer getRssi() {
		return rssi;
	}
    /**
     * 信号强度
     * @param rssi
     */
	public void setRssi(Integer rssi) {
		this.rssi = rssi;
	}
	/**
     * TODO: 心率值
     * @return
     */
    public Integer getHeartate() {
        return heartate;
    }
    /**
     * TODO: 心率值
     * @param heartate
     */
    public void setHeartate(Integer heartate) {
        this.heartate = heartate;
    }
    /**
     * TODO:步数 
     * @return
     */
    public Integer getStep() {
        return step;
    }
    /**
     * 
     * TODO: 步数 
     * @param step
     */
    public void setStep(Integer step) {
        this.step = step;
    }
    /**
     * TODO: 运动模式 0开 1关  
     * @return
     */
    public Integer getMovement() {
        return movement;
    }
    /**
     * TODO: 运动模式 0开 1关 
     * @param movement
     */
    public void setMovement(Integer movement) {
        this.movement = movement;
    }
    /**
     * TODO: 动静状态 0动  1静  2无效(0XFF)
     * @return
     */
    public Integer getDynamic() {
        return dynamic;
    }
    /**
     * TODO: 动静状态 0动  1静  2无效(0XFF)
     * @param dynamic
     */
    public void setDynamic(Integer dynamic) {
        this.dynamic = dynamic;
    }
    /**
     * TODO: 0:清醒； 1：浅度睡眠； 2：深度睡眠； 3:未检测(0XFF)
     * @return
     */
    public Integer getSleep() {
        return sleep;
    }
    /**
     * TODO: 0:清醒； 1：浅度睡眠； 2：深度睡眠； 3:未检测(0XFF)
     * @param sleep
     */
    public void setSleep(Integer sleep) {
        this.sleep = sleep;
    }
    /**
     * TODO: 血压舒张压
     * @return
     */
    public Integer getDiastolic() {
        return diastolic;
    }
    /**
     * TODO: 血压舒张压
     * @param diastolic
     */
    public void setDiastolic(Integer diastolic) {
        this.diastolic = diastolic;
    }
    /**
     * TODO: 血压收缩压
     * @return
     */
    public Integer getSystolic() {
        return systolic;
    }
    /**
     * TODO: 血压收缩压
     * @param diastolic
     */
    public void setSystolic(Integer systolic) {
    	this.systolic = systolic;
    }
    /**
     * TODO: 血氧
     * @return
     */
    public Integer getOxygen() {
        return oxygen;
    }
    /**
     * TODO: 血氧
     * @param oxygen
     */
    public void setOxygen(Integer oxygen) {
        this.oxygen = oxygen;
    }
    /**
     * TODO: hrv
     * @return
     */
    public Integer getHrv() {
        return hrv;
    }
    /**
     * TODO: hrv
     * @param hrv
     */
    public void setHrv(Integer hrv) {
        this.hrv = hrv;
    }
    /**
     * TODO: 软件版本
     * @return
     */
    public Integer getVersion() {
        return version;
    }
    /**
     * TODO: 软件版本
     * @param version
     */
    public void setVersion(Integer version) {
        this.version = version;
    }
    /**
     * TODO: 时间戳  秒 
     * @return
     */
    public Long getTimeStamp() {
        return timeStamp;
    }
    /**
     * TODO: 时间戳 秒 
     * @param timeStamp
     */
    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
    /**
     * TODO: 静止心率
     * @return
     */
    public Integer getStaticHeartate() {
        return staticHeartate;
    }
    /**
     * TODO: 静止心率
     * @param staticHeartate
     */
    public void setStaticHeartate(Integer staticHeartate) {
        this.staticHeartate = staticHeartate;
    }
    /**
     * TODO: 电量 百分比
     * @return
     */
    public Integer getElectricity() {
        return electricity;
    }
    /**
     * TODO:  电量 百分比
     * @param electricity
     */
    public void setElectricity(Integer electricity) {
        this.electricity = electricity;
    }
    
    
    /**
     * 
     * TODO: 数据是否通过校验  0成功 1失败
     * @return
     */
    public Integer getStatus() {
        return status;
    }
    /**
     * 
     * TODO: 数据是否通过校验 0成功 1失败
     * @param status
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
    /**
     * TODO: 异域校验值 
     * @return
     */
    public String getCrc() {
        return crc;
    }
    /**
     * TODO: 异域校验值
     * @param crc
     */
    public void setCrc(String crc) {
        this.crc = crc;
    }
	@Override
	public String toString() {
		return "DeviceData [deviceType=" + deviceType + ", deviceModel=" + deviceModel + ", devMac=" + devMac
				+ ", heartate=" + heartate + ", step=" + step + ", movement=" + movement + ", dynamic=" + dynamic
				+ ", sleep=" + sleep + ", diastolic=" + diastolic + ", Systolic=" + systolic + ", oxygen=" + oxygen
				+ ", hrv=" + hrv + ", version=" + version + ", timeStamp=" + timeStamp + ", staticHeartate="
				+ staticHeartate + ", electricity=" + electricity + ", status=" + status + ", crc=" + crc + "]";
	}
    
}
