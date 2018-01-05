package com.adc.pollutionsource;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.adc.data.WryRecordInfo;
import com.adc.hbj5.R;
import com.adc.hbj5.MyActivityManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WryDetailsActivity extends Activity {

	private LinearLayout wry_details_layout;
	private TextView wry_name;
	//private TextView test_item;
	//private TextView test_time;
	//private TextView data_source;
	//private TextView main_pollutant_name;

	private LinearLayout key_pollution_waste_water_layout;
	private TextView key_pollution_waste_water_result;
	private TextView key_pollution_waste_water_content;
	private TextView key_pollution_waste_water_record_time;
	
	private LinearLayout key_pollution_exhaust_gas_layout;
	private TextView key_pollution_exhaust_gas_result;
	private TextView key_pollution_exhaust_gas_content;
	private TextView key_pollution_exhaust_gas_record_time;
	
	private LinearLayout key_pollution_noise_layout;
	private TextView key_pollution_noise_result;
	private TextView key_pollution_noise_content;
	private TextView key_pollution_noise_record_time;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wry_details);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(WryDetailsActivity.this);

		wry_name = (TextView) findViewById(R.id.wry_name);
		//test_item = (TextView) findViewById(R.id.test_item);
		//test_time = (TextView) findViewById(R.id.test_time);
		//data_source = (TextView) findViewById(R.id.data_source);
		//main_pollutant_name = (TextView) findViewById(R.id.major_pollutant_name);

		key_pollution_waste_water_layout = (LinearLayout) findViewById(R.id.key_pollution_waste_water_layout);
		key_pollution_waste_water_result = (TextView) findViewById(R.id.key_pollution_waste_water_result);
		key_pollution_waste_water_content = (TextView) findViewById(R.id.key_pollution_waste_water_content);
		key_pollution_waste_water_record_time = (TextView) findViewById(R.id.key_pollution_waste_water_record_time);
		
		key_pollution_exhaust_gas_layout = (LinearLayout) findViewById(R.id.key_pollution_exhaust_gas_layout);
		key_pollution_exhaust_gas_result = (TextView) findViewById(R.id.key_pollution_exhaust_gas_result);
		key_pollution_exhaust_gas_content = (TextView) findViewById(R.id.key_pollution_exhaust_gas_content);
		key_pollution_exhaust_gas_record_time = (TextView) findViewById(R.id.key_pollution_exhaust_gas_record_time);
		
		key_pollution_noise_layout = (LinearLayout) findViewById(R.id.key_pollution_noise_layout);
		key_pollution_noise_result = (TextView) findViewById(R.id.key_pollution_noise_result);
		key_pollution_noise_content = (TextView) findViewById(R.id.key_pollution_noise_content);
		key_pollution_noise_record_time = (TextView) findViewById(R.id.key_pollution_noise_record_time);
		
		Intent intent = getIntent();
		Bundle dataBundle = intent.getExtras();
		int idx = dataBundle.getInt("idx");

		wry_name.setText(ZdwryActivity.wry_list.get(idx).getCompany_name());
		//test_item.setText(ZdwryActivity.wry_list.get(idx).getMajor_pollutant());
		//main_pollutant_name.setText(ZdwryActivity.wry_list.get(idx)
		//		.getMajor_pollutant());
		//SimpleDateFormat formatterDate = new SimpleDateFormat(
		//		"yyyy-MM-dd HH:00:00");
		//Date curDate = new Date(System.currentTimeMillis());
		//String string = formatterDate.format(curDate);
		//test_time.setText(string);
		//data_source.setText("重庆市环保局");

		int record_len = ZdwryActivity.wry_list.get(idx).getRecordList().size();
		boolean type1 = false,type2 = false,type5 = false;
		for(int i = 0;i < record_len;i++){
			WryRecordInfo wryRecordInfo = ZdwryActivity.wry_list.get(idx).getRecordList().get(i);
			String record_typeString = wryRecordInfo.getRecord_type();
			int record_type = Integer.parseInt(record_typeString);
			int passed = 0;
			switch (record_type) {
			case 1:
				passed = Integer.parseInt(wryRecordInfo.getRecord_passed());
				if(passed == 1){
					key_pollution_waste_water_layout.setBackgroundResource(R.drawable.key_pollution_qualified_bar);
					key_pollution_waste_water_result.setText("   废水：达标");
				}
				else{
					key_pollution_waste_water_layout.setBackgroundResource(R.drawable.key_pollution_unqualified_bar);
					key_pollution_waste_water_result.setText("   废水：未达标");
				}
				key_pollution_waste_water_content.setText("排放标准："+wryRecordInfo.getRecord_content());
				key_pollution_waste_water_record_time.setText("检测时间："+wryRecordInfo.getRecord_time());
				type1 = true;
				break;

			case 2:
				passed = Integer.parseInt(wryRecordInfo.getRecord_passed());
				if(passed == 1){
					key_pollution_exhaust_gas_layout.setBackgroundResource(R.drawable.key_pollution_qualified_bar);
					key_pollution_exhaust_gas_result.setText("   废气：达标");
				}
				else{
					key_pollution_exhaust_gas_layout.setBackgroundResource(R.drawable.key_pollution_unqualified_bar);
					key_pollution_exhaust_gas_result.setText("   废气：未达标");
				}
				key_pollution_exhaust_gas_content.setText("排放标准："+wryRecordInfo.getRecord_content());
				key_pollution_exhaust_gas_record_time.setText("检测时间："+wryRecordInfo.getRecord_time());
				type2 = true;
				break;
				
			case 5:
				passed = Integer.parseInt(wryRecordInfo.getRecord_passed());
				if(passed == 1){
					key_pollution_noise_layout.setBackgroundResource(R.drawable.key_pollution_qualified_bar);
					key_pollution_noise_result.setText("   噪声：达标");
				}
				else{
					key_pollution_noise_layout.setBackgroundResource(R.drawable.key_pollution_unqualified_bar);
					key_pollution_noise_result.setText("   噪声：未达标");
				}
				key_pollution_noise_content.setText("排放标准："+wryRecordInfo.getRecord_content());
				key_pollution_noise_record_time.setText("检测时间："+wryRecordInfo.getRecord_time());
				type5 = true;
				break;
			default:
				break;
			}
		}
		
		if(type1 == false){
			key_pollution_waste_water_layout.setVisibility(View.GONE);
			key_pollution_waste_water_result.setVisibility(View.GONE);
			key_pollution_waste_water_content.setVisibility(View.GONE);
			key_pollution_waste_water_record_time.setVisibility(View.GONE);
		}
		if(type2 == false){
			key_pollution_exhaust_gas_layout.setVisibility(View.GONE);
			key_pollution_exhaust_gas_result.setVisibility(View.GONE);
			key_pollution_exhaust_gas_content.setVisibility(View.GONE);
			key_pollution_exhaust_gas_record_time.setVisibility(View.GONE);
		}
		if(type5 == false){
			key_pollution_noise_layout.setVisibility(View.GONE);
			key_pollution_noise_result.setVisibility(View.GONE);
			key_pollution_noise_content.setVisibility(View.GONE);
			key_pollution_noise_record_time.setVisibility(View.GONE);
		}
		
		wry_details_layout = (LinearLayout) findViewById(R.id.wry_details_layout);
		wry_details_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
}
