package com.kania.set2.model;

import android.provider.BaseColumns;

public final class SetRankContract {

	public SetRankContract() {}

	public static final int DIFFICULTY_EASY = SetContract.DIFFICULTY_EASY;
	public static final int DIFFICULTY_HARD = SetContract.DIFFICULTY_HARD;
	
	public static abstract class SetRankEntry implements BaseColumns {
		public static final String TABLE_NAME = "setrank";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SCORE = "score";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_DIFFICULTY = "difficulty";
	}
}
