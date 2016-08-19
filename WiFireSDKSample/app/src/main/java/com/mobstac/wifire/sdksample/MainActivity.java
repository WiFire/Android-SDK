package com.mobstac.wifire.sdksample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mobstac.wifire.WiFire;
import com.mobstac.wifire.core.WiFireException;
import com.mobstac.wifire.enums.WiFiState;
import com.mobstac.wifire.interfaces.AuthListener;
import com.mobstac.wifire.interfaces.AutomaticLoginStartListener;
import com.mobstac.wifire.interfaces.ConnectionListener;
import com.mobstac.wifire.interfaces.HotSpotListener;
import com.mobstac.wifire.interfaces.NetworkStateListener;
import com.mobstac.wifire.models.WiFireHotspot;
import com.mobstac.wifire.sdksample.adapters.WiFireAdapter;
import com.mobstac.wifire.sdksample.dialogFragments.UserDetailsDialog;
import com.mobstac.wifire.sdksample.utils.Util;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "WiFireSample";

    private static final String API_KEY = "6CZcDjYYNVd3iCE8ulFYwWO3rhy1";

    WiFire wiFire;
    Context mContext;
    RecyclerView wiFireList;
    RecyclerView.LayoutManager layoutManager;
    WiFireAdapter mAdapter;
    TextView errorText;

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

        final ProgressDialog pd = new ProgressDialog(mContext);
        pd.setMessage("Initiating automatic login");
        pd.setCancelable(false);
        pd.show();

        wiFire.startAutomaticLogin(this, new AutomaticLoginStartListener() {
            @Override
            public void onStart() {
                Log.d(TAG, "Started captive login");
                pd.dismiss();
            }

            @Override
            public void onError(WiFireException e) {
                pd.dismiss();
                String error = "Captive login failed. ";
                switch (e.getMessage()) {

                    case WiFire.CAPTIVE_LOGIN_ERROR_HAS_INTERNET_ACCESS:
                        error = error + "You already have internet access";
                        break;

                    case WiFire.CAPTIVE_LOGIN_ERROR_NO_RESPONSE:
                        error = error + "Network not responding. Please try later";
                        break;

                    case WiFire.CAPTIVE_LOGIN_ERROR_NO_WIFI:
                        error = error + "WiFi disconnected. Try connecting again";
                        break;

                    case WiFire.CAPTIVE_LOGIN_ERROR_USER_NOT_SET:
                        askUserDetails();
                        break;

                    default:
                        error = error + e.getMessage();
                        break;
                }
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
    }

    @Override
    public void onResume() {
        super.onResume();
        startSyncing();
    }

    private void startSyncing() {
        if (wiFire != null) {
            try {
                wiFire.enableSync();
            } catch (WiFireException e) {
                Util.snackBar(e.getMessage(), this, null);
                if (errorText != null)
                    errorText.setText(e.getMessage());
            }
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

                switch (wiFireException.getMessage()) {

                    case WiFire.ERROR_ASSOCIATION_REJECT:
                        //Rejected by the system
                        error = "Rejected by the system";
                        break;

                    case WiFire.ERROR_CONNECTION_FAILED:
                        //Connection failed
                        error = "Connection failed";
                        break;

                    case WiFire.ERROR_DHCP_FAILED:
                        //DHCP failed
                        error = "DHCP failed";
                        break;

                    case WiFire.ERROR_DNS_FAILED:
                        //DNS failed
                        error = "DNS failed";
                        break;

                    case WiFire.ERROR_INCORRECT_PASSWORD:
                        //Password failed
                        error = "Wrong password";
                        break;

                    case WiFire.ERROR_WEAK_SIGNAL:
                        //Signal weak
                        error = "Signal too weak to connect";
                        break;

                    case WiFire.ERROR_DISABLED_BY_SYSTEM:
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

}