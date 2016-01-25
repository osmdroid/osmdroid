
[![Build Status](https://api.travis-ci.org/osmdroid/osmdroid.svg?branch=master)](https://travis-ci.org/osmdroid/osmdroid)

# osmdroid

osmdroid is a (almost) full/free replacement for Android's MapView (v1 API) class. It also includes a modular tile provider system with support for numerous online and offline tile sources and overlay support with built-in overlays for plotting icons, tracking location, and drawing shapes.

Current Release: **5.1 January 24th, 2016**

Please read the [osmdroid wiki](https://github.com/osmdroid/osmdroid/wiki) for  tutorials on integration.

**Gradle dependency**
```groovy
dependencies {
    compile 'org.osmdroid:osmdroid-android:5.1@aar'
    //Note as of 5.0, SLF4j is no longer needed!  compile 'org.slf4j:slf4j-simple:1.6.1'
}
```

**Maven dependency**
```xml
<dependency>
  <groupId>org.osmdroid</groupId>
  <artifactId>osmdroid-android</artifactId>
  <version>5.1</version>
  <type>aar</type>
</dependency>
```

You can also [compile osmdroid from source](https://github.com/osmdroid/osmdroid/wiki/How-to-build-osmdroid-from-source) or [include osmdroid as a JAR or AAR](https://oss.sonatype.org/content/groups/public/org/osmdroid/osmdroid-android/).

## I have a question or want to report a bug

If you have a question, please view the [osmdroid FAQ](https://github.com/osmdroid/osmdroid/wiki/FAQ).  
You can also view the [Stack Overflow osmdroid tag](http://stackoverflow.com/questions/tagged/osmdroid) and [osmdroid Google Group](https://groups.google.com/forum/#!forum/osmdroid) where you can get feedback from a large pool of osmdroid users.

If you still have an issue, please check the [Changelog](https://github.com/osmdroid/osmdroid/wiki/Changelog) page to see if this issue is fixed in a newer or upcoming version of osmdroid.

If think you have a legitimate bug to report then go to the [Issues](https://github.com/osmdroid/osmdroid/issues?state=open) page to see if your issue has been reported. If your issue already exists then please contribute information that will help us track down the source of the issue. If your issue does not exist then create a new issue report. When creating an issue, please include the version of osmdroid, the Android platform target and test device you are using, and a detailed description of the problem with relevant code. It is particularly helpful if you can reproduce the problem using our [OpenStreetMapViewer](https://github.com/osmdroid/osmdroid/tree/master/OpenStreetMapViewer) sample project as your starting point.

## I want to contribute

Great! Osmdroid continues to improve from the contributions of its users. This could include code contributions, sample fragments for [OpenStreetMapViewer](https://github.com/osmdroid/osmdroid/tree/master/OpenStreetMapViewer), or wiki content.
If you have an enhancement to contribute please add a new issue.
Describe the enhancement you are adding, how you implemented it and attach a patch against the latest trunk.
Please take a look at the [Developer Guidelines](https://github.com/osmdroid/osmdroid/wiki/Developer-Guidelines) page for code contributions before submitting code.

## I want more!

The [OSMBonusPack project](https://github.com/MKergall/osmbonuspack) adds additional functionality for use with osmdroid projects.

## Screenshots

![](images/MyLocation.png)
![](images/CustomLayer.png)
![](images/TwoMarkers.png)
