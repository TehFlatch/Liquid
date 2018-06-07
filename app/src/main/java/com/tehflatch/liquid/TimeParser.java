package com.tehflatch.liquid;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static com.tehflatch.liquid.MainActivity.calendar;

public class TimeParser {

    public static String ParseTimeOfDay(int hour, int minute) {
        hour = MainActivity.calendar.get(Calendar.HOUR_OF_DAY);
        String hourString = String.valueOf(hour);
        minute = MainActivity.calendar.get(Calendar.MINUTE);
        String minuteString = String.valueOf(minute);
        if (minute < 10) {
            minuteString += "0";
        }
        return hourString + ":" + minuteString;
    }

    public static int getHour() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.MINUTE);
    }

    public static int hourToMinutes(int hour, int minute) {
        int time = minute;
        if (hour > 0) {
            time += (hour * 60);
        }
        return time;
    }

    public static String getFullDate(long timestamp, String pattern) {
        String date;
        TimeZone timeZone = TimeZone.getDefault();
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        sdf.setTimeZone(timeZone);
        date = sdf.format(calendar.getTime());
        return date;
    }

    public static long getDayInMilliseconds(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfDayinMilliseconds(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }
}
