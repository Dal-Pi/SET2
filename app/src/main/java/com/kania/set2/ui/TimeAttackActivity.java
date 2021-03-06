package com.kania.set2.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kania.set2.R;
import com.kania.set2.model.SetContract;
import com.kania.set2.model.SetRankData;
import com.kania.set2.util.RandomNumberUtil;
import com.kania.set2.model.SetItemData;
import com.kania.set2.model.SetVerifier;
import com.kania.set2.util.SetGameUtil;
import com.kania.set2.util.SetRankUtil;
import com.kania.set2.util.SetSaveStateUtil;
import com.kania.set2.util.ViewUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

public class TimeAttackActivity extends AppCompatActivity implements View.OnClickListener,
        NineCardFragment.OnSelectThreeCardListener{

    private static final int NUM_ALL_CARDS = 9;
    private static final int NUM_ANS_CARDS = 3;

    public static final int GAME_TIME = 61;
    public static final int POINT_USE_NO_HINT = 3;
    public static final int POINT_USE_HINT1 = 1;
    public static final int POINT_USE_HINT2 = 0;

    public static final String KEY_DIFFICULTY = SetContract.EXTRA_DIFFICULTY;
    public static final int DIFFICULTY_EASY = SetContract.DIFFICULTY_EASY;
    public static final int DIFFICULTY_HARD = SetContract.DIFFICULTY_HARD;

    public static final String KEY_REMAIN_TIME = "saved_remain_time";
    public static final String KEY_SCORE = "saved_score";
    public static final String KEY_HINT_COUNT = "saved_hint_count";
    public static final String KEY_ANSWER_LIST = "saved_answer_list";
    public static final String KEY_DECK_LIST = "saved_deck_list";
    public static final String KEY_SELECTED_LIST = "saved_selected_list";

    public static final int ANIMATION_DURATION = 2000;

    //utils
    private RandomNumberUtil mRandomNumberUtil;
    private CountDownTimer mBackTimer;
    private boolean mIsNowBackTimer;

    //game data
    private int mDifficulty;
    private int mRemainTime;
    private int mScore;
    private int mHintCount;
    private boolean mIsExistSavedQuestion;
    private Vector<SetItemData> mAllItemList;
    private int[] mAllItemListSequence;
    private Vector<SetItemData> mAnswerList;
    private Vector<SetItemData> mDeckList;
    private Vector<Integer> mSelectedPositionList;
    private Vector<Integer> mSavedSelectedPositionList;

    //fragments
    private AnswerImageFragment mAnswerImageFragment;
    private NineCardFragment mNineCardFragment;

    //layout
    private ViewGroup mResultLayout;
    private TextView mTextRemainTime;
    private TextView mTextScore;
    private TextView mTextScorePlus;
    private Button mBtnHint;
    private EditText mEditName;
    private Button mBtnRegister;

    //timer handler
    private boolean mIsStart;
    private TimerHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeattack);

        addFragment();

        initViews();
        initTools();
        initGame(savedInstanceState);

        mIsStart = false; // delay for loading fragments
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsStart) {
            //it will only one time run
            mIsStart = true;
            startGame();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_REMAIN_TIME, mRemainTime);
        outState.putInt(KEY_SCORE, mScore);
        outState.putInt(KEY_HINT_COUNT, mHintCount);
        outState.putString(KEY_ANSWER_LIST, SetSaveStateUtil.backupSetItemDataList(mAnswerList));
        outState.putString(KEY_DECK_LIST, SetSaveStateUtil.backupSetItemDataList(mDeckList));
        outState.putString(KEY_SELECTED_LIST,
                SetSaveStateUtil.backupIntegerList(mSelectedPositionList));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackTimer != null) {
            mBackTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        if (mBackTimer != null && mIsNowBackTimer) {
            super.onBackPressed();
        } else {
            mIsNowBackTimer = true;
            SetGameUtil.showBackpressToast(getApplicationContext());
            mBackTimer = new CountDownTimer(SetGameUtil.NUM_END_TIMER_COUNT,
                    SetGameUtil.NUM_END_TIMER_COUNT) {
                @Override
                public void onTick(long l) {
                    //do noting
                }
                @Override
                public void onFinish() {
                    mIsNowBackTimer = false;
                }
            }.start();
        }
    }

    @Override
    public void onThreeCardSelected(Vector<SetItemData> selectedList) {
        if (checkAnswer(selectedList)) {
            addScore();
            setNotiImage(true);
            createNewQuestion();
            askQuestion();
        } else {
            setNotiImage(false);
        }
        mNineCardFragment.unselectAllCard();
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
        //Log.d("SET", "selected : " + mSelectedPositionList.toString());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.timeattack_btn_hint:
                selectHint();
                break;
            case R.id.timeattack_btn_register:
                checkNewRank();
                break;
        }
    }

    private void addFragment() {
        mAnswerImageFragment = AnswerImageFragment.newInstance();
        //TODO expend card type
        mNineCardFragment = NineCardFragment
                .newInstance(NineCardFragment.CARD_TYPE_FILL_AS_PATTERN, true, this);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.timeattack_container_answer, mAnswerImageFragment);
        fragmentTransaction.add(R.id.timeattack_container_ninecard, mNineCardFragment);
        fragmentTransaction.commit();
    }

    private void initViews() {
        mResultLayout = (ViewGroup)findViewById(R.id.timeattack_layout_result);
        mTextRemainTime = (TextView)findViewById(R.id.timeattack_text_remain);
        mTextScore = (TextView)findViewById(R.id.timeattack_text_score);
        mTextScorePlus = (TextView)findViewById(R.id.timeattack_text_plus_score);
        mBtnHint = (Button)findViewById(R.id.timeattack_btn_hint);
        mBtnHint.setOnClickListener(this);
        mEditName = (EditText)findViewById(R.id.timeattack_edit_name);
        mBtnRegister = (Button)findViewById(R.id.timeattack_btn_register);
        mBtnRegister.setOnClickListener(this);

        int accentColor = getResources().getColor(R.color.colorAccent);
        ViewUtil.setButtonColor(mBtnHint, accentColor);
        ViewUtil.setButtonColor(mBtnRegister, accentColor);
    }

    private void initTools() {
        Calendar calendar = Calendar.getInstance();
        mRandomNumberUtil = RandomNumberUtil.getInstance(calendar.getTimeInMillis());

        mHandler = new TimerHandler();
    }

    private void initGame(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mDifficulty = intent.getIntExtra(KEY_DIFFICULTY, DIFFICULTY_HARD);

        //TODO restore setItem as putSerializable
        if (savedInstanceState != null) {
            mRemainTime = savedInstanceState.getInt(KEY_REMAIN_TIME);;
            mScore = savedInstanceState.getInt(KEY_SCORE);
            mHintCount = savedInstanceState.getInt(KEY_HINT_COUNT);
            mAnswerList = SetSaveStateUtil.restoreSetItemDataList(
                    savedInstanceState.getString(KEY_ANSWER_LIST));
            mDeckList = SetSaveStateUtil.restoreSetItemDataList(
                    savedInstanceState.getString(KEY_DECK_LIST));
            mSavedSelectedPositionList = SetSaveStateUtil.restoreIntegerList(
                    savedInstanceState.getString(KEY_SELECTED_LIST));
            mIsExistSavedQuestion = true;
        } else {
            mRemainTime = GAME_TIME;
            mScore = 0;
            mHintCount = 0;
        }

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

        mSelectedPositionList = new Vector<>();
    }

    private void startGame() {
        mTextScore.setText(getResources()
                .getText(R.string.timeattack_text_score) + " : " + mScore);
        mNineCardFragment.setClickable(true);
        if (mIsExistSavedQuestion) {
            mIsExistSavedQuestion = false;
        } else {
            createNewQuestion();
        }
        askQuestion();

        if (mHandler != null) {
            mHandler.sendEmptyMessage(0);
        }
    }

    private void createNewQuestion() {
        createNewDeck();
        initHint();
    }

    private void askQuestion() {
        mNineCardFragment.setCards(mDeckList);
        if (mSavedSelectedPositionList != null && mSavedSelectedPositionList.size() > 0) { //if exist saved question
            mNineCardFragment.selectCard(mSavedSelectedPositionList);
        }
    }

    private void createNewDeck() {
        mAnswerList = getNewAnswer(mDifficulty);
        if (mAnswerList == null) {
            Toast.makeText(this, "error occurred on getNewAnswer()",
                    Toast.LENGTH_SHORT).show();
        }
        //make random position
        Vector<SetItemData> sortedDeckList = getNewDeckWithAnswer(mAnswerList);
        mDeckList = new Vector<>();
        int [] deckListSequence = mRandomNumberUtil.getRandomNumberSet(sortedDeckList.size());
        for (int i = 0; i < NUM_ALL_CARDS; ++i) {
            mDeckList.add(sortedDeckList.get(deckListSequence[i]));
        }

        if (mDeckList == null) {
            Toast.makeText(this, "error occurred on getNewDeckWithAnswer()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private Vector<SetItemData> getNewAnswer(int difficulty) {
        SetItemData firstItem = null;
        SetItemData secondItem = null;
        SetItemData thirdItem = null;
        Vector<SetItemData> ret = new Vector<>();

        mAllItemListSequence = mRandomNumberUtil.getRandomNumberSet(mAllItemList.size()); //3*3*3*1
        firstItem = mAllItemList.get(mAllItemListSequence[0]);
        if (difficulty == DIFFICULTY_HARD) {
            secondItem = mAllItemList.get(mAllItemListSequence[1]);
        } else { //DIFFICULTY_EASY
//            for (SetItemData item : mAllItemList) {
            for (int i = 0; i < mAllItemList.size(); ++i) {
                SetItemData item = mAllItemList.get(mAllItemListSequence[i]);
                int matchCount = 0;
                if (firstItem.mColor == item.mColor) {matchCount++;}
                if (firstItem.mShape == item.mShape) {matchCount++;}
                if (firstItem.mFill == item.mFill) {matchCount++;}
                //not yet
                //if (firstItem.mAmount == item.mAmount) {matchCount++;}
                if (matchCount == 2) {
                    secondItem = item;
                }
            }
        }
        if (secondItem == null) {
            return null;
        }

        //calculate SET from two cards
        int incompleteColor = (firstItem.mColor + secondItem.mColor) % NUM_ANS_CARDS;
        int incompleteShape = (firstItem.mShape + secondItem.mShape) % NUM_ANS_CARDS;
        int incompleteFill = (firstItem.mFill + secondItem.mFill) % NUM_ANS_CARDS;
        //it is the third item
        int thirdItemColor = (NUM_ANS_CARDS - incompleteColor) % NUM_ANS_CARDS;
        int thirdItemShape = (NUM_ANS_CARDS - incompleteShape) % NUM_ANS_CARDS;
        int thirdItemFill = (NUM_ANS_CARDS - incompleteFill) % NUM_ANS_CARDS;
        for (SetItemData item : mAllItemList) {
            if (item.mColor == thirdItemColor && item.mShape == thirdItemShape
                    && item.mFill == thirdItemFill) {
                thirdItem = item;
            }
        }

        if (firstItem != null) {ret.add(firstItem);}
        if (secondItem != null) {ret.add(secondItem);}
        if (thirdItem != null) {ret.add(thirdItem);}

        if (ret.size() != NUM_ANS_CARDS || !SetVerifier.isValidSet(ret)) {
            return null;
        }
        return ret;
    }

    @Nullable
    private Vector<SetItemData> getNewDeckWithAnswer(Vector<SetItemData> answer) {
        Vector<SetItemData> ret = new Vector<>();
        ret.addAll(answer);

        int[] sequence = mRandomNumberUtil.getRandomNumberSet(27); //3*3*3*1
        for (int i = 0; i < mAllItemListSequence.length; ++i) {
            Vector<SetItemData> forCheck = new Vector<>();
            SetItemData candidate = mAllItemList.get(mAllItemListSequence[i]);
            //if include answer, skip
            boolean isAnswerItem = false;
            for (SetItemData answerItem : answer) {
                if (candidate.isSameWith(answerItem)) {
                    isAnswerItem = true;
                }
            }
            //if not include, check it makes no set with answer items
            boolean isItMakeSet = false;
            for (int j = 0; j < ret.size() - 1; ++j) {
                for (int k = j + 1; k < ret.size(); ++k) {
                    //add twice card
                    forCheck.clear();
                    forCheck.add(ret.get(j));
                    forCheck.add(ret.get(k));
                    //add card to check
                    forCheck.add(candidate);

                    if (SetVerifier.isValidSet(forCheck)) {
                        isItMakeSet = true;
                        break;
                    }
                }
                if (isItMakeSet) {
                    break;
                }
            }
            if (!isItMakeSet) {
                ret.add(candidate);
                if (ret.size() == NUM_ALL_CARDS)
                    break;
            }
        }

        if (ret.size() != NUM_ALL_CARDS) {
            return null;
        }
        return ret;
    }

    private void initHint() {
        mHintCount = 0;
        mBtnHint.setEnabled(true);
    }

    private boolean checkAnswer(Vector<SetItemData> candidates) {
        if (candidates.size() != NUM_ANS_CARDS) {
            return false;
        }
        return SetVerifier.isValidSet(candidates);
    }

    private void addScore() {
        int point = calculatePoint();
        mScore += point;
        mTextScore.setText(getResources()
                .getText(R.string.timeattack_text_score) + " : " + mScore);
        mTextScorePlus.setText(" +" + point);
    }

    private int calculatePoint() {
        if (mHintCount >= 2) {
            return POINT_USE_HINT2;
        } else if (mHintCount == 1) {
            return POINT_USE_HINT1;
        } else {
            return POINT_USE_NO_HINT;
        }
    }

    public void setNotiImage(boolean status) {
        mAnswerImageFragment.setNotiImage(status);
        if (status) {
            crossfadeAddedScore();
        }
    }

    private void crossfadeAddedScore() {
        mTextScorePlus.setAlpha(1f);
        mTextScorePlus.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null);
    }

    private void reduceTime() {
        mRemainTime--;
        if (mRemainTime <= 0) {
            endGame();
        } else {
            mTextRemainTime.setText("" + mRemainTime);
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(0, 1000);
            }
        }
    }

    private void endGame() {
        mAnswerImageFragment.setVisible(false);
        mResultLayout.setVisibility(View.VISIBLE);
        mTextRemainTime.setText(getResources().getText(R.string.timeattack_text_end));
        mBtnHint.setEnabled(false);
        ViewUtil.setButtonColor(mBtnHint, getResources().getColor(R.color.base_lightgray));
        mNineCardFragment.setClickable(false);
    }

    private void selectHint() {
        mHintCount = mHintCount >= 2 ? mHintCount : (mHintCount + 1);
        presentHint();
    }

    private void presentHint() {
        SetItemData hintTarget1;
        SetItemData hintTarget2 = null;
        hintTarget1 = mAnswerList.get(1);
        if (mHintCount >= 2) {
            //mBtnHint.setEnabled(false);
            hintTarget2 = mAnswerList.get(2);
        }

        Vector<SetItemData> hints = new Vector<>();
        hints.add(hintTarget1);
        if (hintTarget2 != null) {
            hints.add(hintTarget2);
        }
        mSelectedPositionList.clear();
        mNineCardFragment.selectMatchedCard(hints);
    }

    private void checkNewRank() {
        SetRankUtil rankUtil = SetRankUtil.getInstance(this);
        Calendar calendar = Calendar.getInstance();
        SetRankData newData = new SetRankData();
        String playerName = mEditName.getText().toString();
        if (playerName != null && playerName.isEmpty()) {
            playerName = getResources().getString(R.string.unknown_player);
        }
        newData.name = playerName;
        newData.score = mScore;
        newData.date = calendar.getTimeInMillis();
        newData.difficulty = mDifficulty;

        ArrayList<SetRankData> preRank = rankUtil.getRankList(mDifficulty);
        int rank = 0;
        for (rank = 0; rank < preRank.size(); ++rank) {
            if (rank > SetContract.MAX_RANK_EACH) {
                break;
            }
            SetRankData data = preRank.get(rank);
            if (data.score < newData.score) {
                break;
            }
        }
        if (rank <= SetContract.MAX_RANK_EACH) {
            rankUtil.addNewRank(newData);
        }

        Intent rankIntent = new Intent(this, RankActivity.class);
        rankIntent.putExtra(SetContract.EXTRA_NEW_RANK, newData);
        rankIntent.putExtra(SetContract.EXTRA_DIFFICULTY, mDifficulty);
        startActivity(rankIntent);
        finish();
    }

    //TODO using weakreference
    class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            reduceTime();
        }
    }
}
