package com.kania.set2.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kania.set2.R;
import com.kania.set2.model.SetItemData;
import com.kania.set2.util.RandomNumberUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class NineCardFragment extends Fragment implements View.OnClickListener {
    public static final int CARD_TYPE_FILL_AS_PATTERN = 1;
    public static final int CARD_TYPE_FILL_AS_COLOR = 2;

    private static final String ARG_CARD_TYPE = "card_type";
    private static final String ARG_NUMBER_VISIBLE = "number_visible";

    private static final int NUM_ALL_CARDS = 9;
    private static final int NUM_ANS_CARDS = 3;

    //args
    private int mCardType;
    private boolean mNumberVisible;

    //utils
    private RandomNumberUtil mRandomNumberUtil;
    private int[] mAnswerColors;
    private int[] mColors;
    private int mSelectedColor;
    private int mNotSelectedColor;

    //views
    private TextView[] mCardNumbers;
    private ImageView[] mCardViews;
    private ViewGroup[] mCardLayouts;

    //game data
    private CardViewData[] mCardViewDataList;
    private int mSelectCount;

    private OnSelectThreeCardListener mCallback;

    public NineCardFragment() {
        // Required empty public constructor
    }

    public static NineCardFragment newInstance(int cardType, boolean numberVisible,
                                               OnSelectThreeCardListener callback) {
        NineCardFragment fragment = new NineCardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CARD_TYPE, cardType);
        args.putBoolean(ARG_NUMBER_VISIBLE, numberVisible);
        fragment.setArguments(args);
        fragment.setCallback(callback);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCardType = getArguments().getInt(ARG_CARD_TYPE, CARD_TYPE_FILL_AS_PATTERN);
            mNumberVisible = getArguments().getBoolean(ARG_NUMBER_VISIBLE, true);
        }

        Calendar calendar = Calendar.getInstance();
        mRandomNumberUtil = RandomNumberUtil.getInstance(calendar.getTimeInMillis());

        mCardNumbers = new TextView[NUM_ALL_CARDS];
        mCardViews = new ImageView[NUM_ALL_CARDS];
        mCardLayouts = new RelativeLayout[NUM_ALL_CARDS];
        mCardViewDataList = new CardViewData[NUM_ALL_CARDS];
        mAnswerColors = new int[NUM_ANS_CARDS];

        mColors = getResources().getIntArray(R.array.cardColors);
        mSelectedColor = getResources().getColor(R.color.base_darkgray);
        mNotSelectedColor = getResources().getColor(R.color.base_white);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //test start
