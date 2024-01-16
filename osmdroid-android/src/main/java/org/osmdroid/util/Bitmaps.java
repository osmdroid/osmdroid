package org.osmdroid.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class Bitmaps {

    @NonNull
    public static Drawable convertBitmapToDrawable(@NonNull final Context context, @NonNull final Bitmap bitmap) {
        return new BitmapDrawable(context.getResources(), bitmap);
    }

}
