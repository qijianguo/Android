package com.adc.statistics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.hbj5.R;
import com.adc.hbj5.DateTimePickDialogUtil;
import com.adc.hbj5.MainTabActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.util.UIUtil;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressLint("HandlerLeak")
public class Pm10MonthlyReportActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int QUERY_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private Handler handler = null;
	private Button pm10_monthly_report_goback;
	private EditText pm10_monthly_report_start_time;
	private Button pm10_monthly_report_query;
	private Spinner pm10_monthly_report_spinner;
	private RadioGroup pm10_monthly_report_radioGroup;
	private RadioButton mr_pm2_5Button;
	private RadioButton mr_pm10Button;
	private LinearLayout pm10_monthly_report_layout;
	private int pm2_5_or_pm10;
	private int all_or_work_or_wharf;
	private String start_timesString;
	private List<String> type_list;
	private List<String> spot_name_list;
	private List<String> pm2_5_monthly_report_value_list;
	private List<String> pm10_monthly_report_value_list;

	private String serverURL;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//MainTabActivity.hxdb_tjfx_setup = 1;
			/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
			Intent intent = new Intent(Pm10MonthlyReportActivity.this,
					MainTabActivity.class);
			startActivity(intent);*/
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pm10_monthly_report);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(Pm10MonthlyReportActivity.this);

		serverURL = LoginState.getIns().getServerURL();

		pm10_monthly_report_goback = (Button) findViewById(R.id.pm10_monthly_report_goback);
		pm10_monthly_report_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//MainTabActivity.hxdb_tjfx_setup = 1;
				/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
				Intent intent = new Intent(Pm10MonthlyReportActivity.this,
						MainTabActivity.class);
				startActivity(intent);*/
				finish();
			}
		});

		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -1);
		final String time1 = new SimpleDateFormat("yyyy-MM").format(calendar1
				.getTime());
		pm10_monthly_report_start_time = (EditText) findViewById(R.id.pm10_monthly_report_start_time);
		pm10_monthly_report_start_time.setText(time1);
		pm10_monthly_report_start_time
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
								Pm10MonthlyReportActivity.this, "");
						dataTimePickDialog.dateTimePicKDialog(
								pm10_monthly_report_start_time, 0, 7);
					}
				});

		pm10_monthly_report_spinner = (Spinner) findViewById(R.id.pm10_monthly_report_spinner);
		pm10_monthly_report_spinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						checkConnectWhenClickButton();
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});
		type_list = new ArrayList<String>();
		type_list.add("全部类型");
		type_list.add("工地");
		type_list.add("码头");
		ArrayAdapter<String> type_list_Adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, type_list);
		type_list_Adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pm10_monthly_report_spinner.setAdapter(type_list_Adapter);

		pm10_monthly_report_radioGroup = (RadioGroup) findViewById(R.id.pm10_monthly_report_radio);
		mr_pm10Button = (RadioButton) findViewById(R.id.mr_pm10);
		mr_pm2_5Button = (RadioButton) findViewById(R.id.mr_pm2_5);

		pm2_5_or_pm10 = 0;
		all_or_work_or_wharf = 0;

		pm10_monthly_report_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						if (checkedId == mr_pm10Button.getId()) {
							pm2_5_or_pm10 = 1;
							checkConnectWhenClickButton();
						} else if (checkedId == mr_pm2_5Button.getId()) {
							pm2_5_or_pm10 = 0;
							checkConnectWhenClickButton();
						}
					}
				});

		pm10_monthly_report_query = (Button) findViewById(R.id.pm10_monthly_report_query);
		pm10_monthly_report_layout = (LinearLayout) findViewById(R.id.pm10_monthly_report_layout);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					showQueryProgress();
					queryMonthlyReport();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case START_PROCESS:
					showQueryProgress();
					break;
				case CANCEL_PROCESS:
					cancelProgress();
					break;
				case QUERY_SUCCEED:
					showResult();
					cancelProgress();
					break;
				case URL_REQUEST_FAIL:
					showServerError();
					break;
				default:
					break;
				}
			}
		};
		pm10_monthly_report_query.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				checkConnectWhenClickButton();
			}
		});
	}

	public GraphicalView get_barchart(List<String> name, List<String> value,
			String lengend_name) {
		// 多个渲染
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		// 横向排列,实在是太丑了。。
		// renderer.setOrientation(Orientation.VERTICAL);
		// 多个序列的数据集
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		// 构建数据集以及渲染

		double max_y = 0;// y轴的最大值
		for (int i = 0; i < 1; i++) {
			// 柱状图内容名称
			XYSeries series = new XYSeries(lengend_name);

			for (int j = 0; j < value.size(); j++) {
				Log.i("heheda", "value=" + value.get(j));
				series.add(j + 0.6, Double.valueOf(value.get(j)));
				if (Double.valueOf(value.get(j)) > max_y)
					max_y = Double.valueOf(value.get(j));
				renderer.addXTextLabel(j + 0.6, name.get(j));
			}
			dataset.addSeries(series);
			XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
			xyRenderer.setChartValuesTextSize(30);
			xyRenderer.setDisplayChartValues(true);
			// 设置颜色
			xyRenderer.setColor(Color.rgb(100, 181, 234));
			// 设置点的样式
			// xyRenderer.setPointStyle(PointStyle.);
			// 将要绘制的点添加到坐标绘制中
			renderer.addSeriesRenderer(xyRenderer);
		}
		// 设置x轴标签数
		renderer.setXLabels(0);
		// 设置Y轴标签数
		// renderer.setYLabels(1);
		// 设置x轴的最大值
		// renderer.setXAxisMax(spot_num+2);
		// 设置轴的颜色
		renderer.setAxesColor(Color.BLACK);
		// 设置x轴和y轴的标签对齐方式
		renderer.setXLabelsAlign(Align.LEFT);
		renderer.setYLabelsAlign(Align.CENTER);

		// renderer.setXLabelsPadding(-50);
		// 设置现实网格
		renderer.setShowGrid(true);

		renderer.setShowAxes(true);
		// 设置条形图之间的距离
		renderer.setBarSpacing(0.2);
		// 设置柱子宽度
		// renderer.setBarWidth(5.0f);

		renderer.setInScroll(false);

		renderer.setClickEnabled(false);
		// 设置x轴和y轴标签的颜色
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);

		int length = renderer.getSeriesRendererCount();
		// 设置图标的标题
		// renderer.setChartTitle("jianceshuju");
		// renderer.setLabelsColor(Color.BLACK);

		// 设置图例的字体大小
		renderer.setLegendTextSize(30);
		// 设置x轴标记字体大小
		renderer.setLabelsTextSize(30);
		// 设置x轴标记旋转角度
		renderer.setXLabelsAngle(45f);
		// 设置允许左右拖动,但不允许上下拖动
		renderer.setPanEnabled(true, false);
		// 设置不允许缩放
		renderer.setZoomEnabled(false);

		// 设置x轴和y轴的最大最小值
		renderer.setRange(new double[] { 0, 10, 0, max_y + 10 });
		renderer.setMarginsColor(0x00888888);
		renderer.setDisplayValues(true);
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer ssr = renderer.getSeriesRendererAt(i);
			/*ssr.setChartValuesTextAlign(Align.CENTER);
			ssr.setChartValuesTextSize(30);
			ssr.setDisplayChartValues(true);*/
		}
		GraphicalView mChartView = ChartFactory.getBarChartView(
				getApplicationContext(), dataset, renderer, Type.STACKED);

		return mChartView;

	}

	private byte[] readStream(InputStream is) throws IOException {
		// TODO Auto-generated method stub
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = is.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] in2b = swapStream.toByteArray();
		return in2b;
	}

	protected void checkConnectWhenClickButton() {
		new Thread() {
			@Override
			public void run() {
				ConnectivityManager con = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				boolean wifi = con
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
						.isConnectedOrConnecting();
				boolean internet = con.getNetworkInfo(
						ConnectivityManager.TYPE_MOBILE)
						.isConnectedOrConnecting();
				if (wifi | internet) {
					handler.sendEmptyMessage(NETWORK_CONNECTED);
				} else {
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
				}
			}
		}.start();
	}

	protected void queryMonthlyReport() {
		new Thread() {
			@Override
			public void run() {
				start_timesString = pm10_monthly_report_start_time.getText()
						.toString().substring(0, 7);
				all_or_work_or_wharf = pm10_monthly_report_spinner
						.getSelectedItemPosition();
				String json = null;
				String query_path = serverURL+"dustMonthReport?city_id="
						+ LoginState.getIns().getCityId()
						+ "&time="
						+ start_timesString + "&type=" + all_or_work_or_wharf;
				URL url;

				spot_name_list = new ArrayList<String>();
				pm2_5_monthly_report_value_list = new ArrayList<String>();
				pm10_monthly_report_value_list = new ArrayList<String>();
				try {
					url = new URL(query_path);
					// Log.i("heheda",get_spot_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(50 * 1000);
					conn.setRequestMethod("GET");
					int responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = readStream(is);
						json = new String(data);

						JSONArray jsonArray = new JSONArray(json);

						//天津->特判，对于企业用户只能显示其自身的数据
						boolean is_tianjin = false;
						//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
						if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
							is_tianjin = true;
						}
						int csite_id = Integer.valueOf(LoginState.getIns().getCsiteId());
						
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							if(is_tianjin){
								//是天津的企业用户，csite_id需要匹配csite_2(csite_id2=从json串中读出来的csite_id)
								int csite_id2 = Integer.valueOf(item.getString("csite_id"));
								if(csite_id != -1 && csite_id != csite_id2){
									//Log.i("heheda", "csite_id="+csite_id+",csite_id2="+csite_id2);
									continue;
								}
							}
							String csite_name = item.getString("csite_name");
							spot_name_list.add(csite_name);
							String pm2_5 = item.getString("pm2_5");
							pm2_5_monthly_report_value_list.add(pm2_5);
							String pm10 = item.getString("pm10");
							pm10_monthly_report_value_list.add(pm10);
						}
						handler.sendEmptyMessage(QUERY_SUCCEED);
					} else {
						handler.sendEmptyMessage(URL_REQUEST_FAIL);
						handler.sendEmptyMessage(CANCEL_PROCESS);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					handler.sendEmptyMessage(CANCEL_PROCESS);
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected void showResult() {
		pm10_monthly_report_layout.removeAllViews();
		if (pm2_5_or_pm10 == 0) {
			pm10_monthly_report_layout.addView(
					get_barchart(spot_name_list,
							pm2_5_monthly_report_value_list, "pm2.5"),
					new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
		} else {
			pm10_monthly_report_layout.addView(
					get_barchart(spot_name_list,
							pm10_monthly_report_value_list, "pm10"),
					new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
		}
	}

	protected void showNetworkError() {
		Toast toast = Toast.makeText(Pm10MonthlyReportActivity.this, "网络异常",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(Pm10MonthlyReportActivity.this, "服务器维护中，请稍后再试",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 查询进度条
	 */
	private void showQueryProgress() {
		UIUtil.showProgressDialog(this, "查询中......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
}
