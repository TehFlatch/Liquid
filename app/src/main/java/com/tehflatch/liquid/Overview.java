package com.tehflatch.aquafy;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.tehflatch.aquafy.MainActivity.cigarettes;
import static com.tehflatch.aquafy.MainActivity.cigsMonth;
import static com.tehflatch.aquafy.MainActivity.currency;
import static com.tehflatch.aquafy.MainActivity.currentView;
import static com.tehflatch.aquafy.MainActivity.interstitialTimestamp;
import static com.tehflatch.aquafy.MainActivity.mInterstitial;
import static com.tehflatch.aquafy.MainActivity.monthSmoked;
import static com.tehflatch.aquafy.MainActivity.monthlySpentString;
import static com.tehflatch.aquafy.MainActivity.newCigarettes;
import static com.tehflatch.aquafy.MainActivity.old_layout;
import static com.tehflatch.aquafy.MainActivity.rewardVideoTriggered;
import static com.tehflatch.aquafy.MainActivity.settings;
import static com.tehflatch.aquafy.MainActivity.showSpent;
import static com.tehflatch.aquafy.MainActivity.show_banners;
import static com.tehflatch.aquafy.MainActivity.thisMonthStart;
import static com.tehflatch.aquafy.MainActivity.timeOffset;
import static com.tehflatch.aquafy.MainActivity.todaySpentString;
import static com.tehflatch.aquafy.MainActivity.todayStart;
import static com.tehflatch.aquafy.MainActivity.useNewCigs;
import static com.tehflatch.aquafy.MainActivity.yesterdaySmoked;
import static com.tehflatch.aquafy.MainActivity.yesterdayStart;

//import com.google.firebase.database.DatabaseReference;


public class Overview extends Fragment {

