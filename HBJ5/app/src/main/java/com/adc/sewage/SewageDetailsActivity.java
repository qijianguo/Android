package com.adc.sewage;

import java.util.ArrayList;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.hbj5.R;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.hbj5.TableListViewAdapter;
import com.adc.hbj5.ZJBJMainActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class SewageDetailsActivity extends Activity {

	private Button wsclcxq_goback;
	private ListView sewage_details_list;
	
	private ArrayList<SpotInfo> spotInfos;
	
	private ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			/*Intent intent;
			
			if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
				intent = new Intent(SewageDetailsActivity.this,ZJBJMainActivity.class);
			}else{
				intent = new Intent(SewageDetailsActivity.this, MainActivity.class);
			}
			
			startActivity(intent);
			finish();*/
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sewage_details);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SewageDetailsActivity.this);
		
		spotInfos = new ArrayList<SpotInfo>();
		ArrayList<SpotInfo> original_infos = SpotInfoListInstance.getIns().getList();
		for(int i = 0;i < original_infos.size();i++){
			SpotInfo spotInfo = original_infos.get(i);
			int csite_type = Integer.valueOf(spotInfo.getCsite_type());
			if(csite_type != -2)	continue;
			spotInfos.add(spotInfo);
		}
		
		wsclcxq_goback = (Button) findViewById(R.id.wsclcxq_goback);
		wsclcxq_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Intent intent = new Intent(SewageDetailsActivity.this,
						MainActivity.class);
				startActivity(intent);*/
				finish();
			}
		});
		
		sewage_details_list = (ListView) findViewById(R.id.sewage_details_list);
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("名称");
		list.add("内容");
		lists.add(list);
		for(int i = 0;i < spotInfos.size();i++){
			if(i != 0){
				ArrayList<String> empty_line = new ArrayList<String>();
				empty_line.add("   ");
				empty_line.add("   ");
				lists.add(empty_line);
			}
			ArrayList<String> list1 = new ArrayList<String>();
			list1.add("监测点");
			list1.add(spotInfos.get(i).getCsite_name());
			lists.add(list1);
			
			ArrayList<String> list2 = new ArrayList<String>();
			list2.add("经度");
			list2.add(spotInfos.get(i).getLongitude());
			lists.add(list2);
			
			ArrayList<String> list3 = new ArrayList<String>();
			list3.add("纬度");
			list3.add(spotInfos.get(i).getLatitude());
			lists.add(list3);
			
			ArrayList<String> list4 = new ArrayList<String>();
			list4.add("出水COD");
			list4.add(spotInfos.get(i).getMinute_out_water_cod());
			lists.add(list4);
			
			ArrayList<String> list5 = new ArrayList<String>();
			list5.add("出水氨氮");
			list5.add(spotInfos.get(i).getMinute_out_water_nh3n());
			lists.add(list5);
			
			ArrayList<String> list6 = new ArrayList<String>();
			list6.add("出水流量");
			list6.add(spotInfos.get(i).getMinute_out_water_flow());
			lists.add(list6);
		}
		
		TableListViewAdapter myAdapter = new TableListViewAdapter(SewageDetailsActivity.this, lists);
		sewage_details_list.setAdapter(myAdapter);
		
	}
}
