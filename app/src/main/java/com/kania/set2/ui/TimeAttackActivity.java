package com.kania.set2.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kania.set2.R;
import com.kania.set2.provider.RandomNumberProvider;
import com.kania.set2.model.SetItemData;
import com.kania.set2.model.SetVerifier;

import java.util.ArrayList;
import java.util.Calendar;

public class TimeAttackActivity extends AppCompatActivity implements View.OnClickListener{

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

    //tools
    private RandomNumberProvider mRandomNumberProvider;
    private int[] mColors;

    //game data
    private int mDifficulty;
    private int mRemainTime;
    private int mScore;
    private int mHintCount;
    private int mSelectCount;
    private int mSelectedColor;
    private int mNotSelectedColor;
    private ArrayList<SetItemData> mAllItemList;
    private int[] mAllItemListSequence;
    private ArrayList<SetItemData> mAnswerList;
    private ArrayList<SetItemData> mDeckList;
    private ArrayList<CardViewData> mCardViewList;
    private int[] mDeckListSequence;
    private int[] mAnswerColors;

    //layout
    private LinearLayout mResultLayout;
    private ImageView mImageNoti;
    private TextView mTextRemainTime;
    private TextView mTextScore;
    private TextView mTextScorePlus;
    private Button mBtnHint;
    private ImageView[] mCardViews;
    private RelativeLayout[] mCardLayout;

    //listener
    private CardClickListener mCardClickListener;

