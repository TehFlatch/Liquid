package com.tehflatch.aquafy;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import static com.tehflatch.aquafy.MainActivity.cigarettes;
import static com.tehflatch.aquafy.MainActivity.cigsMonth;
import static com.tehflatch.aquafy.MainActivity.currency;
import static com.tehflatch.aquafy.MainActivity.currentView;
import static com.tehflatch.aquafy.MainActivity.monthlySpentString;
import static com.tehflatch.aquafy.MainActivity.newCigarettes;
import static com.tehflatch.aquafy.MainActivity.pricePerCig;
import static com.tehflatch.aquafy.MainActivity.showSpent;
import static com.tehflatch.aquafy.MainActivity.show_banners;
import static com.tehflatch.aquafy.MainActivity.timeOffset;
import static com.tehflatch.aquafy.MainActivity.todaySpentString;
import static com.tehflatch.aquafy.MainActivity.todayStart;
import static com.tehflatch.aquafy.MainActivity.useNewCigs;
import static com.tehflatch.aquafy.Overview.cigCounter;
import static com.tehflatch.aquafy.Overview.cigMonthly;
import static com.tehflatch.aquafy.Overview.thisYear;


@SuppressWarnings("deprecation")
public class Statistics extends Fragment {
    @SuppressLint("StaticFieldLeak")
    public static TextView tvMonthSpent;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvTodaySpent;
    public static GraphView weekly_monthly;
    public Context mContext;
    public Spinner spinner;
    public Date maxWeek, minWeek, maxMonth, minMonth;

