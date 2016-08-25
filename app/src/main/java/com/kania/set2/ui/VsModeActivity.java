package com.kania.set2.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kania.set2.R;
import com.kania.set2.model.SetItemData;
import com.kania.set2.model.SetVerifier;
import com.kania.set2.util.RandomNumberUtil;
import com.kania.set2.util.ViewUtil;

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
    private static final int NUM_TIME_COMPLETE = 2;
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
    private ArrayList<Integer> mSelectedPositionList = new ArrayList<>(); //TODO why?
    private ArrayList<Integer> mSavedSelectedPositionList;
    private int mNowGameState;
    private ArrayList<PlayerData> mPlayers;
    private PlayerData mNowFlagedPlayer;
    private ArrayList<SetAnswerData> mSelectedAnswerList;

    //fragments
    private AnswerImageFragment mAnswerImageFragment;
    private NineCardFragment mNineCardFragment;

    //layout
    private ViewGroup mResultLayout;
    private Button mBtnStart;
    private TextView mTextTitle;
    private TextView mTextSelectedList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vs);
        addFragment();

        mSetTimerHandler = new SetHandler();

        initViews();
        initCards();
        if (savedInstanceState == null) {
            initGameData();
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
            case R.id.vs_player_btn_name_1:
                mNowFlagedPlayer = mPlayers.get(0);
                changeName();
                break;
            case R.id.vs_player_btn_name_2:
                mNowFlagedPlayer = mPlayers.get(1);
                changeName();
                break;
            //easter egg
            case R.id.vs_text_title:
                mTextSelectedList.setText(getAnswerInfoString());
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
            mSelectedAnswerList.add(target);
            return true;
        } else { //alreay found
            return false;
        }
    }

    private void succeedToFindSet() {
        addScore(NUM_SCORE_SET_SUCCEED);
        setNotiImage(true);
        printSelectedAnswerList();
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

        if (mStage < NUM_MAX_STAGE) {
            startNewStage();
            mNowGameState = GAME_STATE_READY;
            renewViews();
        } else {
            showResultDialog();
        }
    }

    private void showResultDialog() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.dialog_vs_result, null);
        TextView textWinner = (TextView)layout.findViewById(R.id.dialog_vs_text_winner);
        TextView textPlayer1Name = (TextView)layout.findViewById(R.id.dialog_vs_text_name_1);
        TextView textPlayer1Score = (TextView)layout.findViewById(R.id.dialog_vs_text_score_1);
        TextView textPlayer2Name = (TextView)layout.findViewById(R.id.dialog_vs_text_name_2);
        TextView textPlayer2Score = (TextView)layout.findViewById(R.id.dialog_vs_text_score_2);
        textPlayer1Name.setText(mPlayers.get(0).name);
        textPlayer1Name.setTextColor(mPlayers.get(0).color);
        textPlayer1Score.setText("" + mPlayers.get(0).score);
        textPlayer1Score.setTextColor(mPlayers.get(0).color);
        textPlayer2Name.setText(mPlayers.get(1).name);
        textPlayer2Name.setTextColor(mPlayers.get(1).color);
        textPlayer2Score.setText("" + mPlayers.get(1).score);
        textPlayer2Score.setTextColor(mPlayers.get(1).color);
        String winnerString;
        int winnerColor;
        if (mPlayers.get(0).score > mPlayers.get(1).score) {
            winnerString = mPlayers.get(0).name + " "
                    + getResources().getString(R.string.vs_text_dialog_win_postfix);
            winnerColor = mPlayers.get(0).color;
        } else if (mPlayers.get(0).score < mPlayers.get(1).score) {
            winnerString = mPlayers.get(1).name + " "
                    + getResources().getString(R.string.vs_text_dialog_win_postfix);
            winnerColor = mPlayers.get(1).color;
        } else {
            winnerString = getResources().getString(R.string.vs_text_dialog_draw);
            winnerColor = getResources().getColor(R.color.colorAccent);
        }
        textWinner.setText(winnerString);
        textWinner.setTextColor(winnerColor);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.vs_text_dialog_title))
                .setView(layout)
                .setPositiveButton(getResources().getString(R.string.text_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                .setCancelable(false);
        builder.show();
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
            printScore(mNowFlagedPlayer);
        }
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

    private void initViews() {
        mTextTitle = (TextView)findViewById(R.id.vs_text_title);
        //easter egg start
        mTextSelectedList = (TextView)findViewById(R.id.vs_text_info);
        mTextTitle.setOnClickListener(this);
        //easter egg end
        mBtnStart = (Button)findViewById(R.id.vs_btn_start);
        mBtnStart.setOnClickListener(this);
        ViewUtil.setButtonColor(mBtnStart, getResources().getColor(R.color.colorAccent));


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
    }

    private void initGameData() {
        int[] colors = getResources().getIntArray(R.array.controlColors);
        Calendar calendar = Calendar.getInstance();
        int[] randomIndexs = RandomNumberUtil.getInstance(calendar.getTimeInMillis())
                .getRandomNumberSet(colors.length);
        for (int i = 0; i < mPlayers.size(); ++i) {
            PlayerData player = mPlayers.get(i);
            player.name = getResources().getString(R.string.vs_text_player) + (i + 1);
            player.score = 0;
            player.color = colors[randomIndexs[i]];
            player.textName.setTextColor(player.color);
            player.btnName.setText(player.name);
            ViewUtil.setButtonColor(player.btnName, player.color);
//            ViewUtil.setButtonColor(player.btnSet,
//                    getResources().getColor(R.color.base_darkgray));
//            ViewUtil.setButtonColor(player.btnComplete,
//                    getResources().getColor(R.color.base_darkgray));
        }
        mSelectedPositionList = new ArrayList<>();
        mAnswerMap = new HashMap<>();
        mSelectedAnswerList = new ArrayList<>();
    }

    private void initGameState() {
        mNowGameState = GAME_STATE_PREPARE;
        renewViews();
    }

    private void startGame() {
        mBtnStart.setVisibility(View.GONE);
        for (PlayerData player : mPlayers) {
            player.name = player.btnName.getText().toString();
            player.btnName.setEnabled(false);
            player.btnName.setVisibility(View.GONE);
            player.textName.setVisibility(View.VISIBLE);
            player.textName.setText(player.name);
            player.textRemainTime.setVisibility(View.VISIBLE);
            player.textScore.setVisibility(View.VISIBLE);
            player.textScore.setText(getResources().getString(R.string.vs_text_score)
                    + " : " + player.score);
            player.btnSet.setVisibility(View.VISIBLE);
            player.btnSet.setEnabled(true);
            player.btnComplete.setVisibility(View.VISIBLE);
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
        if (mSelectedAnswerList == null) {
            Log.e("SET2", "error! mSelectedAnswerList is null");
            mSelectedAnswerList = new ArrayList<>();
        } else {
            mSelectedAnswerList.clear();
        }
        mTextSelectedList.setText("");
        mStage++;
        //TODO change title like (3/10)
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(getResources().getString(R.string.vs_text_stage)).append("(")
                .append(mStage).append("/").append(NUM_MAX_STAGE).append(")");
        mTextTitle.setText(titleBuilder.toString());
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
            player.textName.setVisibility(View.GONE);
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

    private void printScore(PlayerData player) {
        player.textScore.setText(getResources().getString(R.string.vs_text_score)
                + " : " + player.score);
        player.textScorePlus.setVisibility(View.VISIBLE);
        player.textScorePlus.setText(player.plusScore > 0 ?
                "+" + player.plusScore : "" + player.plusScore);
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

    private void changeName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editName = new EditText(this);
        editName.setHint(getResources().getString(R.string.vs_text_player_hint));
        editName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(editName)
                .setTitle(getResources().getString(R.string.vs_text_player_dialog_title))
                .setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mNowFlagedPlayer != null) {
                            String name = editName.getText().toString();
                            if (name == null || "".equalsIgnoreCase(name)) {
                                name = getResources().getString(R.string.vs_text_player);
                            }
                            mNowFlagedPlayer.textName.setText(name);
                            mNowFlagedPlayer.btnName.setText(name);
                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.text_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
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

    private void printSelectedAnswerList() {
        StringBuilder stringBuilder = new StringBuilder();
        for (SetAnswerData answer : mSelectedAnswerList) {
            stringBuilder.append(answer.toString()).append("  ");
        }
        mTextSelectedList.setText(stringBuilder.toString());
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
