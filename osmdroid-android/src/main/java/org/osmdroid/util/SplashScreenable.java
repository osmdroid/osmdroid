package org.osmdroid.util;

/**
 * @author Fabrice Fontaine
 * Suggested by @InI4 in https://github.com/osmdroid/osmdroid/issues/1005
 * There are some init admin tasks that may take time.
 * For the apps that have a splash screen it makes sense to run them during the splash screen.
 * <p>
 * BE CAUTIOUS: we cannot expect all apps to have a splash screen,
 * and some apps prefer to have the fastest start up.
 * <p>
 * Recommandations (short version):
 * - put there actions that are reasonably slow (less than 30 seconds)
 * - the actions can be run multiple times, but will only do something (and last long) the very first time
 * - don't expect all apps to have a splash screen and therefore to systematically call those actions
 * - as a consequence, there's a fair chance you'll have to run the same actions somewhere else in the code
 * @since 6.0.2
 */
public interface SplashScreenable {

    void runDuringSplashScreen();
}