    //timer handler
    private TimerHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_only_one_set_game);

        initViews();
        initTools();
        initGame(savedInstanceState);

        startGame();
    }

    private void initViews() {
        mCardClickListener = new CardClickListener();
        mCardViews = new ImageView[9];
        mCardLayout = new RelativeLayout[9];
        for (int i = 0; i < 9; ++i) {
            mCardViews[i] = (ImageView)findViewById(getResources().
                    getIdentifier("only_one_card_image" + (i + 1), "id", getPackageName()));
            mCardViews[i].setOnClickListener(mCardClickListener);
            mCardLayout[i] = (RelativeLayout)findViewById(getResources().
                    getIdentifier("only_one_card_layout" + (i + 1), "id", getPackageName()));
        }
        mCardViewList = new ArrayList<>();
        mSelectedColor = getResources().getColor(R.color.base_darkgray);
        mNotSelectedColor = getResources().getColor(R.color.base_white);

        mResultLayout = (LinearLayout)findViewById(R.id.only_one_layout_result);
        mImageNoti = (ImageView)findViewById(R.id.only_one_image_result);
        mTextRemainTime = (TextView)findViewById(R.id.only_one_text_remain);
        mTextScore = (TextView)findViewById(R.id.only_one_text_score);
        mTextScorePlus = (TextView)findViewById(R.id.only_one_text_plus_score);
        mBtnHint = (Button)findViewById(R.id.only_one_btn_hint);
        mBtnHint.setOnClickListener(this);
    }

    private void initTools() {
        Calendar calendar = Calendar.getInstance();
        mRandomNumberProvider = RandomNumberProvider.getInstance(calendar.getTimeInMillis());
        mColors = getResources().getIntArray(R.array.itemColors);
        if (mColors.length < 3) {
            mColors = new int[]{R.color.base_lightgray, R.color.base_darkgray,
                    R.color.base_black};
        }
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
        askQuestion();
        if (mHandler != null) {
            mHandler.sendEmptyMessage(0);
        }
    }

    private void askQuestion() {
        createNewDeck();
        initHint();
        initCardViews();
        initCardLayout();
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

        mAnswerColors = getRandomColor();
        //TODO set to layout
        //make random position
        mDeckListSequence = mRandomNumberProvider.getRandomNumberSet(mDeckList.size());

        mHintCount = 0;
    }

    private int[] getRandomColor() {
        int[] ret = new int[3];
        int[] colorSequence = mRandomNumberProvider.getRandomNumberSet(mColors.length);
        for (int i = 0; i < 3; ++i) {
            ret[i] = mColors[colorSequence[i]];
        }
        return ret;
    }

    private void initCardViews() {
        mCardViewList.clear();
        for (int i = 0; i < 9; ++i) {
            //for debug, if do not put as random, answer will present 1,2,3 cards
            //SetItemData item = mDeckList.get(i);
            SetItemData item = mDeckList.get(mDeckListSequence[i]);
            mCardViews[i].setImageResource(getShapeAndFillResId(item));
            mCardViews[i].setBackgroundColor(mAnswerColors[item.mColor]);
            mCardViewList.add(new CardViewData(item));
            mCardLayout[i].setBackgroundColor(mNotSelectedColor);
        }
    }

    private void initHint() {
        mHintCount = 0;
        mBtnHint.setEnabled(true);
    }

    private void initCardLayout() {
        for (int i = 0; i < 9; ++i) {
            mCardViewList.get(i).mIsChecked = false;
            mCardLayout[i].setBackgroundColor(mNotSelectedColor);
        }
        mSelectCount = 0;
    }

    private int getShapeAndFillResId(SetItemData item) {
        //TODO you can make random shape
        String resourceName = "shape_and_fill_0" + item.mShape + item.mFill + "0"; //color and amount is zero
        return getResources().getIdentifier(resourceName, "drawable", getPackageName());
    }

    @Nullable
    private ArrayList<SetItemData> getNewAnswer(int difficulty) {
        SetItemData firstItem = null;
        SetItemData secondItem = null;
        SetItemData thirdItem = null;
        ArrayList<SetItemData> ret = new ArrayList<>();

        mAllItemListSequence = mRandomNumberProvider.getRandomNumberSet(mAllItemList.size()); //3*3*3*1
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

        int[] sequence = mRandomNumberProvider.getRandomNumberSet(27); //3*3*3*1
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

    private boolean checkAnswer() {
        ArrayList<SetItemData> candidates = new ArrayList<>();
        for (CardViewData card : mCardViewList) {
            if (card.mIsChecked) {
                candidates.add(card.getmSetitemData());
            }
        }
        //TODO verifying
        if (candidates.size() != 3) {
            return false;
        }
        return SetVerifier.isValidSet(candidates);
    }

    private void addScore() {
        int point = calculatePoint();
        mScore += point;
        mTextScore.setText("" + mScore);
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
        if (status) {
            mImageNoti.setImageDrawable(getResources().getDrawable(R.drawable.answer_right));
            crossfadeAddedScore();
        } else {
            mImageNoti.setImageDrawable(getResources().getDrawable(R.drawable.answer_wrong));
        }
        crossfadeNotiImage();
    }

    private void crossfadeAddedScore() {
        mTextScorePlus.setAlpha(1f);
        mTextScorePlus.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null);
    }

    private void crossfadeNotiImage() {
        mImageNoti.setAlpha(1f);
        mImageNoti.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null);
    }

    private void reduceTime() {
        mRemainTime--;
        if (mRemainTime <= 0) {
            //TODO string
            mTextRemainTime.setText("end!");
            mResultLayout.setVisibility(View.VISIBLE);
            endGame();
        } else {
            mTextRemainTime.setText("" + mRemainTime);
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(0, 1000);
            }
        }
    }

    private void endGame() {
        setEndState();
    }

    private void setEndState() {
        //TODO
        //
        mBtnHint.setEnabled(false);
        for (int i = 0; i < 9; ++i) {
            mCardViews[i].setClickable(false);
        }
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

        initCardLayout();
        for (int i = 0; i < 9; ++i) {
            SetItemData candidate = mCardViewList.get(i).getmSetitemData();
            if (hintTarget1.isSameWith(candidate)) {
                selectCard(i);
            } else if (hintTarget2 != null && hintTarget2.isSameWith(candidate)) {
                selectCard(i);
            }
        }
    }

    private void selectCard(int pos) {
        CardViewData target = mCardViewList.get(pos);
        if (target.mIsChecked) {
            target.mIsChecked = false;
            mCardLayout[pos].setBackgroundColor(mNotSelectedColor);
            mSelectCount--;
        } else {
            target.mIsChecked = true;
            mCardLayout[pos].setBackgroundColor(mSelectedColor);
            mSelectCount++;

            if (mSelectCount == 3) {
                if (checkAnswer()) {
                    addScore();
                    setNotiImage(true);
                    askQuestion();
                } else {
                    setNotiImage(false);
                    initCardLayout();
                }
            }
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
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.only_one_btn_hint:
                selectHint();
                break;
        }
    }


    class CardClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            int pos = 0;
            switch (id) {
                case R.id.only_one_card_image1: pos = 0; break;
                case R.id.only_one_card_image2: pos = 1; break;
                case R.id.only_one_card_image3: pos = 2; break;
                case R.id.only_one_card_image4: pos = 3; break;
                case R.id.only_one_card_image5: pos = 4; break;
                case R.id.only_one_card_image6: pos = 5; break;
                case R.id.only_one_card_image7: pos = 6; break;
                case R.id.only_one_card_image8: pos = 7; break;
                case R.id.only_one_card_image9: pos = 8; break;
            }
            selectCard(pos);
        }
    }

    //TODO
    class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            reduceTime();
        }
    }
}
