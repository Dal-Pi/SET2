package com.kania.set2.model;

import java.util.ArrayList;

/**
 * Created by user on 2016-08-14.
 */

public class SetVerifier {
    public static boolean isValidSet(ArrayList<SetItemData> items) {
        if (items.size() != 3) {
            return false;
        }

        int colorSum = 0;
        int shapeSum = 0;
        int fillSum = 0;
        int amountSum = 0;
        for (SetItemData item : items) {
            colorSum += item.mColor;
            shapeSum += item.mShape;
            fillSum += item.mFill;
            amountSum += item.mAmount;
        }

        return ((colorSum % 3 == 0) && (shapeSum % 3 == 0)
                && (fillSum % 3 == 0) && (fillSum % 3 == 0));
    }
}
