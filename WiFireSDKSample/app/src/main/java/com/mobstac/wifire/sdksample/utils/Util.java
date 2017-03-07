package com.mobstac.wifire.sdksample.utils;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.mobstac.wifire.sdksample.R;

/**
 * Created by Kislay on 09/08/16.
 */
public class Util {

    /**
     * Display a snackBar, threadsafe, can be called from a background thread
     *
     * @param message  Message to display in the toast
     * @param activity Calling activity's reference
     */
    public static void snackBar(final String message, final Activity activity,
                                final String textColorCode) {

        if (activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Snackbar snackbar = Snackbar
                            .make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.RED);

                    if (textColorCode != null) {
                        View snackBarView = snackbar.getView();
                        TextView textView = (TextView)
                                snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        textView.setTextColor(Color.parseColor(textColorCode));
                    }

                    snackbar.show();

                }
            });
    }

    public static int getWifireSignalIcon(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return R.drawable.ic_wifire_icon_lvl_01;
            case 1:
                return R.drawable.ic_wifire_icon_lvl_02;
            case 2:
                return R.drawable.ic_wifire_icon_lvl_03;
            case 3:
                return R.drawable.ic_wifire_icon_lvl_04;
            default:
                return R.drawable.ic_wifire_icon_lvl_03;
        }
    }

}