//        Log.d("SET2", "size of container : " + container.getWidth() + " / " + container.getHeight());

        //test end
        View view = inflater.inflate(R.layout.fragment_ninecard, container, false);
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        for (int i = 0; i < NUM_ALL_CARDS; ++i) {
            mCardNumbers[i] = (TextView)rootView.findViewById(getResources()
                    .getIdentifier("ninecard_text_num_card" + (i + 1), "id",
                            getActivity().getPackageName()));
            mCardNumbers[i].setVisibility(mNumberVisible ? View.VISIBLE : View.INVISIBLE);
            mCardViews[i] = (ImageView)rootView.findViewById(getResources()
                    .getIdentifier("ninecard_image_card" + (i + 1), "id",
                            getActivity().getPackageName()));
            mCardViews[i].setOnClickListener(this);
            mCardViews[i].setClickable(false);
            mCardLayouts[i] = (ViewGroup)rootView.findViewById(getResources()
                    .getIdentifier("ninecard_layout_card" + (i + 1), "id",
                            getActivity().getPackageName()));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectThreeCardListener) {
            mCallback = (OnSelectThreeCardListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSelectThreeCardListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int pos = 0;
        switch (id) {
            case R.id.ninecard_image_card1: pos = 0; break;
            case R.id.ninecard_image_card2: pos = 1; break;
            case R.id.ninecard_image_card3: pos = 2; break;
            case R.id.ninecard_image_card4: pos = 3; break;
            case R.id.ninecard_image_card5: pos = 4; break;
            case R.id.ninecard_image_card6: pos = 5; break;
            case R.id.ninecard_image_card7: pos = 6; break;
            case R.id.ninecard_image_card8: pos = 7; break;
            case R.id.ninecard_image_card9: pos = 8; break;
            default:
                Log.d("SET2", "error! invaild position");
                return;
        }
        selectCard(pos);
    }

    public void setCallback(OnSelectThreeCardListener callback) {
        mCallback = callback;
    }

    public void setCards(ArrayList<SetItemData> items) {
        if (items == null) {
            Log.e("SET2", "error! items is null!");
            return;
        }

        if (items.size() == NUM_ALL_CARDS) {
            initRandomAnswerColors();
            initCardViewData(items);
            if (mCardType == CARD_TYPE_FILL_AS_COLOR) {
                //TODO set as color
            } else { //mCardType == CARD_TYPE_FILL_AS_PATTERN
                setImageAsPattern();
            }
        } else {
            Log.e("SET2", "error! wrong number of items!");
        }
    }

    public void unselectAllCard() {
        for (int i = 0; i < NUM_ALL_CARDS; ++i) {
            mCardViewDataList[i].mIsChecked = false;
            mCardLayouts[i].setBackgroundColor(mNotSelectedColor);
            if (mNumberVisible) {
                mCardNumbers[i].setTextColor(mNotSelectedColor);
            }
        }
        mSelectCount = 0;
    }

    public void setClickable(boolean clickable) {
        for (int i = 0; i < NUM_ALL_CARDS; ++i) {
            mCardViews[i].setClickable(clickable);
        }
    }

    /**
     * Only can select maximum 2 items.
     * @param items items to select.
     */
    public void selectMatchedCard(ArrayList<SetItemData> items) {
        if (items == null || items.size() >= NUM_ANS_CARDS) {
            Log.e("SET2", "error! items is wrong!");
            return;
        }

        unselectAllCard();
        for (SetItemData item : items) {
            for (int i = 0; i < NUM_ALL_CARDS; ++i) {
                if (item.isSameWith(mCardViewDataList[i].getmSetitemData())) {
                    selectCard(i);
                }
            }
        }
    }

    public void selectCard(ArrayList<Integer> positions) {
        if (positions.size() >= NUM_ANS_CARDS) {
            Log.d("SET2", "do not try to select over 3 cards");
        }
        for (int pos : positions) {
            selectCard(pos);
        }
    }

    public void selectCard(int position) {
        mCallback.onSelectCard(position);
        CardViewData target = mCardViewDataList[position];
        if (target.mIsChecked) {
            target.mIsChecked = false;
            mCardLayouts[position].setBackgroundColor(mNotSelectedColor);
            if (mNumberVisible) {
                mCardNumbers[position].setTextColor(mNotSelectedColor);
            }
            mSelectCount--;
        } else {
            target.mIsChecked = true;
            mCardLayouts[position].setBackgroundColor(mSelectedColor);
            if (mNumberVisible) {
                mCardNumbers[position].setTextColor(mSelectedColor);
            }
            mSelectCount++;

            if (mSelectCount == NUM_ANS_CARDS) {
                ArrayList<SetItemData> askData = new ArrayList<>();
                for (int i = 0; i < NUM_ALL_CARDS; ++i) {
                    if (mCardViewDataList[i].mIsChecked) {
                        askData.add(mCardViewDataList[i].getmSetitemData());
                    }
                }
                if (askData.size() == NUM_ANS_CARDS) {
                    mCallback.onThreeCardSelected(askData);
                } else {
                    Log.e("SET2", "error! askData size is not 3");
                }
            }
        }
    }

    private void initRandomAnswerColors() {
        int[] colorSequence = mRandomNumberUtil.getRandomNumberSet(mColors.length);
        for (int i = 0; i < 3; ++i) {
            mAnswerColors[i] = mColors[colorSequence[i]];
        }
    }

    private void initCardViewData(ArrayList<SetItemData> items) {
        for (int i = 0; i < NUM_ALL_CARDS; ++i) {
            if (mCardViewDataList[i] == null) {
                mCardViewDataList[i] = new CardViewData(items.get(i));
            } else {
                mCardViewDataList[i].setmSetitemData(items.get(i));
            }
        }
    }

    private void setImageAsPattern() {
        for (int i = 0; i < NUM_ALL_CARDS; ++i) {
            SetItemData item = mCardViewDataList[i].getmSetitemData();
            mCardViews[i].setImageResource(getShapeAndFillResId(item));
            mCardViews[i].setBackgroundColor(mAnswerColors[item.mColor]);
        }
    }

    private int getShapeAndFillResId(SetItemData item) {
        //TODO you can make random shape
        String resourceName = "shape_and_fill_0" + item.mShape + item.mFill + "0"; //color and amount is zero
        return getResources().getIdentifier(resourceName, "drawable",
                getActivity().getPackageName());
    }

    public interface OnSelectThreeCardListener {
        void onThreeCardSelected(ArrayList<SetItemData> selectedList);
        void onSelectCard(int position);
    }

    class CardViewData {
        private SetItemData mSetitemData;
        public boolean mIsChecked;

        public CardViewData(SetItemData itemData) {
            this.mSetitemData = itemData;
            this.mIsChecked = false;
        }

        public void setmSetitemData(SetItemData item) {
            this.mSetitemData = item;
        }

        public SetItemData getmSetitemData() {
            return mSetitemData;
        }
    }
}
