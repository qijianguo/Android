package com.adc.hbj5;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.data.TempData;
import com.adc.hbj5.R;
import com.adc.live.ConstantLive;
import com.adc.live.LiveCallBack;
import com.adc.live.LiveControl;
import com.adc.oldactivity.OldVideoActivity;
import com.adc.util.DebugLog;
import com.adc.util.UIUtil;
import com.adc.util.UtilAudioPlay;
import com.adc.util.UtilFilePath;
import com.hikvision.vmsnetsdk.CameraInfo;
import com.hikvision.vmsnetsdk.RealPlayURL;
import com.hikvision.vmsnetsdk.VMSNetSDK;

/**
 * 预览
 * 
 * @author huangweifeng
 * @Data 2013-10-21
 */
public class VideoLiveActivity extends Activity implements OnClickListener,
		Callback, LiveCallBack {
	private static final String TAG = "VideoLiveActivity";
	/**
	 * 开始播放按钮
	 */
	//private Button mStartBtn;
	/**
	 * 停止播放按钮
	 */
	//private Button mStopBtn;
	/**
	 * 抓拍按钮
	 */
	private Button mCaptureBtn;
	/**
	 * 录像按钮
	 */
	// private Button mRecordBtn;
	/**
	 * 音频按钮
	 */
	// private Button mAudioBtn;
	/**
	 * 码流切换
	 */
	// private RadioGroup mRadioGroup;
	/**
	 * 码流类型
	 */
	private int mStreamType = -1;
	/**
	 * 通过VMSNetSDK返回的预览地址对象
	 */
	private RealPlayURL mRealPlayURL;
	/**
	 * 登录设备的用户名
	 */
	private String mName;
	/**
	 * 登录设备的密码
	 */
	private String mPassword;
	/**
	 * 控制层对象
	 */
	private LiveControl mLiveControl;
	/**
	 * 播放视频的控件对象
	 */
	private SurfaceView mSurfaceView;
	/**
	 * 创建取流等待bar
	 */
	private ProgressBar mProgressBar;
	/**
	 * 创建消息对象
	 */
	private Handler mMessageHandler = new MyHandler();
	/**
	 * 音频是否开启
	 */
	private boolean mIsAudioOpen;
	/**
	 * 是否正在录像
	 */
	private boolean mIsRecord;
	/**
	 * 播放流量
	 */
	private long mStreamRate = 0;
	/**
	 * 监控点信息对象
	 */
	private CameraInfo cameraInfo;
	/**
	 * 云台控制界面布局区域
	 */
	private RelativeLayout cloudCtrlArea;
	/**
	 * 云台控制按钮
	 */
	private Button startCtrlBtn;
	/**
	 * 停止云台控制按钮
	 */
	private Button stopCtrlBtn;
	/**
	 * 云台控制对话框
	 */
	private AlertDialog mDialog;

	/**
	 * 视频直播返回按钮
	 */
	private Button live_goback;

	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			/*Intent intent = new Intent(VideoLiveActivity.this,
					HxdbTabActivity.class);*/
			/*Intent intent = new Intent(VideoLiveActivity.this,
					MainTabActivity.class);
			//MainTabActivity.hxdb_tjfx_setup = 0;
			startActivity(intent);*/
			finish();
			return false;
		}
		return false;
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_live);

		live_goback = (Button) findViewById(R.id.live_goback1);
		live_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Intent intent = new Intent(VideoLiveActivity.this,
						HxdbTabActivity.class);*/
				/*Intent intent = new Intent(VideoLiveActivity.this,
						MainTabActivity.class);
				//MainTabActivity.hxdb_tjfx_setup = 0;
				startActivity(intent);*/
				finish();
			}
		});

		initData();

		initUI();
		
	}
	
	/*
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		startBtnOnClick();
	}
	*/
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		stopBtnOnClick();
		mSurfaceView.setVisibility(8);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startBtnOnClick();
		mSurfaceView.setVisibility(0);
	}
	
	/**
	 * 初始化网络库和控制层对象
	 * 
	 * @since V1.0
	 */
	private void initData() {
		mRealPlayURL = new RealPlayURL();
		mLiveControl = new LiveControl();
		mLiveControl.setLiveCallBack(this);
		CameraInfo temp_info = new CameraInfo();
		int idx = SpotInfoTabActivity.spot_idx;
		if(idx >= 100000)	idx -= 100000;
		temp_info.cameraID = spotInfos.get(idx).getCamera_id();
		//temp_info.acsIP = "140.207.216.106";
		temp_info.acsIP = spotInfos.get(idx).getVideo_service_ip();
		temp_info.acsPort = 7302;
		temp_info.isPTZControl = true;
		
		TempData.getIns().setCameraInfo(temp_info);
				
		cameraInfo = TempData.getIns().getCameraInfo();
		//Log.i("heheda","init data ok!!!!!!!!!!!!!!,camerainfo.id="+cameraInfo.cameraID);
	}

	/**
	 * 初始化控件
	 * 
	 * @since V1.0
	 */
	private void initUI() {
		//mStartBtn = (Button) findViewById(R.id.liveStartBtn1);
		//mStartBtn.setOnClickListener(this);
		//mStopBtn = (Button) findViewById(R.id.liveStopBtn1);
		//mStopBtn.setOnClickListener(this);
		mCaptureBtn = (Button) findViewById(R.id.liveCaptureBtn1);
		mCaptureBtn.setOnClickListener(this);

		/*
		 * mRecordBtn = (Button) findViewById(R.id.liveRecordBtn);
		 * mRecordBtn.setOnClickListener(this); mRecordBtn.setText("启动录像");
		 */

		/*
		 * mAudioBtn = (Button) findViewById(R.id.liveAudioBtn);
		 * mAudioBtn.setOnClickListener(this); mAudioBtn.setText("开启音频");
		 */

		/*
		 * mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		 * mRadioGroup.setOnCheckedChangeListener(this);
		 * mRadioGroup.check(R.id.magRadio);
		 */
		
		//这里被tyz写成固定为子码流（除武汉 20160713）
		//如果是武汉为MAG
		if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
			mStreamType = ConstantLive.MAG;
		}else{
			mStreamType = ConstantLive.SUB_STREAM;
		}
		
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		mSurfaceView.getHolder().addCallback(this);

		mProgressBar = (ProgressBar) findViewById(R.id.liveProgressBar1);
		mProgressBar.setVisibility(View.INVISIBLE);

		//cloudCtrlArea = (RelativeLayout) findViewById(R.id.cloud_area1);
		// 云台控制需要根据权限来显示
		//cloudCtrlArea.setVisibility(cameraInfo.isPTZControl ? View.VISIBLE
		//		: View.GONE);
		startCtrlBtn = (Button) findViewById(R.id.start_ctrl1);
		stopCtrlBtn = (Button) findViewById(R.id.stop_ctrl1);
		startCtrlBtn.setOnClickListener(this);
		stopCtrlBtn.setOnClickListener(this);
		
		Log.i("heheda", "init ui succeed!!!!!!!1");
	}

	/*
	 * @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
	 * if (group.getId() == R.id.radioGroup) { switch
	 * (group.getCheckedRadioButtonId()) { case R.id.mainRadio: mStreamType =
	 * ConstantLive.MAIN_STREAM; break;
	 * 
	 * case R.id.subRadio: mStreamType = ConstantLive.SUB_STREAM; break;
	 * 
	 * case R.id.magRadio: mStreamType = ConstantLive.MAG; break; } } }
	 */

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		//case R.id.liveStartBtn1:
		//	Log.i("heheda", "press liveStartBtn!!!!!");
		//	startBtnOnClick();
		//	break;

		//case R.id.liveStopBtn1:
		//	stopBtnOnClick();
		//	break;

		case R.id.liveCaptureBtn1:
			captureBtnOnClick();
			break;

		// case R.id.liveRecordBtn:
		// recordBtnOnClick();
		// break;

		// case R.id.liveAudioBtn:
		// audioBtnOnClick();
		// break;
		case R.id.start_ctrl1:
			startCloudCtrl();
			break;
		case R.id.stop_ctrl1:
			stopCloudCtrl();
			break;
		default:
			break;
		}
	}

	/**
	 * 开始云台控制，弹出控制界面
	 */
	private void startCloudCtrl() {
		Log.i("heheda", "here ok!!!!!!!!!!!!!!!!000000000000000000000");
		final int[] gestureIDs = { 1, 2, 3, 4, 11, 12, 13, 14, 7, 8, 9, 10 };
		String[] datas = {"云台转上", "云台转下", "云台转左", "云台转右", "云台左上", "云台右上",
				"云台左下", "云台右下", "镜头拉近", "镜头拉远", "镜头近焦", "镜头远焦" };
		Log.i("heheda", "here ok!!!!!!!!!!!!!!!!111111111111111");
		mDialog = new AlertDialog.Builder(getParent()).setSingleChoiceItems(datas, 0,
				new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i("heheda", "here ok!!!!!!!!!!!!!!!!2222222222222222222");
						mDialog.dismiss();
						sendCtrlCmd(gestureIDs[which]);
					}
				}).create();
		mDialog.show();
		Log.i("heheda", "here ok!!!!!!!!!!!!!!!!3333333333333333");
	}

	/**
	 * 发送云台控制命令
	 * 
	 * @param gestureID
	 *            1-云台转上 、2-云台转下 、3-云台转左 、4-云台转右、 11-云台左上 、12-云台右上 13-云台左下
	 *            、14-云台右下、7-镜头拉近、8-镜头拉远、9-镜头近焦、10-镜头远焦
	 */
	private void sendCtrlCmd(final int gestureID) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String sessionID = TempData.getIns().getLoginData().sessionID;
				// 云台控制速度 取值范围(1-10)
				int speed = 1;
				Log.i(Constants.LOG_TAG, "ip:" + cameraInfo.acsIP + ",port:"
						+ cameraInfo.acsPort + ",isPTZControl:"
						+ cameraInfo.isPTZControl);
				Log.e("heheda", "ip:" + cameraInfo.acsIP + ",port:"
						+ cameraInfo.acsPort + ",isPTZControl:"
						+ cameraInfo.isPTZControl);
				// 发送控制命令
				boolean ret = VMSNetSDK.getInstance().sendStartPTZCmd(
						cameraInfo.acsIP, cameraInfo.acsPort, sessionID,
						cameraInfo.cameraID, gestureID, speed, 600);
				Log.i(Constants.LOG_TAG, "sendStartPTZCmd ret:" + ret);
				Log.e("heheda", "sendStartPTZCmd ret:" + ret);
			}
		}).start();
	}

	/**
	 * 停止云台控制
	 */
	private void stopCloudCtrl() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String sessionID = TempData.getIns().getLoginData().sessionID;
				boolean ret = VMSNetSDK.getInstance().sendStopPTZCmd(
						cameraInfo.acsIP, cameraInfo.acsPort, sessionID,
						cameraInfo.cameraID);
				Log.i(Constants.LOG_TAG, "stopPtzCmd sent,ret:" + ret);
			}
		}).start();
	}

	/**
	 * 启动播放 void
	 * 
	 * @since V1.0
	 */
	private void startBtnOnClick() {
		mProgressBar.setVisibility(View.VISIBLE);
		new Thread() {
			@Override
			public void run() {
				super.run();
				mLiveControl.setLiveParams(getPlayUrl(mStreamType), mName,
						mPassword);
				Log.e("hihida", "mname=" + mName);
				Log.e("hihida", "mpwd=" + mPassword);
				Log.e("hihida", "state=" + mLiveControl.getLiveState());
				if (mLiveControl.LIVE_PLAY == mLiveControl.getLiveState()) {
					mLiveControl.stop();
				}

				if (mLiveControl.LIVE_INIT == mLiveControl.getLiveState()) {
					mLiveControl.startLive(mSurfaceView);
				}
			}
		}.start();
	}

	/**
	 * 该方法是获取播放地址的，当mStreamType=2时，获取的是MAG，当mStreamType =1时获取的子码流，当mStreamType =
	 * 0时获取的是主码流 由于该方法中部分参数是监控点的属性，所以需要先获取监控点信息，具体获取监控点信息的方法见resourceActivity。
	 * 
	 * @param streamType
	 *            2、表示MAG取流方式；1、表示子码流取流方式；0、表示主码流取流方式；
	 * @return String 播放地址 ：2、表示返回的是MAG的播放地址;1、表示返回的是子码流的播放地址；0、表示返回的是主码流的播放地址。
	 * @since V1.0
	 */
	private String getPlayUrl(int streamType) {
		String url = "";
		// 登录平台地址
		//String mAddress = Constants.servAddr;
		String mAddress = TempData.getIns().getHikvisionServerAddr();
		// 登录返回的sessionid
		String mSessionID = TempData.getIns().getLoginData().sessionID;
		if (streamType == 2) {
			// TODO 原有代码streamType传0
			VMSNetSDK.getInstance().getRealPlayURL(mAddress, mSessionID,
					cameraInfo.cameraID, streamType, mRealPlayURL);
			if (null == mRealPlayURL) {
				DebugLog.info(TAG, "getPlayUrl():: mRealPlayURL is null");
				return "";
			}
			// MAG地址
			url = mRealPlayURL.url2;
			DebugLog.info(TAG, "11111 getPlayUrl():: url is " + url);
			Log.e("heheda", url);
		} else {
			VMSNetSDK.getInstance().getRealPlayURL(mAddress, mSessionID,
					cameraInfo.cameraID, streamType, mRealPlayURL);
			Log.e("hihida", mAddress);
			Log.e("hihida", mSessionID);
			Log.e("hihida", cameraInfo.cameraID);
			Log.e("hihida", "" + streamType);
			Log.e("hihida", mRealPlayURL.url1);
			if (null == mRealPlayURL) {
				DebugLog.info(TAG, "getPlayUrl():: mRealPlayURL is null");
				return "";
			}
			// mRealPlayURL.url1 是主码流还是子码流取决于 streamType，见上面注释
			url = mRealPlayURL.url1;
			DebugLog.info(TAG, "22222 getPlayUrl():: url is " + url);
			Log.e("heheda", url);
		}
		return url;
	}

	/**
	 * 停止播放 void
	 * 
	 * @since V1.0
	 */
	private void stopBtnOnClick() {
		if (null != mLiveControl) {
			mLiveControl.stop();
		}
	}

	/**
	 * 抓拍 void
	 * 
	 * @since V1.0
	 */
	private void captureBtnOnClick() {
		if (null != mLiveControl) {
			//随即生成一个1到10000的数字，用于抓拍图片名称的一部分，区分图片，开发者可以根据实际情况修改区分图片名称的方法
			int recordIndex = new Random().nextInt(10000);
			boolean ret = mLiveControl.capture(UtilFilePath.getPictureDirPath()
					.getAbsolutePath(), "Picture" + recordIndex + ".jpg");

			if (ret) {
				UIUtil.showToast(VideoLiveActivity.this, "抓拍成功");
				Log.i("heheda", "capture photo path="
						+ UtilFilePath.getPictureDirPath().getAbsolutePath());
				UtilAudioPlay.playAudioFile(VideoLiveActivity.this, R.raw.paizhao);
			} else {
				UIUtil.showToast(VideoLiveActivity.this, "抓拍失败");
				DebugLog.error(TAG, "captureBtnOnClick():: 抓拍失败");
			}
		}
	}

	/**
	 * 录像 void
	 * 
	 * @since V1.0
	 */
	/*
	 * private void recordBtnOnClick() { if (null != mLiveControl) { if
	 * (!mIsRecord) { // 随即生成一个1到10000的数字，用于录像名称的一部分，区分图片，开发者可以根据实际情况修改区分录像名称的方法
	 * int recordIndex = new Random().nextInt(10000);
	 * mLiveControl.startRecord(UtilFilePath
	 * .getVideoDirPath().getAbsolutePath(), "Video" + recordIndex + ".mp4");
	 * Log.i("heheda",
	 * "capture viedo path="+UtilFilePath.getVideoDirPath().getAbsolutePath());
	 * mIsRecord = true; UIUtil.showToast(LiveActivity.this, "启动录像成功");
	 * mRecordBtn.setText("停止录像"); } else { mLiveControl.stopRecord(); mIsRecord
	 * = false; UIUtil.showToast(LiveActivity.this, "停止录像成功");
	 * mRecordBtn.setText("开始录像"); } } }
	 */

	/**
	 * 音频 void
	 * 
	 * @since V1.0
	 */
	/*
	 * private void audioBtnOnClick() { if (null != mLiveControl) { if
	 * (mIsAudioOpen) { mLiveControl.stopAudio(); mIsAudioOpen = false;
	 * UIUtil.showToast(LiveActivity.this, "关闭音频"); mAudioBtn.setText("开启音频"); }
	 * else { boolean ret = mLiveControl.startAudio(); if (!ret) { mIsAudioOpen
	 * = false; UIUtil.showToast(LiveActivity.this, "开启音频失败");
	 * mAudioBtn.setText("开启音频"); } else { mIsAudioOpen = true; //
	 * 开启音频成功，并不代表一定有声音，需要设备开启声音。 UIUtil.showToast(LiveActivity.this, "开启音频成功");
	 * mAudioBtn.setText("关闭音频"); } } }
	 * 
	 * }
	 */

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (null != mLiveControl) {
			if (mIsRecord) {
				// mRecordBtn.setText("开始录像");
				mLiveControl.stopRecord();
				mIsRecord = false;
			}
			mLiveControl.stop();
		}
	}

	@Override
	public void onMessageCallback(int messageID) {
		sendMessageCase(messageID);
	}

	/**
	 * 返回已经播放的流量 void
	 * 
	 * @return long
	 * @since V1.0
	 */
	public long getStreamRate() {
		return mStreamRate;
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

	/**
	 * 消息类
	 * 
	 * @author huangweifeng
	 * @Data 2013-10-23
	 */
	@SuppressLint("HandlerLeak")
	private final class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case ConstantLive.RTSP_SUCCESS:
				UIUtil.showToast(VideoLiveActivity.this, "启动取流成功");
				break;

			case ConstantLive.STOP_SUCCESS:
				UIUtil.showToast(VideoLiveActivity.this, "停止成功");
				break;

			case ConstantLive.START_OPEN_FAILED:
				UIUtil.showToast(VideoLiveActivity.this, "开启播放库失败");
				if (null != mProgressBar) {
					mProgressBar.setVisibility(View.GONE);
				}
				break;

			case ConstantLive.PLAY_DISPLAY_SUCCESS:
				UIUtil.showToast(VideoLiveActivity.this, "播放成功");
				if (null != mProgressBar) {
					mProgressBar.setVisibility(View.GONE);
				}
				break;

			case ConstantLive.RTSP_FAIL:
				UIUtil.showToast(VideoLiveActivity.this, "RTSP链接失败");
				if (null != mProgressBar) {
					mProgressBar.setVisibility(View.GONE);
				}
				if (null != mLiveControl) {
					mLiveControl.stop();
				}
				break;

			case ConstantLive.GET_OSD_TIME_FAIL:
				UIUtil.showToast(VideoLiveActivity.this, "获取OSD时间失败");
				break;

			case ConstantLive.SD_CARD_UN_USEABLE:
				UIUtil.showToast(VideoLiveActivity.this, "SD卡不可用");
				break;

			case ConstantLive.SD_CARD_SIZE_NOT_ENOUGH:
				UIUtil.showToast(VideoLiveActivity.this, "SD卡空间不足");
				break;
			case ConstantLive.CAPTURE_FAILED_NPLAY_STATE:
				UIUtil.showToast(VideoLiveActivity.this, "非播放状态不能抓拍");
				break;
			}
		}
	}
	
}
