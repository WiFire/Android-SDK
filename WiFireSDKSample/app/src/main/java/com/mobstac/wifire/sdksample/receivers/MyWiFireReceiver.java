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
    public static final String TAG = "MyWiFireReceiver";

    public MyWiFireReceiver() {
        //Default constructor
    }

    final int wifiNotificationId = 1234;
    final int captiveNotificationId = 1235;

    public static final int REQUEST_WIFI_AVAILABLE = 34;
    public static final int REQUEST_CAPTIVE_LOGIN = 35;

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        super.onReceive(context, intent);
    }

    @Override
    public void onWiFiStateChange(WiFiState wiFiState, WiFireHotspot hotspot) {
        Log.d(TAG, wiFiState.name());
        //Not connected to a captive portal anymore, clear login notification
        if (mContext != null)
            switch (wiFiState) {
                case WIFI_CAPTIVE_PORTAL:
                    showNotification(mContext, mContext.getString(R.string.app_name), "Login to this network", captiveNotificationId,
                            REQUEST_CAPTIVE_LOGIN);
                    mContext.sendBroadcast(new Intent(BROADCAST_CAPTIVE_NETWORK));
                    break;
                default:
                    cancelNotification(mContext, captiveNotificationId);
                    break;
            }
    }

    @Override
    public void onWiFiNetworkInRange(ArrayList<WiFireHotspot> wiFireHotspots) {
        Log.d(TAG, "Networks in range");
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
                showNotification(mContext, title, subtitle.toString(), wifiNotificationId,
                        REQUEST_WIFI_AVAILABLE);
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
    private void showNotification(Context context, String title, String subTitle,
                                  int notificationId, int requestCode) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("requestCode", requestCode);
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