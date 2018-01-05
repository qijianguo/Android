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
public class SurfaceWaterDataEffectiveRateRankActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int QUERY_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private Handler handler = null;
	private Button surface_water_data_effective_rate_rank_goback;
	private EditText surface_water_data_effective_rate_rank_start_time;
	private Button surface_water_data_effective_rate_rank_query;
	private LinearLayout surface_water_data_effective_rate_rank_layout;
	private int pm2_5_or_pm10;
	private String start_timesString;
	private List<String> spot_name_list;
	private List<String> value_list;
	private List<String> surface_water_data_effective_rate_rank_value_list;

	private String serverURL;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//MainTabActivity.hxdb_tjfx_setup = 1;
			/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
			Intent intent = new Intent(SurfaceWaterDataEffectiveRateRankActivity.this,
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
		setContentView(R.layout.activity_surface_water_data_effective_rate_rank);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SurfaceWaterDataEffectiveRateRankActivity.this);

		serverURL = LoginState.getIns().getServerURL();

		surface_water_data_effective_rate_rank_goback = (Button) findViewById(R.id.surface_water_data_effective_rate_rank_goback);
		surface_water_data_effective_rate_rank_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//MainTabActivity.hxdb_tjfx_setup = 1;
				/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
				Intent intent = new Intent(SurfaceWaterDataEffectiveRateRankActivity.this,
						MainTabActivity.class);
				startActivity(intent);*/
				finish();
			}
		});

		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -1);
		final String time1 = new SimpleDateFormat("yyyy-MM").format(calendar1
				.getTime());
		surface_water_data_effective_rate_rank_start_time = (EditText) findViewById(R.id.surface_water_data_effective_rate_rank_start_time);
		surface_water_data_effective_rate_rank_start_time.setText(time1);
		surface_water_data_effective_rate_rank_start_time
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
								SurfaceWaterDataEffectiveRateRankActivity.this, "");
						dataTimePickDialog.dateTimePicKDialog(
								surface_water_data_effective_rate_rank_start_time, 0, 7);
					}
				});

		surface_water_data_effective_rate_rank_query = (Button) findViewById(R.id.surface_water_data_effective_rate_rank_query);
		surface_water_data_effective_rate_rank_layout = (LinearLayout) findViewById(R.id.surface_water_data_effective_rate_rank_layout);

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
					
					break;
				default:
					break;
				}
			}
		};
		surface_water_data_effective_rate_rank_query.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				checkConnectWhenClickButton();
			}
		});
		
		checkConnectWhenClickButton();
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
				start_timesString = surface_water_data_effective_rate_rank_start_time.getText()
						.toString().substring(0, 7);
				String json = null;
				String query_path = serverURL+"jingzhou/water/youxiaolv?userId="
						+ LoginState.getIns().getUserId()
						+ "&time="
						+ start_timesString;
				Log.i("heheda", query_path);
				URL url;

				spot_name_list = new ArrayList<String>();
				value_list = new ArrayList<String>();
				surface_water_data_effective_rate_rank_value_list = new ArrayList<String>();
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
						JSONObject jsonObject = new JSONObject(json);
						String jsonArrayString = jsonObject.getString("data");
						JSONArray jsonArray = new JSONArray(jsonArrayString);						
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							String csite_name = item.getString("name");
							spot_name_list.add(csite_name);
							String val = item.getString("num");
							value_list.add(val);
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
		surface_water_data_effective_rate_rank_layout.removeAllViews();
		if (pm2_5_or_pm10 == 0) {
			surface_water_data_effective_rate_rank_layout.addView(
					get_barchart(spot_name_list,
							value_list, "pm2.5"),
					new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
		} else {
			surface_water_data_effective_rate_rank_layout.addView(
					get_barchart(spot_name_list,
							surface_water_data_effective_rate_rank_value_list, "百分比"),
					new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
		}
	}

	protected void showNetworkError() {
		Toast toast = Toast.makeText(SurfaceWaterDataEffectiveRateRankActivity.this, "网络异常",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(SurfaceWaterDataEffectiveRateRankActivity.this, "服务器维护中，请稍后再试",
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
