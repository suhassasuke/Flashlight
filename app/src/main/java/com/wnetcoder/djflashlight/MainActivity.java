package com.wnetcoder.djflashlight;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.turn_off_on_light)
    ImageView TurnOffOnLight;

    private CameraManager mCameraManager;
    private String mCameraId;

    boolean state = false;

    private AdView mAdView;

    final int REQUEST_PERMISSION = 0;

    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;

    String[] permission = {
//            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {
            showNoFlashError();
        }

        //getting the camera manager and camera id
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
            mCameraManager.setTorchMode(mCameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        MobileAds.initialize(this, "ca-app-pub-1167522187201829~3125610921");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.e(TAG, "onAdLoaded: ");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e(TAG, "onAdFailedToLoad: " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.e(TAG, "onAdOpened: ");
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.e(TAG, "onAdClicked: ");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.e(TAG, "onAdLeftApplication: ");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Log.e(TAG, "onAdClosed: ");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_PERMISSION) {
                Log.d("permission", "granted $$$$$$$$$$$$$$$$$$");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
        stopService(new Intent(MainActivity.this, FloatingWidgetService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // To prevent starting the service if the required permission is NOT granted.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                //Permission is not available. Display error text.
                errorToast();
                finish();
            }
        }
    }

    private void errorToast() {
        Toast.makeText(this, "Draw over other app permission not available. Can't start the application without the permission.", Toast.LENGTH_LONG).show();
    }

    private void askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available to open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    @OnClick(R.id.turn_off_on_light)
    void onClickTurnOffOnLight() {
        switchFlashLight();
    }

    @OnClick(R.id.float_button)
    void onClickFloatButton() {
        //If the draw over permission is not available to open the settings screen
        //to grant the permission.
        if(Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(MainActivity.this,FloatingWidgetService.class);
            intent.putExtra("flashState",state);
            startService(intent);
//            startService(new Intent(MainActivity.this, FloatingWidgetService.class));
            finish();
        }else{
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    public void showNoFlashError() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .create();
        alert.setTitle("Oops!");
        alert.setMessage("Flash not available in this device...");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, permission[0])
                == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, permission[1])
                == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, permission[2])
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, permission, REQUEST_PERMISSION);
        }
    }

    public void switchFlashLight() {
        try {
            if (!state) {
                TurnOffOnLight.setImageResource(R.drawable.ic_brightness_high_black_24dp);
                state = !state;
            } else {
                TurnOffOnLight.setImageResource(R.drawable.ic_brightness_low_black_24dp);
                state = !state;
            }
            mCameraManager.setTorchMode(mCameraId, state);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
