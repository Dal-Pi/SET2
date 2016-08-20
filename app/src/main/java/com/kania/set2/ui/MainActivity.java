package com.kania.set2.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kania.set2.R;
import com.kania.set2.model.SetContract;
import com.kania.set2.util.RandomNumberUtil;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int STATE_MAIN_BUTTONS = 1;
    private static final int STATE_TIMEATTACK_BUTTONS = 2;

    private int mState;

    private ViewGroup mLayoutMainButtons;
    private ViewGroup mLayoutTimeAttackButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitleBGColor();

        mLayoutMainButtons = (ViewGroup)findViewById(R.id.main_layout_main_buttons);
        mLayoutTimeAttackButtons = (ViewGroup)findViewById(
                R.id.main_layout_timeattack_buttons);

        findViewById(R.id.main_buttons_btn_timeattack).setOnClickListener(this);
        findViewById(R.id.main_buttons_btn_vsmode).setOnClickListener(this);
        findViewById(R.id.main_buttons_btn_howtoplay).setOnClickListener(this);
        findViewById(R.id.main_buttons_btn_rank).setOnClickListener(this);

        findViewById(R.id.main_buttons_btn_easy).setOnClickListener(this);
        findViewById(R.id.main_buttons_btn_hard).setOnClickListener(this);
        mState = STATE_MAIN_BUTTONS;
    }

    @Override
    protected void onResume() {
        super.onResume();
        returnMainState();
    }

    private void setTitleBGColor() {
        int[] colors = getResources().getIntArray(R.array.pastelColors);
        Calendar calendar = Calendar.getInstance();
        int randomIndex = RandomNumberUtil.getInstance(calendar.getTimeInMillis())
                .getRandomNumber(colors.length);

        ViewGroup layoutTitle = (ViewGroup)findViewById(R.id.main_layout_bg_title);
        layoutTitle.setBackgroundColor(colors[randomIndex]);
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
                //TODO not implement yet
                Toast.makeText(this, "not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_buttons_btn_howtoplay:
                //TODO not implement yet
                Toast.makeText(this, "not implemented yet", Toast.LENGTH_SHORT).show();
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

