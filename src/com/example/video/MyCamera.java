package com.example.video;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.videodemo.R;

public class MyCamera extends Activity implements SensorEventListener {
	String						TAG				= "TAG";
	private static final String	PATH			= Environment.getExternalStorageDirectory() + "/2015";

	private Button				record_on_off;															// 开始录制按钮  
	private TextView			timing;
	private Button				toggle;
	private MediaRecorder		mMediaRecorder;														// 录制视频的类  
	private SurfaceView			surfaceview;															// 显示视频的控件  

	private SurfaceHolder		mSurfaceHolder;

	private Camera				mCamera;																// 摄像头
	private int					cameraId		= 0;													//0表示后置摄像头，1表示前置摄像头
	private boolean				isRecording;															//是否正在录制视频

	private CameraPreview		mPreview;
	private boolean             canAutoFocus;

	private boolean				mAutoFocus		= true;
	private SensorManager		mSensorManager;
	private Sensor				mAccel;
	private boolean				mInitialized	= false;
	private float				mLastX			= 0;
	private float				mLastY			= 0;
	private float				mLastZ			= 0;
	private Rect				rec				= new Rect();
	private int					mScreenHeight;
	private int					mScreenWidth;
	private boolean				mInvalidate		= false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_camera);
		initView();

		// Create an instance of Camera
		mCamera = getCameraInstance();
		show();

		// the accelerometer is used for autofocus
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//Log.i(TAG, "onPause()");

		releaseMediaRecorder(); // if you are using MediaRecorder, release it first
		releaseCamera(); // release the camera immediately on pause event

		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
		//Log.i(TAG, "onResume()");
	}

	private void show() {
		mPreview = new CameraPreview(this, mCamera);
		mSurfaceHolder = mPreview.getHolder();
		// Create our Preview view and set it as the content of our activity.
		FrameLayout preview = (FrameLayout) findViewById(R.id.framelayout);
		preview.addView(mPreview, 0);
		canAutoFocus = true;
	}

	private void initView() {
		record_on_off = (Button) this.findViewById(R.id.start_or_stop);
		timing = (TextView) findViewById(R.id.timing);
		toggle = (Button) findViewById(R.id.toggle);

		record_on_off.setOnClickListener(onClickListener);
		toggle.setOnClickListener(onClickListener);
	}

	private OnClickListener	onClickListener	= new View.OnClickListener() {

												@Override
												public void onClick(View v) {

													switch (v.getId()) {
													case R.id.toggle:
														// 切换摄像头
														toggleCamera();
														break;
													case R.id.start_or_stop:
														if (isRecording) {
															// stop recording and release camera
															mMediaRecorder.stop(); // stop the recording
															releaseMediaRecorder(); // release the MediaRecorder object
															mCamera.lock(); // take camera access back from MediaRecorder

															// inform the user that recording has stopped
															isRecording = false;
															record_on_off.setText("start");
															toggle.setVisibility(View.VISIBLE);
															stopTiming();
														}
														else {
															// initialize video camera
															if (prepareVideoRecorder(mCamera, PATH + "/" + System.currentTimeMillis() + ".3gp")) {
																// Camera is available and unlocked, MediaRecorder is prepared,
																// now you can start recording
																mMediaRecorder.start();

																// inform the user that recording has started
																isRecording = true;
																startTiming();
																record_on_off.setText("stop");
																toggle.setVisibility(View.INVISIBLE);
															}
															else {
																// prepare didn't work, release the camera
																releaseMediaRecorder();
																// inform user

															}
														}
														break;
													}
												}
											};

	private boolean prepareVideoRecorder(Camera mCamera, String path) {
		mMediaRecorder = new MediaRecorder();
		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		// Step 2: Set sources
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
		camcorderProfile.fileFormat = MediaRecorder.OutputFormat.THREE_GPP;
		camcorderProfile.audioCodec = MediaRecorder.AudioEncoder.AAC;
		camcorderProfile.videoCodec = MediaRecorder.VideoEncoder.H264;
		camcorderProfile.videoFrameWidth = 640;
		camcorderProfile.videoFrameHeight = 480;
		camcorderProfile.videoFrameRate = 25;
		camcorderProfile.audioSampleRate = 22050;
		camcorderProfile.audioBitRate = 64000;
		camcorderProfile.videoBitRate = 900000;
		mMediaRecorder.setProfile(camcorderProfile);

		//		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		//		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		//		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		//		mMediaRecorder.setVideoSize(640, 480);
		//		mMediaRecorder.setVideoFrameRate(25);
		//		mMediaRecorder.setAudioSamplingRate(22050);
		//		mMediaRecorder.setAudioEncodingBitRate(64000);
		//		mMediaRecorder.setVideoEncodingBitRate(1000000);

		// Step 4: Set output file
		mMediaRecorder.setOutputFile(path);
		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			e.printStackTrace();
		}
		return c; // returns null if camera is unavailable
	}

	public void toggleCamera() {//切换前后摄像头
		CameraInfo cameraInfo = new CameraInfo();
		int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
			if (cameraId == 0) {
				//现在是后置，变更为前置
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置  
					mCamera.stopPreview();//停掉原来摄像头的预览
					mCamera.release();//释放资源
					mCamera = null;//取消原来摄像头
					mCamera = Camera.open(i);//打开当前选中的摄像头
					try {
						mCamera.setPreviewDisplay(mSurfaceHolder);//通过surfaceview显示取景画面
						mCamera.setDisplayOrientation(90);
					} catch (IOException e) {
						e.printStackTrace();
					}
					mCamera.startPreview();//开始预览
					canAutoFocus = true;
					cameraId = i;
					break;
				}
			}
			else if (cameraId == 1) {
				//现在是前置， 变更为后置
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置  
					mCamera.stopPreview();//停掉原来摄像头的预览
					mCamera.release();//释放资源
					mCamera = null;//取消原来摄像头
					mCamera = Camera.open(i);//打开当前选中的摄像头
					try {
						//						Parameters parameters = mCamera.getParameters();
						//						if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						//							//如果是竖屏
						//							parameters.set("orientation", "portrait");
						//							//在2.2以上可以使用
						//							//camera.setDisplayOrientation(90);
						//						}
						//						else {
						//							parameters.set("orientation", "landscape");
						//							//在2.2以上可以使用
						//							//camera.setDisplayOrientation(0);
						//						}
						//						mCamera.setParameters(parameters);
						mCamera.setPreviewDisplay(mSurfaceHolder);//通过surfaceview显示取景画面
						mCamera.setDisplayOrientation(90);
					} catch (IOException e) {
						e.printStackTrace();
					}
					mCamera.startPreview();//开始预览
					canAutoFocus = true;
					cameraId = i;
					break;
				}
			}
		}
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		}
		else {
			// no camera on this device
			return false;
		}
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
		canAutoFocus = false;
	}

	Handler		handler	= new Handler() {

							@Override
							public void handleMessage(Message msg) {
								switch (msg.what) {
								case 0:
									long m = timelong / 60;
									long s = timelong % 60;
									String left = m < 10 ? "0" + m : "" + m;
									String right = s < 10 ? "0" + s : "" + s;
									timing.setText(left + ":" + right);
									break;
								case 1:
									timing.setText("00:00");
									timelong = 0;
									break;
								}

							}
						};

	Timer		timer;
	long		timelong;

	//开始计时
	private synchronized void startTiming() {
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				timelong++;
				handler.obtainMessage(0).sendToTarget();
			}
		};
		if(timer == null)
			timer = new Timer();
		timer.schedule(timerTask, 1000, 1000);
	}

	//停止计时
	private synchronized void stopTiming() {
		timer.cancel();
		timer = null;
		handler.obtainMessage(1).sendToTarget();
	}

	public void setCameraFocus(AutoFocusCallback autoFocus) {
		if (mCamera != null)
			if (mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_AUTO) || mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_MACRO)) {
				mCamera.autoFocus(autoFocus);
			}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (mInvalidate == true) {
			mInvalidate = false;
		}
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			mInitialized = true;
		}
		float deltaX = Math.abs(mLastX - x);
		float deltaY = Math.abs(mLastY - y);
		float deltaZ = Math.abs(mLastZ - z);

		if (deltaX > .5 && mAutoFocus) { //AUTOFOCUS (while it is not autofocusing)
			mAutoFocus = false;
			mPreview.setCameraFocus(mCamera,myAutoFocusCallback);
		}
		if (deltaY > .5 && mAutoFocus) { //AUTOFOCUS (while it is not autofocusing)
			mAutoFocus = false;
			mPreview.setCameraFocus(mCamera,myAutoFocusCallback);
		}
		if (deltaZ > .5 && mAutoFocus) { //AUTOFOCUS (while it is not autofocusing) */
			mAutoFocus = false;
			mPreview.setCameraFocus(mCamera,myAutoFocusCallback);
		}

		mLastX = x;
		mLastY = y;
		mLastZ = z;
	}

	// this is the autofocus call back
	private AutoFocusCallback	myAutoFocusCallback	= new AutoFocusCallback() {

														public void onAutoFocus(boolean autoFocusSuccess, Camera camera) {
															//Wait.oneSec();
															mAutoFocus = true;
														}
													};
}
