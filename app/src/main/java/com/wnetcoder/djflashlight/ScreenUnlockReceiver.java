package com.wnetcoder.djflashlight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenUnlockReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(Intent.ACTION_SCREEN_ON.equals(action)) {
            // start the service
//            Intent intentReceiver = new Intent(context,FloatingWidgetService.class);
//            intent.putExtra("flashState",false);
//            context.startService(intentReceiver);
        } else if(Intent.ACTION_SCREEN_OFF.equals(action)) {
            // stop the service
//            context.stopService(new Intent(context, FloatingWidgetService.class));
        }
    }
}
