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
import com.adc.util.ReadStream;
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
public class SurfaceWaterMajorPollutionSourceProportionActivity extends Activity {
	
	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int GET_SPOT_LIST_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 105;
	private final int QUERY_SPOT_SELECTED = 106;
	private final int QUERY_SUCCEED = 107;

	private Handler handler = null;
	private Button surface_water_major_pollution_source_proportion_goback;
	private EditText surface_water_major_pollution_source_proportion_start_time;
	private Spinner surface_water_major_pollution_source_proportion_spot_spinner;
	private Button surface_water_major_pollution_source_proportion_query;
	private LinearLayout surface_water_major_pollution_source_proportion_layout;

	private List<String> spot_list;
	private String start_time_string;

	/** pieChart1 渲染器 */
	private DefaultRenderer renderer1;
	/** pieChart1 数据序列 */
	private CategorySeries series1;

	private String serverURL;
	
	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//MainTabActivity.hxdb_tjfx_setup = 1;
			/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
			Intent intent = new Intent(SurfaceWaterMajorPollutionSourceProportionActivity.this,
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
		setContentView(R.layout.activity_surface_water_major_pollution_source_proportion);
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SurfaceWaterMajorPollutionSourceProportionActivity.this);
		serverURL = LoginState.getIns().getServerURL();
		
		surface_water_major_pollution_source_proportion_goback = (Button) findViewById(R.id.surface_water_major_pollution_source_proportion_goback);
		surface_water_major_pollution_source_proportion_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//MainTabActivity.hxdb_tjfx_setup = 1;
				/*MainTabActivity.dtgl_qxdb_sjdb_tjfx = 3;
				Intent intent = new Intent(SurfaceWaterMajorPollutionSourceProportionActivity.this,
						MainTabActivity.class);
				startActivity(intent);*/
				finish();
			}
		});

		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -1);
		final String time1 = new SimpleDateFormat("yyyy-MM")
				.format(calendar1.getTime());
		surface_water_major_pollution_source_proportion_start_time = (EditText) findViewById(R.id.surface_water_major_pollution_source_proportion_start_time);
		surface_water_major_pollution_source_proportion_start_time.setText(time1);
		surface_water_major_pollution_source_proportion_start_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
						SurfaceWaterMajorPollutionSourceProportionActivity.this, "");
				dataTimePickDialog
						.dateTimePicKDialog(surface_water_major_pollution_source_proportion_start_time,0,7);
			}
		});

		surface_water_major_pollution_source_proportion_spot_spinner = (Spinner) findViewById(R.id.surface_water_major_pollution_source_proportion_spot_spinner);
		surface_water_major_pollution_source_proportion_spot_spinner
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
		 * surface_water_major_pollution_source_proportion_spot_spinner.setAdapter(spot_list_Adapter);
		 */


		surface_water_major_pollution_source_proportion_query = (Button) findViewById(R.id.surface_water_major_pollution_source_proportion_query);
		surface_water_major_pollution_source_proportion_layout = (LinearLayout) findViewById(R.id.surface_water_major_pollution_source_proportion_layout);

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
					query();
					break;
				case QUERY_SUCCEED:
					showResult();
					break;
				default:
					break;
				}
			}
		};

		surface_water_major_pollution_source_proportion_query.setOnClickListener(new OnClickListener() {

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
				spot_list = new ArrayList<String>();
				try {
					List<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
					for(int i = 0;i < spotInfos.size();i++){
						SpotInfo spotInfo = spotInfos.get(i);
						int csite_type = Integer.valueOf(spotInfo.getCsite_type());
						if(csite_type != -1)	continue;
						spot_list.add(spotInfo.getCsite_name());
					}
					handler.sendEmptyMessage(GET_SPOT_LIST_SUCCEED);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected void loadSpinner() {
		ArrayAdapter<String> spot_list_Adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, spot_list);
		spot_list_Adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		surface_water_major_pollution_source_proportion_spot_spinner.setAdapter(spot_list_Adapter);
	}

	protected void query() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				start_time_string = surface_water_major_pollution_source_proportion_start_time.getText()
						.toString();
				String json = null;
				String query_path = serverURL+"jingzhou/water/wuranzhanbi?userId="
						+ LoginState.getIns().getUserId()
						+ "&time="
						+ start_time_string;

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
						byte[] data = ReadStream.readStream(is);
						json = new String(data);
						JSONObject jsonObject = new JSONObject(json);
						String jsonArrayString = jsonObject.getString("data");
						JSONArray jsonArray = new JSONArray(jsonArrayString);
						if(jsonArray.length() == 0){
							handler.sendEmptyMessage(CANCEL_PROCESS);
							return;
						}
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							if (item.getString("name").equals(
									surface_water_major_pollution_source_proportion_spot_spinner
											.getSelectedItem().toString())) {
								//Log.i("heheda", "jiazai:" + spot_list.get(i));
								double dan = item.getDouble("dan");
								double lin = item.getDouble("lin");
								double gaomengsuanyan = item.getDouble("gaomensuanyan");
								double andan = item.getDouble("andan");
								double ph = item.getDouble("ph");
								int colors[] = { Color.rgb(34, 177, 76),
										Color.rgb(255, 242, 0),
										Color.rgb(255, 127, 39),
										Color.rgb(237, 28, 36),
										Color.rgb(163, 73, 164)};

								Boolean flag1[] = { true, true, true, true, true };
								series1 = new CategorySeries("Pm2.5 分布图");
								if (dan != 0) {
									series1.add("氮", dan);
									flag1[0] = false;
								}
								if (lin != 0) {
									series1.add("磷", lin);
									flag1[1] = false;
								}
								if (gaomengsuanyan != 0) {
 									series1.add("高锰酸盐", gaomengsuanyan);
									flag1[2] = false;
								}
								if (andan != 0) {
									series1.add("氨氮", andan);
									flag1[3] = false;
								}
								if (ph != 0) {
									series1.add("pH值", ph);
									flag1[4] = false;
								}

								renderer1 = new DefaultRenderer();
								renderer1.setLabelsTextSize(50);
								renderer1.setLegendTextSize(50);
								// renderer1.setLegendHeight(20);
								renderer1.setLabelsColor(Color.BLACK);
								renderer1.setPanEnabled(false);
								renderer1.setChartTitle("主要污染源占比统计结果");
								renderer1.setChartTitleTextSize(50);
								renderer1.setZoomEnabled(false);
								renderer1.setDisplayValues(true);
								for (int j = 0; j < 5; j++) {
									if (flag1[j] == true)
										continue;
									SimpleSeriesRenderer r = new SimpleSeriesRenderer();
									r.setColor(colors[j]);
									renderer1.addSeriesRenderer(r);
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
				SurfaceWaterMajorPollutionSourceProportionActivity.this, series1, renderer1);
		surface_water_major_pollution_source_proportion_layout.removeAllViews();
		surface_water_major_pollution_source_proportion_layout.addView(pie1,
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));

		handler.sendEmptyMessage(CANCEL_PROCESS);
	}

	protected void showNetworkError() {
		Toast toast = Toast.makeText(SurfaceWaterMajorPollutionSourceProportionActivity.this, "网络异常",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(SurfaceWaterMajorPollutionSourceProportionActivity.this, "服务器维护中，请稍后再试",
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
