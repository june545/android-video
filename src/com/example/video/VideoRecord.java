package com.example.video;

import java.io.IOException;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.videodemo.R;

public class VideoRecord extends Activity implements SurfaceHolder.Callback {
	String						TAG		= "TAG";
	private static final String	PATH	= Environment.getExternalStorageDirectory() + "/2015";
	private Button				start;															// 开始录制按钮  
	private Button				stop;															// 停止录制按钮  
	private MediaRecorder		mMediaRecorder;												// 录制视频的类  
	private SurfaceView			surfaceview;													// 显示视频的控件  
	// 用来显示视频的一个接口，我靠不用还不行，也就是说用mediarecorder录制视频还得给个界面看  
	// 想偷偷录视频的同学可以考虑别的办法。。嗯需要实现这个接口的Callback接口  
	private SurfaceHolder		mSurfaceHolder;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏  
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏  
		// 设置横屏显示  
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// 选择支持半透明模式,在有surfaceview的activity中使用。  
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.video);
		initView();
		initSurfaceView();
	}

	private void initView() {
		start = (Button) this.findViewById(R.id.start);
		stop = (Button) this.findViewById(R.id.stop);
		start.setOnClickListener(new TestVideoListener());
		stop.setOnClickListener(new TestVideoListener());
	}

	private void initSurfaceView() {
		surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
		mSurfaceHolder = surfaceview.getHolder();// 取得holder  
		mSurfaceHolder.addCallback(this); // holder加入回调接口  
		// setType必须设置，要不出错.  
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void video_h() {
		CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
		System.out.println(mProfile);
		mMediaRecorder = new MediaRecorder();// 创建mediarecorder对象  
		// 设置录制视频源为Camera(相机)  
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

		// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4  
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		// 设置录制的视频编码h263 h264  
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

		// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错  
		mMediaRecorder.setVideoSize(640, 480);
		// 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错  
		System.out.println("aaaaa videoFrameRate " + mProfile.videoFrameRate);
		mMediaRecorder.setVideoFrameRate(25);

		System.out.println("aaaaa audioSampleRate " + mProfile.audioSampleRate);
		mMediaRecorder.setAudioSamplingRate(22050);

		System.out.println("aaaaa audioBitRate " + mProfile.audioBitRate);
		mMediaRecorder.setAudioEncodingBitRate(64000);

		System.out.println("aaaaa videoBitRate " + mProfile.videoBitRate);
		mMediaRecorder.setVideoEncodingBitRate(1000000);

		mMediaRecorder.setOrientationHint(90);

		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

		// 设置视频文件输出的路径  
		mMediaRecorder.setOutputFile(PATH + "/" + System.currentTimeMillis() + ".3gp");
		try {
			// 准备录制  
			mMediaRecorder.prepare();
			// 开始录制  
			mMediaRecorder.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block  
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block  
			e.printStackTrace();
		}

	}

	class TestVideoListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v == start) {
				video_h();
			}
			if (v == stop) {
				if (mMediaRecorder != null) {
					// 停止录制  
					mMediaRecorder.stop();
					// 释放资源  
					mMediaRecorder.release();
					mMediaRecorder = null;
				}
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder  
		mSurfaceHolder = holder;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder  
		mSurfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// surfaceDestroyed的时候同时对象设置为null  
		surfaceview = null;
		mSurfaceHolder = null;
		mMediaRecorder = null;
	}

	/*【2】【相机预览】*/
	//	private void initCamera()//surfaceChanged中调用
	//	{
	//		Log.i(TAG, "going into initCamera");
	//		if (ifPreview) {
	//			mCamera.stopPreview();//stopCamera();
	//		}
	//		if (null != mCamera) {
	//			try {
	//				/* Camera Service settings*/
	//				Camera.Parameters parameters = mCamera.getParameters();
	//				// parameters.setFlashMode("off"); // 无闪光灯
	//				parameters.setPictureFormat(PixelFormat.JPEG); //Sets the image format for picture 设定相片格式为JPEG，默认为NV21    
	//				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP); //Sets the image format for preview picture，默认为NV21
	//				/*【ImageFormat】JPEG/NV16(YCrCb format，used for Video)/NV21(YCrCb format，used for Image)/RGB_565/YUY2/YU12*/
	//
	//				// 【调试】获取caera支持的PictrueSize，看看能否设置？？
	//				List<Size> pictureSizes = mCamera.getParameters().getSupportedPictureSizes();
	//				List<Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
	//				List<Integer> previewFormats = mCamera.getParameters().getSupportedPreviewFormats();
	//				List<Integer> previewFrameRates = mCamera.getParameters().getSupportedPreviewFrameRates();
	//				Log.i(TAG + "initCamera", "cyy support parameters is ");
	//				Size psize = null;
	//				for (int i = 0; i < pictureSizes.size(); i++) {
	//					psize = pictureSizes.get(i);
	//					Log.i(TAG + "initCamera", "PictrueSize,width: " + psize.width + " height" + psize.height);
	//				}
	//				for (int i = 0; i < previewSizes.size(); i++) {
	//					psize = previewSizes.get(i);
	//					Log.i(TAG + "initCamera", "PreviewSize,width: " + psize.width + " height" + psize.height);
	//				}
	//				Integer pf = null;
	//				for (int i = 0; i < previewFormats.size(); i++) {
	//					pf = previewFormats.get(i);
	//					Log.i(TAG + "initCamera", "previewformates:" + pf);
	//				}
	//
	//				// 设置拍照和预览图片大小
	//				parameters.setPictureSize(640, 480); //指定拍照图片的大小
	//				parameters.setPreviewSize(mPreviewWidth, mPreviewHeight); // 指定preview的大小 
	//				//这两个属性 如果这两个属性设置的和真实手机的不一样时，就会报错
	//
	//				// 横竖屏镜头自动调整
	//				if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
	//					parameters.set("orientation", "portrait"); //
	//					parameters.set("rotation", 90); // 镜头角度转90度（默认摄像头是横拍） 
	//					mCamera.setDisplayOrientation(90); // 在2.2以上可以使用
	//				}
	//				else// 如果是横屏
	//				{
	//					parameters.set("orientation", "landscape"); //
	//					mCamera.setDisplayOrientation(0); // 在2.2以上可以使用
	//				}
	//
	//				/* 视频流编码处理 */
	//				//添加对视频流处理函数
	//
	//				// 设定配置参数并开启预览
	//				mCamera.setParameters(parameters); // 将Camera.Parameters设定予Camera    
	//				mCamera.startPreview(); // 打开预览画面
	//				bIfPreview = true;
	//
	//				// 【调试】设置后的图片大小和预览大小以及帧率
	//				Camera.Size csize = mCamera.getParameters().getPreviewSize();
	//				mPreviewHeight = csize.height; //
	//				mPreviewWidth = csize.width;
	//				Log.i(TAG + "initCamera", "after setting, previewSize:width: " + csize.width + " height: " + csize.height);
	//				csize = mCamera.getParameters().getPictureSize();
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}
}
