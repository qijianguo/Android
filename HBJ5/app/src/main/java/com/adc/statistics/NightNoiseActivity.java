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
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.hbj5.R;
import com.adc.hbj5.DateTimePickDialogUtil;
import com.adc.hbj5.MainTabActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.util.UIUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class NightNoiseActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int GET_SPOT_LIST_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 105;
	private final int QUERY_SPOT_SELECTED = 106;
	private final int QUERY_SUCCEED = 107;
	
	private Handler handler = null;
	private Button night_noise_goback;
	private EditText night_noise_start_time;
	private EditText night_noise_end_time;
	private Spinner night_noise_spot_spinner;
	private Button night_noise_query;
	private LinearLayout night_noise_layout;

	private List<String> spot_list;
	private String start_timeString;
	private String end_timeString;

	private List<String> query_time_list;
	private List<String> night_noise_value_list;

	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	
	private String serverURL;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			///MainTabActivity.hxdb_tjfx_setup = 1;
			/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
			Intent intent = new Intent(NightNoiseActivity.this,
					MainTabActivity.class);
			startActivity(intent);
			finish();*/
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_night_noise);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(NightNoiseActivity.this);

		serverURL = LoginState.getIns().getServerURL();
		
		night_noise_goback = (Button) findViewById(R.id.night_noise_goback);
		night_noise_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//MainTabActivity.hxdb_tjfx_setup = 1;
				/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
				Intent intent = new Intent(NightNoiseActivity.this,
						MainTabActivity.class);
				startActivity(intent);*/
				finish();
			}
		});

		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -8);
		final String time1 = new SimpleDateFormat("yyyy-MM-dd")
				.format(calendar1.getTime());
		night_noise_start_time = (EditText) findViewById(R.id.night_noise_start_time);
		night_noise_start_time.setText(time1);
		night_noise_start_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
						NightNoiseActivity.this, time1);
				dataTimePickDialog.dateTimePicKDialog(night_noise_start_time);
			}
		});

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.DATE, -1);
		final String time2 = new SimpleDateFormat("yyyy-MM-dd")
				.format(calendar2.getTime());
		night_noise_end_time = (EditText) findViewById(R.id.night_noise_end_time);
		night_noise_end_time.setText(time2);
		night_noise_end_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
						NightNoiseActivity.this, time2);
				dataTimePickDialog.dateTimePicKDialog(night_noise_end_time);
			}
		});

		night_noise_spot_spinner = (Spinner) findViewById(R.id.night_noise_spot_spinner);
		night_noise_spot_spinner
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

		night_noise_query = (Button) findViewById(R.id.night_noise_query);
		night_noise_layout = (LinearLayout) findViewById(R.id.night_noise_layout);
		Log.i("heheda", "yejianzaosheng  jiazaizhong!");
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getSpotNameList();
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
				case GET_SPOT_LIST_SUCCEED:
					loadSpinner();
					break;
				case URL_REQUEST_FAIL:
					showServerError();
					break;
				case QUERY_SPOT_SELECTED:
					queryNightNoise();
					break;
				case QUERY_SUCCEED:
					showResult();
					break;
				default:
					break;
				}
			}
		};

		checkConnect();

		night_noise_query.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				checkConnectWhenClickButton();
			}
		});
	}

	public GraphicalView get_barchart(List<String> name, List<String> value) {
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
			XYSeries series = new XYSeries("噪声db(A)");

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

	protected void checkConnect() {
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
					handler.sendEmptyMessage(QUERY_SPOT_SELECTED);
				} else {
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
				}
			}
		}.start();
	}

	/** 获得可查询列表 */
	protected void getSpotNameList() {
		new Thread() {
			@Override
			public void run() {
				String json = null;
				// 这个get_spot_path只是用来获得检测地点
				String get_spot_path = serverURL+"pmDistribution?city_id="
						+ LoginState.getIns().getCityId()
						+ "&start_time=2015-07-03&end_time=2015-07-09";

				URL url;
				spot_list = new ArrayList<String>();
				try {
					url = new URL(get_spot_path);
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

						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							spot_list.add(item.getString("csite_name"));
						}
						handler.sendEmptyMessage(GET_SPOT_LIST_SUCCEED);
					}else{
						handler.sendEmptyMessage(URL_REQUEST_FAIL);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);	
					e.printStackTrace();
				}
			}
		}.start();
		
		/*spot_list = new ArrayList<String>();
		
		//天津->特判，对于企业用户只能显示其自身的数据
		boolean is_tianjin = false;
		if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
			is_tianjin = true;
		}
		for(int i = 0;i < spotInfos.size();i++){
			String csite_name = spotInfos.get(i).getCsite_name();
			String csite_id = spotInfos.get(i).getCsite_id();
			if(!is_tianjin){
				//如果不是天津市，直接加
				spot_list.add(csite_name);
			}else{
				//如果是天津市，且是企业用户->特判
				String tianjin_csite_id = LoginState.getIns().getCsiteId();
				if(tianjin_csite_id.equals("-1")){
					//不是企业用户，直接加
					spot_list.add(csite_name);
				}else{
					//是企业用户，csite_id需要匹配tianjin_csite_id
					if(tianjin_csite_id.equals(csite_id)){
						spot_list.add(csite_name);
					}
				}
			}
		}
		handler.sendEmptyMessage(GET_SPOT_LIST_SUCCEED);*/
	}

	/** 加载下拉地点选择列表 */
	protected void loadSpinner() {
		ArrayAdapter<String> spot_list_Adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, spot_list);
		spot_list_Adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		night_noise_spot_spinner.setAdapter(spot_list_Adapter);
	}

	/** 查询监测点夜间噪声数据 */
	protected void queryNightNoise() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				start_timeString = night_noise_start_time.getText().toString();
				end_timeString = night_noise_end_time.getText().toString();

				String csite_id = null;
				String csite_name = night_noise_spot_spinner.getSelectedItem()
						.toString();
				int size = spotInfos.size();
				for (int i = 0; i < size; i++) {
					if (spotInfos.get(i).getCsite_name()
							.equalsIgnoreCase(csite_name)) {
						csite_id = spotInfos.get(i)
								.getCsite_id();
					}
				}

				String query_path = serverURL+"nightNoiseOverview?csite_id="
						+ csite_id
						+ "&begin_time="
						+ start_timeString
						+ "&end_time=" + end_timeString;

				Log.i("heheda", query_path);
				String json = null;
				URL url2;
				query_time_list = new ArrayList<String>();
				night_noise_value_list = new ArrayList<String>();
				try {
					url2 = new URL(query_path);
					// Log.i("heheda",get_spot_path);
					HttpURLConnection conn = (HttpURLConnection) url2
							.openConnection();
					conn.setConnectTimeout(50 * 1000);
					conn.setRequestMethod("GET");
					int responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = readStream(is);
						json = new String(data);

						JSONArray jsonArray = new JSONArray(json);

						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							String query_timeString = item.getString("time");
							query_time_list.add(query_timeString);
							String night_noise_valueString = item
									.getString("value");
							night_noise_value_list.add(night_noise_valueString);
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
		night_noise_layout.removeAllViews();
		night_noise_layout.addView(
				get_barchart(query_time_list, night_noise_value_list),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
		handler.sendEmptyMessage(CANCEL_PROCESS);
	}

	protected void showNetworkError() {
		Toast toast = Toast.makeText(NightNoiseActivity.this, "网络异常",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(NightNoiseActivity.this, "服务器维护中，请稍后再试",
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
