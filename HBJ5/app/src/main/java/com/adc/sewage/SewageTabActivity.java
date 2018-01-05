package com.adc.sewage;

import com.adc.hbj5.R;
import com.adc.hbj5.MyActivityManager;
import com.adc.surfacewater.SurfaceWaterTabActivity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressWarnings("deprecation")
public class SewageTabActivity extends TabActivity {

	public static TabHost mTabHost;
	
	private RadioGroup sewage_radioGroup;
	private RadioButton sewage_dtgl;
	private RadioButton sewage_jcdxx;
	private RadioButton sewage_qxbh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sewage_tab);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SewageTabActivity.this);
		
		mTabHost = getTabHost();
		final TabWidget tabWidget = mTabHost.getTabWidget();
		// tabWidget.setStripEnabled(false);

		sewage_dtgl = (RadioButton) findViewById(R.id.sewage_dtgl);
		sewage_jcdxx = (RadioButton) findViewById(R.id.sewage_jcdxx);
		sewage_qxbh = (RadioButton) findViewById(R.id.sewage_qxbh);
		
		mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("0")
				.setContent(new Intent(this, SewageMapActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("1")
				.setContent(new Intent(this, SewageDetailsActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
				.setContent(new Intent(this, SewageCurveActivity.class)));
		
		mTabHost.setCurrentTab(0);
		
		final int my_size = 80;

		final Drawable map_3x = getResources().getDrawable(R.drawable.map_3x);
		map_3x.setBounds(0, 0, my_size, my_size);

		final Drawable map_3x_press = getResources().getDrawable(
				R.drawable.map_3x_press);
		map_3x_press.setBounds(0, 0, my_size, my_size);

		final Drawable jcdxx_3x = getResources().getDrawable(R.drawable.jcdxx_3x);
		jcdxx_3x.setBounds(0, 0, my_size, my_size);

		final Drawable jcdxx_3x_press = getResources().getDrawable(
				R.drawable.jcdxx_3x_press);
		jcdxx_3x_press.setBounds(0, 0, my_size, my_size);

		final Drawable qxbh_3x = getResources().getDrawable(R.drawable.qxbh_3x);
		qxbh_3x.setBounds(0, 0, my_size, my_size);

		final Drawable qxbh_3x_press = getResources().getDrawable(
				R.drawable.qxbh_3x_press);
		qxbh_3x_press.setBounds(0, 0, my_size, my_size);
		
		sewage_dtgl.setChecked(true);
		sewage_dtgl.setCompoundDrawables(null, map_3x_press, null, null);
		sewage_jcdxx.setCompoundDrawables(null, jcdxx_3x, null, null);
		sewage_qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
		
		sewage_radioGroup = (RadioGroup) findViewById(R.id.sewage_radio);
		sewage_radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == sewage_dtgl.getId()){
					sewage_dtgl.setCompoundDrawables(null, map_3x_press, null,
							null);
					sewage_jcdxx.setCompoundDrawables(null, jcdxx_3x, null, null);
					sewage_qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
					mTabHost.setCurrentTab(0);
				}else if(checkedId == sewage_jcdxx.getId()){
					sewage_dtgl.setCompoundDrawables(null, map_3x, null,
							null);
					sewage_jcdxx.setCompoundDrawables(null, jcdxx_3x_press, null, null);
					sewage_qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
					mTabHost.setCurrentTab(1);
				}else if(checkedId == sewage_qxbh.getId()){
					sewage_dtgl.setCompoundDrawables(null, map_3x, null,
							null);
					sewage_jcdxx.setCompoundDrawables(null, jcdxx_3x, null, null);
					sewage_qxbh.setCompoundDrawables(null, qxbh_3x_press, null, null);
					mTabHost.setCurrentTab(2);
				}
			}
		});
	}
}
