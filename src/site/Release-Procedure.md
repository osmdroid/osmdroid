# Release Procedure

## Pre-release steps

Edit gradle.properties and update the version information :
 - pom.version (usually just remove SNAPSHOT)
 - android.versionCode (it’s probably already okay)

Commit the change and tag with git.  
Use the tag name: 'osmdroid-parent-{version}' to keep in line with the previous releases.  
git push

## Set credentials in gradle.properties

Edit your user gradle.properties (~/.gradle/gradle.properties)
and set the following properties :
```
# Credentials for Maven Central
NEXUS_USERNAME=***
NEXUS_PASSWORD=***
GPG_PATH=***
signing.password=***
signing.keyId=***

# Google Play signing key
android.signingConfigs.release.storeFile=***
android.signingConfigs.release.storePassword=***
android.signingConfigs.release.keyAlias=***
android.signingConfigs.release.keyPassword=***
```

## Build and publish to Nexus (Maven Central)

```
./gradlew clean
./gradlew install -Pprofile=ci
./gradlew publishArtifacts -Pprofile=sources,javadoc
./gradlew cC
./gradlew site
```

You may need to repeat the `install` step a couple of times until it succeeds.

Go to
[Sonatype](https://oss.sonatype.org/),
select “staging repositories”, check osmdroid and click “close” and then “release”.

## Prepare distribution package for Github
```
./gradlew distZip -Pprofile=dist
```

## Upload release zip on Github

[https://github.com/osmdroid/osmdroid/releases/new](https://github.com/osmdroid/osmdroid/releases/new)

Output zip is at osmdroid-dist/build/distributions/

## Publish APK to Google Play

Output APK is at OpenStreetMapViewer\build\outputs\apk\OpenStreetMapViewer-{version}-release.apk

## Push the site changes

Copy the contents of "docs" into a separate clone of osmdroid on the branch `gh-pages`, commit and push.

## Post-release steps

Update readme.md to have the current version number listed.

Edit gradle.properties and update the version information :
 - pom.version (next SNAPSHOT)
 - android.versionCode (increment by 1)

git commit and push

## Wiki updates

Update the change log
Update the upgrade guide