    public Statistics() {

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public static void MoneyStatistics() {
        if (!useNewCigs) {
            if (tvMonthSpent != null) {
                double monthPrice = pricePerCig * cigMonthly;
                tvMonthSpent.setText(String.format("%.2f", monthPrice) + " " + currency);
            }
            if (tvTodaySpent != null) {
                double todayPrice = pricePerCig * cigCounter;
                tvTodaySpent.setText(String.format("%.2f", todayPrice) + " " + currency);
            }
        } else {
            if (tvMonthSpent != null) {
                tvMonthSpent.setText(monthlySpentString + " " + currency);
            }
            if (tvTodaySpent != null) {
                tvTodaySpent.setText(todaySpentString + " " + currency);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        currentView = "statistics";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        maxWeek = cal.getTime();
        maxMonth = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        minWeek = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -23);
        minMonth = cal.getTime();
        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        if (show_banners) {
            mAdView.loadAd(adRequest);
        }
        weekly_monthly = view.findViewById(R.id.weekly_monthly_graph);
        tvMonthSpent = view.findViewById(R.id.monthSpent);
        tvTodaySpent = view.findViewById(R.id.todaySpent);
        spinner = view.findViewById(R.id.range_select);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        WeekStatistics(weekly_monthly);
                        break;
                    case 1:
                        MonthStatistics(weekly_monthly);
                        break;
                    default:
                        WeekStatistics(weekly_monthly);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (showSpent) {
            view.findViewById(R.id.spentCard).setVisibility(View.VISIBLE);
            MoneyStatistics();
        }
        return view;
    }

    private void WeekStatistics(final GraphView target) {
        if (useNewCigs) {
            if (newCigarettes != null) {
                final long startSevenDays = todayStart.getTimeInMillis() - (518400 * 1000);
                final LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>();
                target.getGridLabelRenderer().setNumHorizontalLabels(7);
                target.getGridLabelRenderer().setNumVerticalLabels(10);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
                target.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(mContext, sdf));
                target.getGridLabelRenderer().setHorizontalLabelsAngle(140);
                target.getGridLabelRenderer().setTextSize(25);
                target.getGridLabelRenderer().setLabelHorizontalHeight(100);
                target.getGridLabelRenderer().setHighlightZeroLines(true);
                target.getViewport().setMinX(TimeParser.getDayInMilliseconds(startSevenDays));
                target.getViewport().setMaxX(todayStart.getTimeInMillis() + 120000); //120000 to offset visually
                target.getViewport().setMinY(0);
                target.getViewport().setMaxY(10);
                target.getViewport().setYAxisBoundsManual(true);
                target.getViewport().setXAxisBoundsManual(true);
                lineSeries.setColor(getResources().getColor(R.color.primary));
                lineSeries.setDrawBackground(true);
                lineSeries.setBackgroundColor(getResources().getColor(R.color.primary_transparent));
                target.getGridLabelRenderer().setHumanRounding(false);

                target.removeAllSeries();
                target.addSeries(lineSeries);

                newCigarettes.orderByChild("addedOn").startAt(startSevenDays - timeOffset).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayMap<Long, Integer> values = new ArrayMap<>(7);
                        int counter = 0;
                        double currentMaxY = target.getViewport().getMaxY(false);
                        for (DataSnapshot cig : dataSnapshot.getChildren()) {
                            long milliseconds = TimeParser.getDayInMilliseconds((long) cig.child("addedOn").getValue() + timeOffset);
                            if (values.get(milliseconds) != null) {
                                counter++;
                                values.put(milliseconds, counter);
                            } else {
                                counter = 1;
                                values.put(milliseconds, counter);
                            }
                            if (counter > currentMaxY) {
                                currentMaxY = counter + 2;
                                target.getViewport().setMaxY(currentMaxY);
                            }
                        }
                        TreeMap<Long, Integer> sortedValues = new TreeMap<Long, Integer>(
                                new Comparator<Long>() {

                                    @Override
                                    public int compare(Long o1, Long o2) {
                                        return o1.compareTo(o2);
                                    }

                                });
                        sortedValues.putAll(values);
                        for (Map.Entry entry : sortedValues.entrySet()) {
                            Log.d("qweqwe", "onDataChange: Key : " + entry.getKey()
                                    + " Value : " + entry.getValue());
                            lineSeries.appendData(new DataPoint((Long) entry.getKey(), (int) entry.getValue()), false, 7, false);
                        }
                        target.refreshDrawableState();
//                        for (int i = 0; i < sortedValues.size(); i++) {
//                            lineSeries.appendData(new DataPoint(sortedValues.keyAt(i), sortedValues.get(sortedValues.keyAt(i))), false, 7, false);
//                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
//            1513983600000
//            1514070000000
//            1513897200000
        } else {
            final LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>();
            target.getGridLabelRenderer().setNumHorizontalLabels(7);
            target.getGridLabelRenderer().setNumVerticalLabels(10);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
            target.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(mContext, sdf));

            target.getGridLabelRenderer().setHorizontalLabelsAngle(140);
            target.getGridLabelRenderer().setTextSize(25);
            target.getGridLabelRenderer().setLabelHorizontalHeight(100);
            target.getGridLabelRenderer().setHighlightZeroLines(true);
            target.getViewport().setMinX(minWeek.getTime());
            target.getViewport().setMaxX(maxWeek.getTime());
            target.getViewport().setMinY(0);
            target.getViewport().setMaxY(10);
            target.getViewport().setYAxisBoundsManual(true);
            target.getViewport().setXAxisBoundsManual(true);
            lineSeries.setColor(getResources().getColor(R.color.primary));
            lineSeries.setDrawBackground(true);
            lineSeries.setBackgroundColor(getResources().getColor(R.color.primary_transparent));
            target.getGridLabelRenderer().setHumanRounding(false);

            target.removeAllSeries();
            target.addSeries(lineSeries);

            cigsMonth.limitToLast(7).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double currentMaxY = target.getViewport().getMaxY(false);
                    for (DataSnapshot day : dataSnapshot.getChildren()) {
                        Date dateDay = new Date(Integer.parseInt(cigsMonth.getParent().getKey()) - 1900, Integer.parseInt(cigsMonth.getKey()) - 1, Integer.parseInt(day.getKey()));
                        lineSeries.appendData(new DataPoint(dateDay, day.getChildrenCount()), false, 7, false);
                        if (day.getChildrenCount() > currentMaxY) {
                            currentMaxY = day.getChildrenCount() + 2;
                            target.getViewport().setMaxY(currentMaxY);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private void MonthStatistics(final GraphView target) {
        if (useNewCigs) {
            if (newCigarettes != null) {
                long startThirtyDays = todayStart.getTimeInMillis() - (2592000L * 1000);
                final LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>();
                target.getGridLabelRenderer().setNumHorizontalLabels(15);
                target.getGridLabelRenderer().setNumVerticalLabels(10);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
                target.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(mContext, sdf));
                target.getGridLabelRenderer().setHorizontalLabelsAngle(140);
                target.getGridLabelRenderer().setTextSize(25);
                target.getGridLabelRenderer().setLabelHorizontalHeight(100);
                target.getGridLabelRenderer().setHighlightZeroLines(true);
                target.getViewport().setMinX(TimeParser.getDayInMilliseconds(startThirtyDays));
                target.getViewport().setMaxX(todayStart.getTimeInMillis() + 120000); //120000 to offset visually
                target.getViewport().setMinY(0);
                target.getViewport().setMaxY(10);
                target.getViewport().setYAxisBoundsManual(true);
                target.getViewport().setXAxisBoundsManual(true);
                lineSeries.setColor(getResources().getColor(R.color.primary));
                lineSeries.setDrawBackground(true);
                lineSeries.setBackgroundColor(getResources().getColor(R.color.primary_transparent));
                target.getGridLabelRenderer().setHumanRounding(false);

                target.removeAllSeries();
                target.addSeries(lineSeries);

                newCigarettes.orderByChild("addedOn").startAt(startThirtyDays - timeOffset).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayMap<Long, Integer> values = new ArrayMap<>(30);
                        int counter = 0;
                        double currentMaxY = target.getViewport().getMaxY(false);
                        for (DataSnapshot cig : dataSnapshot.getChildren()) {
                            long milliseconds = TimeParser.getDayInMilliseconds((long) cig.child("addedOn").getValue() + timeOffset);
                            if (values.get(milliseconds) != null) {
                                counter++;
                                values.put(milliseconds, counter);
                            } else {
                                counter = 1;
                                values.put(milliseconds, counter);
                            }
                            if (counter > currentMaxY) {
                                currentMaxY = counter + 2;
                                target.getViewport().setMaxY(currentMaxY);
                            }
                        }
                        TreeMap<Long, Integer> sortedValues = new TreeMap<Long, Integer>(
                                new Comparator<Long>() {

                                    @Override
                                    public int compare(Long o1, Long o2) {
                                        return o1.compareTo(o2);
                                    }

                                });
                        sortedValues.putAll(values);
                        for (Map.Entry entry : sortedValues.entrySet()) {
                            Log.d("qweqwe", "onDataChange: Key : " + entry.getKey()
                                    + " Value : " + entry.getValue());
                            lineSeries.appendData(new DataPoint((Long) entry.getKey(), (int) entry.getValue()), false, 30, false);
                        }
                        target.refreshDrawableState();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } else {
            final LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>();
            target.getViewport().setMinX(minMonth.getTime());
            target.getViewport().setMaxX(maxMonth.getTime());
            target.getViewport().setMinY(0);
            target.getViewport().setMaxY(10);
            target.getGridLabelRenderer().setNumHorizontalLabels(1);
            lineSeries.setColor(getResources().getColor(R.color.primary));
            lineSeries.setDrawBackground(true);
            lineSeries.setBackgroundColor(getResources().getColor(R.color.primary_transparent));
            target.removeAllSeries();
            target.addSeries(lineSeries);
            cigsMonth.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final int month = MainActivity.calendar.get(Calendar.MONTH);
                    if (dataSnapshot.getChildrenCount() < 30) {
                        if (month > 0) {
                            final DatabaseReference lastMonth = cigarettes.child(thisYear).child(String.valueOf(month));
                            lastMonth.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot lastMonthDataSnapshot) {
                                    double currentMaxY = target.getViewport().getMaxY(true);
                                    for (DataSnapshot day : lastMonthDataSnapshot.getChildren()) {
                                        Date dateDay = new Date(Integer.parseInt(cigsMonth.getParent().getKey()) - 1900, Integer.parseInt(lastMonth.getKey()) - 1, Integer.parseInt(day.getKey()));
                                        lineSeries.appendData(new DataPoint(dateDay, day.getChildrenCount()), false, 30, false);
                                        if (day.getChildrenCount() > currentMaxY) {
                                            currentMaxY = day.getChildrenCount() + 2;
                                            target.getViewport().setMaxY(currentMaxY);
                                        }
                                    }
                                    for (DataSnapshot day : dataSnapshot.getChildren()) {
                                        currentMaxY = target.getViewport().getMaxY(true);
                                        Date dateDay = new Date(Integer.parseInt(cigsMonth.getParent().getKey()) - 1900, Integer.parseInt(cigsMonth.getKey()) - 1, Integer.parseInt(day.getKey()));
                                        lineSeries.appendData(new DataPoint(dateDay, day.getChildrenCount()), false, 30, false);
                                        if (day.getChildrenCount() > currentMaxY) {
                                            currentMaxY = day.getChildrenCount() + 2;
                                            target.getViewport().setMaxY(currentMaxY);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            for (DataSnapshot day : dataSnapshot.getChildren()) {
                                double currentMaxY = target.getViewport().getMaxY(true);
                                Date dateDay = new Date(Integer.parseInt(cigsMonth.getParent().getKey()) - 1900, Integer.parseInt(cigsMonth.getKey()) - 1, Integer.parseInt(day.getKey()));
                                lineSeries.appendData(new DataPoint(dateDay, day.getChildrenCount()), false, 30, false);
                                if (day.getChildrenCount() > currentMaxY) {
                                    currentMaxY = day.getChildrenCount() + 2;
                                    target.getViewport().setMaxY(currentMaxY);
                                }
                            }
                        }
                    } else {
                        for (DataSnapshot day : dataSnapshot.getChildren()) {
                            double currentMaxY = target.getViewport().getMaxY(true);
                            Date dateDay = new Date(Integer.parseInt(cigsMonth.getParent().getKey()) - 1900, Integer.parseInt(cigsMonth.getKey()) - 1, Integer.parseInt(day.getKey()));
                            lineSeries.appendData(new DataPoint(dateDay, day.getChildrenCount()), false, 30, false);
                            if (day.getChildrenCount() > currentMaxY) {
                                currentMaxY = day.getChildrenCount() + 2;
                                target.getViewport().setMaxY(currentMaxY);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}
