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
import org.achartengine.model.CategorySeries;
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
import com.adc.util.ReadStream;
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
public class Pm10HourlyMeanOverproofRankActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int QUERY_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private Handler handler = null;
	private Button pm10_hourly_mean_overproof_rank_goback;
	private EditText pm10_hourly_mean_overproof_rank_start_time;
	private Button pm10_hourly_mean_overproof_rank_query;
	private LinearLayout pm10_hourly_mean_overproof_rank_layout;
	private String start_timesString;
	private List<String> spot_name_list;
	private List<String> blue_list;
	private List<String> red_list;
	private List<String> yellow_list;
	
	private String serverURL;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//MainTabActivity.hxdb_tjfx_setup = 1;
			/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
			Intent intent = new Intent(Pm10ConcentrationRankActivity.this,
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
		setContentView(R.layout.activity_pm10_hourly_mean_overproof_rank);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(Pm10HourlyMeanOverproofRankActivity.this);

		serverURL = LoginState.getIns().getServerURL();

		pm10_hourly_mean_overproof_rank_goback = (Button) findViewById(R.id.pm10_hourly_mean_overproof_rank_goback);
		pm10_hourly_mean_overproof_rank_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//MainTabActivity.hxdb_tjfx_setup = 1;
				/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
				Intent intent = new Intent(Pm10ConcentrationRankActivity.this,
						MainTabActivity.class);
				startActivity(intent);*/
				finish();
			}
		});

		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -1);
		final String time1 = new SimpleDateFormat("yyyy-MM").format(calendar1
				.getTime());
		pm10_hourly_mean_overproof_rank_start_time = (EditText) findViewById(R.id.pm10_hourly_mean_overproof_rank_start_time);
		pm10_hourly_mean_overproof_rank_start_time.setText(time1);
		pm10_hourly_mean_overproof_rank_start_time
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
								Pm10HourlyMeanOverproofRankActivity.this, "");
						dataTimePickDialog.dateTimePicKDialog(
								pm10_hourly_mean_overproof_rank_start_time, 0, 7);
					}
				});

		pm10_hourly_mean_overproof_rank_query = (Button) findViewById(R.id.pm10_hourly_mean_overproof_rank_query);
		pm10_hourly_mean_overproof_rank_layout = (LinearLayout) findViewById(R.id.pm10_hourly_mean_overproof_rank_layout);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					showQueryProgress();
					query();
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
		pm10_hourly_mean_overproof_rank_query.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				checkConnectWhenClickButton();
			}
		});
		
		checkConnectWhenClickButton();
	}

	public GraphicalView getTripleValueBarchart(List<String> name, List<String> blue_list,
			List<String> yellow_list, List<String> red_list, String lengend_name) {
		
		String[] titles = new String[] {">150", ">100", ">50"};
		int size = blue_list.size();
		double[] blues = new double[size];
		double[] yellows = new double[size];
		double[] reds = new double[size];
		double max_y = 0;
		for(int i = 0;i < size;i++){
			reds[i] = Double.valueOf(red_list.get(i));
			yellows[i] = Double.valueOf(yellow_list.get(i));
			blues[i] = Double.valueOf(blue_list.get(i));
			double total = reds[i]+yellows[i]+blues[i];
			if (total > max_y) {
				max_y = total;
			}
		}
		List<double[]> values = new ArrayList<double[]>();
		values.add(reds);
		values.add(yellows);
		values.add(blues);
		
		// 多个渲染
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		// 横向排列,实在是太丑了。。
		// renderer.setOrientation(Orientation.VERTICAL);
		
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
		renderer.setDisplayValues(false);

		// red
		XYSeriesRenderer redRenderer = new XYSeriesRenderer();
		redRenderer.setColor(Color.parseColor("#A52A2A"));
		redRenderer.setDisplayChartValues(false);
		renderer.addSeriesRenderer(redRenderer);
		
		// yellow
		XYSeriesRenderer yellowRenderer = new XYSeriesRenderer();
		yellowRenderer.setColor(Color.parseColor("#FFA07A"));
		yellowRenderer.setDisplayChartValues(false);
		renderer.addSeriesRenderer(yellowRenderer);
		
		// blue
		XYSeriesRenderer blueRenderer = new XYSeriesRenderer();
		blueRenderer.setColor(Color.parseColor("#B0E2FF"));
		blueRenderer.setDisplayChartValues(false);
		renderer.addSeriesRenderer(blueRenderer);

		// x轴文字
		for (int i = 0; i < size; i++) {
			renderer.addXTextLabel(i+1.0, name.get(i));
		}
		GraphicalView mChartView = ChartFactory.getBarChartView(
				getApplicationContext(), buildBarDataset(titles, values), renderer, Type.HEAPED);

		return mChartView;

	}

	protected XYMultipleSeriesDataset buildBarDataset(String[] titles, List<double[]> values) {

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        int length = titles.length;
        for (int i = 0; i < length; i++) {
            CategorySeries series = new CategorySeries(titles[i]);
            double[] v = values.get(i);
            int seriesLength = v.length;
            for (int k = 0; k < seriesLength; k++) {
                series.add(v[k]);
            }
            dataset.addSeries(series.toXYSeries());
        }

        return dataset;
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

	protected void query() {
		new Thread() {
			@Override
			public void run() {
				start_timesString = pm10_hourly_mean_overproof_rank_start_time.getText()
						.toString().substring(0, 7);
				String json = null;
				String query_path = serverURL+"jingzhou/pm10/chaobiaoqingkuang?city_id="
						+ LoginState.getIns().getCityId()
						+ "&time="
						+ start_timesString;
				Log.i("heheda", query_path);
				URL url;

				spot_name_list = new ArrayList<String>();
				blue_list = new ArrayList<String>();
				yellow_list = new ArrayList<String>();
				red_list = new ArrayList<String>();
				
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
						byte[] data = ReadStream.readStream(is);
						json = new String(data);
						JSONObject jsonObject = new JSONObject(json);
						String jsonArrayString = jsonObject.getString("data");
						JSONArray jsonArray = new JSONArray(jsonArrayString);
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
							String csite_name = item.getString("name");
							spot_name_list.add(csite_name);
							String blue = item.getString("blue");
							blue_list.add(blue);
							String yellow = item.getString("yellow");
							yellow_list.add(yellow);
							String red = item.getString("red");
							red_list.add(red);
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
		pm10_hourly_mean_overproof_rank_layout.removeAllViews();
		pm10_hourly_mean_overproof_rank_layout.addView(
				getTripleValueBarchart(spot_name_list,
						blue_list,yellow_list,red_list, "小时"),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
	}

	protected void showNetworkError() {
		Toast toast = Toast.makeText(Pm10HourlyMeanOverproofRankActivity.this, "网络异常",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(Pm10HourlyMeanOverproofRankActivity.this, "服务器维护中，请稍后再试",
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
