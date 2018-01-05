package com.adc.statistics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.consts.Constants.Login;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.hbj5.R;
import com.adc.hbj5.DateTimePickDialogUtil;
import com.adc.hbj5.MainTabActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.util.UIUtil;

import android.R.bool;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class Pm10DistributionActivity extends Activity {
	
	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int GET_SPOT_LIST_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 105;
	private final int QUERY_SPOT_SELECTED = 106;
	private final int QUERY_SUCCEED = 107;

	private Handler handler = null;
	private Button pm10_distribution_goback;
	private EditText pm10_distribution_start_time;
	private EditText pm10_distribution_end_time;
	private Spinner pm10_distribution_spot_spinner;
	private Button pm10_distribution_query;
	private LinearLayout pm2_5_distribution_layout;
	private LinearLayout pm10_distribution_layout;

	private List<String> spot_list;
	private String start_timeString;
	private String end_timeString;

	/** pieChart 渲染器1 */
	private DefaultRenderer renderer1;
	/** pieChart 渲染器2 */
	private DefaultRenderer renderer2;
	/** pm2.5数据序列 */
	private CategorySeries series1;
	/** pm10 数据序列 */
	private CategorySeries series2;

	private String serverURL;
	
	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//MainTabActivity.hxdb_tjfx_setup = 1;
			/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
			Intent intent = new Intent(Pm10DistributionActivity.this,
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
		setContentView(R.layout.activity_pm10_distribution);
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(Pm10DistributionActivity.this);

		serverURL = LoginState.getIns().getServerURL();
		
		pm10_distribution_goback = (Button) findViewById(R.id.pm10_distribution_goback);
		pm10_distribution_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//MainTabActivity.hxdb_tjfx_setup = 1;
				/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
				Intent intent = new Intent(Pm10DistributionActivity.this,
						MainTabActivity.class);
				startActivity(intent);*/
				finish();
			}
		});

		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -8);
		final String time1 = new SimpleDateFormat("yyyy-MM-dd")
				.format(calendar1.getTime());
		pm10_distribution_start_time = (EditText) findViewById(R.id.pm10_distribution_start_time);
		pm10_distribution_start_time.setText(time1);
		pm10_distribution_start_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
						Pm10DistributionActivity.this, time1);
				dataTimePickDialog
						.dateTimePicKDialog(pm10_distribution_start_time);
			}
		});
		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.DATE, -1);
		final String time2 = new SimpleDateFormat("yyyy-MM-dd")
				.format(calendar2.getTime());
		pm10_distribution_end_time = (EditText) findViewById(R.id.pm10_distribution_end_time);
		pm10_distribution_end_time.setText(time2);
		pm10_distribution_end_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
						Pm10DistributionActivity.this, time2);
				dataTimePickDialog
						.dateTimePicKDialog(pm10_distribution_end_time);
			}
		});
		pm10_distribution_spot_spinner = (Spinner) findViewById(R.id.pm10_distribution_spot_spinner);
		pm10_distribution_spot_spinner
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
		/*
		 * String json = null; //这个get_spot_path只是用来获得检测地点 String get_spot_path
		 * =
		 * "http://cq.hbjk.com.cn:8081/restfulServer/rest/pmDistribution?city_id="
		 * +LoginState.getIns().getCityId()+
		 * "&start_time=2015-07-03&end_time=2015-07-09";
		 * 
		 * URL url; spot_list = new ArrayList<String>(); try { url = new
		 * URL(get_spot_path); //Log.i("heheda",get_spot_path);
		 * HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		 * conn.setConnectTimeout(50 * 1000); conn.setRequestMethod("GET"); int
		 * responseCode = conn.getResponseCode(); if(responseCode == 200){
		 * InputStream is = conn.getInputStream(); byte[] data = readStream(is);
		 * json = new String(data);
		 * 
		 * JSONArray jsonArray = new JSONArray(json);
		 * 
		 * for(int i = 0;i < jsonArray.length();i++){ JSONObject item =
		 * jsonArray.getJSONObject(i);
		 * spot_list.add(item.getString("csite_name")); } } } catch (Exception
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */

		/*
		 * ArrayAdapter<String> spot_list_Adapter = new
		 * ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
		 * spot_list);
		 * spot_list_Adapter.setDropDownViewResource(android.R.layout
		 * .simple_spinner_dropdown_item);
		 * pm10_distribution_spot_spinner.setAdapter(spot_list_Adapter);
		 */

		pm10_distribution_query = (Button) findViewById(R.id.pm10_distribution_query);
		pm2_5_distribution_layout = (LinearLayout) findViewById(R.id.pm2_5_distribution_layout);
		pm10_distribution_layout = (LinearLayout) findViewById(R.id.pm10_distribution_layout);

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
					queryPm10Distribution();
					break;
				case QUERY_SUCCEED:
					showResult();
					break;
				default:
					break;
				}
			}
		};

		pm10_distribution_query.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				checkConnectWhenClickButton();
			}
		});
		
		checkConnect();
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

						//天津->特判，对于企业用户只能显示其自身的数据
						boolean is_tianjin = false;
						//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
						if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
							is_tianjin = true;
						}
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							if(!is_tianjin){
								//如果不是天津市，直接加
								spot_list.add(item.getString("csite_name"));
							}else{
								//如果是天津市，且是企业用户->特判
								int csite_id = Integer.valueOf(LoginState.getIns().getCsiteId());
								if(csite_id == -1){
									//不是企业用户，直接加
									spot_list.add(item.getString("csite_name"));
								}else{
									//是企业用户，csite_id需要匹配csite_2(csite_id2=从json串中读出来的csite_id)
									int csite_id2 = Integer.valueOf(item.getString("csite_id"));
									if(csite_id2 == csite_id){
										spot_list.add(item.getString("csite_name"));
									}
								}
							}
						}
						handler.sendEmptyMessage(GET_SPOT_LIST_SUCCEED);
					} else {
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

	protected void loadSpinner() {
		ArrayAdapter<String> spot_list_Adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, spot_list);
		spot_list_Adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pm10_distribution_spot_spinner.setAdapter(spot_list_Adapter);
	}

	protected void queryPm10Distribution() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				start_timeString = pm10_distribution_start_time.getText()
						.toString();
				end_timeString = pm10_distribution_end_time.getText()
						.toString();
				String json = null;

				String query_path = serverURL+"pmDistribution?city_id="
						+ LoginState.getIns().getCityId()
						+ "&start_time="
						+ start_timeString + "&end_time=" + end_timeString;

				URL url2;
				try {
					url2 = new URL(query_path);
					Log.i("heheda","query_path="+query_path);
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
						if(jsonArray.length() == 0){
							handler.sendEmptyMessage(CANCEL_PROCESS);
							return;
						}
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							if (item.getString("csite_name").equals(
									pm10_distribution_spot_spinner
											.getSelectedItem().toString())) {
								//Log.i("heheda", "jiazai:" + spot_list.get(i));
								double pm10_orange = item
										.getDouble("pm10_orange");
								double pm2_5_red = item.getDouble("pm2_5_red");
								double pm10_maroon = item
										.getDouble("pm10_maroon");
								double pm10_yellow = item
										.getDouble("pm10_yellow");
								double pm2_5_yellow = item
										.getDouble("pm2_5_yellow");
								double pm2_5_green = item
										.getDouble("pm2_5_green");
								double pm2_5_maroon = item
										.getDouble("pm2_5_maroon");
								double pm2_5_purple = item
										.getDouble("pm2_5_purple");
								double pm10_red = item.getDouble("pm10_red");
								double pm10_purple = item
										.getDouble("pm10_purple");
								double pm10_green = item
										.getDouble("pm10_green");
								double pm2_5_orange = item
										.getDouble("pm2_5_orange");
								int colors[] = { Color.rgb(34, 177, 76),
										Color.rgb(255, 242, 0),
										Color.rgb(255, 127, 39),
										Color.rgb(237, 28, 36),
										Color.rgb(163, 73, 164),
										Color.rgb(136, 0, 21) };

								Boolean flag1[] = { true, true, true, true,
										true, true };
								series1 = new CategorySeries("Pm2.5 分布图");
								if (pm2_5_green != 0) {
									series1.add("0-50", pm2_5_green);
									flag1[0] = false;
								}
								if (pm2_5_yellow != 0) {
									series1.add("50-100", pm2_5_yellow);
									flag1[1] = false;
								}
								if (pm2_5_orange != 0) {
									series1.add("100-150", pm2_5_orange);
									flag1[2] = false;
								}
								if (pm2_5_red != 0) {
									series1.add("150-200", pm2_5_red);
									flag1[3] = false;
								}
								if (pm2_5_purple != 0) {
									series1.add("200-300", pm2_5_purple);
									flag1[4] = false;
								}
								if (pm2_5_maroon != 0) {
									series1.add(">300", pm2_5_maroon);
									flag1[5] = false;
								}

								renderer1 = new DefaultRenderer();
								renderer1.setLabelsTextSize(50);
								renderer1.setLegendTextSize(50);
								// renderer1.setLegendHeight(20);
								renderer1.setLabelsColor(Color.BLACK);
								renderer1.setPanEnabled(false);
								renderer1.setChartTitle("pm2.5统计结果");
								renderer1.setChartTitleTextSize(50);
								renderer1.setZoomEnabled(false);
								renderer1.setDisplayValues(true);
								for (int j = 0; j < 6; j++) {
									if (flag1[j] == true)
										continue;
									SimpleSeriesRenderer r = new SimpleSeriesRenderer();
									r.setColor(colors[j]);
									renderer1.addSeriesRenderer(r);
								}

								Boolean flag2[] = { true, true, true, true,
										true, true };
								series2 = new CategorySeries("Pm10 分布图");
								if (pm10_green != 0) {
									series2.add("0-50", pm10_green);
									flag2[0] = false;
								}
								if (pm10_yellow != 0) {
									series2.add("50-100", pm10_yellow);
									flag2[1] = false;
								}
								if (pm10_orange != 0) {
									series2.add("100-150", pm10_orange);
									flag2[2] = false;
								}
								if (pm10_red != 0) {
									series2.add("150-200", pm10_red);
									flag2[3] = false;
								}
								if (pm10_purple != 0) {
									series2.add("200-300", pm10_purple);
									flag2[4] = false;
								}
								if (pm10_maroon != 0) {
									series2.add(">300", pm10_maroon);
									flag2[5] = false;
								}

								renderer2 = new DefaultRenderer();
								renderer2.setLabelsTextSize(50);
								renderer2.setLegendTextSize(50);
								// renderer1.setLegendHeight(20);
								renderer2.setLabelsColor(Color.BLACK);
								renderer2.setPanEnabled(false);
								renderer2.setChartTitle("pm10统计结果");
								renderer2.setChartTitleTextSize(50);
								renderer2.setZoomEnabled(false);
								renderer2.setDisplayValues(true);
								for (int j = 0; j < 6; j++) {
									if (flag2[j] == true)
										continue;
									SimpleSeriesRenderer r = new SimpleSeriesRenderer();
									r.setColor(colors[j]);
									renderer2.addSeriesRenderer(r);
								}
							}
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
		GraphicalView pie1 = ChartFactory.getPieChartView(
				Pm10DistributionActivity.this, series1, renderer1);
		pm2_5_distribution_layout.removeAllViews();
		pm2_5_distribution_layout.addView(pie1,
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));

		GraphicalView pie2 = ChartFactory.getPieChartView(
				Pm10DistributionActivity.this, series2, renderer2);
		pm10_distribution_layout.removeAllViews();
		pm10_distribution_layout.addView(pie2, new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		handler.sendEmptyMessage(CANCEL_PROCESS);
	}

	protected void showNetworkError() {
		Toast toast = Toast.makeText(Pm10DistributionActivity.this, "网络异常",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(Pm10DistributionActivity.this, "服务器维护中，请稍后再试",
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
