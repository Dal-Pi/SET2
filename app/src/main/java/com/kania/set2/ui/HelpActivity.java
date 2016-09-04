package com.kania.set2.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.kania.set2.R;
import com.kania.set2.util.RandomNumberUtil;
import com.kania.set2.util.ViewUtil;

import java.util.Calendar;

/**
 * Created by user on 2016-09-04.
 */

public class HelpActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnExit;

    private int mColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        mBtnExit = (Button)findViewById(R.id.help_btn_exit);
        mBtnExit.setOnClickListener(this);
        ViewUtil.setButtonColor(mBtnExit, getResources().getColor(R.color.colorAccent));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.help_btn_exit) {
            finish();
        }
    }
}
