# Non-Maven, Non-Gradle users

If you are not yet using Maven or Gradle, you can include osmdroid by downloading directly from [Maven Central](https://oss.sonatype.org/content/groups/public/org/osmdroid/).

You have a choice between using an AAR (for >=5.0) or JAR (for < 4.3) in your libs folder.

For versions < 4.3, you also need slf4j-api and slf4j-android.

Version >= v5.0 WARN: This wiki page is a bit dated. If you're still using Eclipse without a backing building system (maven/gradle), you'll probably want to google around on how to use AAR files with Eclipse. By now, most people have probably moved on to Android Studio with gradle, since it's really the only supported solution. Good luck!