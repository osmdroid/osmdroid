
# External libraries and dependencies

## Version 4.3 and older

osmdroid-android jar requires the [slf4j-android logging library](http://www.slf4j.org/android/). If you are using Gradle or Maven then it will be downloaded for you. If you are using the osmdroid JAR/AAR in some archaic build system or IDE, then you must download the slf4j-android (only for osmdroid < 5) library jar and place it in your libs folder.

osmdroid-thirdparty jar requires org.json:json, google maps, google play services, and the android support compatv4 libraries

## Version 5.0 and newer

None for the osmdroid-android aar. osmdroid-thirdparty aar requires org.json:json, google maps, google play services, and the android support compatv4 libraries
 

# Manifest additions

* You should be targeting the latest API in your project. We support compatibility back to API 8. Note: if you're using the Google Maps Wrapper, you'll need API 9 as the min. Your manifest should have a uses-sdk tag similar to:

```xml
<uses-sdk android:targetSdkVersion="NEWEST AVAILABLE" android:minSdkVersion="8" />
```

* You should turn off hardware acceleration in the manifest. We have experimental hardware acceleration support but users will still run into some issues. See [issue 413](https://code.google.com/p/osmdroid/issues/detail?id=413) for more information. **In most cases, hardware acceleration turned on will work** and it appears to be a hardware specific thing. If you run into issues with hardware acceleration turned on, please let us know.
* osmdroid requires certain permissions in the Android project manifest to perform correctly. You will need to add the following permissions in your manifest file:

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/> 
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```