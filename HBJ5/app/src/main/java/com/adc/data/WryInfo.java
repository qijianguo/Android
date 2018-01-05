package com.adc.data;

import java.util.ArrayList;

public class WryInfo {
	
	private String company_name;
	private String latitude;
	private String longitude;
	private String major_pollutant;
	private String pollution_source_id;
	private ArrayList<WryRecordInfo> recordList;
	
	public String getCompany_name() {
		return company_name;
	}

	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getMajor_pollutant() {
		return major_pollutant;
	}

	public void setMajor_pollutant(String major_pollutant) {
		this.major_pollutant = major_pollutant;
	}

	public String getPollution_source_id() {
		return pollution_source_id;
	}

	public void setPollution_source_id(String pollution_source_id) {
		this.pollution_source_id = pollution_source_id;
	}

	public ArrayList<WryRecordInfo> getRecordList() {
		return recordList;
	}

	public void setRecordList(ArrayList<WryRecordInfo> recordList) {
		this.recordList = recordList;
	}

}
