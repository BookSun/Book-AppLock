package com.book.app.lock.utils;

import android.content.res.bookColorfulResources.bookColorfulStyle;
import android.content.res.bookColorfulResources;
import book.util.ColorUtils;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.Context;

public class bookColorfulAdapter {

    public static Drawable getColorDrawable(Context context,Drawable sourceDraw) {
        bookColorfulStyle.ColorfulNode colorfulStyle = bookColorfulResources.getInstance().getColorfulStyle(context);
        if (colorfulStyle != null) {
             int color = colorfulStyle.getMainColor();
             Drawable resultDrawable = ColorUtils.resetDrawableColor(context, sourceDraw, color);
                return resultDrawable;
            }
        return sourceDraw;
    }

}