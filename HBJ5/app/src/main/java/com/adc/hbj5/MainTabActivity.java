package com.adc.hbj5;

import com.adc.data.LoginState;
import com.adc.hbj5.R;
import com.adc.statistics.TjfxActivity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Radio;
import android.provider.SyncStateContract.Constants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressWarnings("deprecation")
public class MainTabActivity extends TabActivity {

	public static TabHost mTabHost;

	private RadioGroup main_radioGroup;
	//private RadioButton hxdb;
	private RadioButton main_tab_dtgl;
	private RadioButton main_tab_qxdb;
	private RadioButton main_tab_sjdb;
	private RadioButton main_tab_tjfx;
	//private RadioButton set_up;
	//public static int hxdb_tjfx_setup = 0;
	public static int dtgl_qxdb_sjdb_tjfx = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_tab);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(MainTabActivity.this);

		mTabHost = getTabHost();
		final TabWidget tabWidget = mTabHost.getTabWidget();
		// tabWidget.setStripEnabled(false);

		main_tab_dtgl = (RadioButton) findViewById(R.id.main_tab_dtgl);
		main_tab_qxdb = (RadioButton) findViewById(R.id.main_tab_qxdb);
		main_tab_sjdb = (RadioButton) findViewById(R.id.main_tab_sjdb);
		main_tab_tjfx = (RadioButton) findViewById(R.id.main_tab_tjfx);

		/*mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("0")
				.setContent(new Intent(this, HxdbActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("1")
				.setContent(new Intent(this, TjfxActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
				.setContent(new Intent(this, SetupActivity.class)));*/

		mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("0")
				.setContent(new Intent(this, MapOverviewActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("1")
				.setContent(new Intent(this, CurveContrastActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
				.setContent(new Intent(this, DataContrastActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("3")
				.setContent(new Intent(this, TjfxActivity.class)));
		
		mTabHost.setCurrentTab(0);

		final int my_size = 80;

		/*final Drawable hxdb_3x = getResources().getDrawable(R.drawable.hxdb_3x);
		hxdb_3x.setBounds(0, 0, my_size, my_size);

		final Drawable hxdb_3x_press = getResources().getDrawable(
				R.drawable.hxdb_3x_press);
		hxdb_3x_press.setBounds(0, 0, my_size, my_size);*/

		final Drawable map_3x = getResources().getDrawable(R.drawable.map_3x);
		map_3x.setBounds(0, 0, my_size, my_size);

		final Drawable map_3x_press = getResources().getDrawable(
				R.drawable.map_3x_press);
		map_3x_press.setBounds(0, 0, my_size, my_size);

		final Drawable line_3x = getResources().getDrawable(R.drawable.line_3x);
		line_3x.setBounds(0, 0, my_size, my_size);

		final Drawable line_3x_press = getResources().getDrawable(
				R.drawable.line_3x_press);
		line_3x_press.setBounds(0, 0, my_size, my_size);

		final Drawable data_3x = getResources().getDrawable(R.drawable.data_3x);
		data_3x.setBounds(0, 0, my_size, my_size);

		final Drawable data_3x_press = getResources().getDrawable(
				R.drawable.data_3x_press);
		data_3x_press.setBounds(0, 0, my_size, my_size);
		
		final Drawable tjfx_3x = getResources().getDrawable(R.drawable.tjfx_3x);
		tjfx_3x.setBounds(0, 0, my_size, my_size);

		final Drawable tjfx_3x_press = getResources().getDrawable(
				R.drawable.tjfx_3x_press);
		tjfx_3x_press.setBounds(0, 0, my_size, my_size);

		/*final Drawable set_up_3x = getResources().getDrawable(
				R.drawable.set_up_3x);
		set_up_3x.setBounds(0, 0, my_size, my_size);

		final Drawable set_up_3x_press = getResources().getDrawable(
				R.drawable.set_up_3x_press);
		set_up_3x_press.setBounds(0, 0, my_size, my_size);*/

		/*if (hxdb_tjfx_setup == 0) {
			hxdb.setChecked(true);
			mTabHost.setCurrentTab(0);
			hxdb.setCompoundDrawables(null, hxdb_3x_press, null, null);
			tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
			set_up.setCompoundDrawables(null, set_up_3x, null, null);
		} else if (hxdb_tjfx_setup == 1) {
			tjfx.setChecked(true);
			mTabHost.setCurrentTab(1);
			hxdb.setCompoundDrawables(null, hxdb_3x, null, null);
			tjfx.setCompoundDrawables(null, tjfx_3x_press, null, null);
			set_up.setCompoundDrawables(null, set_up_3x, null, null);
		} else if (hxdb_tjfx_setup == 2) {
			set_up.setChecked(true);
			mTabHost.setCurrentTab(2);
			hxdb.setCompoundDrawables(null, hxdb_3x, null, null);
			tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
			set_up.setCompoundDrawables(null, set_up_3x_press, null, null);
		}*/
		
		if (dtgl_qxdb_sjdb_tjfx == 0) {
			main_tab_dtgl.setChecked(true);
			mTabHost.setCurrentTab(0);
			main_tab_dtgl.setCompoundDrawables(null, map_3x_press, null, null);
			main_tab_qxdb.setCompoundDrawables(null, line_3x, null, null);
			main_tab_sjdb.setCompoundDrawables(null, data_3x, null, null);
			main_tab_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
		} else if (dtgl_qxdb_sjdb_tjfx == 1) {
			main_tab_qxdb.setChecked(true);
			mTabHost.setCurrentTab(1);
			main_tab_dtgl.setCompoundDrawables(null, map_3x, null, null);
			main_tab_qxdb.setCompoundDrawables(null, line_3x_press, null, null);
			main_tab_sjdb.setCompoundDrawables(null, data_3x, null, null);
			main_tab_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
		} else if (dtgl_qxdb_sjdb_tjfx == 2) {
			main_tab_dtgl.setChecked(true);
			mTabHost.setCurrentTab(2);
			main_tab_dtgl.setCompoundDrawables(null, map_3x, null, null);
			main_tab_qxdb.setCompoundDrawables(null, line_3x, null, null);
			main_tab_sjdb.setCompoundDrawables(null, data_3x_press, null, null);
			main_tab_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
		} else if (dtgl_qxdb_sjdb_tjfx == 3) {
			main_tab_tjfx.setChecked(true);
			mTabHost.setCurrentTab(3);
			main_tab_dtgl.setCompoundDrawables(null, map_3x, null, null);
			main_tab_qxdb.setCompoundDrawables(null, line_3x, null, null);
			main_tab_sjdb.setCompoundDrawables(null, data_3x, null, null);
			main_tab_tjfx.setCompoundDrawables(null, tjfx_3x_press, null, null);
		}

		main_radioGroup = (RadioGroup) findViewById(R.id.main_radio);

		main_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == main_tab_dtgl.getId()) {
							mTabHost.setCurrentTab(0);
							main_tab_dtgl.setCompoundDrawables(null, map_3x_press, null, null);
							main_tab_qxdb.setCompoundDrawables(null, line_3x, null, null);
							main_tab_sjdb.setCompoundDrawables(null, data_3x, null, null);
							main_tab_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
						}else if (checkedId == main_tab_qxdb.getId()) {
							mTabHost.setCurrentTab(1);
							main_tab_dtgl.setCompoundDrawables(null, map_3x, null, null);
							main_tab_qxdb.setCompoundDrawables(null, line_3x_press, null, null);
							main_tab_sjdb.setCompoundDrawables(null, data_3x, null, null);
							main_tab_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
						}else if (checkedId == main_tab_sjdb.getId()) {
							mTabHost.setCurrentTab(2);
							main_tab_dtgl.setCompoundDrawables(null, map_3x, null, null);
							main_tab_qxdb.setCompoundDrawables(null, line_3x, null, null);
							main_tab_sjdb.setCompoundDrawables(null, data_3x_press, null, null);
							main_tab_tjfx.setCompoundDrawables(null, tjfx_3x, null, null);
						}else if (checkedId == main_tab_tjfx.getId()) {
							mTabHost.setCurrentTab(3);
							main_tab_dtgl.setCompoundDrawables(null, map_3x, null, null);
							main_tab_qxdb.setCompoundDrawables(null, line_3x, null, null);
							main_tab_sjdb.setCompoundDrawables(null, data_3x, null, null);
							main_tab_tjfx.setCompoundDrawables(null, tjfx_3x_press, null, null);
						}
					}
				});
		
		if(LoginState.getIns().getUi_type().equals("4")){
			//中建八局用户ui_type="4"
			main_tab_tjfx.setVisibility(View.GONE);
		}

	}

}
