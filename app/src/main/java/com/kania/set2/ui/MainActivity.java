package com.kania.set2.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.kania.set2.R;
import com.kania.set2.model.SetContract;
import com.kania.set2.util.RandomNumberUtil;
import com.kania.set2.util.ViewUtil;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int STATE_MAIN_BUTTONS = 1;
    private static final int STATE_TIMEATTACK_BUTTONS = 2;

    private int mState;

    private ViewGroup mLayoutMainButtons;
    private ViewGroup mLayoutTimeAttackButtons;

    private Button mBtnTimeAttack;
    private Button mBtnVsMode;
    private Button mBtnHowToPlay;
    private Button mBtnRank;
    private Button mBtnTimeAttackEasy;
    private Button mBtnTimeAttackHard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayoutMainButtons = (ViewGroup)findViewById(R.id.main_layout_main_buttons);
        mLayoutTimeAttackButtons = (ViewGroup)findViewById(
                R.id.main_layout_timeattack_buttons);

        mBtnTimeAttack = (Button)findViewById(R.id.main_buttons_btn_timeattack);
        mBtnTimeAttack.setOnClickListener(this);

        mBtnVsMode = (Button)findViewById(R.id.main_buttons_btn_vsmode);
        mBtnVsMode.setOnClickListener(this);
        mBtnHowToPlay = (Button)findViewById(R.id.main_buttons_btn_howtoplay);
        mBtnHowToPlay.setOnClickListener(this);
        mBtnRank = (Button)findViewById(R.id.main_buttons_btn_rank);
        mBtnRank.setOnClickListener(this);
        mBtnTimeAttackEasy = (Button)findViewById(R.id.main_buttons_btn_easy);
        mBtnTimeAttackEasy.setOnClickListener(this);
        mBtnTimeAttackHard = (Button)findViewById(R.id.main_buttons_btn_hard);
        mBtnTimeAttackHard.setOnClickListener(this);

        mState = STATE_MAIN_BUTTONS;

        setTitleBGColor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        returnMainState();
    }

    private void setTitleBGColor() {
        int[] colors = getResources().getIntArray(R.array.pointColors);
        Calendar calendar = Calendar.getInstance();
        int randomIndex = RandomNumberUtil.getInstance(calendar.getTimeInMillis())
                .getRandomNumber(colors.length);

        ViewGroup layoutTitle = (ViewGroup)findViewById(R.id.main_layout_bg_title);
        layoutTitle.setBackgroundColor(colors[randomIndex]);

        ViewUtil.setButtonColor(mBtnTimeAttack, colors[randomIndex]);
        ViewUtil.setButtonColor(mBtnVsMode, colors[randomIndex]);
        ViewUtil.setButtonColor(mBtnHowToPlay, colors[randomIndex]);
        ViewUtil.setButtonColor(mBtnRank, colors[randomIndex]);
        ViewUtil.setButtonColor(mBtnTimeAttackEasy, colors[randomIndex]);
        ViewUtil.setButtonColor(mBtnTimeAttackHard, colors[randomIndex]);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent = null;
        switch (id) {
            case R.id.main_buttons_btn_timeattack:
                mLayoutMainButtons.setVisibility(View.GONE);
                mLayoutTimeAttackButtons.setVisibility(View.VISIBLE);
                mState = STATE_TIMEATTACK_BUTTONS;
                break;
            case R.id.main_buttons_btn_vsmode:
                intent = new Intent(this, VsModeActivity.class);
                startActivity(intent);
                break;
            case R.id.main_buttons_btn_howtoplay:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                break;
            case R.id.main_buttons_btn_rank:
                intent = new Intent(this, RankActivity.class);
                startActivity(intent);
                break;
            case R.id.main_buttons_btn_easy:
                intent = new Intent(this, TimeAttackActivity.class);
                intent.putExtra(SetContract.EXTRA_DIFFICULTY, SetContract.DIFFICULTY_EASY);
                startActivity(intent);
                break;
            case R.id.main_buttons_btn_hard:
                intent = new Intent(this, TimeAttackActivity.class);
                intent.putExtra(SetContract.EXTRA_DIFFICULTY, SetContract.DIFFICULTY_HARD);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mState == STATE_TIMEATTACK_BUTTONS) {
            returnMainState();
        } else {
            super.onBackPressed();
        }
    }

    private void returnMainState() {
        mLayoutTimeAttackButtons.setVisibility(View.GONE);
        mLayoutMainButtons.setVisibility(View.VISIBLE);
        mState = STATE_MAIN_BUTTONS;
    }
}

