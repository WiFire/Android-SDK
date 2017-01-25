package com.mobstac.wifire.sdksample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mobstac.wifire.WiFire;
import com.mobstac.wifire.WiFireHotspot;
import com.mobstac.wifire.core.ErrorCodes;
import com.mobstac.wifire.core.WiFireException;
import com.mobstac.wifire.enums.WiFiState;
import com.mobstac.wifire.interfaces.AuthListener;
import com.mobstac.wifire.interfaces.ConnectionListener;
import com.mobstac.wifire.interfaces.HotSpotListener;
import com.mobstac.wifire.interfaces.NetworkStateListener;
import com.mobstac.wifire.interfaces.WiFireErrorListener;
import com.mobstac.wifire.sdksample.adapters.WiFireAdapter;
import com.mobstac.wifire.sdksample.dialogFragments.UserDetailsDialog;
import com.mobstac.wifire.sdksample.receivers.WiFireStateReceiver;
import com.mobstac.wifire.sdksample.utils.Util;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "WiFireSample";

    private static final String API_KEY = "YOUR_API_KEY";

    WiFire wiFire;
    Context mContext;
    RecyclerView wiFireList;
    RecyclerView.LayoutManager layoutManager;
    WiFireAdapter mAdapter;
    TextView errorText;

    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        mContext = this;

        layoutManager = new LinearLayoutManager(mContext);
        wiFireList = (RecyclerView) findViewById(R.id.wifire_list);
        wiFireList.setLayoutManager(layoutManager);
        errorText = (TextView) findViewById(R.id.error_text);

        WiFire.initialise(getApplicationContext(), API_KEY, new AuthListener() {
            @Override
            public void onSuccess() {
                startSyncing();
            }

            @Override
            public void onFailure(WiFireException e) {
                Util.snackBar(e.getMessage(), MainActivity.this, null);
                errorText.setText(e.getMessage());
            }
        });

        wiFire = WiFire.getInstance();

        wiFire.setHotSpotListener(new HotSpotListener() {
            @Override
            public void onScanComplete(ArrayList<WiFireHotspot> wiFireHotSpots) {
                mAdapter = new WiFireAdapter(wiFireHotSpots, mContext);
                wiFireList.setAdapter(mAdapter);
                if (wiFireHotSpots.size() > 0) {
                    errorText.setVisibility(View.GONE);
                } else {
                    errorText.setVisibility(View.VISIBLE);
                }
            }
        });

        boolean notificationClicked = getIntent().getBooleanExtra("wifireNotificationClicked", false);
        if (notificationClicked)
            startCaptiveLogin();

        snackbar = Snackbar.make(wiFireList, "This network requires a login", Snackbar.LENGTH_INDEFINITE)
                .setAction("Login", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startCaptiveLogin();
                    }
                });

    }

    private void getNetworkState() {

        wiFire.getCurrentState(new NetworkStateListener() {
            @Override
            public void onResult(WiFiState wiFiState) {
                String state = null;
                switch (wiFiState) {
                    case NO_WIFI:
                        state = "Not connected to WiFi";
                        break;
                    case WIFI_CAPTIVE_PORTAL:
                        state = "Login needed";
                        break;
                    case WIFI_INTERNET:
                        state = "On the internet. Have fun";
                        break;
                    default:
                        state = "No internet access";
                        break;
                }
                Util.snackBar(state, (Activity) mContext, null);
            }
        });
    }

    private void startCaptiveLogin() {
        wiFire.startAutomaticLogin(this, new WiFireErrorListener() {
            @Override
            public void onError(WiFireException e) {
                if (e.getErrorCode() == ErrorCodes.USER_NOT_SET)
                    askUserDetails();
                String error = e.getMessage();
                com.mobstac.wifire.sdksample.utils.Util.snackBar(error, (Activity) mContext, null);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (wiFire != null) {
            wiFire.disableSync();
        }
        unregisterReceiver(captiveReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        startSyncing();
        registerReceiver(captiveReceiver, new IntentFilter(WiFireStateReceiver.BROADCAST_CAPTIVE_NETWORK));
    }

    private void startSyncing() {
        if (wiFire != null) {

            wiFire.enableSync(new WiFireErrorListener() {
                @Override
                public void onError(WiFireException e) {
                    Util.snackBar(e.getMessage(), (Activity) mContext, null);
                    if (errorText != null)
                        errorText.setText(e.getMessage());
                }
            });
        }
    }

    public void onItemClick(final WiFireHotspot hotspot) {

        final ProgressDialog pd = new ProgressDialog(mContext);
        pd.setMessage("Connecting to " + hotspot.getName());
        pd.setCancelable(false);
        pd.show();

        wiFire.connectToNetwork(hotspot, new ConnectionListener() {
            @Override
            public void onSuccess() {
                pd.dismiss();
                Util.snackBar("Connected to " + hotspot.getName(), (Activity) mContext, null);
            }

            @Override
            public void onFailure(WiFireException wiFireException) {
                pd.dismiss();
                String error = "Connection failed";

                switch (wiFireException.getErrorCode()) {

                    case ErrorCodes.USER_NOT_SET:
                        //Must provide user's phone number to comply with TRAI regulations
                        askUserDetails();
                        break;

                    case ErrorCodes.WIFI_DISABLED_ASSOCIATION_REJECT:
                        //Rejected by the system
                        error = "Rejected by the system";
                        break;

                    case ErrorCodes.WIFI_DISABLED_UNKNOWN_REASON:
                    case ErrorCodes.UNKNOWN_ERROR:
                        //Connection failed
                        error = "Connection failed";
                        break;

                    case ErrorCodes.WIFI_DISABLED_DHCP_FAILURE:
                        //DHCP failed
                        error = "DHCP failed";
                        break;

                    case ErrorCodes.WIFI_DISABLED_DNS_FAILURE:
                        //DNS failed
                        error = "DNS failed";
                        break;

                    case ErrorCodes.WIFI_DISABLED_AUTH_FAILURE:
                        //Password failed
                        error = "Wrong password";
                        break;

                    case ErrorCodes.WIFI_DISABLED_LOW_SIGNAL:
                        //Signal weak
                        error = "Signal too weak to connect";
                        break;

                    case ErrorCodes.WIFI_DISABLED_BY_WIFI_MANAGER:
                        //Disabled by system's WiFiManager
                        error = "Network disabled by system";
                        break;
                }

                Util.snackBar(error, (Activity) mContext, null);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_check_state) {
            getNetworkState();
        } else if (item.getItemId() == R.id.action_login_captive) {
            startCaptiveLogin();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        if (requestCode == WiFire.CAPTIVE_LOGIN_REQUEST) {
            if (responseCode == WiFire.CAPTIVE_LOGIN_SUCCESS) {
                Util.snackBar("Login successful", (Activity) mContext, null);
            } else {
                Util.snackBar("Login failed", (Activity) mContext, null);
            }
        }
    }

    public void askUserDetails() {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            UserDetailsDialog userDetailsDialog = UserDetailsDialog.newInstance();
            userDetailsDialog.setRetainInstance(true);
            userDetailsDialog.show(fragmentManager, "Dialog Fragment");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean notificationClicked = intent.getBooleanExtra("wifireNotificationClicked", false);
        if (notificationClicked)
            startCaptiveLogin();
    }

    BroadcastReceiver captiveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (snackbar != null && !snackbar.isShown())
                snackbar.show();
        }
    };

}