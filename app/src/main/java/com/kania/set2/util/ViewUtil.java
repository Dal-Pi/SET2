package com.kania.set2.util;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.widget.Button;

/**
 * Created by user on 2016-08-26.
 */

public class ViewUtil {

    public static void setButtonColor(Button btn, int color) {
        Drawable d = btn.getBackground();
        d.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        btn.setBackgroundDrawable(d);
    }
}
