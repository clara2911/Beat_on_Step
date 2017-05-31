package com.example.clara.accelerometer;

import android.util.Log;

import java.io.*;
import java.util.*;

public class Pedometer {
    private long[] last;
    private int currBPM;
    private int lastBPM;
    private int count;
    private boolean steady;

    private static final int listLength = 10;
    private static final double limit = 0.1;

    public Pedometer() {
        last = new long[listLength];
        for (int i = 0; i < listLength; i++) {
            last[i] = 0L;
        }
        count = 0;
        steady = false;
    }

    public int getBPM() {
        last = updateLast(System.currentTimeMillis());
        double difference = (double) (1.0d * (last[listLength-1] - last[0]));
        int newBPM = (int) (60.0d * (listLength * 1.0d) / (difference / 1000.0d));
        if (!close(lastBPM, newBPM)) {
            steady = false;
            count = 0;
            lastBPM = newBPM;
            if(currBPM == 0) {
                return 100;
            } else {
                return currBPM;
            }
        } else {
            if(steady == false) {
                if(count != 5) {
                    count++;
                    lastBPM = newBPM;
                    if(currBPM == 0) {
                        return 100;
                    } else {
                        return currBPM;
                    }
                } else {
                    count = 0;
                    steady = true;
                    currBPM = newBPM;
                    lastBPM = newBPM;
                    if(newBPM == 0) {
                        return 100;
                    } else {
                        return newBPM;
                    }
                }
            } else {
                if(!close(newBPM, currBPM)) {
                    steady = false;
                }
                lastBPM = newBPM;
                if(currBPM ==0) {
                    Log.d("sd","current BPM was 0 returned 100");
                    return 100;
                } else {
                    Log.d("currBPM: ", String.valueOf(currBPM));
                    return currBPM;
                }
            }
        }
    }

    private long[] updateLast(long newLong) {
        for (int i = 0; i < listLength - 1; i++) {
            last[i] = last[i+1];
        }
        last[listLength-1] = newLong;
        return last;
    }

    private boolean close(int num1, int num2) {
        double ratio = (1.0d * num1) / (1.0d * num2);
        if (ratio > 1+limit || ratio < 1-limit) {
            return false;
        }
        return true;
    }
}
