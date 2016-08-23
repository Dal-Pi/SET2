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
import android.widget.TextView;

import com.kania.set2.R;
import com.kania.set2.model.SetItemData;
import com.kania.set2.model.SetVerifier;
import com.kania.set2.util.RandomNumberUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by user on 2016-08-22.
 */

public class VsModeActivity extends AppCompatActivity implements View.OnClickListener,
        NineCardFragment.OnSelectThreeCardListener {

    private static final int NUM_ALL_CARDS = 9;
    private static final int NUM_ANS_CARDS = 3;
    private static final int NUM_MAX_STAGE = 10;
    private static final int NUM_TIME_SET = 5;
    private static final int NUM_TIME_COMPLETE = 5;
    private static final int NUM_MAX_PLAYER = 2;
    private static final int NUM_SCORE_SET_SUCCEED = 1;
    private static final int NUM_SCORE_SET_FAIL = -1;
    private static final int NUM_SCORE_COMPLETE_SUCCEED = 3;
    private static final int NUM_SCORE_COMPLETE_FAIL = -1;

    private static final int MESSAGE_WHAT_SET = 1;
    private static final int MESSAGE_WHAT_COMPLETE = 2;

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
    private SetHandler mSetTimerHandler;


    //game data
    private int mStage;
    private int mRemainSetTime;
    private int mRemainCompleteTime;

    private ArrayList<SetItemData> mAllItemList;
    private int[] mAllItemListSequence;
    private HashMap<String, SetAnswerData> mAnswerMap;
    private ArrayList<SetItemData> mDeckList;
    private ArrayList<Integer> mSelectedPositionList;
    private ArrayList<Integer> mSavedSelectedPositionList;
    private int mNowGameState;
    private ArrayList<PlayerData> mPlayers;
    private PlayerData mNowFlagedPlayer;

    //fragments
    private AnswerImageFragment mAnswerImageFragment;
    private NineCardFragment mNineCardFragment;

    //layout
    private ViewGroup mResultLayout;
    private Button mBtnStart;
    private TextView mTextTitle;
    private TextView mTextHelp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vs);
        addFragment();

        mSetTimerHandler = new SetHandler();

        getComponents();
        initCards();
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
    protected void onDestroy() {
        super.onDestroy();
        mSetTimerHandler.removeMessages(MESSAGE_WHAT_SET);
        mSetTimerHandler.removeMessages(MESSAGE_WHAT_COMPLETE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.vs_btn_start:
                mStage = 0;
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
            case R.id.vs_player_btn_complete_1:
                mNowFlagedPlayer = mPlayers.get(0);
                completeCalled();
                break;
            case R.id.vs_player_btn_complete_2:
                mNowFlagedPlayer = mPlayers.get(1);
                completeCalled();
                break;
            //easter egg
            case R.id.vs_text_title:
                mTextHelp.setText(getAnswerInfoString());
                break;
        }
    }

    @Override
    public void onThreeCardSelected(ArrayList<SetItemData> selectedList) {
        mSetTimerHandler.removeMessages(MESSAGE_WHAT_SET);
        if (checkAnswer(selectedList)) {
            succeedToFindSet();
        } else {
            failToFindSet();
        }
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
        Log.d("SET", "selected : " + mSelectedPositionList.toString());
    }

    private boolean checkAnswer(ArrayList<SetItemData> candidates) {
        //TODO verifying
        if (candidates.size() != NUM_ANS_CARDS) {
            return false;
        }
        SetAnswerData candidate = new SetAnswerData(mSelectedPositionList.get(0),
                mSelectedPositionList.get(1), mSelectedPositionList.get(2));
        SetAnswerData target = mAnswerMap.get(candidate.toString());
        mSelectedPositionList.clear();
        if (target != null && !target.isSelected) {
            target.isSelected = true;
            return true;
        } else { //alreay found
            return false;
        }
    }

    private void succeedToFindSet() {
        addScore(NUM_SCORE_SET_SUCCEED);
        setNotiImage(true);
        mNowGameState = GAME_STATE_CHANCE_COMPLETE;
        renewViews();
    }

    private void failToFindSet() {
        addScore(NUM_SCORE_SET_FAIL);
        setNotiImage(false);
        mNowGameState = GAME_STATE_READY;
        renewViews();
    }

    private void succeedToComplete() {
        addScore(NUM_SCORE_COMPLETE_SUCCEED);
        setNotiImage(true);

        startNewStage();
        mNowGameState = GAME_STATE_READY;
        renewViews();
    }

    private void failToComplete() {
        addScore(NUM_SCORE_COMPLETE_FAIL);
        setNotiImage(false);
        mNowGameState = GAME_STATE_READY;
        renewViews();
    }

    private void addScore(int getScore) {
        if (mNowFlagedPlayer != null) {
            mNowFlagedPlayer.score += getScore;
            mNowFlagedPlayer.plusScore = getScore;
        }
        printScore();
    }

    public void setNotiImage(boolean status) {
        mAnswerImageFragment.setNotiImage(status);
        crossfadeAddedScore();
    }

    private void crossfadeAddedScore() {
        mNowFlagedPlayer.textScorePlus.setAlpha(1f);
        mNowFlagedPlayer.textScorePlus.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null);
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

    private void getComponents() {
        mTextTitle = (TextView)findViewById(R.id.vs_text_title);
        //easter egg start
        mTextHelp = (TextView)findViewById(R.id.vs_text_info);
        mTextTitle.setOnClickListener(this);
        //easter egg end
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

    private void initCards() {
        mAllItemList = new ArrayList<>();
        for(int color = 0; color < NUM_ANS_CARDS; ++color) {
            for (int shape = 0; shape < NUM_ANS_CARDS; ++shape) {
                for (int fill = 0; fill < NUM_ANS_CARDS; ++fill) {
                    //not yet
                    //for (int amount = 0; amount < 3; ++amount) {}
                    mAllItemList.add(new SetItemData(color, shape, fill, 0));
                }
            }
        }
        mSelectedPositionList = new ArrayList<>();
        mAnswerMap = new HashMap<>();
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
        startNewStage();
        mNowGameState = GAME_STATE_READY;
        renewViews();
    }

    private void renewViews() {
        if (mNowGameState == GAME_STATE_PREPARE) {
            setPrepareState();
        } else if (mNowGameState == GAME_STATE_READY) {
            setReadyState();
        } else if (mNowGameState == GAME_STATE_CALLED_SET) {
            printAnswerInfo();
            setCalledSetState();
        } else if (mNowGameState == GAME_STATE_CHANCE_COMPLETE) {
            printAnswerInfo();
            setChanceCompleteState();
        } else if (mNowGameState == GAME_STATE_FINISH) {
            //TODO
        } else {
            Log.e("SET2", "invalid state!");
        }
    }

    private void startNewStage() {
        //pick nine cards
        Calendar calendar = Calendar.getInstance();
        mAllItemListSequence = RandomNumberUtil.getInstance(calendar.getTimeInMillis())
                .getRandomNumberSet(mAllItemList.size()); //3*3*3*1
        if (mDeckList == null) {
            mDeckList = new ArrayList<>();
        } else {
            mDeckList.clear();
        }
        for (int i = 0; i < NUM_ALL_CARDS; ++i) {
            mDeckList.add(mAllItemList.get(mAllItemListSequence[i]));
        }
        //get every answer
        if (mAnswerMap == null) {
            mAnswerMap = new HashMap<>();
        } else {
            mAnswerMap.clear();
        }
        for (int i = 0; i < mDeckList.size()-2; ++i) {
            for (int j = i + 1; j < mDeckList.size() - 1; ++j) {
                for (int k = j + 1; k < mDeckList.size(); ++k) {
                    ArrayList<SetItemData> candidates = new ArrayList<>();
                    candidates.add(mDeckList.get(i));
                    candidates.add(mDeckList.get(j));
                    candidates.add(mDeckList.get(k));
                    if (SetVerifier.isValidSet(candidates)) {
                        SetAnswerData answer = new SetAnswerData(i, j, k);
                        mAnswerMap.put(answer.toString(), answer);
                    }
                }
            }
        }
        printAnswerInfo();
        mStage++;
        //TODO change title like (3/10)
        mTextTitle.setText(getResources().getString(R.string.vs_text_stage) + mStage);
        mNineCardFragment.setCards(mDeckList);
    }

    //for test
    private void printAnswerInfo() {
        //debug
        Log.d("SET2", "mAnswerMap size = " + mAnswerMap.size());
        Log.d("SET2", getAnswerInfoString());
    }

    private String getAnswerInfoString() {
        Iterator<String> it = mAnswerMap.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            SetAnswerData data = mAnswerMap.get(it.next());
            sb.append(data.toString()).append("[").append(data.isSelected).append("]")
                    .append(" / ");
        }
        return sb.toString();
    }

    private void setPrepareState() {
        Log.d("SET2", "[state] setPrepareState");
        mBtnStart.setVisibility(View.VISIBLE);
        for (PlayerData player : mPlayers) {
            player.btnName.setVisibility(View.VISIBLE);
            player.btnName.setEnabled(true);
            player.textName.setVisibility(View.INVISIBLE);
            player.textName.setText(player.btnName.getText());
            player.btnSet.setEnabled(false);
            player.btnComplete.setEnabled(false);
        }
    }

    private void setReadyState() {
        Log.d("SET2", "[state] setReadyState");
        mNineCardFragment.unselectAllCard();
        mSelectedPositionList.clear();
        mNineCardFragment.setClickable(false);
        enableAllButton(true);
        for (PlayerData player : mPlayers) {
            player.textRemainTime.setText("");
        }
    }

    private void setCalledSetState(){
        Log.d("SET2", "[state] setCalledSetState");
        enableAllButton(false);
        mRemainSetTime = NUM_TIME_SET;
        mSetTimerHandler.sendEmptyMessage(MESSAGE_WHAT_SET);
        mNineCardFragment.setClickable(true);
    }

    private void setChanceCompleteState() {
        Log.d("SET2", "[state] setChanceCompleteState");
        mNineCardFragment.unselectAllCard();
        mSelectedPositionList.clear();
        mNineCardFragment.setClickable(false);
        enableAllButton(false);
        mNowFlagedPlayer.btnComplete.setEnabled(true);
        mRemainCompleteTime = NUM_TIME_COMPLETE;
        mSetTimerHandler.sendEmptyMessage(MESSAGE_WHAT_COMPLETE);
    }

    private void printScore() {
        mNowFlagedPlayer.textScore.setText("" + mNowFlagedPlayer.score);
        mNowFlagedPlayer.textScorePlus.setText(mNowFlagedPlayer.plusScore > 0 ?
                "+" + mNowFlagedPlayer.plusScore : "" + mNowFlagedPlayer.plusScore);
    }

    private void enableAllButton(boolean enable) {
        for (PlayerData player : mPlayers) {
            player.btnSet.setEnabled(enable);
            player.btnComplete.setEnabled(enable);
        }
    }

    private void reduceSetTime() {

        if (mRemainSetTime <= 0) { //fail to find
            failToFindSet();
        } else {
            if (mNowGameState == GAME_STATE_CALLED_SET) {
                if (mNowFlagedPlayer != null) {
                    mNowFlagedPlayer.textRemainTime.setText("" + mRemainSetTime);
                }
            } else {
                Log.e("SET2", "invalid state!");
                return;
            }
            mRemainSetTime--;
            mSetTimerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT_SET, 1000);
        }
    }

    private void reduceCompleteTime() {

        if (mRemainCompleteTime <= 0) { //fail to find
            mNowGameState = GAME_STATE_READY;
            renewViews();
        } else {
            if (mNowGameState == GAME_STATE_CHANCE_COMPLETE) {
                if (mNowFlagedPlayer != null) {
                    mNowFlagedPlayer.textRemainTime.setText("" + mRemainCompleteTime);
                }
            } else {
                Log.e("SET2", "invalid state!");
                return;
            }
            mRemainCompleteTime--;
            mSetTimerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT_COMPLETE, 1000);
        }
    }

    private void completeCalled() {
        mSetTimerHandler.removeMessages(MESSAGE_WHAT_COMPLETE);
        if (isNowComplete()) {
            succeedToComplete();
        } else {
            failToComplete();
        }
    }

    private boolean isNowComplete() {
        boolean ret = true;
        Iterator<String> it = mAnswerMap.keySet().iterator();
        while (it.hasNext()) {
            if (!mAnswerMap.get(it.next()).isSelected) {
                ret = false;
            }
        }
        return ret;
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
        public int plusScore;
        public int color;
    }

    class SetAnswerData {
        public int first;
        public int second;
        public int third;

        public boolean isSelected;

        public SetAnswerData(int a, int b, int c) {
            this.first = a;
            this.second = b;
            this.third = c;
            int temp;
            //TODO need to test
            if (first > second) {
                temp = second;
                second = first;
                first = temp;
            }
            if (second > third) {
                temp = third;
                third = second;
                second = temp;
            }
            if (first > second) {
                temp = second;
                second = first;
                first = temp;
            }
            this.isSelected = false;
        }

        @Override
        public String toString() {
            StringBuffer ret = new StringBuffer();
            return ret.append(this.first + 1).append(this.second + 1).append(this.third + 1)
                    .toString();
        }
    }

    //TODO using weakreference
    class SetHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_WHAT_SET) {
                reduceSetTime();
            } else if (msg.what == MESSAGE_WHAT_COMPLETE) {
                reduceCompleteTime();
            }
        }
    }
}
