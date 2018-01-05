package com.adc.data;

public class SpotInfo {
	private String csite_id;
	private String csite_name;
	private String addr;
	private String longitude;
	private String latitude;
	private String monitor_type;
	private String realtime_pm_2_5;
	private String hour_pm2_5;
	private String reltime_pm_10;
	private String hour_pm10;
	private String realtime_noise;
	private String hour_noise;
	private String get_time;
	/** 摄像头id*/
	private String camera_id;
	/** 码流类型 */
	private String video_phone_mag;
	
	//地表水专有属性

	/** 高锰酸盐（小时） */
	private String hour_water_kmn;
	/** 高锰酸盐（实时）*/
	private String minute_water_kmn;
	/** 水流速（小时） */
	private String hour_water_flow;
	/** 水流速（实时）*/
	private String minute_water_flow;

	// 污水处理厂专有属性
	/** 出水COD（小时） */
	private String hour_out_water_cod;
	/** 出水COD（实时）*/
	private String minute_out_water_cod;
	/** 出水氨氮（小时） */
	private String hour_out_water_nh3n;
	/** 出水氨氮（实时）*/
	private String minute_out_water_nh3n;
	/** 出水流量（小时） */
	private String hour_out_water_flow;
	/** 出水流量（实时） */
	private String minute_out_water_flow;
	
	//地表水、污水处理厂共有属性
	/** 监测时间（小时） */
	private String hour_sample_time;
	/** 监测时间（实时） */
	private String minute_sample_time;
	
	/** 监测点属性标识 */
	private String csite_type;
	
	/** 监测点对应的海康视频服务器地址 */
	private String video_service_ip;
	
	public String getGet_time() {
		return get_time;
	}

	public void setGet_time(String get_time) {
		this.get_time = get_time;
	}

	public String getGet_hour() {
		return get_hour;
	}

	public void setGet_hour(String get_hour) {
		this.get_hour = get_hour;
	}

	private String get_hour;

	public String getCsite_id() {
		return csite_id;
	}

	public void setCsite_id(String csite_id) {
		this.csite_id = csite_id;
	}

	public String getCsite_name() {
		return csite_name;
	}

	public void setCsite_name(String csite_name) {
		this.csite_name = csite_name;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getMonitor_type() {
		return monitor_type;
	}

	public void setMonitor_type(String monitor_type) {
		this.monitor_type = monitor_type;
	}

	public String getRealtime_pm_2_5() {
		return realtime_pm_2_5;
	}

	public void setRealtime_pm_2_5(String realtime_pm_2_5) {
		this.realtime_pm_2_5 = realtime_pm_2_5;
	}

	public String getHour_pm2_5() {
		return hour_pm2_5;
	}

	public void setHour_pm2_5(String hour_pm2_5) {
		this.hour_pm2_5 = hour_pm2_5;
	}

	public String getReltime_pm_10() {
		return reltime_pm_10;
	}

	public void setReltime_pm_10(String reltime_pm_10) {
		this.reltime_pm_10 = reltime_pm_10;
	}

	public String getHour_pm10() {
		return hour_pm10;
	}

	public void setHour_pm10(String hour_pm10) {
		this.hour_pm10 = hour_pm10;
	}

	public String getRealtime_noise() {
		return realtime_noise;
	}

	public void setRealtime_noise(String realtime_noise) {
		this.realtime_noise = realtime_noise;
	}

	public String getHour_noise() {
		return hour_noise;
	}

	public void setHour_noise(String hour_noise) {
		this.hour_noise = hour_noise;
	}

	public String getHour_water_kmn() {
		return hour_water_kmn;
	}

	public void setHour_water_kmn(String hour_water_kmn) {
		this.hour_water_kmn = hour_water_kmn;
	}

	public String getMinute_water_kmn() {
		return minute_water_kmn;
	}

	public void setMinute_water_kmn(String minute_water_kmn) {
		this.minute_water_kmn = minute_water_kmn;
	}

	public String getHour_water_flow() {
		return hour_water_flow;
	}

	public void setHour_water_flow(String hour_water_flow) {
		this.hour_water_flow = hour_water_flow;
	}

	public String getMinute_water_flow() {
		return minute_water_flow;
	}

	public void setMinute_water_flow(String minute_water_flow) {
		this.minute_water_flow = minute_water_flow;
	}

	public String getHour_out_water_cod() {
		return hour_out_water_cod;
	}

	public void setHour_out_water_cod(String hour_out_water_cod) {
		this.hour_out_water_cod = hour_out_water_cod;
	}

	public String getMinute_out_water_cod() {
		return minute_out_water_cod;
	}

	public void setMinute_out_water_cod(String minute_out_water_cod) {
		this.minute_out_water_cod = minute_out_water_cod;
	}

	public String getHour_out_water_nh3n() {
		return hour_out_water_nh3n;
	}

	public void setHour_out_water_nh3n(String hour_out_water_nh3n) {
		this.hour_out_water_nh3n = hour_out_water_nh3n;
	}

	public String getMinute_out_water_nh3n() {
		return minute_out_water_nh3n;
	}

	public void setMinute_out_water_nh3n(String minute_out_water_nh3n) {
		this.minute_out_water_nh3n = minute_out_water_nh3n;
	}

	public String getHour_out_water_flow() {
		return hour_out_water_flow;
	}

	public void setHour_out_water_flow(String hour_out_water_flow) {
		this.hour_out_water_flow = hour_out_water_flow;
	}

	public String getMinute_out_water_flow() {
		return minute_out_water_flow;
	}

	public void setMinute_out_water_flow(String minute_out_water_flow) {
		this.minute_out_water_flow = minute_out_water_flow;
	}

	public String getHour_sample_time() {
		return hour_sample_time;
	}

	public void setHour_sample_time(String hour_sample_time) {
		this.hour_sample_time = hour_sample_time;
	}

	public String getMinute_sample_time() {
		return minute_sample_time;
	}

	public void setMinute_sample_time(String minute_sample_time) {
		this.minute_sample_time = minute_sample_time;
	}

	public String getCsite_type() {
		return csite_type;
	}

	public void setCsite_type(String csite_type) {
		this.csite_type = csite_type;
	}

	public String getCamera_id() {
		return camera_id;
	}

	public void setCamera_id(String camera_id) {
		this.camera_id = camera_id;
	}

	public String getVideo_service_ip() {
		return video_service_ip;
	}

	public void setVideo_service_ip(String video_service_ip) {
		this.video_service_ip = video_service_ip;
	}

	public String getVideo_phone_mag() {
		return video_phone_mag;
	}

	public void setVideo_phone_mag(String video_phone_mag) {
		this.video_phone_mag = video_phone_mag;
	}
	
	
}
