@echo off
echo BE SURE TO USE JDK11
echo Gradle does not honor exit codes and this script cannot detect
echo a gradle build failure. Watch the output!

rem seriously gradle + android is just crap
rem it may be fast and less verbose, but it's crap 
rem if you don't agree, make publishing osmdroid to oss.sonatype.org work and i'll forever been in your debt

rem normal build + local publish
call gradlew clean build publishToMavenLocal


rem the release helper does the following
rem copy all the locally published artifacts to a temp folder
rem inject our 'extra' javadoc content when needed
rem fix the poms
rem sign everything with your encrypted password from local.properties
rem hash everything
rem upload to maven central with your encrypted password from local.properties
rem prepare the distzip release artifact to upload to github

cd releaseHelper
call mvn install
if errorlevel 1 goto fail
cd ..
call  java -jar releaseHelper\target\releaseHelper-1.0-SNAPSHOT-jar-with-dependencies.jar
if errorlevel 1 goto fail
goto success


:fail
echo Failure
exit /b 1

:success
echo Success
exit /b 0

