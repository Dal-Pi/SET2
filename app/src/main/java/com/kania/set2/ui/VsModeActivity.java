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
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

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

    public static final String KEY_STATE = "saved_state";
    public static final String KEY_SELECT_PLAYER_INDEX = "saved_select_player_index";
    public static final String KEY_PLAYER_NAME_PREFIX = "saved_player_name_";
    public static final String KEY_PLAYER_SCORE_PREFIX = "saved_player_score_";
    public static final String KEY_REMAIN_SET = "saved_remain_set";
    public static final String KEY_REMAIN_COMPLETE = "saved_remain_complete";

    public static final String KEY_DECK_LIST = "saved_deck_list";
    public static final String KEY_SELECTED_LIST = "saved_selected_list";
    public static final String KEY_ANSWER_MAP = "saved_answer_list";
    public static final String KEY_STAGE_NUM = "saved_stage_number";

    public static final String DELIMETER = "/";

    public static final int ANIMATION_DURATION = 2000;

    public static final int GAME_STATE_PREPARE = 1;
    public static final int GAME_STATE_READY = 2;
    public static final int GAME_STATE_CALLED_SET = 3;
    public static final int GAME_STATE_CHANCE_COMPLETE = 4;

    public static final int GAME_STATE_PASS = 5;
    public static final int GAME_STATE_END_GAME = 6;

    //utils
    private SetHandler mSetTimerHandler;
    private boolean isSaved = false;


    //game data
    private int mStage;
    private int mRemainSetTime;
    private int mRemainCompleteTime;

    private Vector<SetItemData> mAllItemList;
    private int[] mAllItemListSequence;
    private HashMap<String, SetAnswerData> mAnswerMap;
    private Vector<SetItemData> mDeckList;
    private Vector<Integer> mSelectedPositionList = new Vector<>();
    private Vector<Integer> mSavedSelectedPositionList;
    private int mNowGameState;
    private Vector<PlayerData> mPlayers;
    private PlayerData mNowFlagedPlayer;

    //fragments
    private AnswerImageFragment mAnswerImageFragment;
    private NineCardFragment mNineCardFragment;

    //layout
    private ViewGroup mSelectedLayout;
    private Button mBtnStart;
    private Button mBtnPass;
    private TextView mTextTitle;
    private TextView mTextSelectedList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vs);
        addNineCardFragment();
        addAnswerImageFragment();

        mSetTimerHandler = new SetHandler();

        initViews();
        initCards();
        initGameData();
        if (savedInstanceState != null) {
            loadPlayerData(savedInstanceState);
            //game will resume when onResume called.
        } else {
            mNowGameState = GAME_STATE_PREPARE;
            renewViews();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeGame();
    }

    private void loadPlayerData(Bundle savedInstanceState) {
        //load start
        mNowGameState = savedInstanceState.getInt(KEY_STATE);
        for (int i = 0; i < mPlayers.size(); ++i) {
            mPlayers.get(i).name = savedInstanceState.getString(KEY_PLAYER_NAME_PREFIX + i);
            mPlayers.get(i).btnName.setText(mPlayers.get(i).name);
            mPlayers.get(i).score = savedInstanceState.getInt(KEY_PLAYER_SCORE_PREFIX + i);
        }

        if (mNowGameState == GAME_STATE_CALLED_SET) {
            mNowFlagedPlayer = mPlayers
                    .get(savedInstanceState.getInt(KEY_SELECT_PLAYER_INDEX));
            mRemainSetTime = savedInstanceState.getInt(KEY_REMAIN_SET, NUM_TIME_SET);
        } else if (mNowGameState == GAME_STATE_CHANCE_COMPLETE) {
            mNowFlagedPlayer = mPlayers
                    .get(savedInstanceState.getInt(KEY_SELECT_PLAYER_INDEX));
            mRemainCompleteTime = savedInstanceState
                    .getInt(KEY_REMAIN_COMPLETE, NUM_TIME_COMPLETE);
        }

        if (mNowGameState != GAME_STATE_PREPARE) {
            mStage = savedInstanceState.getInt(KEY_STAGE_NUM, 0);
            restoreDeckList(savedInstanceState.getString(KEY_DECK_LIST));
            restoreSelected(savedInstanceState.getString(KEY_SELECTED_LIST));
            restoreAnswerMap(savedInstanceState.getString(KEY_ANSWER_MAP));
            isSaved = true;
        }
        //load end
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_STATE, mNowGameState);
        for (int i = 0; i < mPlayers.size(); ++i) {
            outState.putString(KEY_PLAYER_NAME_PREFIX + i, mPlayers.get(i).name);
            outState.putInt(KEY_PLAYER_SCORE_PREFIX + i, mPlayers.get(i).score);
        }

        if (mNowGameState == GAME_STATE_CALLED_SET) {
            outState.putInt(KEY_SELECT_PLAYER_INDEX, mPlayers.indexOf(mNowFlagedPlayer));
            outState.putInt(KEY_REMAIN_SET, mRemainSetTime);
        } else if (mNowGameState == GAME_STATE_CHANCE_COMPLETE) {
            outState.putInt(KEY_SELECT_PLAYER_INDEX, mPlayers.indexOf(mNowFlagedPlayer));
            outState.putInt(KEY_REMAIN_COMPLETE, mRemainCompleteTime);
        }

        if (mNowGameState != GAME_STATE_PREPARE) {
            outState.putInt(KEY_STAGE_NUM, mStage);
            outState.putString(KEY_DECK_LIST, backupDeckList());
            outState.putString(KEY_SELECTED_LIST, backupSelected());
            outState.putString(KEY_ANSWER_MAP, backupAnswerMap());
        }
    }

    private String backupDeckList() {
        StringBuilder sb = new StringBuilder();
        for (SetItemData data : mDeckList) {
            sb.append(data.toString()).append(DELIMETER);
        }
        return sb.toString();
    }
    private void restoreDeckList(String deckListString) {
        mDeckList = new Vector<>();
        if(deckListString == null || "".equalsIgnoreCase(deckListString)) {
            return;
        }
        String[] items = deckListString.split(DELIMETER);
        for (int i = 0; i < NUM_ALL_CARDS; ++i) {
            mDeckList.add(new SetItemData(
                    Integer.parseInt(items[i].substring(0, 1)),
                    Integer.parseInt(items[i].substring(1, 2)),
                    Integer.parseInt(items[i].substring(2, 3)),
                    Integer.parseInt(items[i].substring(3, 4))));
        }
    }

    private String backupSelected() {
        StringBuilder sb = new StringBuilder();
        for (int pos : mSelectedPositionList) {
            sb.append("" + pos).append(DELIMETER);
        }
        return sb.toString();
    }
    private void restoreSelected(String selectedListString) {
        mSavedSelectedPositionList = new Vector<>();
        if(selectedListString == null || "".equalsIgnoreCase(selectedListString)) {
            return;
        }
        String[] positions = selectedListString.split(DELIMETER);
        for (int i = 0; i < positions.length; ++i) {
            mSavedSelectedPositionList.add(Integer.parseInt(positions[i]));
        }
    }

    private String backupAnswerMap() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = mAnswerMap.keySet().iterator();
        while (it.hasNext()) {
            sb.append(mAnswerMap.get(it.next()).toStringWithSelected()).append(DELIMETER);
        }
        return sb.toString();
    }
    private void restoreAnswerMap(String answerMapString) {
        mAnswerMap = new HashMap<>();
        if(answerMapString == null || "".equalsIgnoreCase(answerMapString)) {
            return;
        }
        String[] answers = answerMapString.split(DELIMETER);
        for (int i = 0; i < answers.length; ++i) {
            SetAnswerData answer = new SetAnswerData(
                    Integer.parseInt(answers[i].substring(0, 1)),
                    Integer.parseInt(answers[i].substring(1, 2)),
                    Integer.parseInt(answers[i].substring(2, 3)),
                    "T".equalsIgnoreCase(answers[i].substring(3, 4)) ? true : false);
            mAnswerMap.put(answer.toString(), answer);
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
                mRemainSetTime = NUM_TIME_SET;
                renewViews();
                break;
            case R.id.vs_player_btn_set_2:
                mNowFlagedPlayer = mPlayers.get(1);
                mNowGameState = GAME_STATE_CALLED_SET;
                mRemainSetTime = NUM_TIME_SET;
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
            case R.id.vs_btn_pass:
                callPass();
                break;
        }
    }

    @Override
    public void onThreeCardSelected(Vector<SetItemData> selectedList) {
        mSetTimerHandler.removeMessages(MESSAGE_WHAT_SET);
        if (checkAnswer(selectedList)) {
            succeedToFindSet();
        } else {
            failToFindSet();
        }
        mSelectedPositionList.clear();
    }

    @Override
    public void onSelectCard(int position) {
        if (mSelectedPositionList == null) {
            mSelectedPositionList = new Vector<>();
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

    private boolean checkAnswer(Vector<SetItemData> candidates) {
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
        printSelectedAnswerList();
        mRemainCompleteTime = NUM_TIME_COMPLETE;
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
        goNextStage();
    }

    private void goNextStage() {
        if (mStage < NUM_MAX_STAGE) {
            startNewStage();
            mNowGameState = GAME_STATE_READY;
        } else {
            mNowGameState = GAME_STATE_END_GAME;
        }
        renewViews();
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
                    + getString(R.string.vs_text_dialog_win_postfix);
            winnerColor = mPlayers.get(0).color;
        } else if (mPlayers.get(0).score < mPlayers.get(1).score) {
            winnerString = mPlayers.get(1).name + " "
                    + getString(R.string.vs_text_dialog_win_postfix);
            winnerColor = mPlayers.get(1).color;
        } else {
            winnerString = getString(R.string.vs_text_dialog_draw);
            winnerColor = getResources().getColor(R.color.colorAccent);
        }
        textWinner.setText(winnerString);
        textWinner.setTextColor(winnerColor);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.vs_text_dialog_title))
                .setView(layout)
                .setPositiveButton(getString(R.string.text_ok),
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

    private void addNineCardFragment() {
        //TODO expend card type
        mNineCardFragment = NineCardFragment
                .newInstance(NineCardFragment.CARD_TYPE_FILL_AS_PATTERN, true, this);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.vs_container_ninecard, mNineCardFragment,
                NineCardFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    private void addAnswerImageFragment() {
        mAnswerImageFragment = AnswerImageFragment.newInstance();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.vs_container_answer, mAnswerImageFragment);
        fragmentTransaction.commit();
    }

    private void initViews() {
        mTextTitle = (TextView)findViewById(R.id.vs_text_title);
        mSelectedLayout = (ViewGroup)findViewById(R.id.vs_layout_ninecard);
        mTextSelectedList = (TextView)findViewById(R.id.vs_text_info);
        //easter egg end
        mBtnStart = (Button)findViewById(R.id.vs_btn_start);
        mBtnStart.setOnClickListener(this);
        ViewUtil.setButtonColor(mBtnStart, getResources().getColor(R.color.colorAccent));
        mBtnPass = (Button)findViewById(R.id.vs_btn_pass);
        mBtnPass.setOnClickListener(this);

        mPlayers = new Vector<>();
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
        mAllItemList = new Vector<>();
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
        int[] colors = getResources().getIntArray(R.array.pointColors);
        Calendar calendar = Calendar.getInstance();
        Log.d("SET2", "colors = " + colors.toString());
        int[] randomIndexs = RandomNumberUtil.getInstance(calendar.getTimeInMillis())
                .getRandomNumberSet(colors.length);
        Log.d("SET2", "randomIndexs = " + randomIndexs.toString());
        for (int i = 0; i < mPlayers.size(); ++i) {
            PlayerData player = mPlayers.get(i);
            player.name = getString(R.string.vs_text_player) + (i + 1);
            player.score = 0;
            player.color = colors[randomIndexs[i]];
            player.textName.setTextColor(player.color);
            player.btnName.setText(player.name);
            ViewUtil.setButtonColor(player.btnName, player.color);
        }
        mSelectedPositionList = new Vector<>();
        mAnswerMap = new HashMap<>();
    }

    private void startGame() {
        changeViewModeAsStart();
        startNewStage();
        mNowGameState = GAME_STATE_READY;
        renewViews();
    }

    private void resumeGame() {
        if (isSaved) {
            if (mNowGameState != GAME_STATE_PREPARE) {
                changeViewModeAsStart();
                mNineCardFragment.setCards(mDeckList);
                mNineCardFragment.selectCard(mSavedSelectedPositionList);
                printStageTitle();
                printSelectedAnswerList();
            }
            isSaved = false;
        }
        renewViews();
    }

    private void changeViewModeAsStart() {
        mBtnStart.setVisibility(View.GONE);
        for (PlayerData player : mPlayers) {
            player.name = player.btnName.getText().toString();
            player.btnName.setEnabled(false);
            player.btnName.setVisibility(View.GONE);
            player.textName.setVisibility(View.VISIBLE);
            player.textName.setText(player.name);
            player.textRemainTime.setVisibility(View.VISIBLE);
            player.textScore.setVisibility(View.VISIBLE);
            player.textScore.setText(getString(R.string.vs_text_score) + " : " + player.score);
            player.btnSet.setVisibility(View.VISIBLE);
            player.btnSet.setEnabled(true);
            player.btnComplete.setVisibility(View.VISIBLE);
            player.btnComplete.setEnabled(true);
        }
    }

    private void renewViews() {
        if (mNowGameState == GAME_STATE_PREPARE) {
            setPrepareState();
        } else if (mNowGameState == GAME_STATE_READY) {
            setReadyState();
        } else if (mNowGameState == GAME_STATE_CALLED_SET) {
            setCalledSetState();
        } else if (mNowGameState == GAME_STATE_CHANCE_COMPLETE) {
            setChanceCompleteState();
        } else if (mNowGameState == GAME_STATE_PASS) {
            setPassState();
        } else if (mNowGameState == GAME_STATE_END_GAME) {
            setEndState();
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
            mDeckList = new Vector<>();
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
                    Vector<SetItemData> candidates = new Vector<>();
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
        mTextSelectedList.setText("");
        mStage++;
        printStageTitle();
        mNineCardFragment.setCards(mDeckList);
    }

    private void printStageTitle() {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(getString(R.string.vs_text_stage)).append("(")
                .append(mStage).append("/").append(NUM_MAX_STAGE).append(")");
        mTextTitle.setText(titleBuilder.toString());
    }

    private SpannableStringBuilder getAnswerInfoString() {
        Iterator<String> it = mAnswerMap.keySet().iterator();
        SpannableStringBuilder coloredString = new SpannableStringBuilder();
        while (it.hasNext()) {
            SetAnswerData data = mAnswerMap.get(it.next());
            String answer = data.toStringWithPlus1();
            int start = coloredString.length();
            int end = start + answer.length();
            coloredString.append(answer).append(" ");
            if (!data.isSelected) {
                coloredString.setSpan(
                        new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return coloredString;
    }

    //state methods [start]
    private void setPrepareState() {
        Log.d("SET2", "[state] setPrepareState");
        mBtnStart.setVisibility(View.VISIBLE);
        mBtnPass.setVisibility(View.INVISIBLE);
        enablePlayerButton(false);
        for (PlayerData player : mPlayers) {
            player.textRemainTime.setVisibility(View.INVISIBLE);
            player.btnSet.setVisibility(View.INVISIBLE);
            player.btnComplete.setVisibility(View.INVISIBLE);
            player.textScore.setVisibility(View.INVISIBLE);
            player.textScorePlus.setVisibility(View.INVISIBLE);
            player.btnName.setVisibility(View.VISIBLE);
            player.textName.setVisibility(View.GONE);
            player.textName.setText(player.btnName.getText());
        }
    }

    private void setReadyState() {
        Log.d("SET2", "[state] setReadyState");
        mBtnPass.setVisibility(View.VISIBLE);
        mBtnPass.setText(getString(R.string.vs_btn_pass));
        enablePassButton(true);
        mNineCardFragment.unselectAllCard();
        mNineCardFragment.setClickable(false);
        mSelectedLayout.setBackgroundColor(getResources().getColor(R.color.base_white));
        enablePlayerButton(true);
        for (PlayerData player : mPlayers) {
            player.textRemainTime.setText("");
        }
        mSelectedPositionList.clear();
    }

    private void setCalledSetState(){
        Log.d("SET2", "[state] setCalledSetState");
        enablePassButton(false);
        mNineCardFragment.setClickable(true);
        mSelectedLayout.setBackgroundColor(mNowFlagedPlayer.color);
        enablePlayerButton(false);
        mSetTimerHandler.sendEmptyMessage(MESSAGE_WHAT_SET);
    }

    private void setChanceCompleteState() {
        Log.d("SET2", "[state] setChanceCompleteState");
        enablePassButton(false);
        mNineCardFragment.unselectAllCard();
        mNineCardFragment.setClickable(false);
        mSelectedLayout.setBackgroundColor(getResources().getColor(R.color.base_white));
        enablePlayerButton(false);
        mNowFlagedPlayer.btnComplete.setEnabled(true);
        ViewUtil.setButtonColor(mNowFlagedPlayer.btnComplete, mNowFlagedPlayer.color);
        mSetTimerHandler.sendEmptyMessage(MESSAGE_WHAT_COMPLETE);
    }

    private void setPassState() {
        enablePassButton(true);
        mBtnPass.setText(getString(R.string.vs_btn_next));
        enablePlayerButton(false);
        mTextSelectedList.setText(getAnswerInfoString());
    }

    private void setEndState() {
        showResultDialog();
    }
    //state methods [end]

    private void printScore(PlayerData player) {
        player.textScore.setText(getString(R.string.vs_text_score) + " : " + player.score);
        player.textScorePlus.setVisibility(View.VISIBLE);
        player.textScorePlus.setText(player.plusScore > 0 ?
                "+" + player.plusScore : "" + player.plusScore);
    }

    private void enablePlayerButton(boolean enable) {
        for (PlayerData player : mPlayers) {
            player.btnSet.setEnabled(enable);
            player.btnComplete.setEnabled(enable);
            if (enable) {
                ViewUtil.setButtonColor(player.btnSet, player.color);
                ViewUtil.setButtonColor(player.btnComplete, player.color);
            } else {
                ViewUtil.setButtonColor(player.btnSet, getResources()
                        .getColor(R.color.base_lightgray));
                ViewUtil.setButtonColor(player.btnComplete, getResources()
                        .getColor(R.color.base_lightgray));
            }
        }
    }

    private void enablePassButton(boolean enable) {
        mBtnPass.setEnabled(enable);
        if (enable) {
            ViewUtil.setButtonColor(mBtnPass, getResources().getColor(R.color.colorAccent));
        } else {
            ViewUtil.setButtonColor(mBtnPass, getResources().getColor(R.color.base_lightgray));
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
        editName.setHint(R.string.vs_text_player_hint);
        editName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(editName)
                .setTitle(R.string.vs_text_player_dialog_title)
                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mNowFlagedPlayer != null) {
                            String name = editName.getText().toString();
                            if (name == null || "".equalsIgnoreCase(name)) {
                                name = getString(R.string.vs_text_player);
                            }
                            mNowFlagedPlayer.name = name;
                            mNowFlagedPlayer.btnName.setText(name);
                        }
                    }
                }).setNegativeButton(R.string.text_cancel,
                new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                }).show();
    }

    private void callPass() {
        if (mNowGameState == GAME_STATE_READY) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.vs_text_dialog_pass_title)
                    .setMessage(R.string.vs_text_dialog_pass_desc)
                    .setPositiveButton(R.string.vs_text_dialog_pass_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mNowGameState = GAME_STATE_PASS;
                                    renewViews();
                                }
                            }).setNegativeButton(R.string.text_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
        } else if (mNowGameState == GAME_STATE_PASS) {
            goNextStage();
        }
    }

    private void showAllAnswer(){

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
        Iterator<String> it = mAnswerMap.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            SetAnswerData data = mAnswerMap.get(it.next());
            if (data.isSelected) {
                sb.append(data.toStringWithPlus1() + " ");
            }
        }
        mTextSelectedList.setText(sb.toString());
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

        public SetAnswerData(int a, int b, int c, boolean initSelected) {
            this(a, b, c);
            this.isSelected = initSelected;
        }

        public SetAnswerData(int a, int b, int c) {
            this.first = a;
            this.second = b;
            this.third = c;
            int temp;

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
            return ret.append(this.first).append(this.second).append(this.third)
                    .toString();
        }

        public String toStringWithSelected() {
            StringBuffer ret = new StringBuffer(toString());
            return ret.append(isSelected ? "T" : "F").toString();
        }

        public String toStringWithPlus1() {
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
