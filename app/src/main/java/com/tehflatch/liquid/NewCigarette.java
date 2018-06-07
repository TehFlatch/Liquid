package com.tehflatch.liquid;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import static com.tehflatch.liquid.MainActivity.brandName;
import static com.tehflatch.liquid.MainActivity.currencyRate;
import static com.tehflatch.liquid.MainActivity.currentView;
import static com.tehflatch.liquid.MainActivity.hoursString;
import static com.tehflatch.liquid.MainActivity.minuteString;
import static com.tehflatch.liquid.MainActivity.minutesString;
import static com.tehflatch.liquid.MainActivity.newCigarettes;
import static com.tehflatch.liquid.MainActivity.pricePerCig;
import static com.tehflatch.liquid.MainActivity.timeOffset;
import static com.tehflatch.liquid.Overview.timeSince;


public class NewCigarette {
    private String brand;
    private long addedOn, timestamp;
    private double perCigCost;
    private int day, month, year;

    public NewCigarette() {

    }


    public NewCigarette(String brand, long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        this.brand = brand;
        long timeInMilliseconds = time - timeOffset;
        this.timestamp = timeInMilliseconds * -1;
        this.addedOn = timeInMilliseconds;
        this.perCigCost = toBaseCurrency(pricePerCig);
        this.day = cal.get(Calendar.DAY_OF_MONTH);
        this.month = cal.get(Calendar.MONTH) + 1;
        this.year = cal.get(Calendar.YEAR);
    }

    static void AddCigarette(int count, SharedPreferences prefs, SharedPreferences.Editor prefsEditor) {
        for (int i = 0; i < count; i++) {
            newCigarettes.push().setValue(new NewCigarette(brandName, Calendar.getInstance().getTimeInMillis()));
        }
    }

    public static void AddCigarette(int count) {
        if (newCigarettes != null) {
            for (int i = 0; i < count; i++) {
                newCigarettes.push().setValue(new NewCigarette(brandName, Calendar.getInstance().getTimeInMillis()));
            }
            if (currentView.equals("history")) History.getHistoryList();
        }
    }

    public static void AddCigaretteCustomDate(int count, long timestamp) {
        if (newCigarettes != null) {
            for (int i = 0; i < count; i++) {
                newCigarettes.push().setValue(new NewCigarette(brandName, timestamp)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (currentView.equals("history")) History.getHistoryList();
                    }
                });
            }
        }
    }

    public static void DeleteCigarette(String key) {
        newCigarettes.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (currentView.equals("history")) History.getHistoryList();
            }
        });
    }

    public static void RemoveCigarette(int count, TextView cigCounterText, ProgressBar progressBar) {
        if (newCigarettes != null) {
            for (int i = 0; i < count; i++) {
                newCigarettes.keepSynced(true);
                newCigarettes.orderByChild("addedOn").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot cigarette : dataSnapshot.getChildren()) {
                            cigarette.getRef().removeValue();
                        }
                        if (currentView.equals("history")) History.getHistoryList();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public static void timeSinceLastCigarette() {
        if (newCigarettes != null) {
            newCigarettes.keepSynced(true);
            newCigarettes.orderByChild("addedOn").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long newTimestamp = Calendar.getInstance().getTimeInMillis() - timeOffset;
                    long newTimeMinutes = 0;
                    for (DataSnapshot param : dataSnapshot.getChildren()) {
                        if (param.child("addedOn").getValue() != null) {
                            newTimeMinutes = (newTimestamp - (long) param.child("addedOn").getValue() + timeOffset) / 60000;
                        }
                    }
                    if (timeSince != null) {
                        timeSince.setText(timeSinceString(newTimeMinutes));
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static String timeSinceString(long newTimeMinutes) {
        long minute, hour;

        String time;
//                long newTimeMinutes = (newTimestamp - lastTimestamp) / 60000;
        if (Overview.cigCounter != 0) {
            if (newTimeMinutes > 60) {
                hour = newTimeMinutes / 60;
                minute = newTimeMinutes % 60;
                time = hour + " " + hoursString + " & " + minute + " " + minutesString + " ";
            } else {
                minute = newTimeMinutes;
                if (newTimeMinutes == 1) {
                    time = minute + " " + minuteString + " ";
                } else {
                    time = minute + " " + minutesString + " ";
                }
            }
            time = String.format(MainActivity.timeSinceString, time);
        } else {
            time = MainActivity.noCigSmoked;
        }
        return time;
    }


    public static double toBaseCurrency(double price) {
        if (price == 0) {
            return 0.00;
        } else {
            return price / currencyRate;
        }

    }

    public static double fromBaseCurrency(double price) {
        if (price == 0) {
            return 0.00;
        } else {
            return price * currencyRate;
        }
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getPerCigCost() {
        return perCigCost;
    }

    public void setPerCigCost(float perCigCost) {
        this.perCigCost = perCigCost;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(long addedOn) {
        this.addedOn = addedOn;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    @Override
    public String toString() {
        return "Cigarette{" +
                "brand='" + brand + '\'' +
                "timestamp='" + timestamp + '\'' +
                "addedOn='" + addedOn + '\'' +
                "perCigCost='" + perCigCost + '\'' +
                "day='" + day + '\'' +
                "month='" + month + '\'' +
                "year='" + year + '\'' +
                '}';
    }
}
