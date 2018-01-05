package com.adc.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;

public class GetSpotInfo {
	
	private static ArrayList<SpotInfo> SpotInfoList;
	
	public static void getSpotInfo(String serverURL) throws Exception {
		String user_id = LoginState.getIns().getUserId();
		String spot_path = serverURL+"getSpotListByUser?user_id="
				+ user_id + "&with_data=1";
		URL url2;
		url2 = new URL(spot_path);
		HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
		conn.setConnectTimeout(50 * 1000);
		conn.setRequestMethod("GET");
		int responsecode = conn.getResponseCode();
		if (responsecode == 200) {
			SpotInfoList = new ArrayList<SpotInfo>();
			InputStream is = conn.getInputStream();
			byte[] data = ReadStream.readStream(is);
			String json = new String(data);

			JSONArray jsonArray = new JSONArray(json);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject item = jsonArray.getJSONObject(i);

				SpotInfo spotInfo = new SpotInfo();
				spotInfo.setCsite_id(item.getString("csite_id"));
				spotInfo.setCsite_name(item.getString("csite_name"));
				spotInfo.setAddr(item.getString("addr"));
				spotInfo.setLongitude(item.getString("longitude"));
				spotInfo.setLatitude(item.getString("latitude"));
				spotInfo.setMonitor_type(item.getString("monitor_type"));
				spotInfo.setRealtime_pm_2_5(item.getString("realtime_pm2_5"));
				spotInfo.setHour_pm2_5(item.getString("hour_pm2_5"));
				spotInfo.setReltime_pm_10(item.getString("realtime_pm10"));
				spotInfo.setHour_pm10(item.getString("hour_pm10"));
				spotInfo.setRealtime_noise(item.getString("realtime_noise"));
				spotInfo.setHour_noise(item.getString("hour_noise"));

				spotInfo.setHour_water_kmn(item.getString("hour_water_kmn"));
				spotInfo.setMinute_water_kmn(item.getString("minute_water_kmn"));
				spotInfo.setHour_water_flow(item.getString("hour_water_flow"));
				spotInfo.setMinute_water_flow(item.getString("minute_water_flow"));
				
				spotInfo.setHour_out_water_cod(item.getString("hour_out_water_cod"));
				spotInfo.setMinute_out_water_cod(item.getString("minute_out_water_cod"));
				spotInfo.setHour_out_water_nh3n(item.getString("hour_out_water_nh3n"));
				spotInfo.setMinute_out_water_nh3n(item.getString("minute_out_water_nh3n"));
				spotInfo.setHour_out_water_flow(item.getString("hour_out_water_flow"));
				spotInfo.setMinute_out_water_flow(item.getString("minute_out_water_flow"));
				
				spotInfo.setHour_sample_time(item.getString("hour_sample_time"));
				spotInfo.setMinute_sample_time(item.getString("minute_sample_time"));
				
				spotInfo.setCsite_type(item.getString("csite_type"));
				
				spotInfo.setCamera_id(item.getString("camera_id"));
				
				spotInfo.setVideo_service_ip(item.getString("video_service_ip"));
				Calendar calendar = Calendar.getInstance();
				//calendar = GetCurrentTimeFromBaidu.getCurrentTime();
				spotInfo.setGet_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:00")
						.format(calendar.getTime()));
				spotInfo.setGet_hour(new SimpleDateFormat("yyyy-MM-dd HH:00:00")
						.format(calendar.getTime()));

				if (item.has("minute_water_sewage")) {
					spotInfo.setMinute_water_sewage(item.getString("minute_water_sewage"));
				}
				if (item.has("minute_water_nh3n")) {
					spotInfo.setMinute_water_nh3n(item.getString("minute_water_nh3n"));
				}
				if (item.has("minute_water_pho")) {
					spotInfo.setMinute_water_pho(item.getString("minute_water_pho"));
				}
				if (item.has("minute_water_ntu")) {
					spotInfo.setMinute_water_ntu(item.getString("minute_water_ntu"));
				}
				if (item.has("minute_water_ph")) {
					spotInfo.setMinute_water_ph(item.getString("minute_water_ph"));
				}
				if (item.has("minute_water_N")) {
					spotInfo.setMinute_water_N(item.getString("minute_water_N"));
				}
				if (item.has("status")) {
					spotInfo.setStatus(item.getString("status"));
				}
				SpotInfoList.add(spotInfo);
				// Log.i("heheda", "ok" + i);
			}
			SpotInfoListInstance.getIns().setList(SpotInfoList);
		}
	}
}
