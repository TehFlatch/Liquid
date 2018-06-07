package com.tehflatch.aquafy;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.tehflatch.aquafy.MainActivity.appContext;
import static com.tehflatch.aquafy.MainActivity.currency;
import static com.tehflatch.aquafy.MainActivity.currentView;
import static com.tehflatch.aquafy.MainActivity.newCigarettes;
import static com.tehflatch.aquafy.MainActivity.timeOffset;
import static com.tehflatch.aquafy.MainActivity.todayStart;

public class History extends Fragment {
    public static CigaretteHistoryRecyclerAdapter recAdapter;
    @SuppressLint("StaticFieldLeak")
    private static RecyclerView historyList;
    @SuppressLint("StaticFieldLeak")
    private static ConstraintLayout loaderWrapper;
    @SuppressLint("StaticFieldLeak")
    private static ConstraintLayout emptyHolder;
    private static int daysToGoBack;

    public History() {
    }

    public static void getHistoryListCustomRange(long start, long end) {
        HandleLoader();
    }

    public static void getHistoryListLifetime() {
        HandleLoader();
        newCigarettes.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!currentView.equals("history")) {
                    newCigarettes.removeEventListener(this);
                    return;
                }
                List<CigaretteHistoryModel> cigaretteList = new ArrayList<>();
                int orderCounter = 0;
                for (DataSnapshot cig : dataSnapshot.getChildren()) {
                    double price;
                    if (cig.child("perCigCost").getValue() instanceof java.lang.Long) {
                        Long temp = (Long) cig.child("perCigCost").getValue();
                        price = temp != null ? NewCigarette.fromBaseCurrency(temp.doubleValue()) : 0;
                    } else {
                        price = NewCigarette.fromBaseCurrency((double) cig.child("perCigCost").getValue());
                    }
                    @SuppressLint("DefaultLocale") String priceString = currency + " " + String.format("%.2f", price);
                    orderCounter++;
                    cigaretteList.add(new CigaretteHistoryModel(cig.getKey(), TimeParser.getFullDate((long) cig.child("addedOn").getValue(), "dd-MMM-yyyy HH:mm"), String.valueOf(orderCounter), priceString, cig.child("brand").getValue(String.class), orderCounter));
                }
                if (orderCounter == 0 && emptyHolder != null) {
                    emptyHolder.setVisibility(View.VISIBLE);
                }
                HandleRecyclerView(historyList, cigaretteList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void getHistoryList() {
        if (daysToGoBack == 0) {
            getHistoryListLifetime();
        } else {
            long daysToGoBackInSec = 86400 * daysToGoBack;
            long startTimestamp = todayStart.getTimeInMillis() - (daysToGoBackInSec * 1000);
            HandleLoader();
            newCigarettes.orderByChild("timestamp").endAt((startTimestamp - timeOffset) * -1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!currentView.equals("history")) {
                        newCigarettes.removeEventListener(this);
                        return;
                    }
                    List<CigaretteHistoryModel> cigaretteList = new ArrayList<>();
                    int orderCounter = 0;
                    for (DataSnapshot cig : dataSnapshot.getChildren()) {
                        double price;
                        if (cig.child("perCigCost").getValue() instanceof java.lang.Long) {
                            Long temp = (Long) cig.child("perCigCost").getValue();
                            price = temp != null ? NewCigarette.fromBaseCurrency(temp.doubleValue()) : 0;
                        } else {
                            price = NewCigarette.fromBaseCurrency((double) cig.child("perCigCost").getValue());
                        }
                        @SuppressLint("DefaultLocale") String priceString = currency + " " + String.format("%.2f", price);
                        orderCounter++;
                        cigaretteList.add(new CigaretteHistoryModel(cig.getKey(), TimeParser.getFullDate((long) cig.child("addedOn").getValue(), "dd-MMM-yyyy HH:mm"), String.valueOf(orderCounter), priceString, cig.child("brand").getValue(String.class), orderCounter));
                    }
                    HandleRecyclerView(historyList, cigaretteList);
                    if (orderCounter == 0 && emptyHolder != null) {
                        emptyHolder.setVisibility(View.VISIBLE);
                        historyList.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static void HandleRecyclerView(RecyclerView target, List<CigaretteHistoryModel> cigaretteList) {
        recAdapter = new CigaretteHistoryRecyclerAdapter(cigaretteList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(appContext);
        if (target != null) {
            target.setLayoutManager(mLayoutManager);
            target.setHasFixedSize(false);
            target.setAdapter(recAdapter);
        }
        HandleLoader();
    }

    private static void HandleLoader() {
        if (emptyHolder != null) emptyHolder.setVisibility(View.GONE);
        if (loaderWrapper != null) {
            if (loaderWrapper.getVisibility() == View.GONE) {
                loaderWrapper.setVisibility(View.VISIBLE);
                historyList.setVisibility(View.GONE);
            } else {
                loaderWrapper.setVisibility(View.GONE);
                historyList.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        historyList = view.findViewById(R.id.historyList);
        loaderWrapper = view.findViewById(R.id.loader);
        emptyHolder = view.findViewById(R.id.emptyHolder);
        currentView = "history";
        Spinner historyRange = view.findViewById(R.id.historyRange);
        historyRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (recAdapter != null ) {
                    recAdapter.notifyDataSetChanged();
                }
                switch (i) {
                    case 0:
                        daysToGoBack = 7;
                        getHistoryList();
                        break;
                    case 1:
                        daysToGoBack = 30;
                        getHistoryList();
                        break;
                    case 2:
                        daysToGoBack = 90;
                        getHistoryList();
                        break;
                    case 3:
                        daysToGoBack = 0;
                        getHistoryListLifetime();
                        break;
                    default:
                        daysToGoBack = 7;
                        getHistoryList();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }
}
