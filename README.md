# osmdroid

osmdroid is a (almost) full/free replacement for Android's MapView (v1 API) class. It also includes a modular tile provider system with support for numerous online and offline tile sources and overlay support with built-in overlays for plotting icons, tracking location, and drawing shapes.

**osmdroid is migrated from [Google Code](https://code.google.com/p/osmdroid/). See [issue 1](https://github.com/osmdroid/osmdroid/issues/1).**

# Current Release

5.0.1 November 12th, 2015

# Read the [Wiki](https://github.com/osmdroid/osmdroid/wiki) for additional examples

## Prerequisites

Before you add osmdroid to your project make sure you check the [Prerequisites](https://github.com/osmdroid/osmdroid/wiki/Prerequisites) page.

## I want to use osmdroid in my project

```groovy
dependencies {
    compile 'org.osmdroid:osmdroid-android:5.0.1@aar'
    //Note as of 5.0, SLF4j is no longer needed!  compile 'org.slf4j:slf4j-simple:1.6.1'
}
```

```xml
<dependency>
  <groupId>org.osmdroid</groupId>
  <artifactId>osmdroid-android</artifactId>
  <version>5.0.1</version>
  <type>aar</type>
</dependency>
```


You can include osmdroid using Maven or by adding an AAR file to your project. See [HowToIncludeInYourProject](https://github.com/osmdroid/osmdroid/wiki).

You may find it useful to read the [Javadoc](http://javadoc.osmdroid.org/).

## I want to compile the source

The preferred method is by using Maven. See [HowToMaven](https://github.com/osmdroid/osmdroid/wiki/How-to-build-OsmDroid-from-source).

## I have a question

See if it's already been answered in the
[Discussion Group](http://groups.google.com/group/osmdroid).
If not, please ask there.

## I want to report a bug

First check the [FAQ](https://github.com/osmdroid/osmdroid/wiki/FAQ) page for answers.

Then check the [Changelog](https://github.com/osmdroid/osmdroid/wiki/Changelog) page to see if this issue is fixed in a newer or upcoming version of osmdroid.

If think you have a legitimate bug to report then go to the [Issues](https://github.com/osmdroid/osmdroid/issues?state=open) page. If your issue already exists then contribute any additional information you may have that will help us track down the source of the issue. If your issue does not exist then create a new issue report. Include what version of osmdroid you are using, what version of Android you are using, a detailed description of the problem and any relevant code. It is particularly helpful if you can reproduce the problem using our [OpenStreetMapViewer](https://github.com/osmdroid/osmdroid/tree/master/OpenStreetMapViewer) sample project as your starting point.

If you have a question on _how_ to do something then your question is better suited for a public forum such as [Stack Overflow](http://stackoverflow.com/questions/tagged/osmdroid) or the osmdroid [Google Groups](https://groups.google.com/forum/#!forum/osmdroid) page where you can get feedback from a large pool of osmdroid users.

## I want to contribute

Great! Osmdroid continues to improve from the contributions of its users. This could include code contributions, sample fragments for [OpenStreetMapViewer](https://github.com/osmdroid/osmdroid/tree/master/OpenStreetMapViewer), or wiki content.
If you have an enhancement to contribute please add a new issue.
Describe the enhancement you are adding, how you implemented it and attach a patch against the latest trunk.
Please take a look at the [DeveloperGuidelines](https://github.com/osmdroid/osmdroid/wiki/Developer-Guidelines) page for code contributions before submitting code.

## I want more!

Maybe take a look at the fine [osmbonuspack project](https://github.com/MKergall/osmbonuspack) that acts as a companion to osmdroid and adds a lot of useful functionality.

## Screenshots

![](images/MyLocation.png)
![](images/CustomLayer.png)
![](images/TwoMarkers.png)
