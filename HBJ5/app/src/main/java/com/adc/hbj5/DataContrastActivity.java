package com.adc.hbj5;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.hbj5.R;

import android.R.array;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class DataContrastActivity extends Activity {

	//private ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
	private RadioGroup data_contrast_radioGroup;
	private RadioButton realtime_dataButton;
	private RadioButton hour_dataButton;
    private ListView listView;
	private Button sjdb_goback;
	private int noise_or_pm10;
	private int realtime_or_hour;
	private final int noise_checked = 0;
	private final int pm10_checked = 1;
	private final int realtime_checked = 0;
	private final int hour_checked = 1;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			/*Intent goback_intent = null;
			if (LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)) {
				goback_intent = new Intent(DataContrastActivity.this,LoginActivity.class);
			}else{
				goback_intent = new Intent(DataContrastActivity.this,MainActivity.class);
			}
			startActivity(goback_intent);*/
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_contrast);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(DataContrastActivity.this);
		
		data_contrast_radioGroup = (RadioGroup)findViewById(R.id.data_contrast_radio);
		realtime_dataButton = (RadioButton)findViewById(R.id.data_contrast_realtime_data);
		hour_dataButton = (RadioButton)findViewById(R.id.data_contrast_hour_data);
		listView = (ListView)findViewById(R.id.monitor_list);
		
		sjdb_goback = (Button)findViewById(R.id.sjdb_goback);
		sjdb_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*Intent goback_intent = null;
				if (LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)) {
					goback_intent = new Intent(DataContrastActivity.this,LoginActivity.class);
				}else{
					goback_intent = new Intent(DataContrastActivity.this,MainActivity.class);
				}
				startActivity(goback_intent);*/
				finish();
			}
		});
		
		noise_or_pm10 = LoginState.getIns().getNoise_or_pm10();
		realtime_or_hour = realtime_checked;
		
		listView.setAdapter(get_table(noise_or_pm10,realtime_or_hour));
		
		data_contrast_radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == realtime_dataButton.getId()){
					realtime_or_hour = realtime_checked;
					//listView.removeAllViews();
					listView.setAdapter(get_table(noise_or_pm10, realtime_or_hour));
				}else if(checkedId == hour_dataButton.getId()){
					realtime_or_hour = hour_checked;
					//listView.removeAllViews();
					listView.setAdapter(get_table(noise_or_pm10, realtime_or_hour));
				}
			}
		});
	}
	
	public TableListViewAdapter2 get_table(int noise_or_pm10,int realtime_or_hour) {
		ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
		ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
		if(noise_or_pm10 == noise_checked){
			ArrayList<String> list = new ArrayList<String>();
			list.add("监测对象");
			list.add("噪声(db)");
			list.add("时间");
			lists.add(list);
			if(realtime_or_hour == realtime_checked){
				for(int i = 0;i < spotInfos.size();i++){
					if(Double.valueOf(spotInfos.get(i).getRealtime_noise()) == 0)	continue;
					ArrayList<String> list1 = new ArrayList<String>();
					list1.add(spotInfos.get(i).getCsite_name());
					double val = Double.valueOf(spotInfos.get(i).getRealtime_noise());
					list1.add(""+(int)val);
					list1.add(spotInfos.get(i).getGet_time());
					lists.add(list1);
				}
				TableListViewAdapter2 myAdapter = new TableListViewAdapter2(DataContrastActivity.this, lists);
				return myAdapter;
			}else if(realtime_or_hour == hour_checked){
				for(int i = 0;i < spotInfos.size();i++){
					if(Double.valueOf(spotInfos.get(i).getHour_noise()) == 0)	continue;
					ArrayList<String> list1 = new ArrayList<String>();
					list1.add(spotInfos.get(i).getCsite_name());
					double val = Double.valueOf(spotInfos.get(i).getHour_noise());
					list1.add(""+(int)val);
					list1.add(spotInfos.get(i).getGet_hour());
					lists.add(list1);
				}
				TableListViewAdapter2 myAdapter = new TableListViewAdapter2(DataContrastActivity.this, lists);
				return myAdapter;
			}
		}else if(noise_or_pm10 == pm10_checked){
			ArrayList<String> list = new ArrayList<String>();
			list.add("监测对象");
			list.add("PM10(μg/m³)");
			list.add("时间");
			lists.add(list);
			if(realtime_or_hour == realtime_checked){
				for(int i = 0;i < spotInfos.size();i++){
					if(Double.valueOf(spotInfos.get(i).getReltime_pm_10()) == 0)	continue;
					ArrayList<String> list1 = new ArrayList<String>();
					list1.add(spotInfos.get(i).getCsite_name());
					double val = Double.valueOf(spotInfos.get(i).getReltime_pm_10());
					list1.add(""+(int)val);
					list1.add(spotInfos.get(i).getGet_time());
					lists.add(list1);
				}
				TableListViewAdapter2 myAdapter = new TableListViewAdapter2(DataContrastActivity.this, lists);
				return myAdapter;
			}else if(realtime_or_hour == hour_checked){
				for(int i = 0;i < spotInfos.size();i++){
					if(Double.valueOf(spotInfos.get(i).getHour_pm10()) == 0)	continue;
					ArrayList<String> list1 = new ArrayList<String>();
					list1.add(spotInfos.get(i).getCsite_name());
					double val = Double.valueOf(spotInfos.get(i).getHour_pm10());
					list1.add(""+(int)val);
					list1.add(spotInfos.get(i).getGet_hour());
					lists.add(list1);
				}
				TableListViewAdapter2 myAdapter = new TableListViewAdapter2(DataContrastActivity.this, lists);
				return myAdapter;
			}
		}
		return null;
	}
}
