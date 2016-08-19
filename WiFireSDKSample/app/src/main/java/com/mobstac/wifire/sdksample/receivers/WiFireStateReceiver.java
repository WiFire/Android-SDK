package com.mobstac.wifire.sdksample.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mobstac.wifire.enums.WiFiState;
import com.mobstac.wifire.receivers.WiFiStateReceiver;
import com.mobstac.wifire.sdksample.MainActivity;
import com.mobstac.wifire.sdksample.R;

public class WiFireStateReceiver extends WiFiStateReceiver {

    public WiFireStateReceiver() {
        //Default constructor
    }

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        super.onReceive(context, intent);
    }

    @Override
    public void onWiFiStateChange(WiFiState wiFiState) {
        Log.d("WiFireStateReceiver", wiFiState.name());
    }

    @Override
    public void onCaptivePortalConnected(WiFiState wiFiState) {
        showNotification(mContext);
    }

    private void showNotification(Context context) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("wifireNotificationClicked", true);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText("Login to this network");

        int mNotificationId = 1234;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }


}