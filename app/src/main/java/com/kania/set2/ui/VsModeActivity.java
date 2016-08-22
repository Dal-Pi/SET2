package com.kania.set2.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kania.set2.R;
import com.kania.set2.model.SetItemData;
import com.kania.set2.util.RandomNumberUtil;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by user on 2016-08-22.
 */

public class VsModeActivity extends AppCompatActivity implements View.OnClickListener,
        NineCardFragment.OnSelectThreeCardListener {

    private static final int NUM_ALL_CARDS = 9;
    private static final int NUM_ANS_CARDS = 3;
    private static final int NUM_MAX_STAGE = 10;
    private static final int NUM_TIME_SET = 5;
    private static final int NUM_TIME_COMPLETE = 2;
    private static final int NUM_MAX_PLAYER = 2;

    public static final String KEY_ANSWER_LIST = "saved_answer_list";
    public static final String KEY_DECK_LIST = "saved_deck_list";
    public static final String KEY_SELECTED_LIST = "saved_selected_list";
    public static final String KEY_STAGE_NUM = "saved_selected_list";

    public static final int ANIMATION_DURATION = 2000;

    public static final int GAME_STATE_PREPARE = 1;
    public static final int GAME_STATE_READY = 2;
    public static final int GAME_STATE_CALLED_SET = 3;
    public static final int GAME_STATE_CHANCE_COMPLETE = 4;
    public static final int GAME_STATE_FINISH = 5;

    //utils
    private RandomNumberUtil mRandomNumberUtil;
    private TimerHandler mHandler;


    //game data
    private int mStage;
    private int mRemainTime;

    private ArrayList<SetItemData> mAllItemList;
    private int[] mAllItemListSequence;
    private ArrayList<SetItemData> mAnswerList;
    private ArrayList<SetItemData> mDeckList;
    private ArrayList<Integer> mSelectedPositionList;
    private ArrayList<Integer> mSavedSelectedPositionList;
    private int mNowGameState;
    private ArrayList<PlayerData> mPlayers;
    private PlayerData mNowFlagedPlayer;
    private boolean mAlreadySelected;

    //fragments
    private AnswerImageFragment mAnswerImageFragment;
    private NineCardFragment mNineCardFragment;

    //layout
    private ViewGroup mResultLayout;
    private Button mBtnStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vs);
        addFragment();

        mHandler = new TimerHandler();

        prepareGame();
        if (savedInstanceState == null) {
            initPlayerData();
            initGameState();
            renewViews();
        } else {
            //TODO
            //loadPlayerData();
            //resumeGame()
            renewViews();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.vs_btn_start:
                mBtnStart.setVisibility(View.GONE);
                startGame();
                break;
            case R.id.vs_player_btn_set_1:
                mNowFlagedPlayer = mPlayers.get(0);
                mNowGameState = GAME_STATE_CALLED_SET;
                renewViews();
                break;
            case R.id.vs_player_btn_set_2:
                mNowFlagedPlayer = mPlayers.get(1);
                mNowGameState = GAME_STATE_CALLED_SET;
                renewViews();
                break;
        }
    }

    @Override
    public void onThreeCardSelected(ArrayList<SetItemData> selectedList) {

    }

    @Override
    public void onSelectCard(int position) {
        if (mSelectedPositionList == null) {
            mSelectedPositionList = new ArrayList<>();
        }
        if (mSelectedPositionList.contains(position)) {
            for (int i = 0; i < mSelectedPositionList.size(); ++i) {
                if (mSelectedPositionList.get(i) == position) {
                    mSelectedPositionList.remove(i);
                }
            }
        } else {
            mSelectedPositionList.add(position);
        }
        //Log.d("SET", "selected : " + mSelectedPositionList.toString());
    }
    private void addFragment() {
        mAnswerImageFragment = AnswerImageFragment.newInstance();
        //TODO expend card type
        mNineCardFragment = NineCardFragment
                .newInstance(NineCardFragment.CARD_TYPE_FILL_AS_PATTERN, true, this);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.vs_container_answer, mAnswerImageFragment);
        fragmentTransaction.add(R.id.vs_container_ninecard, mNineCardFragment);
        fragmentTransaction.commit();
    }

    private void prepareGame() {
        mBtnStart = (Button)findViewById(R.id.vs_btn_start);
        mBtnStart.setOnClickListener(this);
        mPlayers = new ArrayList<>();
        for (int i = 0; i < NUM_MAX_PLAYER; ++i) {
            PlayerData player = new PlayerData();
            player.btnName = (Button)findViewById(getResources()
                    .getIdentifier("vs_player_btn_name_" + (i + 1), "id", getPackageName()));
            player.btnName.setOnClickListener(this);
            player.textName = (TextView)findViewById(getResources()
                    .getIdentifier("vs_player_text_name_" + (i + 1), "id", getPackageName()));
            player.textRemainTime = (TextView)findViewById(getResources()
                    .getIdentifier("vs_player_text_remain_time_" + (i + 1), "id",
                            getPackageName()));
            player.textScore = (TextView)findViewById(getResources()
                    .getIdentifier("vs_player_text_score_" + (i + 1), "id", getPackageName()));
            player.textScorePlus = (TextView)findViewById(getResources()
                            .getIdentifier("vs_player_text_score_plus_" + (i + 1), "id",
                                    getPackageName()));
            player.btnSet = (Button)findViewById(getResources()
                    .getIdentifier("vs_player_btn_set_" + (i + 1), "id", getPackageName()));
            player.btnSet.setOnClickListener(this);
            player.btnComplete = (Button)findViewById(getResources()
                    .getIdentifier("vs_player_btn_complete_" + (i + 1), "id", getPackageName()));
            player.btnComplete.setOnClickListener(this);
            mPlayers.add(player);
        }
    }

    private void initPlayerData() {
        int[] colors = getResources().getIntArray(R.array.pastelColors);
        Calendar calendar = Calendar.getInstance();
        int[] randomIndexs = RandomNumberUtil.getInstance(calendar.getTimeInMillis())
                .getRandomNumberSet(colors.length);
        for (int i = 0; i < mPlayers.size(); ++i) {
            PlayerData player = mPlayers.get(i);
            player.name = getResources().getString(R.string.vs_text_player + i);
            player.score = 0;
            player.color = colors[randomIndexs[i]];
        }
    }

    private void initGameState() {
        mNowGameState = GAME_STATE_PREPARE;
        renewViews();
    }

    private void startGame() {
        for (PlayerData player : mPlayers) {
            player.btnName.setEnabled(false);
            player.btnName.setVisibility(View.INVISIBLE);
            player.textName.setVisibility(View.VISIBLE);
            player.btnSet.setEnabled(true);
            player.btnComplete.setEnabled(true);
        }
        mNowGameState = GAME_STATE_READY;
        renewViews();
    }

    private void renewViews() {
        if (mNowGameState == GAME_STATE_PREPARE) {
            setPrepareState();
        } else if (mNowGameState == GAME_STATE_READY) {
            setReadyState();
        } else if (mNowGameState == GAME_STATE_CALLED_SET) {
            setCalledSetState();
        } else if (mNowGameState == GAME_STATE_CHANCE_COMPLETE) {

        } else if (mNowGameState == GAME_STATE_FINISH) {

        } else {
            Log.e("SET2", "invalid state!");
        }
    }

    private void setPrepareState() {
        mBtnStart.setVisibility(View.VISIBLE);
        for (PlayerData player : mPlayers) {
            player.btnName.setVisibility(View.VISIBLE);
            player.btnName.setEnabled(true);
            player.textName.setVisibility(View.INVISIBLE);
            player.btnSet.setEnabled(false);
            player.btnComplete.setEnabled(false);
        }
    }

    private void setReadyState() {
        enableAllButton(true);
    }

    private void setCalledSetState(){
        enableAllButton(false);
        //TODO send message
        mAlreadySelected = false;
        mRemainTime = NUM_TIME_SET;
        mHandler.sendEmptyMessage(0);
    }

    private void enableAllButton(boolean enable) {
        for (PlayerData player : mPlayers) {
            player.btnSet.setEnabled(enable);
            player.btnComplete.setEnabled(enable);
        }
    }

    private void reduceTime() {
        if (mAlreadySelected) {
            //do nothing
            return;
        }

        if (mRemainTime <= 0) { //fail to find
            if (mNowFlagedPlayer != null) {
                mNowFlagedPlayer.textRemainTime.setText("");
            }
            mNowGameState = GAME_STATE_READY;
            renewViews();
        } else {
            if (mNowGameState == GAME_STATE_CALLED_SET
                    || mNowGameState == GAME_STATE_CHANCE_COMPLETE) {
                if (mNowFlagedPlayer != null) {
                    mNowFlagedPlayer.textRemainTime.setText("" + mRemainTime);
                }
            } else {
                Log.e("SET2", "invalid state!");
                return;
            }
            mRemainTime--;
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    class PlayerData {
        public Button btnName;
        public TextView textName;
        public TextView textRemainTime;
        public TextView textScore;
        public TextView textScorePlus;
        public Button btnSet;
        public Button btnComplete;

        public String name;
        public int score;
        public int color;
    }

    //TODO using weakreference
    class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            reduceTime();
        }
    }
}
