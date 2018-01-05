package com.adc.shapingba;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.interfaces.RSAKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.shapingba.R;
import com.adc.util.GetSpotInfo;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ClipData.Item;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class LoginActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int START_PROCESS = 102;
	private final int CANCEL_PROCESS = 103;
	private final int LOGIN_INFO_CORRECT = 104;
	private final int LOGIN_INFO_WRONG = 105;
	private final int URL_REQUEST_FAIL = 106;
	private final int GET_APPLIST_SUCCEED = 107;
	/** 强制更新部分变量 **/
	private final int NETWORK_CONNECTED_FOR_UPDATE = 1100;
	private final int NETWORK_UNCONNECTED_FOR_UPDATE = 1101;
	private final int URL_REQUEST_FAIL_FOR_UPDATE = 1102;
	private final int START_PROCESS_FOR_UPDATE = 1103;
	private final int CANCEL_PROCESS_FOR_UPDATE = 1104;
	private final int GET_VERSION_INFO_SUCCEED_FOR_UPDATE = 1105;
	private final int UPDATE_NEEDED_FOR_UPDATE = 1106;
	private final int UPDATE_DOWNLOAD_PROGRESS_FOR_UPDATE = 1107;
	private final int DOWNLOAD_FINISHED_FOR_UPDATE = 1108;
	private String currentVersion;
	private String latestVersion;
	private String downloadUrl;
	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/* 下载进度条 */
	private ProgressBar mProgress;
	/** 强制更新部分变量 **/
	
	private boolean cancelUpdate = false;

	private Dialog mDownloadDialog;
	
	private Handler handler = null;
	private Button bt_login;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private CheckBox rem_pwd;
	private SharedPreferences sp;
	private String username;
	private String password;
	private ArrayList<SpotInfo> SpotInfoList;

	private long currentBackPressedTime = 0;
	private static final int BACK_PRESSED_INTERVAL = 2000;

	private String applistURL;
	
	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - currentBackPressedTime > BACK_PRESSED_INTERVAL) {
			currentBackPressedTime = System.currentTimeMillis();
			Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
		} else {
			MyActivityManager mam = MyActivityManager.getInstance();
			mam.finishAllActivity();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				// 任意一个线程异常后统一的处理
				System.out.println(ex.getLocalizedMessage());
				finish();
			}
		});

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(LoginActivity.this);

		applistURL = Constants.applistURL;
		//serverURL = this.getString(R.string.ServerURL);
		
		sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);

		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		rem_pwd = (CheckBox) findViewById(R.id.rem_pwd);
		rem_pwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (rem_pwd.isChecked()) {
					sp.edit().putBoolean("ISCHECK", true).commit();
				} else {
					sp.edit().putBoolean("ISCHECK", false).commit();
				}
			}
		});
		if (sp.getBoolean("ISCHECK", false)) {
			rem_pwd.setChecked(true);
			usernameEditText.setText(sp.getString("USER_NAME", ""));
			passwordEditText.setText(sp.getString("PASSWORD", ""));
		}

		currentVersion = "";
		try {
			currentVersion = getVersionName();
			Log.i("heheda", "get current version success");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED_FOR_UPDATE:
					Log.i("heheda", "NETWORK_CONNECTED_FOR_UPDATE");
					getLatestVersionName();
					break;
				case NETWORK_UNCONNECTED_FOR_UPDATE:
					Log.i("heheda", "NETWORK_UNCONNECTED_FOR_UPDATE");
					showNetworkError();
					break;
				case START_PROCESS_FOR_UPDATE:
					Log.i("heheda", "START_PROCESS_FOR_UPDATE");
					showLoginProgress();
					break;
				case CANCEL_PROCESS_FOR_UPDATE:
					Log.i("heheda", "CANCEL_PROCESS_FOR_UPDATE");
					cancelProgress();
					break;
				case GET_VERSION_INFO_SUCCEED_FOR_UPDATE:
					Log.i("heheda", "GET_VERSION_INFO_SUCCEED_FOR_UPDATE");
					checkVersion();
					break;
				case UPDATE_NEEDED_FOR_UPDATE:
					Log.i("heheda", "UPDATE_NEEDED_FOR_UPDATE");
					showDownloadDialog();
					break;
				case UPDATE_DOWNLOAD_PROGRESS_FOR_UPDATE:
					Log.i("heheda", "UPDATE_DOWNLOAD_PROGRESS_FOR_UPDATE");
					mProgress.setProgress(progress);
					break;
				case DOWNLOAD_FINISHED_FOR_UPDATE:
					Log.i("heheda", "DOWNLOAD_FINISHED_FOR_UPDATE");
					installApk();
					break;
				case URL_REQUEST_FAIL_FOR_UPDATE:
					Log.i("heheda", "URL_REQUEST_FAIL_FOR_UPDATE");
					showServerError();
					break;
				case NETWORK_CONNECTED:
					// 已连接网络，开始登录
					getApplist();
					break;
				case NETWORK_UNCONNECTED:
					// 提示用户未连接网络
					showNetworkError();
					break;
				case GET_APPLIST_SUCCEED:
					doLogin();
					break;
				//case LOGIN_INFO_CORRECT:
					// 登录信息正确，开始获得监测点详情信息
					// 2015.10.16 现在觉得还是什么都不要做好
					//getSpotInfoFirst();
					//break;
				case LOGIN_INFO_WRONG:
					// 登录信息有误，提示用户名密码错误
					showLoginError();
					break;
				case URL_REQUEST_FAIL:
					// 服务器连接失败，提示服务器维护中
					showServerError();
					break;
				case START_PROCESS:
					// 显示登录进度条
					Log.i("heheda", "showloginprogress!!!!");
					showLoginProgress();
					break;
				case CANCEL_PROCESS:
					// 取消登录进度条
					Log.i("heheda", "cancel!!!!!!!!!!!!");
					cancelProgress();
					break;
				default:
					break;
				}
				;
			}
		};

		bt_login = (Button) findViewById(R.id.bt_login);
		bt_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				checkConnectForUpdate();
			}

		});
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

	protected void getApplist() {
		new Thread() {
			@Override
			public void run() {
				//handler.sendEmptyMessage(START_PROCESS);
				username = usernameEditText.getText().toString();
				password = passwordEditText.getText().toString();
				String json = null;
				int responseCode = 0;
				String get_applist = applistURL+"userUrl?user="+username+"&password="+password;
				URL url;
				try {
					url = new URL(get_applist);
					Log.i("heheda", get_applist);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(5 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);

						JSONObject item = new JSONObject(json);
						Log.i("heheda", "server=" + item.getString("server"));
						LoginState.getIns().setServerURL(item.getString("server"));
						handler.sendEmptyMessage(GET_APPLIST_SUCCEED);
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
	protected void doLogin() {
		new Thread() {
			@Override
			public void run() {
				//handler.sendEmptyMessage(START_PROCESS);
				username = usernameEditText.getText().toString();
				password = passwordEditText.getText().toString();
				String json = null;
				int responseCode = 0;
				String login_path = LoginState.getIns().getServerURL()+"login?username="
						+ username + "&password=" + password;
				URL url;
				try {
					url = new URL(login_path);
					Log.i("heheda", login_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(30 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);
						Log.e("heheda", "json="+json);
						JSONObject item = new JSONObject(json);
						LoginState.getIns().setState(item.getString("state"));
						LoginState.getIns().setUserId(item.getString("id"));
						LoginState.getIns().setCityId(item.getString("cityId"));
						LoginState.getIns().setCsiteId(item.getString("csite_id"));
						LoginState.getIns().setEdition_type(item.getString("edition_type"));
						Log.i("heheda", "cityid=" + item.getString("cityId"));
						Log.e("heheda", "LoginState.getIns().getState()="+LoginState.getIns().getState());
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
				if (responseCode == 200
						&& Integer.valueOf(LoginState.getIns().getState()) == 0) {
					//handler.sendEmptyMessage(LOGIN_INFO_CORRECT);
					if (rem_pwd.isChecked()) {
						Editor editor = sp.edit();
						editor.putString("USER_NAME", username);
						editor.putString("PASSWORD", password);
						editor.commit();
					}
					handler.sendEmptyMessage(CANCEL_PROCESS);
					Intent intent = new Intent(LoginActivity.this,WuhanMainActivity.class);
					startActivity(intent);
					finish();
				}
				if (responseCode == 200
						&& Integer.valueOf(LoginState.getIns().getState()) == 1) {
					handler.sendEmptyMessage(LOGIN_INFO_WRONG);
					handler.sendEmptyMessage(CANCEL_PROCESS);
				}
			}
		}.start();
	}

	/*
	 * 强制更新代码 从SetupActivity里面抄来的。
	 */
	private void checkConnectForUpdate() {
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
					handler.sendEmptyMessage(NETWORK_CONNECTED_FOR_UPDATE);
				} else {
					handler.sendEmptyMessage(NETWORK_UNCONNECTED_FOR_UPDATE);
				}
			}
		}.start();
	}
	
	private void checkVersion() {
		handler.sendEmptyMessage(CANCEL_PROCESS_FOR_UPDATE);
		Log.i("heheda", "cur" + currentVersion);
		Log.i("heheda", "la" + latestVersion);
		double v1 = Double.valueOf(currentVersion);
		double v2 = Double.valueOf(latestVersion);
		if (v1 == v2) {
			Log.i("heheda", "yiyang");
			/*
			Toast toast = Toast.makeText(SetupActivity.this, "您安装的当前版本已是最新版本！",
					Toast.LENGTH_SHORT);
			toast.show();
			*/
			//版本监测出来是一样的才让登录
			//handler.sendEmptyMessage(CANCEL_PROCESS_FOR_UPDATE);
			checkConnect();
		} else {
			Log.i("heheda", "buyiyang");
			AlertDialog.Builder builder = new AlertDialog.Builder(
					LoginActivity.this);
			builder.setTitle("有重要的版本更新，请您更新软件");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// 这里添加点击确定后的逻辑
							cancelUpdate = false;
							handler.sendEmptyMessage(UPDATE_NEEDED_FOR_UPDATE);
						}
					});
			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// 这里添加点击确定后的逻辑,反正就是不让你登录
						}
					});
			builder.create().show();
		}
	}

	private String getVersionName() throws Exception {
		PackageManager packageManager = getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
				0);
		return packInfo.versionName;
	}

	private void getLatestVersionName() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS_FOR_UPDATE);
				String query_path = "http://112.64.16.233:8088/Android_App/version.txt";
				String json = null;
				int responseCode = 0;
				URL url;
				try {
					url = new URL(query_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(10 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);
						JSONObject item = new JSONObject(json);

						latestVersion = item.getString("shapingba_version");
						downloadUrl = item.getString("shapingba_download_url");
						handler.sendEmptyMessage(GET_VERSION_INFO_SUCCEED_FOR_UPDATE);
					} else {
						handler.sendEmptyMessage(URL_REQUEST_FAIL_FOR_UPDATE);
						handler.sendEmptyMessage(CANCEL_PROCESS_FOR_UPDATE);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED_FOR_UPDATE);
					handler.sendEmptyMessage(CANCEL_PROCESS_FOR_UPDATE);
					e.printStackTrace();
				}
			}
		}.start();
	}
	

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog() {
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(LoginActivity.this);
		builder.setTitle("正在更新");
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton("取消更新",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// 这里添加点击确定后的逻辑
						dialog.dismiss();
						cancelUpdate = true;
					}
				});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// 下载文件
		downloadApk();
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk() {
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	/**
	 * 下载文件线程
	 * 
	 * @author coolszy
	 * @date 2012-4-26
	 * @blog http://blog.92coding.com
	 */
	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					mSavePath = sdpath + "download";
					URL url = new URL(downloadUrl);
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath, "HBJ");
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						handler.sendEmptyMessage(UPDATE_DOWNLOAD_PROGRESS_FOR_UPDATE);
						if (numread <= 0) {
							// 下载完成
							handler.sendEmptyMessage(DOWNLOAD_FINISHED_FOR_UPDATE);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		File apkfile = new File(mSavePath, "HBJ");
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		startActivity(i);
	}
	protected void showNetworkError() {
		Toast toast = Toast.makeText(LoginActivity.this, "亲，你还没连接网络呢= =!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showLoginError() {
		Toast toast = Toast.makeText(LoginActivity.this, "账号或密码错误",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(LoginActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 登录进度条
	 */
	private void showLoginProgress() {
		UIUtil.showProgressDialog(this, "登录中......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
}
