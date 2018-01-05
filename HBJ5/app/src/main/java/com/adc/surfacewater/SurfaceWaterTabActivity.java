package com.adc.surfacewater;

import com.adc.hbj5.R;
import com.adc.statistics.TjfxActivity;
import com.adc.hbj5.MyActivityManager;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabWidget;

@SuppressWarnings("deprecation")
public class SurfaceWaterTabActivity extends TabActivity {
	
	public static TabHost mTabHost;
	
	private RadioGroup surface_water_radioGroup;
	private RadioButton surface_water_dtgl;
	private RadioButton surface_water_jcdxx;
	private RadioButton surface_water_qxbh;
	private RadioButton surface_water_tjfx;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surface_water_tab);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SurfaceWaterTabActivity.this);
		
		mTabHost = getTabHost();
		final TabWidget tabWidget = mTabHost.getTabWidget();
		// tabWidget.setStripEnabled(false);

		surface_water_dtgl = (RadioButton) findViewById(R.id.surface_water_dtgl);
		surface_water_jcdxx = (RadioButton) findViewById(R.id.surface_water_jcdxx);
		surface_water_qxbh = (RadioButton) findViewById(R.id.surface_water_qxbh);
		surface_water_tjfx = (RadioButton) findViewById(R.id.surface_water_tjfx);
		
		mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("0")
				.setContent(new Intent(this, SurfaceWaterMapActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("1")
				.setContent(new Intent(this, SurfaceWaterCurveContrastActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
				.setContent(new Intent(this, SurfaceWaterDetailsActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG4").setIndicator("3")
				.setContent(new Intent(this, TjfxActivity.class)));
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
		
		final Drawable tjfx_3x = getResources().getDrawable(R.drawable.tjfx_3x);
		tjfx_3x.setBounds(0, 0, my_size, my_size);

		final Drawable tjfx_3x_press = getResources().getDrawable(
				R.drawable.tjfx_3x_press);
		tjfx_3x_press.setBounds(0, 0, my_size, my_size);
		
		surface_water_dtgl.setChecked(true);
		surface_water_dtgl.setCompoundDrawables(null, map_3x_press, null, null);
		surface_water_jcdxx.setCompoundDrawables(null, jcdxx_3x, null, null);
		surface_water_qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
		surface_water_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
		
		surface_water_radioGroup = (RadioGroup) findViewById(R.id.surface_water_radio);
		surface_water_radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == surface_water_dtgl.getId()){
					surface_water_dtgl.setCompoundDrawables(null, map_3x_press, null,
							null);
					surface_water_jcdxx.setCompoundDrawables(null, jcdxx_3x, null, null);
					surface_water_qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
					surface_water_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
					mTabHost.setCurrentTab(0);
				}else if(checkedId == surface_water_qxbh.getId()){
					surface_water_dtgl.setCompoundDrawables(null, map_3x, null,
							null);
					surface_water_jcdxx.setCompoundDrawables(null, jcdxx_3x, null, null);
					surface_water_qxbh.setCompoundDrawables(null, qxbh_3x_press, null, null);
					surface_water_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
					mTabHost.setCurrentTab(1);
				}else if(checkedId == surface_water_jcdxx.getId()){
					surface_water_dtgl.setCompoundDrawables(null, map_3x, null,
							null);
					surface_water_jcdxx.setCompoundDrawables(null, jcdxx_3x_press, null, null);
					surface_water_qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
					surface_water_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
					mTabHost.setCurrentTab(2);
				}else if(checkedId == surface_water_tjfx.getId()){
					surface_water_dtgl.setCompoundDrawables(null, map_3x, null,
							null);
					surface_water_jcdxx.setCompoundDrawables(null, jcdxx_3x, null, null);
					surface_water_qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
					surface_water_tjfx.setCompoundDrawables(null, tjfx_3x_press, null, null);
					mTabHost.setCurrentTab(3);
				}
			}
		});
	}
}
