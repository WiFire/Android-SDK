package com.mobstac.wifire.sdksample.receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mobstac.wifire.receivers.WiFireCaptiveReceiver;

public class CaptiveReceiver extends WiFireCaptiveReceiver {
    public CaptiveReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void captiveNetworkDetected() {
        Log.d("CaptiveReceiver", "Detected");
    }
}