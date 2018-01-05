package com.adc.data;

import java.util.ArrayList;

public class SpotInfoListInstance {
	private static SpotInfoListInstance ins = new SpotInfoListInstance();
	
	private ArrayList<SpotInfo> list = new ArrayList<SpotInfo>();

	public static SpotInfoListInstance getIns() {
		return ins;
	}

	public ArrayList<SpotInfo> getList() {
		return list;
	}

	public void setList(ArrayList<SpotInfo> list) {
		this.list = list;
	}
	
	
}
