package com.kania.set2.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kania.set2.R;
import com.kania.set2.provider.RandomNumberProvider;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitleBGColor();

        findViewById(R.id.main_buttons_btn_timeattack).setOnClickListener(this);
        findViewById(R.id.main_buttons_btn_vsmode).setOnClickListener(this);
        findViewById(R.id.main_buttons_btn_howtoplay).setOnClickListener(this);
        findViewById(R.id.main_buttons_btn_rank).setOnClickListener(this);
    }

    private void setTitleBGColor() {
        int[] colors = getResources().getIntArray(R.array.itemColors);
        Calendar calendar = Calendar.getInstance();
        int randomIndex = RandomNumberProvider.getInstance(calendar.getTimeInMillis())
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
                intent = new Intent(this, TimeAttackActivity.class);
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
                //TODO not implement yet
                Toast.makeText(this, "not implemented yet", Toast.LENGTH_SHORT).show();
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}

