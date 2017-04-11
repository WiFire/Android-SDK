package com.mobstac.wifire.sdksample.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mobstac.wifire.WiFireHotspot;
import com.mobstac.wifire.WiFireReceiver;
import com.mobstac.wifire.enums.WiFiState;
import com.mobstac.wifire.sdksample.MainActivity;
import com.mobstac.wifire.sdksample.R;

import java.util.ArrayList;

public class MyWiFireReceiver extends WiFireReceiver {

    public static final String BROADCAST_CAPTIVE_NETWORK = "connected_to_captive";

    public MyWiFireReceiver() {
        //Default constructor
    }

    final int wifiNotificationId = 1234;
    final int captiveNotificationId = 1235;

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        super.onReceive(context, intent);
    }

    @Override
    public void onWiFiStateChange(WiFiState wiFiState) {
        Log.d("MyWiFireReceiver", wiFiState.name());
        //Not connected to a captive portal anymore, clear login notification
        if (mContext != null && wiFiState != WiFiState.WIFI_CAPTIVE_PORTAL) {
            cancelNotification(mContext, captiveNotificationId);
        }
    }

    @Override
    public void onCaptivePortalConnected() {
        if (mContext != null) {
            showNotification(mContext, mContext.getString(R.string.app_name), "Login to this network", captiveNotificationId);
            mContext.sendBroadcast(new Intent(BROADCAST_CAPTIVE_NETWORK));
        }
    }

    @Override
    public void onWiFiNetworkInRange(ArrayList<WiFireHotspot> wiFireHotspots) {
        Log.d("MyWiFireReceiver", "Networks in range");
        if (mContext != null) {
            if (wiFireHotspots.size() > 0) {
                //WiFi in range, show notification
                String title = mContext.getResources().getQuantityString(R.plurals.networks_in_range,
                        wiFireHotspots.size(), wiFireHotspots.size());
                StringBuilder subtitle = new StringBuilder();
                for (WiFireHotspot wiFireHotspot : wiFireHotspots) {
                    if (subtitle.toString().length() > 1) {
                        subtitle.append(", ");
                    }
                    subtitle.append(wiFireHotspot.getSsid());
                }
                showNotification(mContext, title, subtitle.toString(), wifiNotificationId);
            } else {
                //WiFi went out of range, clear notification
                cancelNotification(mContext, wifiNotificationId);
            }
        }
    }

    /**
     * Display a notification to the user
     *
     * @param context        Application context
     * @param notificationId A unique id which can be used to clear the notification
     */
    private void showNotification(Context context, String title, String subTitle, int notificationId) {

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
                        .setContentTitle(title)
                        .setContentText(subTitle);

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(notificationId, mBuilder.build());

    }

    /**
     * Cancel the previously displayed login notification, ideal when the device
     * gets disconnected from the WiFi or login is complete
     *
     * @param context        Application context
     * @param notificationId The unique id used to create the notification
     */
    private void cancelNotification(Context context, int notificationId) {
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(notificationId);
    }


}