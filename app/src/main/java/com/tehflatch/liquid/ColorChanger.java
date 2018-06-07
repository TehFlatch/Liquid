package com.tehflatch.aquafy;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ColorChanger {
    public static void ChangeColors(int rounded, TextView cigCounterText, ProgressBar progressBar) {
//        float percentage = ((float) cigCounter/cigDaily) * 10000; //getting percent from max
//        int rounded = Math.round(percentage); //must use int for progress
        if (rounded <= 4000) {
            cigCounterText.setTextColor(Color.parseColor("#64dd17"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#64dd17")));
            }
        } else if (rounded > 4000 && rounded <= 7000) {
            cigCounterText.setTextColor(Color.parseColor("#ffc107"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#ffc107")));
            }
        } else if (rounded > 7000 && rounded <= 10000) {
            cigCounterText.setTextColor(Color.parseColor("#d50000"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#d50000")));
            }
        } else {
            cigCounterText.setTextColor(Color.parseColor("#000000"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
            }
        }
    }
}
