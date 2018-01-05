package com.adc.hbj5;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adc.consts.Constants;
import com.adc.data.TempData;
import com.adc.hbj5.R;
import com.adc.hbj5.DataContrastActivity;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MainTabActivity;
import com.adc.live.LiveCallBack;
import com.adc.oldactivity.OldVideoActivity;
import com.adc.playback.ConstantPlayBack;
import com.adc.playback.PlayBackActivity;
import com.adc.playback.PlayBackCallBack;
import com.adc.playback.PlayBackControl;
import com.adc.playback.PlayBackParams;
import com.adc.util.DebugLog;
import com.adc.util.GetCurrentTimeFromBaidu;
import com.adc.util.UIUtil;
import com.adc.util.UtilAudioPlay;
import com.adc.util.UtilFilePath;
import com.hik.mcrsdk.rtsp.ABS_TIME;
import com.hik.mcrsdk.rtsp.RtspClient;
import com.hikvision.vmsnetsdk.RecordInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;

/**
 * 回放UI类
 * 
 * @author huangweifeng
 * @Data 2013-10-29
 */
public class VideoPlaybackActivity extends Activity implements OnClickListener,
		PlayBackCallBack,Callback {

	/**
	 * 日志
	 */
	private static final String TAG = "PlayBackActivity";
	/**
	 * 播放视图控件
	 */
	private SurfaceView mSurfaceView;
	/**
	 * 开始按钮
	 */
	private Button mStartButton;
	/**
	 * 停止按钮
	 */
	private Button mStopButton;
	/**
	 * 暂停按钮
	 */
	private Button mPauseButton;
	/**
	 * 抓拍按钮
	 */
	private Button mCaptureButton;
	/**
	 * 录像按钮
	 */
	private Button mRecordButton;
	/**
	 * 音频按钮
	 */
	// private Button mAudioButton;
	/** 等待框 */
	private ProgressBar mProgressBar;
	/**
	 * 控制层对象
	 */
	private PlayBackControl mControl;
	/**
	 * 创建消息对象
	 */
	private Handler mMessageHandler = new MyHandler();
	/**
	 * 回放时的参数对象
	 */
	private PlayBackParams mParamsObj;
	/**
	 * 是否暂停标签
	 */
	private boolean mIsPause;

	/**
	 * 音频是否开启
	 */
	private boolean mIsAudioOpen;
	/**
	 * 是否正在录像
	 */
	private boolean mIsRecord;
	/**
	 * 回放时间
	 */
	private TextView play_back_time;
	/**
	 * 回放进度控制条
	 */
	private SeekBar play_back_seekbar;
	private String mCameraID;
	private VMSNetSDK mVmsNetSDK = null;
	private String mDeviceID = "";

	private Calendar calendar;
	private Date date;
	private String timeString;

	private Button play_back_goback;
	private Handler startHandler;
	// private Handler timeHandler;
	// private boolean isTiming = false;

	private int year1 = 0;
	private int mon1 = 0;
	private int day1 = 0;
	private int hour1 = 0;
	private int min1 = 0;
	private int sec1 = 0;
	private int year2 = 0;
	private int mon2 = 0;
	private int day2 = 0;
	private int hour2 = 0;
	private int min2 = 0;
	private int sec2 = 0;

	/**
	 * 是否正在播放
	 */
	private boolean isPlaying;

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			stopBtnOnClick();
			//MainTabActivity.hxdb_tjfx_setup = 0;
			/*Intent intent = new Intent(VideoPlaybackActivity.this,
					HxdbTabActivity.class);
			startActivity(intent);*/
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_playback);

		play_back_goback = (Button) findViewById(R.id.play_back_goback1);
		play_back_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopBtnOnClick();
				//MainTabActivity.hxdb_tjfx_setup = 0;
				/*Intent intent = new Intent(VideoPlaybackActivity.this,
						HxdbTabActivity.class);
				startActivity(intent);*/
				finish();
			}
		});

		startHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					queryPlaybackInfo(year1, mon1, day1, hour1, min1, sec1,
							year2, mon2, day2, hour2, min2, sec2);
					break;
				case 1:
					mProgressBar.setVisibility(View.VISIBLE);
					if (null != mProgressBar) {
						new Thread() {
							@Override
							public void run() {
								if (null != mControl) {
									mControl.startPlayBack(mParamsObj);
								}
								super.run();
							}
						}.start();
					}
					break;

				default:
					break;
				}
			}
		};

		/*
		 * timeHandler = new Handler(){ public void handleMessage(Message msg){
		 * super.handleMessage(msg); switch (msg.what) { case 0:
		 * while(isTiming){ try { Thread.sleep(1000); } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } int val = play_back_seekbar.getProgress();
		 * play_back_seekbar.setProgress(val+1); } break; default: break; } } };
		 */
		getPlayBackInfo();

		setUpView();

		init();

		// queryPlaybackInfo();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		stopBtnOnClick();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startBtnOnClick();
	}
	/**
	 * 该方法用来获取回放的信息，使用者自己实现 void
	 * 
	 * @since V1.0
	 */
	private void getPlayBackInfo() {
		mCameraID = TempData.getIns().getCameraInfo().cameraID;
		//mDeviceID = getIntent().getStringExtra(Constants.IntentKey.DEVICE_ID);
	}

	/**
	 * 初始化
	 * 
	 * @since V1.0
	 */
	private void init() {
		// 打开日志的开关
		DebugLog.setLogOption(true);
		// 创建和cms平台交互的对象
		mVmsNetSDK = VMSNetSDK.getInstance();
		// 初始化远程回放控制层对象
		mControl = new PlayBackControl();
		// 设置远程回放控制层回调
		mControl.setPlayBackCallBack(this);
		// 创建远程回放需要的参数
		mParamsObj = new PlayBackParams();
		// 播放控件
		mParamsObj.surfaceView = mSurfaceView;
	}

	/**
	 * 进行远程回放录像查询
	 * 
	 * @since V1.0
	 */
	public void queryPlaybackInfo(final int b_year, final int b_mon,
			final int b_day, final int b_hour, final int b_min,
			final int b_sec, final int e_year, final int e_mon,
			final int e_day, final int e_hour, final int e_min, final int e_sec) {
		Log.i("hihida", "now query!!!");
		new Thread(new Runnable() {

			@Override
			public void run() {
				// cms平台地址
				//String servAddr = Constants.servAddr;
				String servAddr = TempData.getIns().getHikvisionServerAddr();
				// 登录成功后返回的回话ID
				String sessionID = TempData.getIns().getLoginData().sessionID;
				// 查询的录像类型，1-计划录像，2-移动录像，16-手动录像，4-报警录像
				String recordType = "1,2,4,16";
				// 查询的录像位置，也就是存储介质类型0-IPSAN、1-设备录像、2-PCNVR、3-ENVR、4-CISCO、5-DSNVR、7-CVR，目前支持单个查询
				String recordPos = "1";

				Calendar startCalendar = Calendar.getInstance();
				Calendar endCalendar = Calendar.getInstance();
				// 这里startCalendar
				// 设置的时间是2014年6月10号00:00::00,其实是2014年7月10号00:00:00，具体原因参考Calendar类
				startCalendar.set(b_year, b_mon, b_day, b_hour, b_min, b_sec);
				Log.i("heheda", "query begin:"+b_year+" "+b_mon+" "+b_day+" "+b_hour+" "+b_min+" "+b_sec);
				// 这里endCalendar
				// 设置的时间是2014年6月10号23:59::59,其实是2014年7月10号23:59:59，具体原因参考Calendar类
				endCalendar.set(e_year, e_mon, e_day, e_hour, e_min, e_sec);
				Log.i("heheda", "query end:"+e_year+" "+e_mon+" "+e_day+" "+e_hour+" "+e_min+" "+e_sec);
				// 查询录像库中的时间对象，注意Calendar时间，使用前请先了解下Calendar
				com.hikvision.vmsnetsdk.ABS_TIME startTime = new com.hikvision.vmsnetsdk.ABS_TIME(
						startCalendar);
				com.hikvision.vmsnetsdk.ABS_TIME endTime = new com.hikvision.vmsnetsdk.ABS_TIME(
						endCalendar);

				setParamsObjTime(startTime, endTime);
				RecordInfo recordInfo = new RecordInfo();
				if (mVmsNetSDK == null) {
					Log.e(Constants.LOG_TAG, "mVmsNetSDK is " + null);
					return;
				}
				boolean ret = mVmsNetSDK.queryCameraRecord(servAddr, sessionID,
						mCameraID, recordType, recordPos, startTime, endTime,
						recordInfo);
				Log.e("heheda", "param:"+servAddr+" "+sessionID+" "+mCameraID+" "+recordType+" "+recordPos+" "+startTime+" "+endTime+" "+recordInfo);
				Log.i(Constants.LOG_TAG, "ret : " + ret);
				if (recordInfo != null) {
					Log.i(Constants.LOG_TAG, "segmentListPlayUrl : "
							+ recordInfo.segmentListPlayUrl);
				}

				if (ret) {
					mParamsObj.url = recordInfo.segmentListPlayUrl;
				}
				//DeviceInfo deviceInfo = new DeviceInfo();
				//ret = mVmsNetSDK.getDeviceInfo(servAddr, sessionID, mDeviceID,
				//		deviceInfo);
				//if (ret && deviceInfo != null) {
					mParamsObj.name = Constants.userName;
					mParamsObj.passwrod = Constants.password;
					startHandler.sendEmptyMessage(1);
				//} else {
				//	Log.e(Constants.LOG_TAG, "getDeviceInfo():: fail");
				//}
			}
		}).start();
	}

	/**
	 * 设置远程回放取流的开始时间和结束时间
	 * 
	 * @param startTime
	 * @param endTime
	 * @since V1.0
	 */
	protected void setParamsObjTime(com.hikvision.vmsnetsdk.ABS_TIME startTime,
			com.hikvision.vmsnetsdk.ABS_TIME endTime) {
		if (startTime == null || endTime == null) {
			Log.e(Constants.LOG_TAG, "setParamsObjTime():: startTime is "
					+ startTime + "endTime is " + endTime);
		}
		// 取流库中的时间对象
		ABS_TIME rtspEndTime = new ABS_TIME();
		ABS_TIME rtspStartTime = new ABS_TIME();

		// 设置播放结束时间
		rtspEndTime.setYear(endTime.dwYear);
		// 之所以要加1，是由于我们查询接口中的时间和取流中的时间采用的是两个自定义的时间类，这个地方开发者按照demo中实现就可以了。
		rtspEndTime.setMonth(endTime.dwMonth + 1);
		rtspEndTime.setDay(endTime.dwDay);
		rtspEndTime.setHour(endTime.dwHour);
		rtspEndTime.setMinute(endTime.dwMinute);
		rtspEndTime.setSecond(endTime.dwSecond);

		// 设置开始播放时间
		rtspStartTime.setYear(startTime.dwYear);
		// 之所以要加1，是由于我们查询接口中的时间和取流中的时间采用的是两个自定义的时间类，这个地方开发者按照demo中实现就可以了。
		rtspStartTime.setMonth(startTime.dwMonth + 1);
		rtspStartTime.setDay(startTime.dwDay);
		rtspStartTime.setHour(startTime.dwHour);
		rtspStartTime.setMinute(startTime.dwMinute);
		rtspStartTime.setSecond(startTime.dwSecond);

		if (mParamsObj != null) {
			// 设置开始远程回放的开始时间和结束时间。
			mParamsObj.endTime = rtspEndTime;
			mParamsObj.startTime = rtspStartTime;
		}
	}

	/**
	 * 初始化控件 void
	 * 
	 * @since V1.0
	 */
	private void setUpView() {
		mSurfaceView = (SurfaceView) findViewById(R.id.playbackSurfaceView1);
		mSurfaceView.getHolder().addCallback(this);
		
		mStartButton = (Button) findViewById(R.id.playBackStart1);
		mStartButton.setOnClickListener(this);

		mStopButton = (Button) findViewById(R.id.playBackStop1);
		mStopButton.setOnClickListener(this);

		mPauseButton = (Button) findViewById(R.id.playBackPause1);
		mPauseButton.setOnClickListener(this);

		mCaptureButton = (Button) findViewById(R.id.playBackCapture1);
		mCaptureButton.setOnClickListener(this);

		// mRecordButton = (Button) findViewById(R.id.playBackRecord);
		// mRecordButton.setOnClickListener(this);

		// mAudioButton = (Button) findViewById(R.id.playBackRadio);
		// mAudioButton.setOnClickListener(this);

		mProgressBar = (ProgressBar) findViewById(R.id.playBackProgressBar1);
		mProgressBar.setVisibility(View.GONE);

		calendar = GetCurrentTimeFromBaidu.getCurrentTime();
		year2 = calendar.get(Calendar.YEAR);
		mon2 = calendar.get(Calendar.MONTH);
		day2 = calendar.get(Calendar.DAY_OF_MONTH);
		hour2 = calendar.get(Calendar.HOUR_OF_DAY);
		min2 = calendar.get(Calendar.MINUTE);
		sec2 = calendar.get(Calendar.SECOND);

		calendar.add(Calendar.MINUTE, -5);
		year1 = calendar.get(Calendar.YEAR);
		mon1 = calendar.get(Calendar.MONTH);
		day1 = calendar.get(Calendar.DAY_OF_MONTH);
		hour1 = calendar.get(Calendar.HOUR_OF_DAY);
		min1 = calendar.get(Calendar.MINUTE);
		sec1 = calendar.get(Calendar.SECOND);
		date = calendar.getTime();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\n HH:mm:ss");
		timeString = df.format(date);
		play_back_time = (TextView) findViewById(R.id.play_back_time1);
		play_back_time.setText(timeString);

		play_back_seekbar = (SeekBar) findViewById(R.id.play_back_seekbar1);
		play_back_seekbar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());
		play_back_seekbar.setProgress(hour1 * 3600 + min1 * 60 + sec1);
	}

	private class OnSeekBarChangeListenerImp implements
			SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			int hour = 0, min = 0, second = 0;
			int val = seekBar.getProgress();
			while (val - 3600 >= 0) {
				val -= 3600;
				hour++;
			}
			while (val - 60 >= 0) {
				val -= 60;
				min++;
			}
			second = val;

			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = df2.format(date);
			String showString = "" + dateString + "\n ";
			if (hour < 10)
				showString += "0";
			showString += "" + hour + ":";
			if (min < 10)
				showString += "0";
			showString += "" + min + ":";

			if (second < 10)
				showString += "0";
			showString += "" + second;

			play_back_time.setText(showString);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			stopBtnOnClick();

			calendar = GetCurrentTimeFromBaidu.getCurrentTime();
			calendar.add(Calendar.MINUTE, -2);
			year2 = calendar.get(Calendar.YEAR);
			mon2 = calendar.get(Calendar.MONTH);
			day2 = calendar.get(Calendar.DAY_OF_MONTH);
			hour2 = calendar.get(Calendar.HOUR_OF_DAY);
			min2 = calendar.get(Calendar.MINUTE);
			sec2 = calendar.get(Calendar.SECOND);

			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int min = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			int second_val = hour * 3600 + min * 60 + second - 180;

			int val = play_back_seekbar.getProgress();

			if (val > second_val) {
				play_back_seekbar.setProgress(second_val);
				val = second_val;
			}

			hour = 0;
			min = 0;
			second = 0;
			while (val - 3600 >= 0) {
				val -= 3600;
				hour++;
			}
			while (val - 60 >= 0) {
				val -= 60;
				min++;
			}
			second = val;

			year1 = year2;
			mon1 = mon2;
			day1 = day2;
			hour1 = hour;
			min1 = min;
			sec1 = second;
			startBtnOnClick();
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.playBackStart1:
			startBtnOnClick();
			break;

		case R.id.playBackStop1:
			stopBtnOnClick();
			break;

		case R.id.playBackPause1:
			pauseBtnOnClick();
			break;

		case R.id.playBackCapture1:
			captureBtnOnClick();
			break;

		// case R.id.playBackRecord:
		// recordBtnOnClick();
		// break;

		// case R.id.playBackRadio:
		// audioBtnOnClick();
		// break;

		}
	}

	/**
	 * 音频按钮 void
	 * 
	 * @since V1.0
	 */
	/*
	 * private void audioBtnOnClick() { if (null != mControl) { if
	 * (mIsAudioOpen) { mControl.stopAudio(); mIsAudioOpen = false;
	 * UIUtil.showToast(PlayBackActivity.this, "关闭音频");
	 * mAudioButton.setText("开启音频"); } else { boolean ret =
	 * mControl.startAudio(); if (!ret) { mIsAudioOpen = false;
	 * UIUtil.showToast(PlayBackActivity.this, "开启音频失败");
	 * mAudioButton.setText("开启音频"); } else { mIsAudioOpen = true; //
	 * 开启音频成功，并不代表一定有声音，需要设备开启声音。 UIUtil.showToast(PlayBackActivity.this,
	 * "开启音频成功"); mAudioButton.setText("关闭音频"); } } } }
	 */

	/**
	 * 启动播放 void
	 * 
	 * @since V1.0
	 */
	private void startBtnOnClick() {
		stopBtnOnClick();
		startHandler.sendEmptyMessage(0);
	}

	/**
	 * 停止播放 void
	 * 
	 * @since V1.0
	 */
	private void stopBtnOnClick() {
		if (null != mControl) {
			new Thread() {
				@Override
				public void run() {
					super.run();
					mControl.stopPlayBack();
				}
			}.start();
		}
	}

	/**
	 * 暂停、回放播放 void
	 * 
	 * @since V1.0
	 */
	private void pauseBtnOnClick() {
		if (null != mControl) {
			new Thread() {
				@Override
				public void run() {
					if (!mIsPause) {
						mControl.pausePlayBack();
					} else {
						mControl.resumePlayBack();
					}
					super.run();
				}
			}.start();
		}
	}

	/**
	 * 抓拍 void
	 * 
	 * @since V1.0
	 */
	private void captureBtnOnClick() {
		if (null != mControl) {
			// 随即生成一个1到10000的数字，用于抓拍图片名称的一部分，区分图片
			int recordIndex = new Random().nextInt(10000);
			boolean ret = mControl.capture(UtilFilePath.getPictureDirPath()
					.getAbsolutePath(), "Picture" + recordIndex + ".jpg");
			if (ret) {
				UIUtil.showToast(VideoPlaybackActivity.this, "抓拍成功");
				UtilAudioPlay.playAudioFile(VideoPlaybackActivity.this,
						R.raw.paizhao);
			} else {
				UIUtil.showToast(VideoPlaybackActivity.this, "抓拍失败");
				DebugLog.error(TAG, "captureBtnOnClick():: 抓拍失败");
			}
		}
	}

	/**
	 * 录像 void
	 * 
	 * @since V1.0
	 */
	private void recordBtnOnClick() {
		if (null != mControl) {
			if (!mIsRecord) {
				int recordIndex = new Random().nextInt(10000);
				mControl.startRecord(UtilFilePath.getVideoDirPath()
						.getAbsolutePath(), "Video" + recordIndex + ".mp4");
				mIsRecord = true;
				UIUtil.showToast(VideoPlaybackActivity.this, "启动录像成功");
				mRecordButton.setText("停止录像");
			} else {
				mControl.stopRecord();
				mIsRecord = false;
				UIUtil.showToast(VideoPlaybackActivity.this, "停止录像成功");
				mRecordButton.setText("开始录像");
			}
		}
	}

	@Override
	public void onMessageCallback(int message) {
		sendMessageCase(message);
	}

	/**
	 * 发送消息
	 * 
	 * @param i
	 *            void
	 * @since V1.0
	 */
	private void sendMessageCase(int i) {
		if (null != mMessageHandler) {
			Message msg = Message.obtain();
			msg.arg1 = i;
			mMessageHandler.sendMessage(msg);
		}
	}

	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case ConstantPlayBack.START_RTSP_SUCCESS:
				UIUtil.showToast(VideoPlaybackActivity.this, "启动取流库成功");
				break;

			case ConstantPlayBack.START_RTSP_FAIL:
				UIUtil.showToast(VideoPlaybackActivity.this, "启动取流库失败");
				if (null != mProgressBar) {
					mProgressBar.setVisibility(View.GONE);
				}
				break;

			case ConstantPlayBack.PAUSE_SUCCESS:
				UIUtil.showToast(VideoPlaybackActivity.this, "暂停成功");
				mPauseButton.setText("恢复");
				mIsPause = true;
				break;

			case ConstantPlayBack.PAUSE_FAIL:
				UIUtil.showToast(VideoPlaybackActivity.this, "暂停失败");
				mPauseButton.setText("暂停");
				mIsPause = false;

				break;

			case ConstantPlayBack.RESUEM_FAIL:
				UIUtil.showToast(VideoPlaybackActivity.this, "恢复播放失败");
				mPauseButton.setText("恢复");
				mIsPause = true;
				break;

			case ConstantPlayBack.RESUEM_SUCCESS:
				UIUtil.showToast(VideoPlaybackActivity.this, "恢复播放成功");
				mPauseButton.setText("暂停");
				mIsPause = false;
				break;

			case ConstantPlayBack.START_OPEN_FAILED:
				UIUtil.showToast(VideoPlaybackActivity.this, "启动播放库失败");
				if (null != mProgressBar) {
					mProgressBar.setVisibility(View.GONE);
				}
				break;

			case ConstantPlayBack.PLAY_DISPLAY_SUCCESS:
				if (null != mProgressBar) {
					mProgressBar.setVisibility(View.GONE);
				}
				DebugLog.info(TAG, "回放成功");
				break;
			case ConstantPlayBack.CAPTURE_FAILED_NPLAY_STATE:
				UIUtil.showToast(VideoPlaybackActivity.this, "非播状态不能抓怕");
				break;
			case ConstantPlayBack.PAUSE_FAIL_NPLAY_STATE:
				UIUtil.showToast(VideoPlaybackActivity.this, "非播放状态不能暂停");
				break;
			case ConstantPlayBack.RESUEM_FAIL_NPAUSE_STATE:
				UIUtil.showToast(VideoPlaybackActivity.this, "非播放状态");
				break;

			case RtspClient.RTSPCLIENT_MSG_CONNECTION_EXCEPTION:
				if (null != mProgressBar) {
					mProgressBar.setVisibility(View.GONE);
				}
				UIUtil.showToast(VideoPlaybackActivity.this, "RTSP链接异常");
				break;

			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

}