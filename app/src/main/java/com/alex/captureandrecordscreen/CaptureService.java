package com.alex.captureandrecordscreen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CaptureService extends Service {

    public MyApplication myApplication;
    private String imagePath;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private WindowManager windowManager;
    private int windowWidth, windowHeight, screenDensity;
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;

    public CaptureService() {
    }

    public class CaptureServiceBinder extends Binder {
        public CaptureService getService() {
            return CaptureService.this;
        }
    }

    private CaptureServiceBinder captureServiceBinder = new CaptureServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return captureServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    private void createEnvironment() {
        imagePath = Environment.getExternalStorageDirectory().getPath();//+ "/CaptureAndRecordScreen/Capture/";
        File dirFile = new File(imagePath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        mediaProjectionManager = myApplication.getmediaProjectionManager();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowWidth = windowManager.getDefaultDisplay().getWidth();
        windowHeight = windowManager.getDefaultDisplay().getHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenDensity = displayMetrics.densityDpi;
        imageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaProjection != null)
            mediaProjection.stop();
        mediaProjection = null;
    }

    private void startVirtual() {
        //open virtual screen
        if (mediaProjection != null) {

        } else {
            int resultCode = myApplication.getResultCode();
            Intent resultIntent = myApplication.getResultIntent();
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultIntent);
        }
        virtualDisplay = mediaProjection.createVirtualDisplay(MainActivity.TAG, windowWidth, windowHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, imageReader.getSurface(), null, null);

    }

    private void stopVirtual() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        stopSelf();
    }

    private void startCapture() {
        String mImageName;
        mImageName = System.currentTimeMillis() + ".png";
        Log.e(MainActivity.TAG, "image name is : " + mImageName);
        Image image = imageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();

        if (bitmap != null) {
            Log.e(MainActivity.TAG, "bitmap  create success ");
            try {
                File fileFolder = new File(imagePath);
                if (!fileFolder.exists())
                    fileFolder.mkdirs();
                File file = new File(imagePath, mImageName);
                if (!file.exists()) {
                    Log.e(MainActivity.TAG, "file create success ");
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                Log.e(MainActivity.TAG, "file save success ");
                Toast.makeText(this.getApplicationContext(), "截图成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    public void start(MyApplication myApplication) {
        this.myApplication = myApplication;
        //create envriroment
        createEnvironment();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(MainActivity.TAG, "start startVirtual");
                startVirtual();
            }
        }, 500);
        // Handler handler1 = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(MainActivity.TAG, "start startCapture");
                startCapture();
            }
        }, 1000);
        // Handler handler2 = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(MainActivity.TAG, "start stopVirtual");
                stopVirtual();
            }
        }, 1500);
    }
}
