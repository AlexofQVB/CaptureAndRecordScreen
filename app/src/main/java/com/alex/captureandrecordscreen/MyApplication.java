package com.alex.captureandrecordscreen;

import android.app.Application;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

/**
 * Created by 建伟 on 2016/8/26.
 */
public class MyApplication extends Application {
    private Intent resultIntent = null;
    private int resultCode = 0;
    private MediaProjectionManager mediaProjectionManager;

    public Intent getResultIntent() {
        return this.resultIntent;
    }

    public void setResultIntent(Intent resultIntent) {
        this.resultIntent = resultIntent;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
    

    public MediaProjectionManager getmediaProjectionManager() {
        return mediaProjectionManager;
    }

    public void setmediaProjectionManager(MediaProjectionManager mediaProjectionManager) {
        this.mediaProjectionManager = mediaProjectionManager;
    }


    @Override
    public void onCreate() {
        super.onCreate();

    }
}
