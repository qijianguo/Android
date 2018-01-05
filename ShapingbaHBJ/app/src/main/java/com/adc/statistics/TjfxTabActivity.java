package com.adc.statistics;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.shapingba.MyActivityManager;
import com.adc.shapingba.R;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressWarnings("deprecation")
public class TjfxTabActivity extends TabActivity {

	public static TabHost mTabHost;

	private RadioGroup tjfx_radioGroup;
	private RadioButton pm10_monthly_report;
	private RadioButton night_noise;
	private RadioButton night_noise_distribution;
	private RadioButton pm10_distribution;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tjfx_tab);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(TjfxTabActivity.this);

		mTabHost = getTabHost();
		final TabWidget tabWidget = mTabHost.getTabWidget();
		// tabWidget.setStripEnabled(false);

		pm10_monthly_report = (RadioButton) findViewById(R.id.pm10_monthly_report);
		night_noise = (RadioButton) findViewById(R.id.night_noise);
		night_noise_distribution = (RadioButton) findViewById(R.id.night_noise_distribution);
		pm10_distribution = (RadioButton) findViewById(R.id.pm10_distribution);

		//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
		if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
			night_noise.setVisibility(8);
			night_noise_distribution.setVisibility(8);
		}
		/*mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("0")
				.setContent(new Intent(this, Pm10MonthlyReportActivity.class)));*/
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("1")
				.setContent(new Intent(this, NightNoiseActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
				.setContent(new Intent(this, NoiseExceedRateActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
				.setContent(new Intent(this, Pm10DistributionActivity.class)));

		mTabHost.setCurrentTab(TjfxActivity.pm10mon_or_nightnoise_or_noisedis_or_pm10dis);

		final int my_size = 80;

		final Drawable pm10_monthly_report_3x_press = getResources()
				.getDrawable(R.drawable.pm10_monthly_report_3x_press);
		pm10_monthly_report_3x_press.setBounds(0, 0, my_size, my_size);

		final Drawable pm10_monthly_report_3x = getResources().getDrawable(
				R.drawable.pm10_monthly_report_3x);
		pm10_monthly_report_3x.setBounds(0, 0, my_size, my_size);

		final Drawable night_noise_3x_press = getResources().getDrawable(
				R.drawable.night_noise_3x_press);
		night_noise_3x_press.setBounds(0, 0, my_size, my_size);

		final Drawable night_noise_3x = getResources().getDrawable(
				R.drawable.night_noise_3x);
		night_noise_3x.setBounds(0, 0, my_size, my_size);

		final Drawable night_noise_distribution_3x_press = getResources()
				.getDrawable(R.drawable.night_noise_distribution_3x_press);
		night_noise_distribution_3x_press.setBounds(0, 0, my_size, my_size);

		final Drawable night_noise_distribution_3x = getResources()
				.getDrawable(R.drawable.night_noise_distribution_3x);
		night_noise_distribution_3x.setBounds(0, 0, my_size, my_size);

		final Drawable pm10_distribution_3x_press = getResources().getDrawable(
				R.drawable.pm10_distribution_3x_press);
		pm10_distribution_3x_press.setBounds(0, 0, my_size, my_size);

		final Drawable pm10_distribution_3x = getResources().getDrawable(
				R.drawable.pm10_distribution_3x);
		pm10_distribution_3x.setBounds(0, 0, my_size, my_size);

		if (TjfxActivity.pm10mon_or_nightnoise_or_noisedis_or_pm10dis == 0) {
			pm10_monthly_report.setChecked(true);
			pm10_monthly_report.setCompoundDrawables(null,
					pm10_monthly_report_3x_press, null, null);
			night_noise.setCompoundDrawables(null, night_noise_3x, null, null);
			night_noise_distribution.setCompoundDrawables(null,
					night_noise_distribution_3x, null, null);
			pm10_distribution.setCompoundDrawables(null, pm10_distribution_3x,
					null, null);
		} else if (TjfxActivity.pm10mon_or_nightnoise_or_noisedis_or_pm10dis == 1) {
			night_noise.setChecked(true);
			pm10_monthly_report.setCompoundDrawables(null,
					pm10_monthly_report_3x, null, null);
			night_noise.setCompoundDrawables(null, night_noise_3x_press, null,
					null);
			night_noise_distribution.setCompoundDrawables(null,
					night_noise_distribution_3x, null, null);
			pm10_distribution.setCompoundDrawables(null, pm10_distribution_3x,
					null, null);
		} else if (TjfxActivity.pm10mon_or_nightnoise_or_noisedis_or_pm10dis == 2) {
			night_noise_distribution.setChecked(true);
			pm10_monthly_report.setCompoundDrawables(null,
					pm10_monthly_report_3x, null, null);
			night_noise.setCompoundDrawables(null, night_noise_3x, null, null);
			night_noise_distribution.setCompoundDrawables(null,
					night_noise_distribution_3x_press, null, null);
			pm10_distribution.setCompoundDrawables(null, pm10_distribution_3x,
					null, null);
		} else if (TjfxActivity.pm10mon_or_nightnoise_or_noisedis_or_pm10dis == 3) {
			pm10_distribution.setChecked(true);
			pm10_monthly_report.setCompoundDrawables(null,
					pm10_monthly_report_3x, null, null);
			night_noise.setCompoundDrawables(null, night_noise_3x, null, null);
			night_noise_distribution.setCompoundDrawables(null,
					night_noise_distribution_3x, null, null);
			pm10_distribution.setCompoundDrawables(null,
					pm10_distribution_3x_press, null, null);
		}

		tjfx_radioGroup = (RadioGroup) findViewById(R.id.tjfx_radio);

		tjfx_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == pm10_monthly_report.getId()) {
							pm10_monthly_report.setCompoundDrawables(null,
									pm10_monthly_report_3x_press, null, null);
							night_noise.setCompoundDrawables(null,
									night_noise_3x, null, null);
							night_noise_distribution.setCompoundDrawables(null,
									night_noise_distribution_3x, null, null);
							pm10_distribution.setCompoundDrawables(null,
									pm10_distribution_3x, null, null);
							mTabHost.setCurrentTab(0);
						} else if (checkedId == night_noise.getId()) {
							pm10_monthly_report.setCompoundDrawables(null,
									pm10_monthly_report_3x, null, null);
							night_noise.setCompoundDrawables(null,
									night_noise_3x_press, null, null);
							night_noise_distribution.setCompoundDrawables(null,
									night_noise_distribution_3x, null, null);
							pm10_distribution.setCompoundDrawables(null,
									pm10_distribution_3x, null, null);
							mTabHost.setCurrentTab(1);
						} else if (checkedId == night_noise_distribution
								.getId()) {
							pm10_monthly_report.setCompoundDrawables(null,
									pm10_monthly_report_3x, null, null);
							night_noise.setCompoundDrawables(null,
									night_noise_3x, null, null);
							night_noise_distribution.setCompoundDrawables(null,
									night_noise_distribution_3x_press, null,
									null);
							pm10_distribution.setCompoundDrawables(null,
									pm10_distribution_3x, null, null);
							mTabHost.setCurrentTab(2);
						} else if (checkedId == pm10_distribution.getId()) {
							pm10_monthly_report.setCompoundDrawables(null,
									pm10_monthly_report_3x, null, null);
							night_noise.setCompoundDrawables(null,
									night_noise_3x, null, null);
							night_noise_distribution.setCompoundDrawables(null,
									night_noise_distribution_3x, null, null);
							pm10_distribution.setCompoundDrawables(null,
									pm10_distribution_3x_press, null, null);
							mTabHost.setCurrentTab(3);
						}
					}
				});

	}

}
