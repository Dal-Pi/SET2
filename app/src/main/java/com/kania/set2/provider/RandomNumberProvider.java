package com.kania.set2.provider;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by user on 2016-08-14.
 */

public class RandomNumberProvider {

    private static RandomNumberProvider mInstance;
    private Random mRandom;

    private RandomNumberProvider(long seed) {
        mRandom = new Random(seed);
    }

    public static RandomNumberProvider getInstance(long seed) {
        if (mInstance == null) {
            mInstance = new RandomNumberProvider(seed);
        }
        return mInstance;
    }

    public int[] getRandomNumberSet(int amount) {
        int[] ret = new int[amount];
        ArrayList<Integer> numList = new ArrayList<>();
        for (int i = 0; i < amount; ++i) {
            numList.add(i);
        }
        for (int i = 0; i < amount; ++i) {
            int target = getRandomNumber(numList.size());
            ret[i] = numList.remove(target);
        }
        return ret;
    }

    public int getRandomNumber(int max) {
        if (mRandom != null) {
            return mRandom.nextInt(max);
        } else {
            return 0;
        }
    }

    public int getRandomNumber(int min, int max) {
        if (min >= max)
            return 0;
        return getRandomNumber(max - min) + min;
    }

}
