package com.example.mig.lambdaball;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Mig on 26-May-16.
 */
public class BluetoothData {

    private static final String TAG = "ShowDataActivity";

    String G = "G:";
    String A = "A:";
    String M = "M:";
    String B = "B:";

    public void printMe() {
        Log.d(TAG, G);
        Log.d(TAG, A);
        Log.d(TAG, M);
        Log.d(TAG, B);
    }

    public void clearAll() {
        G = "G:";
        A = "A:";
        M = "M:";
        B = "B:";
    }

}
