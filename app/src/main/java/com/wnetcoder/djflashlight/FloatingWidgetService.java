package com.wnetcoder.djflashlight;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.Calendar;

public class FloatingWidgetService extends Service {


    private WindowManager mWindowManager;
    private View mOverlayView;
    ImageView floatButton;
    boolean state = false;
    private CameraManager mCameraManager;
    private String mCameraId;
    BroadcastReceiver mReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenUnlockReceiver();
        registerReceiver(mReceiver, filter);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        state = intent.getBooleanExtra("flashState", false);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        setTheme(R.style.AppTheme);

        mOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);

        //getting the camera manager and camera id
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;


        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mWindowManager != null) {
            mWindowManager.addView(mOverlayView, params);
        }


        floatButton = (ImageView) mOverlayView.findViewById(R.id.float_button);

        if (state) {
            floatButton.setImageResource(R.drawable.ic_brightness_high_black_24dp);
        } else {
            floatButton.setImageResource(R.drawable.ic_brightness_low_black_24dp);
        }

        floatButton.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private static final int CLICK_DURATION = 200;
            private long startClickTime;
            final Handler handler = new Handler();
            Runnable mLongPressed = new Runnable() {
                public void run() {
                    Log.i("Long press", "Long press!!!!!!!!!!!!!!");
                    Intent dialogIntent = new Intent(getApplicationContext(), MainActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.postDelayed(mLongPressed, 1000);
                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;


                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        startClickTime = Calendar.getInstance().getTimeInMillis();

                        return true;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(mLongPressed);
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if (clickDuration < CLICK_DURATION) {
                            //click event has occurred
                            //Add code for launching application and positioning the widget to nearest edge.
                            switchFlashLight();
                        }

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - initialTouchX) > 5 || Math.abs(event.getRawY() - initialTouchY) > 5) {
                            handler.removeCallbacks(mLongPressed);
                        }
                        float Xdiff = Math.round(event.getRawX() - initialTouchX);
                        float Ydiff = Math.round(event.getRawY() - initialTouchY);


                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) Xdiff;
                        params.y = initialY + (int) Ydiff;

                        //Update the layout with new X & Y coordinates
                        mWindowManager.updateViewLayout(mOverlayView, params);


                        return true;
                }
                return false;
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    public void switchFlashLight() {
        try {
            if (!state) {
                floatButton.setImageResource(R.drawable.ic_brightness_high_black_24dp);
                state = !state;
            } else {
                floatButton.setImageResource(R.drawable.ic_brightness_low_black_24dp);
                state = !state;
            }
            mCameraManager.setTorchMode(mCameraId, state);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOverlayView != null)
            mWindowManager.removeView(mOverlayView);
        unregisterReceiver(mReceiver);
    }

}
