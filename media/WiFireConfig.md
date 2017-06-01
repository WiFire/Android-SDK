## Some aspects of the WiFire SDK can be customised


#### This requires you to create a JSON file in your `raw` folder.

```xml
	main/res/raw/wifire_config.json
```

### - Customise AutoLogin success behaviour

```json
{
  "captive_config": {
    "target": "http://www.example.com",
    "cta_text": "Click me",
    "bundle_params": {
      "custom_message": "Hello user"
    }
  }
}
```

#### - `wifire_config.json` should have a `JsonObject` called `captive_config`
#### ⁣ 
#### - `target` defines what the SDK will open after user clicks on CTA after login success
- It can be a URL like `http://www.example.com`
- It can be an Activity class name like `com.mypackage.myapp.MainActivity`
- The SDK will throw exceptions if the URL or Class name is invalid
- To simply close the auto login screen set it to `finish`

#### - `cta_text` This text will be displayed on the CTA after login success
#### ⁣ 
#### - `bundle_params` can contain key values which will be passed on to the target activity via intent
- Key and Value should both be of type `String`