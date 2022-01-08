package com.zxj.highandroid;

import android.util.Log;

public class Test {

    private void test1(double param1) {
        double newParam1 = param1;
        Log.e("Test", "test1");
    }


    private void test2(double param1, double param2) {
        double newParam1 = param1 + 1;
        double newParam2 = param2 + 2;
        double newParam3 = newParam1 + newParam2;
        Log.e("Test", "test2 = " + newParam3);
    }


    private void test3(double param1, double param2, double param3) {
        double newParam1 = param1;
        double newParam2 = param2;
        double newParam3 = param3;
        Log.e("Test", "test3");
    }
}
