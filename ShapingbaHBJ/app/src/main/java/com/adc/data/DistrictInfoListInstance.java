package com.adc.data;

import java.util.ArrayList;

public class DistrictInfoListInstance {
	private static DistrictInfoListInstance ins = new DistrictInfoListInstance();
	
	private ArrayList<DistrictInfo> list = new ArrayList<DistrictInfo>();

	public static DistrictInfoListInstance getIns() {
		return ins;
	}

	public ArrayList<DistrictInfo> getList() {
		return list;
	}

	public void setList(ArrayList<DistrictInfo> list) {
		this.list = list;
	}
	
	
}
