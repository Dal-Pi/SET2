package com.kania.set2.util;

import android.content.Context;
import android.widget.Toast;

import com.kania.set2.R;

/**
 * Created by user on 2016-09-04.
 */

public class SetGameUtil {

    public static final int NUM_END_TIMER_COUNT = 5000;

    public static void showBackpressToast(Context context) {
        Toast.makeText(context, R.string.text_backpress, Toast.LENGTH_SHORT).show();
    }
}
