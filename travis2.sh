#!/bin/bash
# Abort on Error

# this automates some tasks for travis for setting up the android sdk with enough stuff to 
# 1) compile all of osmdroid
# 2) create an avd with the desired api level and/or google api set
# 3) start the avd

# why is this here?
# see issue #701 on github for osmdroid, basically, increase the api set and system images
# requires more disk space that the travis vms have available, which is silly but whatever.
# this scripts reduces the disk space requirements by only installing what's needed for each test

# expected environment variables
# API = the numerical android api level with an optional 'g' at the end, which signifies to include the google apis
# ----------

# add the packages we need to compile osmdroid
PACKAGES=$PACKAGES "build-tools;23.0.2" "build-tools;23.0.1" "build-tools;23.0.3" 
PACKAGES=$PACKAGES "extra;google;m2repository" "extra;google;google_play_services"
PACKAGES=$PACKAGES "extra;android;m2repository"
PACKAGES=$PACKAGES "platforms;android-23 add-ons;addon-google_apis-google-23"


# this part simply setups up some commands to send the sdkmanager
case "$API" in
'10')
    PACKAGES=$PACKAGES "platforms;android-10" 
	PACKAGES=$PACKAGES "system-images;android-10;default;armeabi-v7a" 
    ;;
'10g')
    PACKAGES=$PACKAGES "platforms;android-10"
	PACKAGES=$PACKAGES "system-images;android-10;google_apis;armeabi-v7a"
	PACKAGES=$PACKAGES "add-ons;addon-google_apis-google-19"
    ;;
'15')
    PACKAGES=$PACKAGES "platforms;android-15" 
	PACKAGES=$PACKAGES "system-images;android-15;default;armeabi-v7a" 
    ;;
'15g')
    PACKAGES=$PACKAGES "platforms;android-15" 
	PACKAGES=$PACKAGES "system-images;android-15;google_apis;armeabi-v7a"
	PACKAGES=$PACKAGES "add-ons;addon-google_apis-google-15"
    ;;
'19')
    PACKAGES=$PACKAGES "platforms;android-19" 
	PACKAGES=$PACKAGES "system-images;android-19;default;armeabi-v7a" 
    ;;
'19g')
    PACKAGES=$PACKAGES  "platforms;android-19" 
	PACKAGES=$PACKAGES "system-images;android-19;google_apis;armeabi-v7a"
	PACKAGES=$PACKAGES "add-ons;addon-google_apis-google-19"
    ;;
'23')
    PACKAGES=$PACKAGES  "platforms;android-23" 
	PACKAGES=$PACKAGES "system-images;android-23;default;armeabi-v7a" 
    ;;
'23g')
    PACKAGES=$PACKAGES "platforms;android-23" 
	PACKAGES=$PACKAGES "system-images;android-23;google_apis;armeabi-v7a"
	PACKAGES=$PACKAGES "add-ons;addon-google_apis-google-23"
    ;;
'24')
    PACKAGES=$PACKAGES "platforms;android-24" 
	PACKAGES=$PACKAGES "system-images;android-24;default;armeabi-v7a" 
    ;;
'24g')
    PACKAGES=$PACKAGES "platforms;android-24" 
	PACKAGES=$PACKAGES "system-images;android-24;google_apis;armeabi-v7a"
	PACKAGES=$PACKAGES "add-ons;addon-google_apis-google-24"
    ;;
*)
    PACKAGES=$PACKAGES $API was not handled
    ;;
esac

# now call sdkmanager with our package list

echo calling sdkmanager with $PACKAGES
sdkmanager $PACKAGES
