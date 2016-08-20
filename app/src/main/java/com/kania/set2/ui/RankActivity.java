package com.kania.set2.ui;

import android.content.Context;
import android.content.Intent;
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
import com.kania.set2.util.SetRankUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RankActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String KEY_DIFFICULTY = SetContract.EXTRA_DIFFICULTY;
    public static final int DIFFICULTY_EASY = SetContract.DIFFICULTY_EASY;
    public static final int DIFFICULTY_HARD = SetContract.DIFFICULTY_HARD;

    private TextView mTextTitle;
    private Button mBtnEasy;
    private Button mBtnHard;
    private ListView mListRank;
    private Button mBtnExit;

    private RankAdapter mAdapter;

    private int mDifficulty;

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

        mPreRankEasy = SetRankUtil.getInstance(this).getRankList(DIFFICULTY_EASY);
        mPreRankHard = SetRankUtil.getInstance(this).getRankList(DIFFICULTY_HARD);

        Intent intent = getIntent();
        if (intent != null) {
            mDifficulty = intent.getIntExtra(KEY_DIFFICULTY, DIFFICULTY_HARD);
        } else {
            mDifficulty = DIFFICULTY_EASY;
        }

        ArrayList<SetRankData> adapterDate = new ArrayList<>();
        if (mDifficulty == DIFFICULTY_EASY) {
            for (SetRankData data : mPreRankEasy) {
                adapterDate.add(data);
            }
        } else {
            for (SetRankData data : mPreRankHard) {
                adapterDate.add(data);
            }
        }
        mAdapter = new RankAdapter(this, R.layout.item_rank, adapterDate);
        mListRank.setAdapter(mAdapter);
    }

    //TODO get New rank and noti
    
    //TODO saveInstance

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.rank_btn_easy:
                mAdapter.clear();
                mAdapter.addAll(mPreRankEasy);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.rank_btn_hard:
                mAdapter.clear();
                mAdapter.addAll(mPreRankHard);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.rank_btn_exit:
                finish();
                break;
        }
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

            return view;
        }
    }
}
