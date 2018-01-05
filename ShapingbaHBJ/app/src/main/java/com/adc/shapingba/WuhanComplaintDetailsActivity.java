package com.adc.shapingba;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.CsitePm10RankInfo;
import com.adc.data.LoginState;
import com.adc.util.IsNumericUtil;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WuhanComplaintDetailsActivity extends Activity {

	private static final int NETWORK_CONNECTED = 200;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int START_PROCESS = 102;
	private static final int CANCEL_PROCESS = 103;
	private static final int GET_INFO_SUCCESS = 105;
	private static final int URL_REQUEST_FAIL = 106;
	
	private Button complaint_details_goback;
	private TextView complaint_tv_people;
	private TextView complaint_tv_phone;
	private TextView complaint_tv_addr;
	//private TextView complaint_tv_theme;
	private TextView complaint_tv_content;
	private TextView complaint_tv_time;
	
	private ImageView complaint_iv_pic;
	
	private String complain_id;
	private String people_name;
	private String people_phone;
	private String complaint_time;
	private String uploadfiles;
	private String complaint_theme;
	private String complaint_content;
	private String addr;
	
	private Handler handler = null;
		
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
		}
		return false;
	};
	
	/*@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (complaint_iv_pic == null) return;
        Drawable drawable = complaint_iv_pic.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        super.onDestroy();
	}*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wuhan_complaint_details);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		complain_id = bundle.getString("complain_id");
		
		complaint_tv_people = (TextView) findViewById(R.id.complaint_tv_people);
		complaint_tv_phone = (TextView) findViewById(R.id.complaint_tv_phone);
		complaint_tv_addr = (TextView) findViewById(R.id.complaint_tv_addr);
		//complaint_tv_theme = (TextView) findViewById(R.id.complaint_tv_theme);
		complaint_tv_time = (TextView) findViewById(R.id.complaint_tv_time);
		complaint_tv_content = (TextView) findViewById(R.id.complaint_tv_content);
		complaint_iv_pic = (ImageView) findViewById(R.id.complaint_iv_pic);
		complaint_details_goback = (Button) findViewById(R.id.complaint_details_goback);
		complaint_details_goback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getComplainInfo();
					break;
				case NETWORK_UNCONNECTED:
					// 提示用户未连接网络
					showNetworkError();
					break;
				case URL_REQUEST_FAIL:
					// 服务器连接失败，提示服务器维护中
					showServerError();
					break;
				case START_PROCESS:
					// 显示登录进度
					Log.i("heheda", "show progress!!!!");
					showProgress();
					break;
				case CANCEL_PROCESS:
					// 取消登录进度
					Log.i("heheda", "cancel!!!!!!!!!!!!");
					cancelProgress();
					break;
				case GET_INFO_SUCCESS:
					Log.i("heheda", "GET_INFO_SUCCESS");
					showInfo();
				default:
					break;
				}
			}
		};
		
		checkConnect();
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
	
	protected void getComplainInfo() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				String json = null;
				int responseCode = 0;
				String query_path = LoginState.getIns().getServerURL() + "getOnlineComplaintDetail?complain_id="+complain_id;
				URL url;
				try {
					url = new URL(query_path);
					Log.i("heheda", query_path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(30 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);
						Log.i("heheda", "json="+json);
						JSONArray jsonArray = new JSONArray(json);
						
						JSONObject jsonObject = null;
						if (jsonArray.length() > 0) {
							jsonObject = jsonArray.getJSONObject(0);
							if(jsonObject.has("people_name")){
								people_name = jsonObject.getString("people_name");
							}
							if(jsonObject.has("people_phone")){
								people_phone = jsonObject.getString("people_phone");
							}
							if(jsonObject.has("complaint_time")){
								complaint_time = jsonObject.getString("complaint_time");
							}
							if(jsonObject.has("uploadfiles")){
								uploadfiles = jsonObject.getString("uploadfiles");
							}
							if(jsonObject.has("complaint_content")){
								complaint_content = jsonObject.getString("complaint_content");
							}
							if(jsonObject.has("complaint_theme")){
								complaint_theme = jsonObject.getString("complaint_theme");
							}
							if(jsonObject.has("addr")){
								addr = jsonObject.getString("addr");
							}
							if (jsonObject.has("uploadfiles")) {
								uploadfiles = jsonObject.getString("uploadfiles");
							}
						}
						
						handler.sendEmptyMessage(GET_INFO_SUCCESS);
						handler.sendEmptyMessage(CANCEL_PROCESS);
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
	
	protected void showInfo() {
		complaint_tv_people.setText(people_name);
		complaint_tv_phone.setText(people_phone);
		complaint_tv_addr.setText(addr);
		//complaint_tv_theme.setText(complaint_theme);
		complaint_tv_content.setText(complaint_content);
		complaint_tv_time.setText(complaint_time);
		
		ImageLoader.getInstance().displayImage("http://cq.hbjk.com.cn:8014/"+uploadfiles, complaint_iv_pic);
	}
	/**
	 * 搜索进度
	 */
	private void showProgress() {
		UIUtil.showProgressDialog(WuhanComplaintDetailsActivity.this, "搜索中......");
	}

	/**
	 * 取消进度
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	protected void showNetworkError() {
		Toast toast = Toast.makeText(WuhanComplaintDetailsActivity.this, "亲，你还没连接网络呢= =!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(WuhanComplaintDetailsActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}
}
