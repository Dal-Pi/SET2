package com.kania.set2.util;

import com.kania.set2.model.SetItemData;
import com.kania.set2.ui.VsModeActivity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by user on 2016-09-04.
 */

public class SetSaveStateUtil {

    public static final String DELIMETER = "/";

    public static String backupSetItemDataList(Vector<SetItemData> list) {
        StringBuilder sb = new StringBuilder();
        for (SetItemData data : list) {
            sb.append(data.toString()).append(DELIMETER);
        }
        return sb.toString();
    }

    public static Vector<SetItemData> restoreSetItemDataList(String deckListString) {
        Vector<SetItemData> retList = new Vector<>();
        if(deckListString == null || "".equalsIgnoreCase(deckListString)) {
            return null;
        }
        String[] items = deckListString.split(DELIMETER);
        for (int i = 0; i < items.length; ++i) {
            retList.add(new SetItemData(
                    Integer.parseInt(items[i].substring(0, 1)),
                    Integer.parseInt(items[i].substring(1, 2)),
                    Integer.parseInt(items[i].substring(2, 3)),
                    Integer.parseInt(items[i].substring(3, 4))));
        }
        return retList;
    }

    public static String backupIntegerList(Vector<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int pos : list) {
            sb.append("" + pos).append(DELIMETER);
        }
        return sb.toString();
    }

    public static Vector<Integer> restoreIntegerList(String selectedListString) {
        Vector<Integer> retList = new Vector<>();
        if(selectedListString == null || "".equalsIgnoreCase(selectedListString)) {
            return retList;
        }
        String[] positions = selectedListString.split(DELIMETER);
        for (int i = 0; i < positions.length; ++i) {
            retList.add(Integer.parseInt(positions[i]));
        }
        return retList;
    }
}
