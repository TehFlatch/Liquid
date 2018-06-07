package com.tehflatch.liquid;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;

import static com.tehflatch.liquid.MainActivity.brandName;
import static com.tehflatch.liquid.MainActivity.editor;
import static com.tehflatch.liquid.MainActivity.hoursString;
import static com.tehflatch.liquid.MainActivity.mInterstitial;
import static com.tehflatch.liquid.MainActivity.minuteString;
import static com.tehflatch.liquid.MainActivity.minutesString;
import static com.tehflatch.liquid.MainActivity.settings;
import static com.tehflatch.liquid.Overview.calendar;
import static com.tehflatch.liquid.Overview.cigCounter;
import static com.tehflatch.liquid.Overview.cigDaily;
import static com.tehflatch.liquid.Overview.percentage;
import static com.tehflatch.liquid.Overview.rounded;


public class Cigarette {
    private static final String TAG = "Cigarette";
    private String brand, minutesOfDay;

    public Cigarette() {

    }

    public Cigarette(String brand, String minutesOfDay) {
        this.brand = brand;
        this.minutesOfDay = minutesOfDay;
    }

    //Widget
    public static void AddCigarette(int count, SharedPreferences prefs, SharedPreferences.Editor prefsEditor) {
        cigCounter = prefs.getInt("cigCounter", 0);
        String brandsToDump = prefs.getString("brandsToDump", "");
        String timestampsToDump = prefs.getString("timestampsToDump", "");
        String cigCount = prefs.getString("cigCount", "");
        int cigsToDump = prefs.getInt("cigsToDump", 0);
        for (int i = 0; i < count; i++) {
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            int oldDay = prefs.getInt("day_of_year", 0);
            if (oldDay != day) {
                prefsEditor.putInt("day_of_year", day);
                prefsEditor.commit();
                prefsEditor.putInt("cigCounter", 0);
                prefsEditor.commit();
                cigCounter = 0;
            }
            cigCounter++;
            prefsEditor.putInt("cigCounter", cigCounter);
            int timeMinutes = TimeParser.hourToMinutes(TimeParser.getHour(), TimeParser.getMinute());
            brandsToDump += prefs.getString("defaultBrand", "Widget Add") + ",";
            prefsEditor.putString("brandsToDump", brandsToDump);
            timestampsToDump += String.valueOf(timeMinutes) + ",";
            cigCount += String.valueOf(cigCounter) + ",";
            cigsToDump++;
            prefsEditor.putString("cigCount", cigCount);
            prefsEditor.putString("timestampsToDump", timestampsToDump);
            prefsEditor.putInt("cigsToDump", cigsToDump);
            prefsEditor.putInt("timeMinutes", timeMinutes);
            prefsEditor.commit();
        }
    }

    public static void AddCigarette(int count, TextView cigCounterText, ProgressBar progressBar, TextView timeSince, DatabaseReference cigs) {
        for (int i = 0; i < count; i++) {
            int oldRound = rounded;
            cigCounter++;
            Log.d(TAG, "AddCigarette: " + cigs);
            percentage = ((float) cigCounter / cigDaily) * 10000; //getting percent from max
            rounded = Math.round(percentage); //must use int for progress
            editor.putInt("cigCounter", cigCounter);
            int timeMinutes = TimeParser.hourToMinutes(TimeParser.getHour(), TimeParser.getMinute());
            cigs.setValue(new Cigarette(brandName, String.valueOf(timeMinutes)));
            editor.putInt("timeMinutes", timeMinutes);
            editor.commit();
            cigCounterText.setText(String.valueOf(cigCounter));
            ColorChanger.ChangeColors(rounded, cigCounterText, progressBar);
            ObjectAnimator additionalAnimation = ObjectAnimator.ofInt(progressBar, "progress", oldRound, rounded);
            additionalAnimation.setDuration(1000); //in milliseconds
            additionalAnimation.setInterpolator(new DecelerateInterpolator());
            additionalAnimation.start();
            progressBar.clearAnimation();
            if (cigCounter > cigDaily) {
                if (mInterstitial.isLoaded()) {
                    mInterstitial.show();
                }
            }
        }
    }

    public static void RemoveCigarette(int count, TextView cigCounterText, ProgressBar progressBar, final DatabaseReference cigs) {
        for (int i = 0; i < count; i++) {
            int oldRound = rounded;
            cigCounter--;
            percentage = ((float) cigCounter / cigDaily) * 10000; //getting percent from max
            rounded = Math.round(percentage); //must use int for progress
            editor.putInt("cigCounter", cigCounter);
            editor.commit();
            cigCounterText.setText(String.valueOf(cigCounter));
            ColorChanger.ChangeColors(rounded, cigCounterText, progressBar);
            ObjectAnimator additionalAnimation = ObjectAnimator.ofInt(progressBar, "progress", oldRound, rounded);
            additionalAnimation.setDuration(1000); //in milliseconds
            additionalAnimation.setInterpolator(new DecelerateInterpolator());
            additionalAnimation.start();
            progressBar.clearAnimation();
            cigs.removeValue();
        }
    }

    public static String timeSinceLastCigarette(String timeSince, String noCigSmokedString) {
        int minute, hour;
        int tmpTime;
        String time;
        int timeMinutes = settings.getInt("timeMinutes", 0);
        hour = TimeParser.getHour();
        minute = TimeParser.getMinute();
        int newTimeMinutes = TimeParser.hourToMinutes(hour, minute);
        tmpTime = newTimeMinutes - timeMinutes;
        if (Overview.cigCounter != 0) {
            if (tmpTime > 60) {
                hour = tmpTime / 60;
                minute = tmpTime % 60;
                time = hour + " " + hoursString + " & " + minute + " " + minutesString + " ";
            } else {
                minute = tmpTime;
                if (tmpTime == 1) {
                    time = minute + " " + minuteString + " ";
                } else {
                    time = minute + " " + minutesString + " ";
                }
            }
            time = String.format(timeSince, time);
        } else {
            time = noCigSmokedString;
        }
        return time;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getMinutesOfDay() {
        return minutesOfDay;
    }

    public void setMinutesOfDay(String minutesOfDay) {
        this.minutesOfDay = minutesOfDay;
    }
    @Override
    public String toString() {
        return "Cigarette{" +
                "brand='" + brand + '\'' +
                ", minutesOfDay='" + minutesOfDay + '\'' +
                '}';
    }
}
