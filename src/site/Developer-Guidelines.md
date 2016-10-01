## Introduction

This page is to list some of the more important guidelines for developers **contributing code to osmdroid**.  This is not intended to strictly enforce a particular style - more to keep the code internally consistent.

### General guidelines

 * Use spaces for indenting, not tabs (**recent change**)
 * Make variables final where possible
 * Prefix member variables with m
 * Prefix method arguments with p
 * Versions <= 4.3, Use org.slf4j.Logger. Verions > 4.3 use android.util.Log
 * Use 100 character line width
 * Use final wherever possible, especially parameters
 * Eclipse formatting and clean-up XML settings files (both of which cover most of the above) are included in the repository. Import them, and use them to make your life easier!

### Guidelines for osmdroid-android project

Test your app with a device or emulate

`mvn clean install` - NOTE Maven has been broken for Android builds since Apr 2016

or with gradle

`gradle clean install connectedCheck`

## Building

Make sure you build with the follow commands before opening a pull request. There are several related wiki pages on setting up the development environment. See https://github.com/osmdroid/osmdroid/wiki/How-to-build-OsmDroid-from-source



```` gradle clean install connectedCheck ````
