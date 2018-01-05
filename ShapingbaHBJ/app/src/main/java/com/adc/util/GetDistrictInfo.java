package com.adc.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.DistrictInfo;
import com.adc.data.DistrictInfoListInstance;
import com.adc.data.LoginState;

public class GetDistrictInfo {
	
	private static ArrayList<DistrictInfo> districtInfos;
	
	public static void getDistrictInfo(String serverURL) throws Exception {
		String user_id = LoginState.getIns().getUserId();
		String spot_path = serverURL+"getDistrictList?user_id=" + user_id;
		URL url2;
		url2 = new URL(spot_path);
		HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
		conn.setConnectTimeout(50 * 1000);
		conn.setRequestMethod("GET");
		int responsecode = conn.getResponseCode();
		if (responsecode == 200) {
			districtInfos = new ArrayList<DistrictInfo>();
			InputStream is = conn.getInputStream();
			byte[] data = ReadStream.readStream(is);
			String json = new String(data);

			JSONArray jsonArray = new JSONArray(json);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject item = jsonArray.getJSONObject(i);

				DistrictInfo districtInfo = new DistrictInfo();
				districtInfo.setDistrictId(item.getString("districtId"));
				districtInfo.setName(item.getString("name"));
				districtInfos.add(districtInfo);
			}
			DistrictInfoListInstance.getIns().setList(districtInfos);
		}
	}
}
