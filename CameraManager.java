package com.sukohi.lib;

/*  Dependency: DisplaySize.java  */

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class CameraManager {

	private boolean takingPictureFlag = false;
	private int maxPictureWidth = 1280;
	private int cameraCount = 0;
	private int currentCameraId = 0;
	private int backCameraId = -1;
	private int frontCameraId = -1;
	private int openCameraId = -1;
	private byte[] previewData;
	private Map<String, String> parameterMap = new HashMap<String, String>();
	private Context context;
	private SurfaceView surfaceView;
	private Camera camera;
	private SurfaceHolder holder;
	private Camera.Parameters cameraParameters;
	private CameraManagerTakePictureCallback takePictureCallback;
	private CameraManagerAutoFocusCallback autoFocusCallback;
	private CameraManagerPreviewCallback previewCallback;
	
	public CameraManager(Context context, SurfaceView surfaceView) {
		
		this.context = context;
		this.surfaceView = surfaceView;
		
	}
	
	public void setMaxPictureWidth(int width) {
		
		maxPictureWidth = width;
		
	}

	public void setTakePictureCallback(CameraManagerTakePictureCallback callback) {
		
		takePictureCallback = callback;
		
	}
	
	public void takePicture() {
		
		takingPictureFlag = true;
		
		camera.takePicture(null, null, new Camera.PictureCallback() {
			
			public void onPictureTaken(byte[] data, Camera camera) {

				if(takePictureCallback != null) {
					
					takePictureCallback.result(data, camera);
					
				}
				
				if(data != null) {
					
					data = null;
					camera.startPreview();
					
				}
				
				takingPictureFlag = false;
				
			}
			
		});
		
	}

	public void setAutoFocusCallback(CameraManagerAutoFocusCallback callback) {
		
		autoFocusCallback = callback;
		
	}
	
	public void autoFocus() {
		
		if(camera == null) {
			
			return;
			
		}
		
		camera.autoFocus(new AutoFocusCallback() {
			
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				
				autoFocusCallback.result(previewData, camera);

			}
			
		});
		
	}
	
	public void setPreviewCallback(CameraManagerPreviewCallback callback) {
		
		previewCallback = callback;
		
	}
	
	public int getFrontCameraId() {
		
		return frontCameraId;
		
	}
	
	public int getBackCameraId() {
		
		return backCameraId;
		
	}
	
	public int getCurrentCameraId() {
		
		return currentCameraId;
		
	}
	
	public int getCameraCount() {
		
		return cameraCount;
		
	}
	
	public boolean isTakingPicture() {
		
		return takingPictureFlag;
		
	}
	
	public boolean isFrontCamera() {
		
		return (currentCameraId == frontCameraId);
		
	}
	
	public boolean isBackCamera() {
		
		return (currentCameraId == backCameraId);
		
	}
	
	public boolean isFlashSupported(String flashMode) {
		
		List<String> supportedFlashModes = cameraParameters.getSupportedFlashModes();
		return (supportedFlashModes.contains(flashMode));
		
	}
	
	public boolean hasFlash() {
		
		cameraParameters = camera.getParameters();
		return (cameraParameters.getSupportedFlashModes() != null);
		
	}
	
	public boolean hasAutoFocus() {
		
		PackageManager packageManager = context.getApplicationContext().getPackageManager();
		return (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS));
		
	}
	
	public void changeCamera() {
		
		surfaceHolderCallback.surfaceDestroyed(holder);
 
		if(currentCameraId == backCameraId) {
			
			currentCameraId = frontCameraId;
			
		} else if (currentCameraId == frontCameraId) {
			
			currentCameraId = backCameraId;
			
		} else {
			
			currentCameraId = frontCameraId;
			
		}
		
		camera = Camera.open(currentCameraId);
		try {
			
			surfaceHolderCallback.surfaceChanged(holder, 0, 0, 0);
			camera.startPreview();
			camera.setPreviewDisplay(holder);
			setCameraParameters();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public void setParameters(Map<String, String> parameterMap) {
		
		this.parameterMap = parameterMap;
		
	}
	
	public Parameters getParameters() {
		
		return camera.getParameters();
		
	}
	
	public void setFlashMode(String flashMode) {

		if(camera == null) {
			
			return;
			
		}
		
		if(flashMode == null) {
			
			flashMode = getAvailableFlashMode();
			
		}
		
		if(!flashMode.equals("")) {

			cameraParameters = camera.getParameters();
			cameraParameters.setFlashMode(flashMode);
			camera.setParameters(cameraParameters);
			
		}
		
	}
	
	private String getAvailableFlashMode() {
		
		String returnValue = "";
		String[] flashModes = {
				Parameters.FLASH_MODE_AUTO, 
				Parameters.FLASH_MODE_ON, 
				Parameters.FLASH_MODE_TORCH
		};
		List<String> supportedFlashModes = cameraParameters.getSupportedFlashModes();
		
		for(String flashMode : flashModes) {
			
			if(supportedFlashModes.indexOf(flashMode) != -1) {
				
				returnValue = flashMode;
				break;
				
			}
			
		}
		
		return returnValue;
		
	}
	
	@SuppressWarnings("deprecation")
	public void initialize() {
		
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		
		for(int i = 0; i < cameraCount; i++) {
			
			Camera.getCameraInfo(i, cameraInfo);
			
			if(cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				
				backCameraId = i;
				
			} else if(cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
				
				frontCameraId = i;
				
			}
			
			if(i == 0) {
				
				currentCameraId = i;
				
			}
			
		}
		
		holder = surfaceView.getHolder();
		
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			
		}
		
		holder.addCallback(surfaceHolderCallback);
				
	}
	
	private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			
			release();
			
		}
 
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			
			try {
				
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					
					if(openCameraId != -1) {
						
						currentCameraId = openCameraId;
						openCameraId = -1;
						
					}
					
					camera = Camera.open(currentCameraId);
					
				} else {
					
					camera = Camera.open();
					
				}

				camera.setPreviewDisplay(holder);
				camera.setPreviewCallback(cameraPreviewCallback);
				setCameraParameters();
				
			} catch (Exception e) {
					
				release();
				((Activity) context).finish();
			
			}
			
		}
 
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format , int width, int height) {
			
			camera.stopPreview();
			int rotation = getRotation();
			int previewOrientation = previewOrientation(currentCameraId);
			
			camera.setDisplayOrientation(previewOrientation);
			Camera.Size pictureSize = getPictureSize();
			
			Point displaySize = DisplaySize.get(context);
			int pictureWidth, pictureHeight, layoutWidth;
			
			if(rotation == 90 || rotation == 270) {
				
				layoutWidth = displaySize.x;
				pictureWidth = pictureSize.width;
				pictureHeight = pictureSize.height;
				
			} else {

				layoutWidth = displaySize.y;
				pictureWidth = pictureSize.height;
				pictureHeight = pictureSize.width;
				
			}
			
			float ratio = (float) pictureHeight/pictureWidth;
			ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
			int layoutHeight = (int) (layoutWidth * ratio);
			layoutParams.width = layoutWidth;
			layoutParams.height = layoutHeight;
			surfaceView.setLayoutParams(layoutParams);
			setCameraParameters();
			camera.setPreviewCallback(cameraPreviewCallback);
			camera.startPreview();
			
		}
		
	};
	
	private Camera.PreviewCallback cameraPreviewCallback = new Camera.PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			
			if(previewCallback != null) {
				
				previewCallback.result(data, camera);
				
			}

			if(autoFocusCallback != null) {
				
				previewData = data;
				
			}
			
		}

	};
	
	private int getCameraOrientation(int targetId) {
		
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(targetId, info);
		return info.orientation;
		
	}
	
	private Camera.Size getPictureSize() {
		
		cameraParameters = camera.getParameters();
		List<Size> sizes = cameraParameters.getSupportedPictureSizes();
		
		if(sizes != null && sizes.size() > 0) {
			
			for(Size size : sizes){
				
				if(Math.max(size.width, size.height) <= maxPictureWidth) {
					
					return size;
					
				}
				
			}
			
		}
		
		return cameraParameters.getPictureSize();
		
	}
	
	private int previewOrientation(int targetId) {
		
		int rotation = getRotation();
		int cameraOrientation = getCameraOrientation(targetId);
		int previewOrientation;
		if(targetId == frontCameraId) {

			previewOrientation = (360 - (rotation + cameraOrientation) % 360) % 360;
			
		} else {

			previewOrientation = (360 + (cameraOrientation - rotation)) % 360;
			
		}
		
		return previewOrientation;
		
	}
	
	private int getRotation() {
		
		int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		
		switch(rotation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 90; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 270; break;
		}
		
		return degrees;
		
	}
	
	private void setCameraParameters() {
		
		if(parameterMap.size() > 0) {
			
			cameraParameters = camera.getParameters();
			
			for(Map.Entry<String, String> entry : parameterMap.entrySet()) {
				
				String parameterKey = entry.getKey();
				String parameterValue = entry.getValue();
				
				if(parameterKey.equals("Antibanding")) {
					
					List<String> supportedAnitibandingList = cameraParameters.getSupportedAntibanding();
					
					if(supportedAnitibandingList != null && supportedAnitibandingList.contains(parameterValue)) {
						
						cameraParameters.setAntibanding(parameterValue);
						
					}
					
				} else if(parameterKey.equals("SceneMode")) {

					List<String> supportedSceneModeList = cameraParameters.getSupportedSceneModes();
					
					if(supportedSceneModeList != null && supportedSceneModeList.contains(parameterValue)) {

						cameraParameters.setSceneMode(parameterValue);
						
					}
					
				} else if(parameterKey.equals("WhiteBalance")) {

					List<String> supportedWhiteBalanceList = cameraParameters.getSupportedWhiteBalance();
					
					if(supportedWhiteBalanceList != null && supportedWhiteBalanceList.contains(parameterValue)) {
						
						cameraParameters.setWhiteBalance(parameterValue);
						
					}
					
				} else if(parameterKey.equals("ColorEffect")) {

					List<String> supportedColorEffectsList = cameraParameters.getSupportedColorEffects();
					
					if(supportedColorEffectsList != null && supportedColorEffectsList.contains(parameterValue)) {
						
						cameraParameters.setColorEffect(parameterValue);
						
					}
					
				} else if(parameterKey.equals("ExposureCompensation")) {

					int exposureValue = Integer.parseInt(parameterValue);
					int minExposureValue = cameraParameters.getMinExposureCompensation();
					int maxExposureValue = cameraParameters.getMaxExposureCompensation();
					
					if(minExposureValue <= exposureValue && exposureValue <= maxExposureValue) {
						
						cameraParameters.setExposureCompensation(exposureValue);
						
					}
					
				}
			    
			}
			
			camera.setParameters(cameraParameters);
			
		}
		
	}
	
	private void release() {
		
		if(camera != null) {
			
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
			
		}
		
	}
	
	// Callback class
	
	public static class CameraManagerTakePictureCallback {
		
		public void result(byte[] data, Camera camera) {}
		
	}
	
	public static class CameraManagerAutoFocusCallback {
		
		public void result(byte[] data, Camera camera) {}
		
	}
	
	public static class CameraManagerPreviewCallback {
		
		public void result(byte[] data, Camera camera) {}
		
	}
	
}
/*** Example

	Map<String, String> parameterMap = new HashMap<String, String>();
	parameterMap.put("Antibanding", Parameters.ANTIBANDING_AUTO);
	parameterMap.put("SceneMode", Parameters.SCENE_MODE_AUTO);
	parameterMap.put("WhiteBalance", Parameters.WHITE_BALANCE_AUTO);
	parameterMap.put("ColorEffect", Parameters.EFFECT_NONE);

	SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
	final CameraManager cameraManager = new CameraManager(this, surfaceView);
	cameraManager.setTakePictureCallback(new CameraManagerTakePictureCallback(){
		
		@Override
		public void result(byte[] data, Camera camera) {

			camera.stopPreview();
			// do something..
			camera.startPreview();
				
		}
		
	});
	cameraManager.setAutoFocusCallback(new CameraManagerAutoFocusCallback(){
		
		@Override
		public void result(byte[] data, Camera camera) {

			camera.stopPreview();
			// do something..
			camera.startPreview();
			
		}
		
	});
	cameraManager.setPreviewCallback(new CameraManagerPreviewCallback(){
		
		@Override
		public void result(byte[] data, Camera camera) {
			
			camera.stopPreview();
			// do something..
			camera.startPreview();
			
		}
		
	});
	cameraManager.setParameters(parameterMap);	// Skippable
	cameraManager.initialize();
	
	// Take Picture
	
	Button buttonTakePicture = (Button) findViewById(R.id.button_takepicture);
	buttonTakePicture.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {

			cameraManager.takePicture();
			
		}
		
	});
	
	// Flash Mode
	
	Button buttonFlash = (Button) findViewById(R.id.button_flash);
	buttonFlash.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {

			if(cameraManager.hasFlash()) {
					
				if(cameraManager.isFlashSupported(Parameters.FLASH_MODE_TORCH)) {
					
					cameraManager.setFlashMode(Parameters.FLASH_MODE_TORCH);	// If null, detected automatically.
					
				}
				
			}
			
		}
		
	});
	
	// Auto Focus
	
	Button buttonAutoFocus = (Button) findViewById(R.id.button_autofocus);
	buttonAutoFocus.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {

			if(cameraManager.hasAutoFocus()) {
				
				cameraManager.autoFocus();	// Note: You should not use camera.stopPreview() before here.
				
			}
			
		}
		
	});
	
	// Switch camera
	
	Button buttonSwitch = (Button) findViewById(R.id.button_switch);
	buttonSwitch.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {

			if(cameraManager.getCameraCount() > 1) {
				
				cameraManager.changeCamera();
				
			}
			
		}
		
	});

***/
