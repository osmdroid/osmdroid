@echo off
rem serious gradle + android is just crap
rem it may be fast and less verbose, but it's crap 
rem if you don't agree, make publishing osmdroid to oss.sonatype.org work and i'll forever been in your debt

rem normal build
call gradlew clean build publishToMavenLocal
rem local publish


rem copy all the locally published artifacts to a temp folder
rem inject our 'extra' javadoc content when needed

rem fix the poms
cd releaseHelper
call mvn install
if errorlevel 1 goto fail
cd ..
call  java -jar releaseHelper\target\releaseHelper-1.0-SNAPSHOT-jar-with-dependencies.jar
if errorlevel 1 goto fail
:goto success
rem sign all the artifacts

rem hash all the signatures and artifacts 


rem push to sonatype oss


:fail
echo Failure
goto end

:success
echo Success
goto end

:end