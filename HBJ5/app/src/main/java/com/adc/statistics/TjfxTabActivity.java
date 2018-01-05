package com.adc.statistics;

import java.util.ArrayList;
import java.util.List;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.hbj5.R;
import com.adc.hbj5.MyActivityManager;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressWarnings("deprecation")
public class TjfxTabActivity extends TabActivity {

	public static TabHost mTabHost;

	private RadioGroup tjfx_radioGroup;
	private RadioButton pm10_concentration_rank;		//pm10浓度排名
	private RadioButton pm10_hourly_mean_distribution;	//pm10小时均值分布
	private RadioButton pm10_hourly_mean_overproof_rank;		//PM10小时均值超标情况排名
	private RadioButton surface_water_data_effective_rate_rank;	//地表水数据传输有效率排名
	private RadioButton surface_water_overproof_time_rank;		//地表水超标时长排名
	private RadioButton surface_water_major_pollution_source_proportion;	//地表水主要污染源占比情况
	private List<RadioButton> radioButtons;
	
	final int my_size = 80;

	private Drawable pm10_monthly_report_3x_press;
	private Drawable pm10_distribution_3x_press;
	private Drawable concentration_rank_press;
	private Drawable hourly_mean_distribution_press;
	private Drawable effective_rate_rank_press;
	private Drawable tle_rank_press;
	private Drawable proportion_press;
	private List<Drawable> pressedDrawables;

