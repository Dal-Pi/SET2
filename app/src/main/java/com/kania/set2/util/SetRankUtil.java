package com.kania.set2.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kania.set2.model.SetRankContract.SetRankEntry;
import com.kania.set2.model.SetRankDBHelper;
import com.kania.set2.model.SetRankData;

import java.util.ArrayList;

/**
 * Created by user on 2016-08-21.
 */

public class SetRankUtil {

    private static SetRankUtil mInstance;
    private static ArrayList<SetRankData> mRankDataList;

    private static Context mContext;

    public static SetRankUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SetRankUtil(context);
        }
        return mInstance;
    }

    private SetRankUtil(Context context) {
        this.mContext = context;
    }

    public ArrayList<SetRankData> getRankList(int difficulty) {
        ArrayList<SetRankData> ret = new ArrayList();

        if (true/*mRankDataList == null*/) {
            mRankDataList = new ArrayList<>();

            SetRankDBHelper dbHelper = new SetRankDBHelper(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(dbHelper.getSelectQueryString(), null);

//            cursor.moveToFirst();
            while(cursor.moveToNext()) {
                SetRankData data = new SetRankData();
                data.name = cursor.getString(cursor.getColumnIndexOrThrow(
                        SetRankEntry.COLUMN_NAME_NAME));
                data.score = cursor.getInt(cursor.getColumnIndexOrThrow(
                        SetRankEntry.COLUMN_NAME_SCORE));
                data.date = cursor.getLong(cursor.getColumnIndexOrThrow(
                        SetRankEntry.COLUMN_NAME_DATE));
                data.difficulty = cursor.getInt(cursor.getColumnIndexOrThrow(
                        SetRankEntry.COLUMN_NAME_DIFFICULTY));
                mRankDataList.add(data);
                if (data.difficulty == difficulty) {
                    ret.add(data);
                }
            }
            db.close();
        } else {
            for (SetRankData data : mRankDataList) {
                if (data.difficulty == difficulty) {
                    ret.add(data);
                }
            }
        }

        return ret;
    }

    public void addNewRank(SetRankData newData) {
        SetRankDBHelper dbHelper = new SetRankDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SetRankEntry.COLUMN_NAME_NAME, newData.name);
        values.put(SetRankEntry.COLUMN_NAME_SCORE, newData.score);
        values.put(SetRankEntry.COLUMN_NAME_DATE, newData.date);
        values.put(SetRankEntry.COLUMN_NAME_DIFFICULTY, newData.difficulty);
        db.insert(SetRankEntry.TABLE_NAME, null, values);
        db.close();
    }
}