    public static int cigCounter, cigDaily, cigMonthly;
    @SuppressLint("StaticFieldLeak")
    public static TextView cigCounterText, dailyCigCountText, timeSince;
    public static String dailyString;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressBar;
    public static Calendar calendar = Calendar.getInstance();
    public static float percentage;
    public static int rounded;
    @SuppressLint("StaticFieldLeak")
    public static TextView monthlyCigCountText;
    @SuppressLint("StaticFieldLeak")
    public static TextView yesterdayCounter;
    public static String yesterday;
    public static int yesterdayInt;
    public static String thisYear = String.valueOf(calendar.get(Calendar.YEAR));
    public static String thisMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);
    //public static String thisDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    public static ObjectAnimator animation;
    public static boolean firstRun = true;
    public static ValueEventListener todayListener, yesterdayListener, monthListener;
    public static RecyclerView.OnClickListener historyClickListener;
    @SuppressLint("StaticFieldLeak")
    public static RecyclerView todayHistoryList;
    public SharedPreferences sharedPref;
    public SharedPreferences.Editor editor;

    public Overview() {
    }

    public static void CallbackDatabaseReference() {
        if (!useNewCigs) {
            cigarettes.child(thisYear).child(thisMonth).child(String.valueOf(yesterdayInt)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    yesterday = String.valueOf(dataSnapshot.getChildrenCount());
                    if (yesterdayCounter != null) {
                        yesterdayCounter.setText(yesterday);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            cigsMonth.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Overview.OnDataChangeMonthlyText(dataSnapshot);
                    Statistics.MoneyStatistics();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            if (newCigarettes != null) {
                if (todayListener != null)
                    newCigarettes.orderByChild("addedOn").startAt(todayStart.getTimeInMillis() - timeOffset, "addedOn").removeEventListener(todayListener);
                if (yesterdayListener != null)
                    newCigarettes.orderByChild("addedOn").startAt(yesterdayStart.getTimeInMillis() - timeOffset, "addedOn").endAt(todayStart.getTimeInMillis() - timeOffset, "addedOn").removeEventListener(yesterdayListener);
                if (monthListener != null)
                    newCigarettes.orderByChild("addedOn").startAt(thisMonthStart.getTimeInMillis() - timeOffset, "addedOn").removeEventListener(monthListener);

                    //Today count
                    newCigarettes.orderByChild("addedOn").startAt(todayStart.getTimeInMillis() - timeOffset, "addedOn").addValueEventListener(todayListener);
                    //Yesterday count
                    newCigarettes.orderByChild("addedOn").startAt(yesterdayStart.getTimeInMillis() - timeOffset, "addedOn").endAt(todayStart.getTimeInMillis() - timeOffset, "addedOn").addValueEventListener(yesterdayListener);
                    //This month count
                    newCigarettes.orderByChild("addedOn").startAt(thisMonthStart.getTimeInMillis() - timeOffset, "addedOn").addValueEventListener(monthListener);
                }
        }
    }

    public static void OnDataChangeMonthlyText(DataSnapshot dataSnapshot) {
        cigMonthly = 0;
        for (DataSnapshot day : dataSnapshot.getChildren()) {
            cigMonthly += day.getChildrenCount();
        }
        monthlyCigCountText.setText(String.valueOf(cigMonthly));
    }

    //TODO typoooo
    public static boolean OnFabClickAddCigarete(String timeSinceString, String noCigSmoked, String noConnection, Context context) {
        if (newCigarettes != null) {
            NewCigarette.AddCigarette(1);
            return true;
        } else {
            Toast.makeText(context, noConnection,
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static void OnFabClickAddCigaretteUndo() {
        NewCigarette.RemoveCigarette(1, cigCounterText, progressBar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getApplicationContext();
    }

    @SuppressLint({"SetTextI18n", "CommitPrefEdits"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        old_layout = settings.getBoolean("old_layout", false);
        if (old_layout) {
            view = inflater.inflate(R.layout.fragment_overview, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_overview_new, container, false);
            todayHistoryList = view.findViewById(R.id.todayHistoryList);
        }
        //Views
        currentView = "overview";
        cigCounterText = view.findViewById(R.id.actualCigCount);
        dailyCigCountText = view.findViewById(R.id.dailyCigCount);
        timeSince = view.findViewById(R.id.timeSince);
        yesterdayCounter = view.findViewById(R.id.yesterdayCounter);
        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        if (show_banners) {
            mAdView.loadAd(adRequest);
        }
        if (historyClickListener == null) {
            historyClickListener = new RecyclerView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView deleteButton = view.findViewById(R.id.deleteButton);
                    boolean expanded = (deleteButton.getVisibility() == View.VISIBLE);
                    if (!expanded) {
                        deleteButton.setVisibility(View.VISIBLE);
                    } else {
                        deleteButton.setVisibility(View.GONE);
                    }
                }
            };
        }
        if (todayListener == null) {
            todayListener = new ValueEventListener() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String tempStr = settings.getString("cigDaily", "10");
                    cigDaily = !tempStr.equals("") ? Integer.parseInt(tempStr) : 10;
                    dailyCigCountText.setText(String.valueOf(cigDaily) + " " + dailyString);
                    cigCounter = (int) dataSnapshot.getChildrenCount();
                    cigCounterText.setText(String.valueOf(cigCounter));
                    percentage = ((float) cigCounter / cigDaily) * 10000; //getting percent from max
                    int oldRound = (firstRun) ? 0 : rounded;
                    firstRun = false;
                    rounded = Math.round(percentage); //must use int for progress
                    ColorChanger.ChangeColors(rounded, cigCounterText, progressBar);
                    animation = ObjectAnimator.ofInt(progressBar, "progress", oldRound, rounded);
                    animation.setDuration(1000); //in milliseconds
                    animation.setInterpolator(new AccelerateDecelerateInterpolator());
                    animation.start();
                    progressBar.clearAnimation();
                    if (cigCounter > cigDaily) {
                        if (mInterstitial.isLoaded()) {
                            long timestampNow = Calendar.getInstance().getTimeInMillis();
                            if (interstitialTimestamp + 60000 <= timestampNow && !rewardVideoTriggered) {
                                mInterstitial.show();
                                interstitialTimestamp = timestampNow;
                            }
                        }
                    }
                    NewCigarette.timeSinceLastCigarette();
                    double todaySpent = 0;
                    List<CigaretteHistoryModel> cigaretteList = new ArrayList<>();
                    int orderCounter = 0;
                    for (DataSnapshot cig : dataSnapshot.getChildren()) {
                        double price;
                        if (cig.child("perCigCost").getValue() instanceof java.lang.Long) {
                            Long temp = (Long) cig.child("perCigCost").getValue();
                            todaySpent += temp != null ? NewCigarette.fromBaseCurrency(temp.doubleValue()) : 0;
                            price = temp != null ? NewCigarette.fromBaseCurrency(temp.doubleValue()) : 0;
                        } else {
                            todaySpent += NewCigarette.fromBaseCurrency((double) cig.child("perCigCost").getValue());
                            price = NewCigarette.fromBaseCurrency((double) cig.child("perCigCost").getValue());
                        }
                        String priceString = currency + " " + String.format("%.2f", price);
                        orderCounter++;
                        cigaretteList.add(new CigaretteHistoryModel(cig.getKey(), TimeParser.getFullDate((long) cig.child("addedOn").getValue(), "dd-MMM-yyyy HH:mm"), String.valueOf(orderCounter), priceString, cig.child("brand").getValue(String.class), orderCounter));
                    }
                    todaySpentString = String.format("%.2f", todaySpent);
                    Statistics.MoneyStatistics();
                    if (!old_layout && currentView.equals("overview")) {
                        History.HandleRecyclerView(todayHistoryList, cigaretteList);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
        if (yesterdayListener == null) {
            yesterdayListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (yesterdayCounter != null) {
                        yesterdayCounter.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    }
                    yesterdaySmoked = dataSnapshot.getChildrenCount();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
        if (monthListener == null) {
            monthListener = new ValueEventListener() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double monthSpent = 0;
                    if (monthlyCigCountText != null) {
                        monthlyCigCountText.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    }
                    monthSmoked = dataSnapshot.getChildrenCount();
                    for (DataSnapshot cig : dataSnapshot.getChildren()) {
                        if (cig.child("perCigCost").getValue() instanceof java.lang.Long) {
                            Long temp = (Long) cig.child("perCigCost").getValue();
                            monthSpent += temp != null ? NewCigarette.fromBaseCurrency(temp.doubleValue()) : 0;
                        } else {
                            monthSpent += NewCigarette.fromBaseCurrency((double) cig.child("perCigCost").getValue());
                        }
                    }
                    monthlySpentString = String.format("%.2f", monthSpent);

                    if (showSpent) Statistics.MoneyStatistics();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setMax(10000);

        monthlyCigCountText = view.findViewById(R.id.thisMonthCounter);

        sharedPref = MainActivity.sharedPref;
        dailyString = getString(R.string.dailyString);
        dailyCigCountText.setText(String.valueOf(cigDaily) + " " + dailyString);
        //settings = MainActivity.settings;
        editor = settings.edit();
        String tempStr = settings.getString("cigDaily", "10");
        cigDaily = !tempStr.equals("") ? Integer.parseInt(tempStr) : 10;

        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        MainActivity.dumpListToDatabase();
        super.onResume();
    }
}
