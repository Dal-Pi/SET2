package com.kania.set2.ui;

import com.kania.set2.model.SetItemData;

/**
 * Created by user on 2016-08-15.
 */

public class CardViewData {
    private SetItemData mSetitemData;
    public boolean mIsChecked;

    public CardViewData(SetItemData itemData) {
        this.mSetitemData = itemData;
        this.mIsChecked = false;
    }

    public SetItemData getmSetitemData() {
        return mSetitemData;
    }
}
