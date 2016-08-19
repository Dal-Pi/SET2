package com.kania.set2.model;

import java.io.Serializable;

/**
 * Created by user on 2016-08-14.
 */

public class SetItemData implements Serializable {
    public int mColor;
    public int mShape;
    public int mFill;
    public int mAmount;

    public SetItemData(int color, int shape, int fill, int amount) {
        this.mColor = color;
        this.mShape = shape;
        this.mFill = fill;
        this.mAmount = amount;
    }

    public boolean isSameWith(SetItemData target) {
        return (this.mColor == target.mColor) && (this.mShape == target.mShape)
                && (this.mFill == target.mFill) && (this.mAmount == target.mAmount);
    }

    @Override
    public String toString() {
        return "" + mColor + mShape + mFill + mAmount;
    }
}
