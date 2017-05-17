# WiFire SDK for Android. Allows apps to embed WiFire functionality

You will need an API key for the WiFire SDK service to work. Please email wifire.bizdev@mobstac.com to get an API key.

## Integration with your existing project in Android Studio

#### In the `build.gradle` file of the app, add the following in the dependencies section:

```groovy
dependencies {
    ...
    compile 'com.mobstac.wifire:WiFireSDK:1.0.0'
}
```

##### Latest version

[ ![WiFireSDK](https://api.bintray.com/packages/mobstac/maven/WiFireSDK/images/download.svg) ](https://bintray.com/mobstac/maven/WiFireSDK/_latestVersion)


## Permissions
#### WiFire requires the following permissions.
```xml
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.READ_SMS" />
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
```

It is not necessary to explicitly add these permissions to your app. They will be added automatically when you include the SDK

### Optional permissions
These permissions are optional and can be removed

- READ_SMS
- RECEIVE_SMS
 
    **Usage -** `Required to read the one time password (OTP) sent by the network providers to verify users.`
    **Consequence -** `The user will have to read the SMS and manully enter the otp during captive login`

- WRITE_SETTINGS

    **Usage -** `Required to better manage saved WiFi networks in the android system starting Android 6.0.`
    **Consequence -** `WiFire won't be able to delete or modify a saved network on the user's device`

#### How to remove
Add this line to your `AndroidManifest.xml`
```xml
    <uses-permission android:name="android.permission.WRITE_SETTINGS"
        tools:node="remove" />
```

### Runtime permissions
Since Android 6.0 android has introduced the concept of runtime permissions. WiFire SDK requires two runtime permissions - 

#### Location
WiFire requires the location permission to sync nearby Hotspots. WiFire SDKs `enableSync()` method will fail if location permission is denied.

#### SMS read/receive
WiFire requires the SMS permission to read SMS messages and extract the `one time password (OTP)` for logging in to a captive network. WiFire SDKs `startAutomaticLogin()` method will fail if SMS permission is not granted


## Setup test network

### [Instructions for setting up your own WiFire test network](media/TestSetup.md)


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
    	wiFire.enableSync(new WiFireErrorListener() {
                @Override
                public void onError(WiFireException e) {
                    Log.e("WiFireSDK", e.getMessage());
                }
            });
}


//To sync within a required radius, pass an argument which is the radius in metres
wiFire.enableSync(1000, new WiFireErrorListener() {
    @Override
    public void onError(WiFireException e) {
        Log.e("WiFireSDK", e.getMessage());
    }
});


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


#### 4. Getting a list of [WiFireHotspot](media/WiFireHotspot.md) objects

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

To listen to WiFi connection/state changes you can create a `BroadcastReceiver` which extends `com.mobstac.wifire.WiFireReceiver`

This listener can work even when the app is in the background. *Location and sync must be enabled* for **onWiFiNetworkInRange** to work.

This allows for the following functionality
* Show notifications when a WiFi network is available for connection
* Recognise the business establishment the user is currently at
* Show notifications for network login, when the user connects to a captive network
* Clear older notifications when the user leaves a place
* Get updates about WiFi connectivity changes

```java
public class MyWiFireReceiver extends WiFireReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onWiFiStateChange(WiFiState wiFiState) {
        Log.d("WiFireStateChanged", wiFiState.name());
        //WiFi network's state has changed, possible states are 
        //connected, disconnected, captive portal detected.
    }

    @Override
    public void onCaptivePortalConnected(WiFiState wiFiState) {
        //The device has just connected to a captive portal.
        //Show notification for network login here.
    }

    @Override
    public void onWiFiNetworkInRange(ArrayList<WiFireHotspot> wiFireHotspots) {

        //This callback will work while WiFire sync is enabled. 

        //You can show notification for WiFi availability here. 

        //This will also have the business name so you can detect where the user is.

        //You will receiver this callback only once for a fixed set of WiFi networks.

        //When there are no networks in range, you will receive this callback once 
        //with an empty list to help clear any connectivity/business related notifications.
    }
}
```

Set-up the `intent filter` for it like this

```xml
<receiver
    android:name=".my.package.MyWiFireReceiver"
    android:enabled="true">
    <intent-filter>
        <!--For listening to changes in WiFi state-->
        <action android:name="com.mobstac.wifire.WIFI_STATE_CHANGE" />
        <!--For listening to WiFi networks in range-->
        <action android:name="com.mobstac.wifire.WIFI_IN_RANGE" />
    </intent-filter>
</receiver>
```

#### 7. Starting automatic network login

```java
wiFire.startAutomaticLogin(this, new WiFireErrorListener() {
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
        } else if (responseCode == WiFire.CAPTIVE_LOGIN_FAILED) {
            Log.d("WiFire", "Login failed");
        } else if (responseCode == WiFire.CAPTIVE_LOGIN_CANCELLED) {
            String reason = data.getStringExtra("reason");
            if (reason == null)
                reason = "Login cancelled";
            Log.d("WiFire", reason);
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

#### 11. Setting user details

As per TRAI regulations a public WiFi hotspot is supposed to collect the user's phone number

```java
WiFire.getInstance().setUserDetails(countryCode, phoneNumber, emailID, name);
```

You will need to set the user's details in the SDK before connecting to a network.
Email ID and name fields are optional, but some providers require those fields to be filled out before they give you Internet access.

