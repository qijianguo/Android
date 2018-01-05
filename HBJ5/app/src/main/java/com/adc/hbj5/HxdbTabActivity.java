package com.adc.hbj5;

import com.adc.hbj5.R;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressWarnings("deprecation")
public class HxdbTabActivity extends TabActivity {

	public static TabHost mTabHost;

	private RadioGroup hxdb_radioGroup;
	private RadioButton dtgl;
	private RadioButton qxdb;
	private RadioButton sjdb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hxdb_tab);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(HxdbTabActivity.this);

		mTabHost = getTabHost();
		final TabWidget tabWidget = mTabHost.getTabWidget();
		// tabWidget.setStripEnabled(false);

		dtgl = (RadioButton) findViewById(R.id.dtgl);
		qxdb = (RadioButton) findViewById(R.id.qxdb);
		sjdb = (RadioButton) findViewById(R.id.sjdb);

		mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("0")
				.setContent(new Intent(this, MapOverviewActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("1")
				.setContent(new Intent(this, CurveContrastActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
				.setContent(new Intent(this, DataContrastActivity.class)));

		mTabHost.setCurrentTab(HxdbActivity.map_or_curve_or_data);

		final int my_size = 80;

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

		if (HxdbActivity.map_or_curve_or_data == 0) {
			dtgl.setChecked(true);
			dtgl.setCompoundDrawables(null, map_3x_press, null, null);
			qxdb.setCompoundDrawables(null, line_3x, null, null);
			sjdb.setCompoundDrawables(null, data_3x, null, null);
		} else if (HxdbActivity.map_or_curve_or_data == 1) {
			qxdb.setChecked(true);
			dtgl.setCompoundDrawables(null, map_3x, null, null);
			qxdb.setCompoundDrawables(null, line_3x_press, null, null);
			sjdb.setCompoundDrawables(null, data_3x, null, null);
		} else if (HxdbActivity.map_or_curve_or_data == 2) {
			sjdb.setChecked(true);
			dtgl.setCompoundDrawables(null, map_3x, null, null);
			qxdb.setCompoundDrawables(null, line_3x, null, null);
			sjdb.setCompoundDrawables(null, data_3x_press, null, null);
		}

		hxdb_radioGroup = (RadioGroup) findViewById(R.id.hxdb_radio);

		hxdb_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == dtgl.getId()) {
							dtgl.setCompoundDrawables(null, map_3x_press, null,
									null);
							qxdb.setCompoundDrawables(null, line_3x, null, null);
							sjdb.setCompoundDrawables(null, data_3x, null, null);
							mTabHost.setCurrentTab(0);
						} else if (checkedId == qxdb.getId()) {
							dtgl.setCompoundDrawables(null, map_3x, null, null);
							qxdb.setCompoundDrawables(null, line_3x_press,
									null, null);
							sjdb.setCompoundDrawables(null, data_3x, null, null);
							mTabHost.setCurrentTab(1);
						} else if (checkedId == sjdb.getId()) {
							dtgl.setCompoundDrawables(null, map_3x, null, null);
							qxdb.setCompoundDrawables(null, line_3x, null, null);
							sjdb.setCompoundDrawables(null, data_3x_press,
									null, null);
							mTabHost.setCurrentTab(2);
						}
					}
				});

	}

}
