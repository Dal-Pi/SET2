package com.kania.set2.util;

import android.graphics.PorterDuff;
import android.widget.Button;

/**
 * Created by user on 2016-08-26.
 */

public class ViewUtil {

    public static void setButtonColor(Button btn, int color) {
        btn.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }
}
