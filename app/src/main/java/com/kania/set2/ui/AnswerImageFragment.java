package com.kania.set2.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kania.set2.R;

public class AnswerImageFragment extends Fragment {

    public static final int ANIMATION_DURATION = 2000;

    private ImageView mImageNoti;

    public AnswerImageFragment() {
        // Required empty public constructor
    }

    public static AnswerImageFragment newInstance() {
        AnswerImageFragment fragment = new AnswerImageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_answerimage, container, false);
        mImageNoti = (ImageView)view.findViewById(R.id.answer_image_result);
        //test
        Log.d("SET2", "mImageNoti = " + mImageNoti);
        return view;
    }

    public void setNotiImage(boolean status) {
        if (mImageNoti != null) {
            mImageNoti.setVisibility(View.VISIBLE);
            if (status) {
                mImageNoti.setImageDrawable(getResources().getDrawable(R.drawable.answer_right));
            } else {
                mImageNoti.setImageDrawable(getResources().getDrawable(R.drawable.answer_wrong));
            }
            crossfadeNotiImage();
        }
    }

    public void setVisible(boolean visible) {
        mImageNoti.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void crossfadeNotiImage() {
        mImageNoti.setAlpha(1f);
        mImageNoti.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null);
    }
}
