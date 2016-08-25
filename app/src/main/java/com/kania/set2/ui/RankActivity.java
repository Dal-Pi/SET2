package com.kania.set2.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.kania.set2.R;
import com.kania.set2.model.SetContract;
import com.kania.set2.model.SetRankData;
import com.kania.set2.util.RandomNumberUtil;
import com.kania.set2.util.SetRankUtil;
import com.kania.set2.util.ViewUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RankActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_DIFFICULTY = SetContract.EXTRA_DIFFICULTY;
    public static final String EXTRA_NEW_RANK = SetContract.EXTRA_NEW_RANK;
    public static final String KEY_DIFFICULTY = "saved_difficulty";
    public static final String KEY_NEW_RANK = "saved_new_rank";
    public static final int DIFFICULTY_EASY = SetContract.DIFFICULTY_EASY;
    public static final int DIFFICULTY_HARD = SetContract.DIFFICULTY_HARD;

    private TextView mTextTitle;
    private Button mBtnEasy;
    private Button mBtnHard;
    private ListView mListRank;
    private Button mBtnExit;

    private RankAdapter mAdapter;

    private SetRankData mNewRankData;
    private int mDifficulty;
    private int mBasicColor;
    private int mNewColor;

    private ArrayList<SetRankData> mPreRankEasy;
    private ArrayList<SetRankData> mPreRankHard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rank);

        mTextTitle = (TextView)findViewById(R.id.rank_text_title);
        mBtnEasy = (Button)findViewById(R.id.rank_btn_easy);
        mBtnEasy.setOnClickListener(this);
        mBtnHard = (Button)findViewById(R.id.rank_btn_hard);
        mBtnHard.setOnClickListener(this);
        mListRank = (ListView)findViewById(R.id.rank_list_ranklist);
        mBtnExit = (Button)findViewById(R.id.rank_btn_exit);
        mBtnExit.setOnClickListener(this);
        mBasicColor = getResources().getColor(R.color.base_darkgray);

        mPreRankEasy = SetRankUtil.getInstance(this).getRankList(DIFFICULTY_EASY);
        mPreRankHard = SetRankUtil.getInstance(this).getRankList(DIFFICULTY_HARD);

        Calendar calendar = Calendar.getInstance();
        int[] colors = getResources().getIntArray(R.array.pointColors);
        int randomIndex = RandomNumberUtil.getInstance(calendar.getTimeInMillis())
                .getRandomNumber(colors.length);
        mNewColor = colors[randomIndex];

        Intent intent = getIntent();
        if (intent != null) {
            mDifficulty = intent.getIntExtra(EXTRA_DIFFICULTY, DIFFICULTY_HARD);
            mNewRankData = (SetRankData)intent.getSerializableExtra(EXTRA_NEW_RANK);
        } else {
            mDifficulty = DIFFICULTY_EASY;
        }

        if (savedInstanceState != null) {
            mDifficulty = savedInstanceState.getInt(KEY_DIFFICULTY);
            mNewRankData = (SetRankData)savedInstanceState.getSerializable(KEY_NEW_RANK);
        }
        setTitle();

        addNewRankIfScope();

        ArrayList<SetRankData> adapterDate = new ArrayList<>();
        if (mDifficulty == DIFFICULTY_EASY) {
            adapterDate.addAll(mPreRankEasy);
        } else {
            adapterDate.addAll(mPreRankHard);
        }
        mAdapter = new RankAdapter(this, R.layout.item_rank, adapterDate);
        mListRank.setAdapter(mAdapter);

        setButtonColor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNewRankData != null) {
            ArrayList<SetRankData> targetList;
            if (mDifficulty == DIFFICULTY_EASY) {
                targetList = mPreRankEasy;
            } else {
                targetList = mPreRankHard;
            }

            int position = 0;
            for (int i = 0; i < targetList.size(); ++i) {
                if (targetList.get(i).date == mNewRankData.date) {
                    position = i;
                    break;
                }
            }
            mListRank.setSelection(position);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_DIFFICULTY, mDifficulty);
        if (mNewRankData != null) {
            outState.putSerializable(KEY_NEW_RANK, mNewRankData);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.rank_btn_easy:
                mDifficulty = DIFFICULTY_EASY;
                changeList();
                break;
            case R.id.rank_btn_hard:
                mDifficulty = DIFFICULTY_HARD;
                changeList();
                break;
            case R.id.rank_btn_exit:
                finish();
                break;
        }
    }

    private void addNewRankIfScope() {
        if (mNewRankData == null) {
            return;
        }
        ArrayList<SetRankData> targetList;
        if (mDifficulty == DIFFICULTY_EASY) {
            targetList = mPreRankEasy;
        } else {
            targetList = mPreRankHard;
        }

        boolean isRankInList = false;
        for (SetRankData data : targetList) {
            if (data.date == mNewRankData.date) {
                isRankInList = true;
                break;
            }
        }
        if (!isRankInList && mNewRankData.difficulty == mDifficulty) {
            targetList.add(mNewRankData);
        }
    }

    private void changeList() {
        mAdapter.clear();
        if (mDifficulty == DIFFICULTY_EASY) {
            mAdapter.addAll(mPreRankEasy);
        } else {
            mAdapter.addAll(mPreRankHard);
        }
        mAdapter.notifyDataSetChanged();
        setTitle();
    }

    private void setTitle() {
        StringBuffer stringBuffer = new StringBuffer();
        Resources resouces = getResources();
        stringBuffer.append(resouces.getString(R.string.rank_text_title)).append("(");
        if (mDifficulty == DIFFICULTY_EASY) {
            stringBuffer.append(resouces.getString(R.string.easy));
        } else {
            stringBuffer.append(resouces.getString(R.string.hard));
        }
        stringBuffer.append(")");
        mTextTitle.setText(stringBuffer.toString());
    }

    private void setButtonColor() {
        ViewUtil.setButtonColor(mBtnEasy, mNewColor);
        ViewUtil.setButtonColor(mBtnHard, mNewColor);
        ViewUtil.setButtonColor(mBtnExit, mNewColor);
    }

    class RankAdapter extends ArrayAdapter<SetRankData> {

        public RankAdapter(Context context, int resource, List<SetRankData> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_rank, null, false);
            }
            TextView rank = (TextView)view.findViewById(R.id.item_text_rank);
            TextView name = (TextView)view.findViewById(R.id.item_text_name);
            TextView score = (TextView)view.findViewById(R.id.item_text_score);
            TextView date = (TextView)view.findViewById(R.id.item_text_date);

            SetRankData data = getItem(position);
            rank.setText("" + (position + 1));
            name.setText(data.name);
            score.setText("" + data.score);
            date.setText(DateUtils.formatDateTime(getContext(), data.date,
                    DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_NUMERIC_DATE));
            if (mNewRankData != null && mNewRankData.date == data.date) {
                rank.setTextColor(mNewColor);
                name.setTextColor(mNewColor);
                score.setTextColor(mNewColor);
                date.setTextColor(mNewColor);
            } else {
                rank.setTextColor(mBasicColor);
                name.setTextColor(mBasicColor);
                score.setTextColor(mBasicColor);
                date.setTextColor(mBasicColor);
            }

            return view;
        }
    }
}
