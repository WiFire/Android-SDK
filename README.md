# Android-SDK
WiFire SDK for Android devices

## Integration with your existing project in Android Studio

1. Download or clone this repo on your system.
2. Copy the [WiFireSDK.aar](https://github.com/WiFire/Android-SDK/blob/master/WiFireSDKSample/app/libs/WifireSDK.aar) file into the `libs` directory of your app. Refer the included sample app for example.
3. In the `build.gradle` file of your project, add the following in the repositories section

```groovy
flatDir {
    dirs 'libs'
}
```

4. In the `build.gradle` file of the app, add the following in the dependencies section:

```groovy
compile(name: 'WiFireSDK', ext: 'aar')
compile 'com.google.firebase:firebase-database:9.4.0'
compile 'com.google.firebase:firebase-auth:9.4.0'
compile 'com.google.firebase:firebase-storage:9.4.0'
compile 'com.google.android.gms:play-services-base:9.4.0'
compile 'com.google.android.gms:play-services-location:9.4.0'
compile 'com.firebase:geofire-android:2.0.0'
```

5. Perform a gradle sync ( ⌘+F9 or Ctrl + F9)


## Usage

#### 1. Initialise the SDK with your API key (preferably in the Application class)

##### Note: The API key is tied to the package name of your app and will not work with another app.

```java
WiFire.initialise(getApplicationContext(), "MY_API_KEY", new AuthListener() {
    @Override
    public void onSuccess() {
        Log.d("WiFire", "Authentication successful");
    }

    @Override
    public void onFailure(WiFireException e) {
        Log.d("WiFire", "Authentication failed");
    }
});
```

#### 2. Get WiFire instance

```java
WiFire wiFire = WiFire.getInstance();
```

#### 3. Syncing for Hotspots

```java
//Starting sync
@Override
public void onResume() {
    super.onResume();
    if (wiFire != null) 
    	wiFire.enableSync();
}

//Stopping sync
@Override
public void onPause() {
    super.onPause();
    if (wiFire != null) 
    	wiFire.disableSync();
}
```

Since syncing can be resource intensive onResume() and onPause() would be ideal places to enable and disable sync respectively.
While sync is enabled WiFire will both perform WiFire scanning and sync WiFire hotspots in the vicinity when the user's location changes


#### 4. Getting list of WiFire hotspots

```java
wiFire.setHotSpotListener(new HotSpotListener() {
    @Override
    public void onScanComplete(ArrayList<WiFireHotspot> wiFireHotSpots) {
        //Populate your listView or recyclerView here
    }
});
```


WiFire will perform WiFi scans and will return a list of all the hotspots that are in range and can connect to.
A list will be delivered every ~10 seconds.

#### 5. Connecting to a network

```java
wiFire.connectToNetwork(hotspot, new ConnectionListener() {
        
    @Override
    public void onSuccess() {
    	Log.d("WiFire", "Connected");
    }

    @Override
    public void onFailure(WiFireException wiFireException) {
      	Log.d("WiFire", wiFireException.getMessage());
    }
});
```

#### 6. Listening to WiFi network updates

To listen to WiFi connection/state changes you can create a `BroadcastReceiver` which extends `com.mobstac.wifire.receivers.WiFiStateReceiver`

```java
public class WiFireStateReceiver extends WiFiStateReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onWiFiStateChange(WiFiState wiFiState) {
        Log.d("WiFireStateChanged", wiFiState.name());
    }

    @Override
    public void onCaptivePortalConnected(WiFiState wiFiState) {
        //Show a notification for network login here
    }
}
```

Set-up the `intent filter` for it like this

```xml
<receiver
    android:name=".my.package.WiFireStateReceiver"
    android:enabled="true">
    <intent-filter>
        <action android:name="com.mobstac.wifire.WIFI_STATE_CHANGE" />
    </intent-filter>
</receiver>
```

#### 7. Starting automatic network login

```java
wiFire.startAutomaticLogin(this, new AutomaticLoginStartListener() {
    @Override
    public void onStart() {
        Log.d("WiFire", "Started captive login");
    }

    @Override
    public void onError(WiFireException e) {
        Log.d("WiFire", e.getMessage());
    }
});
```

This will launch an activity which will take care of the login flow.
To check if the activity successfully completed login, use `onActivityResult()`

```java
@Override
public void onActivityResult(int requestCode, int responseCode, Intent data) {
    super.onActivityResult(requestCode, responseCode, data);
    if (requestCode == WiFire.CAPTIVE_LOGIN_REQUEST) {
        if (responseCode == WiFire.CAPTIVE_LOGIN_SUCCESS) {
            Log.d("WiFire", "Login successful");
        } else {
            Log.d("WiFire", "Login failed");
        }
    }
}
```

#### 8. Get current WiFi's state 

```java
wiFire.getCurrentState(new NetworkStateListener() {
    @Override
    public void onResult(WiFiState wiFiState) {
        switch (wiFiState) {
            case NO_WIFI:
                //Not connected to WiFi;
                break;
            case WIFI_CAPTIVE_PORTAL:
                //Login needed to access network;
                break;
            case WIFI_INTERNET:
                //On the internet
                break;
            default:
                //No internet access
                break;
        }
    }
});
```

#### 9. Get the SSID of the current network

```java
String networkName = wiFire.getCurrentNetworkName();
```

`null` if not connected to WiFi


#### 10. Check if the current network is a WiFire verified public network

```java
boolean verified = wiFire.isWiFireNetwork();
```