	private Drawable pm10_monthly_report_3x;
	private Drawable pm10_distribution_3x;
	private Drawable concentration_rank;
	private Drawable hourly_mean_distribution;
	private Drawable effective_rate_rank;
	private Drawable tle_rank;
	private Drawable proportion;
	private List<Drawable> unpressedDrawables;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tjfx_tab);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(TjfxTabActivity.this);

		mTabHost = getTabHost();
		final TabWidget tabWidget = mTabHost.getTabWidget();
		// tabWidget.setStripEnabled(false);
		pm10_concentration_rank = (RadioButton) findViewById(R.id.pm10_concentration_rank);
		pm10_hourly_mean_distribution = (RadioButton) findViewById(R.id.pm10_hourly_mean_distribution);
		pm10_hourly_mean_overproof_rank = (RadioButton) findViewById(R.id.pm10_hourly_mean_overproof_rank);
		surface_water_data_effective_rate_rank = (RadioButton) findViewById(R.id.surface_water_data_effective_rate_rank);
		surface_water_overproof_time_rank = (RadioButton) findViewById(R.id.surface_water_overproof_time_rank);
		surface_water_major_pollution_source_proportion = (RadioButton) findViewById(R.id.surface_water_major_pollution_source_proportion);

		radioButtons = new ArrayList<RadioButton>();
		radioButtons.add(pm10_concentration_rank);
		radioButtons.add(pm10_hourly_mean_distribution);
		radioButtons.add(pm10_hourly_mean_overproof_rank);
		radioButtons.add(surface_water_data_effective_rate_rank);
		radioButtons.add(surface_water_overproof_time_rank);
		radioButtons.add(surface_water_major_pollution_source_proportion);

		pm10_monthly_report_3x_press = getResources().getDrawable(R.drawable.pm10_monthly_report_3x_press);
		pm10_monthly_report_3x_press.setBounds(0, 0, my_size, my_size);
		pm10_distribution_3x_press = getResources().getDrawable(R.drawable.pm10_distribution_3x_press);
		pm10_distribution_3x_press.setBounds(0, 0, my_size, my_size);
		concentration_rank_press = getResources().getDrawable(R.drawable.concentration_rank_press);
		concentration_rank_press.setBounds(0, 0, my_size, my_size);
		hourly_mean_distribution_press = getResources().getDrawable(R.drawable.hourly_mean_distribution_press);
		hourly_mean_distribution_press.setBounds(0, 0, my_size, my_size);
		effective_rate_rank_press = getResources().getDrawable(R.drawable.effective_rate_rank_press);
		effective_rate_rank_press.setBounds(0, 0, my_size, my_size);
		tle_rank_press = getResources().getDrawable(R.drawable.tle_rank_press);
		tle_rank_press.setBounds(0, 0, my_size, my_size);
		proportion_press = getResources().getDrawable(R.drawable.proportion_press);
		proportion_press.setBounds(0, 0, my_size, my_size);
		pressedDrawables = new ArrayList<Drawable>();
		//pressedDrawables.add(pm10_monthly_report_3x_press);
		//pressedDrawables.add(pm10_distribution_3x_press);
		pressedDrawables.add(concentration_rank_press);
		pressedDrawables.add(hourly_mean_distribution_press);
		pressedDrawables.add(pm10_monthly_report_3x_press);
		pressedDrawables.add(effective_rate_rank_press);
		pressedDrawables.add(tle_rank_press);
		pressedDrawables.add(proportion_press);

		pm10_distribution_3x = getResources().getDrawable(R.drawable.pm10_distribution_3x);
		pm10_distribution_3x.setBounds(0, 0, my_size, my_size);
		pm10_monthly_report_3x = getResources().getDrawable(R.drawable.pm10_monthly_report_3x);
		pm10_monthly_report_3x.setBounds(0, 0, my_size, my_size);
		concentration_rank = getResources().getDrawable(R.drawable.concentration_rank);
		concentration_rank.setBounds(0, 0, my_size, my_size);
		hourly_mean_distribution = getResources().getDrawable(R.drawable.hourly_mean_distribution);
		hourly_mean_distribution.setBounds(0, 0, my_size, my_size);
		effective_rate_rank = getResources().getDrawable(R.drawable.effective_rate_rank);
		effective_rate_rank.setBounds(0, 0, my_size, my_size);
		tle_rank = getResources().getDrawable(R.drawable.tle_rank);
		tle_rank.setBounds(0, 0, my_size, my_size);
		proportion = getResources().getDrawable(R.drawable.proportion);
		proportion.setBounds(0, 0, my_size, my_size);
		unpressedDrawables = new ArrayList<Drawable>();
		//unpressedDrawables.add(pm10_monthly_report_3x);
		//unpressedDrawables.add(pm10_distribution_3x);
		unpressedDrawables.add(concentration_rank);
		unpressedDrawables.add(hourly_mean_distribution);
		unpressedDrawables.add(pm10_monthly_report_3x);
		unpressedDrawables.add(effective_rate_rank);
		unpressedDrawables.add(tle_rank);
		unpressedDrawables.add(proportion);


		mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("1")
				.setContent(new Intent(this, Pm10ConcentrationRankActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("2")
				.setContent(new Intent(this, Pm10HourlyMeanDistributionActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("3")
						.setContent(new Intent(this, Pm10HourlyMeanOverproofRankActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG4").setIndicator("4")
				.setContent(new Intent(this, SurfaceWaterDataEffectiveRateRankActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG5").setIndicator("5")
				.setContent(new Intent(this, SurfaceWaterOverproofTimeRankActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG6").setIndicator("6")
				.setContent(new Intent(this, SurfaceWaterMajorPollutionSourceProportionActivity.class)));


		mTabHost.setCurrentTab(TjfxActivity.tabIdx);

		setRadioButtons(TjfxActivity.tabIdx);

		tjfx_radioGroup = (RadioGroup) findViewById(R.id.tjfx_radio);

		tjfx_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == pm10_concentration_rank.getId()) {
							setRadioButtons(0);
							mTabHost.setCurrentTab(0);
						} else if (checkedId == pm10_hourly_mean_distribution.getId()) {
							setRadioButtons(1);
							mTabHost.setCurrentTab(1);
						} else if (checkedId == pm10_hourly_mean_overproof_rank.getId()) {
							setRadioButtons(2);
							mTabHost.setCurrentTab(2);
						} else if (checkedId == surface_water_data_effective_rate_rank.getId()) {
							setRadioButtons(3);
							mTabHost.setCurrentTab(3);
						} else if (checkedId == surface_water_overproof_time_rank.getId()) {
							setRadioButtons(4);
							mTabHost.setCurrentTab(4);
						} else if (checkedId == surface_water_major_pollution_source_proportion.getId()) {
							setRadioButtons(5);
							mTabHost.setCurrentTab(5);
						} 
					}
				});

	}
	
	private void setRadioButtons(int selectedIdx){
		for(int i = 0;i < radioButtons.size();i++){
			RadioButton radioButton = radioButtons.get(i);
			if (i == selectedIdx) {
				radioButton.setChecked(true);
				Drawable drawable = pressedDrawables.get(i);
				radioButton.setCompoundDrawables(null, drawable, null, null);
			} else {
				Drawable drawable = unpressedDrawables.get(i);
				radioButton.setCompoundDrawables(null, drawable, null, null);
			}
		}
	}

}
