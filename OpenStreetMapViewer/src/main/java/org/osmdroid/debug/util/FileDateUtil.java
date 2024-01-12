package org.osmdroid.debug.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * from http://stackoverflow.com/a/38024962/1203182
 *
 * @since 5.6.2
 */

public class FileDateUtil {

    public static String getModifiedDate(long modified) {
        return getModifiedDate(Locale.getDefault(), modified);
    }

    public static String getModifiedDate(Locale locale, long modified) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(getDateFormat(locale));
        return dateFormat.format(new Date(modified));
    }

    public static String getDateFormat(Locale locale) {
        return DateFormat.getBestDateTimePattern(locale, "MM/dd/yyyy hh:mm:ss aa");
    }
}
