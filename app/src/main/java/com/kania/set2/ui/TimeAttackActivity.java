package com.kania.set2.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kania.set2.R;
import com.kania.set2.util.RandomNumberUtil;
import com.kania.set2.model.SetItemData;
import com.kania.set2.model.SetVerifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

public class TimeAttackActivity extends AppCompatActivity implements View.OnClickListener,
        NineCardFragment.OnSelectThreeCardListener{

    public static final int GAME_TIME = 60;
    public static final int POINT_USE_NO_HINT = 3;
    public static final int POINT_USE_HINT1 = 1;
    public static final int POINT_USE_HINT2 = 0;

    public static final String KEY_DIFFICULTY = "difficulty";
    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_HARD = 2;

    public static final String KEY_REMAIN_TIME = "saved_remain_time";
    public static final String KEY_SCORE = "saved_score";
    public static final String KEY_HINT_COUNT = "saved_hint_count";

    public static final int ANIMATION_DURATION = 2000;

    //utils
    private RandomNumberUtil mRandomNumberUtil;

    //game data
    private int mDifficulty;
    private int mRemainTime;
    private int mScore;
    private int mHintCount;
    private ArrayList<SetItemData> mAllItemList;
    private int[] mAllItemListSequence;
    private ArrayList<SetItemData> mAnswerList;
    private ArrayList<SetItemData> mDeckList;
    private int[] mDeckListSequence;

    //fragments
    private AnswerImageFragment mAnswerImageFragment;
    private NineCardFragment mNineCardFragment;

    //layout
    private ViewGroup mResultLayout;
    private TextView mTextResult;
    private TextView mTextRemainTime;
    private TextView mTextScore;
    private TextView mTextScorePlus;
    private Button mBtnHint;
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
        //TODO save setItem as putSerializable
    }

    @Override
    public void onThreeCardSelected(ArrayList<SetItemData> selectedList) {
        if (checkAnswer(selectedList)) {
            addScore();
            setNotiImage(true);
            askQuestion();
        } else {
            setNotiImage(false);
            mNineCardFragment.unselectAllCard();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.timeattack_btn_hint:
                selectHint();
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
        //TODO add score framgent
        fragmentTransaction.commit();
    }

    private void initViews() {
        mResultLayout = (ViewGroup)findViewById(R.id.timeattack_layout_result);
        mTextResult = (TextView)findViewById(R.id.timeattack_text_result);
        mBtnRegister = (Button)findViewById(R.id.timeattack_btn_register);
        mTextRemainTime = (TextView)findViewById(R.id.timeattack_text_remain);
        mTextScore = (TextView)findViewById(R.id.timeattack_text_score);
        mTextScorePlus = (TextView)findViewById(R.id.timeattack_text_plus_score);
        mBtnHint = (Button)findViewById(R.id.timeattack_btn_hint);
        mBtnHint.setOnClickListener(this);
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
        } else {

            mRemainTime = GAME_TIME;
            mScore = 0;
            mHintCount = 0;
        }

        mAllItemList = new ArrayList<>();
        for(int color = 0; color < 3; ++color) {
            for (int shape = 0; shape < 3; ++shape) {
                for (int fill = 0; fill < 3; ++fill) {
                    //not yet
                    //for (int amount = 0; amount < 3; ++amount) {}
                    mAllItemList.add(new SetItemData(color, shape, fill, 0));
                }
            }
        }
    }

    private void startGame() {
        mTextScore.setText(getResources()
                .getText(R.string.timeattack_text_score) + " : " + mScore);
        mNineCardFragment.setClickable(true);
        askQuestion();
        if (mHandler != null) {
            mHandler.sendEmptyMessage(0);
        }
    }

    private void askQuestion() {
        createNewDeck();
        initHint();

        //make random position
        mDeckListSequence = mRandomNumberUtil.getRandomNumberSet(mDeckList.size());
        ArrayList<SetItemData> askList = new ArrayList<>();
        for (int i = 0; i < 9; ++i) {
            askList.add(mDeckList.get(mDeckListSequence[i]));
        }
        mNineCardFragment.setCards(askList);
    }

    private void createNewDeck() {
        mAnswerList = getNewAnswer(mDifficulty);
        if (mAnswerList == null) {
            Toast.makeText(this, "error occurred on getNewAnswer()",
                    Toast.LENGTH_SHORT).show();
        }
        mDeckList = getNewDeckWithAnswer(mAnswerList);
        if (mAnswerList == null) {
            Toast.makeText(this, "error occurred on getNewDeckWithAnswer()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private ArrayList<SetItemData> getNewAnswer(int difficulty) {
        SetItemData firstItem = null;
        SetItemData secondItem = null;
        SetItemData thirdItem = null;
        ArrayList<SetItemData> ret = new ArrayList<>();

        mAllItemListSequence = mRandomNumberUtil.getRandomNumberSet(mAllItemList.size()); //3*3*3*1
        firstItem = mAllItemList.get(mAllItemListSequence[0]);
        if (difficulty == DIFFICULTY_HARD) {
            secondItem = mAllItemList.get(mAllItemListSequence[1]);
        } else { //DIFFICULTY_EASY
            for (SetItemData item : mAllItemList) {
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
        int incompleteColor = (firstItem.mColor + secondItem.mColor) % 3;
        int incompleteShape = (firstItem.mShape + secondItem.mShape) % 3;
        int incompleteFill = (firstItem.mFill + secondItem.mFill) % 3;
        //it is the third item
        int thirdItemColor = (3 - incompleteColor) % 3;
        int thirdItemShape = (3 - incompleteShape) % 3;
        int thirdItemFill = (3 - incompleteFill) % 3;
        for (SetItemData item : mAllItemList) {
            if (item.mColor == thirdItemColor && item.mShape == thirdItemShape
                    && item.mFill == thirdItemFill) {
                thirdItem = item;
            }
        }

        if (firstItem != null) {ret.add(firstItem);}
        if (secondItem != null) {ret.add(secondItem);}
        if (thirdItem != null) {ret.add(thirdItem);}

        //TODO verifying
        if (ret.size() != 3 || !SetVerifier.isValidSet(ret)) {
            return null;
        }
        return ret;
    }

    @Nullable
    private ArrayList<SetItemData> getNewDeckWithAnswer(ArrayList<SetItemData> answer) {
        ArrayList<SetItemData> ret = new ArrayList<>();
        ret.addAll(answer);

        int[] sequence = mRandomNumberUtil.getRandomNumberSet(27); //3*3*3*1
        for (int i = 0; i < mAllItemListSequence.length; ++i) {
            ArrayList<SetItemData> forCheck = new ArrayList<>();
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
                if (ret.size() == 9)
                    break;
            }
        }

        //TODO verifying
        if (ret.size() != 9) {
            return null;
        }
        return ret;
    }

//    private void initCardViews() {
//        mCardViewList.clear();
//        for (int i = 0; i < 9; ++i) {
//            //for debug, if do not put as random, answer will present 1,2,3 cards
//            //SetItemData item = mDeckList.get(i);
//            SetItemData item = mDeckList.get(mDeckListSequence[i]);
//            mCardViews[i].setImageResource(getShapeAndFillResId(item));
//            mCardViews[i].setBackgroundColor(mAnswerColors[item.mColor]);
//            mCardViewList.add(new CardViewData(item));
//            mCardLayout[i].setBackgroundColor(mNotSelectedColor);
//        }
//    }

    private void initHint() {
        mHintCount = 0;
        mBtnHint.setEnabled(true);
    }

    private boolean checkAnswer(ArrayList<SetItemData> candidates) {
        //TODO verifying
        if (candidates.size() != 3) {
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
        //TODO remove?
        mTextResult.setText(getResources().getText(R.string.timeattack_text_result)
                .toString() + " " + mScore);
        mResultLayout.setVisibility(View.VISIBLE);
        mTextRemainTime.setText(getResources().getText(R.string.timeattack_text_end));
        mBtnHint.setEnabled(false);
        mNineCardFragment.setClickable(false);
    }

    private void selectHint() {
        //TODO when click hint
        SetItemData hintTarget1;
        SetItemData hintTarget2 = null;
        mHintCount = mHintCount >= 2 ? mHintCount : (mHintCount + 1);
        hintTarget1 = mAnswerList.get(1);
        if (mHintCount >= 2) {
            //mBtnHint.setEnabled(false);
            hintTarget2 = mAnswerList.get(2);
        }

        ArrayList<SetItemData> hints = new ArrayList<>();
        hints.add(hintTarget1);
        if (hintTarget2 != null) {
            hints.add(hintTarget2);
        }
        mNineCardFragment.selectCard(hints);
    }

    //TODO using weakreference
    class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            reduceTime();
        }
    }
}
