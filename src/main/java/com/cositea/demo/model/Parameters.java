package com.cositea.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("设备设置参数")
public class Parameters {
    @ApiModelProperty("蓝牙主机MAC")
    private String  mac;
    @ApiModelProperty("设备MAC")
    private String  devMac;
    @ApiModelProperty("时钟格式  0:  12小时制    1: 24小时制")
    private Integer  timeFormat;
    @ApiModelProperty("心率监控间隔 单位分钟：最小值 5 分钟，最大值 60 分钟；若为 1 表示连续心率监测(注：目前大部分设备不支持连续心率监测）")
    private Integer  heartRateInterval;
    @ApiModelProperty("单位 0:公制  ,1:英制")
    private Integer  unit;
    @ApiModelProperty("疲劳度监控开关  0：关闭   , 1：开启")
    private Integer  fatigueMonitor;
    @ApiModelProperty("心率自动监控开关   0：关闭     , 1：开启")
    private Integer  heartRateAuto;
    @ApiModelProperty("日期显示格式  0 月：日     , 1  日：月")
    private Integer  dateFormat;
    @ApiModelProperty("设备语言版本    0: 中文 , 1：英文 ,3:繁体中文")
    private Integer  language;
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getDevMac() {
		return devMac;
	}
	public void setDevMac(String devMac) {
		this.devMac = devMac;
	}
	public Integer getTimeFormat() {
		return timeFormat;
	}
	public void setTimeFormat(Integer timeFormat) {
		this.timeFormat = timeFormat;
	}
	public Integer getHeartRateInterval() {
		return heartRateInterval;
	}
	public void setHeartRateInterval(Integer heartRateInterval) {
		this.heartRateInterval = heartRateInterval;
	}
	public Integer getUnit() {
		return unit;
	}
	public void setUnit(Integer unit) {
		this.unit = unit;
	}
	public Integer getFatigueMonitor() {
		return fatigueMonitor;
	}
	public void setFatigueMonitor(Integer fatigueMonitor) {
		this.fatigueMonitor = fatigueMonitor;
	}
	public Integer getHeartRateAuto() {
		return heartRateAuto;
	}
	public void setHeartRateAuto(Integer heartRateAuto) {
		this.heartRateAuto = heartRateAuto;
	}
	public Integer getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(Integer dateFormat) {
		this.dateFormat = dateFormat;
	}
	public Integer getLanguage() {
		return language;
	}
	public void setLanguage(Integer language) {
		this.language = language;
	}
	@Override
	public String toString() {
		return "Parameters [mac=" + mac + ", devMac=" + devMac + ", timeFormat=" + timeFormat + ", heartRateInterval="
				+ heartRateInterval + ", unit=" + unit + ", fatigueMonitor=" + fatigueMonitor + ", heartRateAuto="
				+ heartRateAuto + ", dateFormat=" + dateFormat + ", language=" + language + "]";
	}
    
    
    
}
