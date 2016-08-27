package com.alex.captureandrecordscreen;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements OnClickListener, ServiceConnection {

    public MyApplication myApplication = new MyApplication();
    public final static String TAG = "MainActivity";
    private Button btn_capture = null;
    private Button btn_record = null;
    private MediaProjectionManager mediaProjectionManager = null;
    private static final int REQUEST_CODE = 1;
    private int resultCode = 0;
    private Intent resultIntent = null;

    private View touchView = null;

    private CaptureService captureService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_capture = (Button) findViewById(R.id.btn_CaptureScreen);
        btn_record = (Button) findViewById(R.id.btn_RecordScreen);

        btn_capture.setOnClickListener(this);
        btn_record.setOnClickListener(this);

        mediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public void onClick(View v) {
        //First---you need to get MediaProjectionManager result code and intent.
        startIntent();
        touchView = v;
    }

    private void startIntent() {
        if ((resultCode == 0) || (resultIntent == null))
        {
            //has not got MediaProjectionManager result code and intent.
            Intent intent = mediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(intent, REQUEST_CODE);
        }
        if (captureService!=null){
            moveTaskToBack(true);
            captureService.start(myApplication);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                this.resultCode = resultCode;
                this.resultIntent = data;
                //save current mediaProjectionManager and result code & intent
                myApplication.setmediaProjectionManager(mediaProjectionManager);
                myApplication.setResultCode(this.resultCode);
                myApplication.setResultIntent(this.resultIntent);
                moveTaskToBack(true);
                processBtn();
            }
        }
    }

    private void processBtn() {
        switch (touchView.getId()) {
            case R.id.btn_RecordScreen:
                //start record screen service
                startService(new Intent(MainActivity.this, RecordService.class));
                break;
            case R.id.btn_CaptureScreen:
                //start capture screen service
                if (captureService == null) {
                    bindService(new Intent(MainActivity.this, CaptureService.class), this, BIND_AUTO_CREATE);
                }
                break;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        captureService = ((CaptureService.CaptureServiceBinder) service).getService();
        captureService.start(myApplication);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
