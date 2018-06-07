package com.tehflatch.liquid;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.tehflatch.liquid.MainActivity.currencies;
import static com.tehflatch.liquid.MainActivity.currency;
import static com.tehflatch.liquid.MainActivity.currencyRate;
import static com.tehflatch.liquid.MainActivity.currencyTimestamp;
import static com.tehflatch.liquid.MainActivity.editor;
import static com.tehflatch.liquid.MainActivity.languageSelected;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */


public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final String TAG = "SettingsActivity";

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof ListPreference) {
                String stringValue = value.toString();
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                Log.d(TAG, "onPreferenceChange: VALUE: " + stringValue);
                Log.d(TAG, "onPreferenceChange: Key: " + preference.getKey());
                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
//                MainActivity.brands.child("currency").setValue(stringValue);
//                MainActivity.userPrefs.child("currency").setValue(stringValue);
                if (preference.getKey().equals("defaultCurrency")) {
                    MainActivity.brands.child("currency").setValue(stringValue);
                    MainActivity.userPrefs.child("currency").setValue(stringValue);
                    currency = stringValue;

                    if (!currency.equals("USD")) {
                        currencies.child("rates/" + currency).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                    currencyRate = Double.valueOf(dataSnapshot.getValue().toString());
                                    editor.putString("currencyRate", String.valueOf(currencyRate));
                                    currencyTimestamp = Calendar.getInstance().getTimeInMillis();
                                    editor.putLong("currencyTimestamp", currencyTimestamp);
                                    editor.apply();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        currencyRate = 1.00;
                    }
                }
            } else if (preference instanceof CheckBoxPreference) {
                if (preference.getKey().equals("force_english")) {
                    MainActivity.userPrefs.child("forceEnglish").setValue(value);
                } else if (preference.getKey().equals("colored_navigation")) {
                    MainActivity.userPrefs.child("coloredNavigation").setValue(value);
                }
            } else {
                String stringValue = value.toString();
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
                while (MainActivity.brands == null || MainActivity.userPrefs == null) {
                    Log.d(TAG, "onPreferenceChange: NULL POINTER IN DB!");
                }
                if (preference.getKey().equals("defaultBrand")) {
                    MainActivity.brands.child("default").setValue(stringValue);
                    MainActivity.userPrefs.child("default").setValue(stringValue);
                } else if (preference.getKey().equals("defaultPrice")) {
                    MainActivity.brands.child("price").setValue(stringValue);
                    MainActivity.userPrefs.child("price").setValue(stringValue);
                } else if (preference.getKey().equals("defaultCurrency")) {
                    MainActivity.brands.child("currency").setValue(stringValue);
                    MainActivity.userPrefs.child("currency").setValue(stringValue);
                    currencies.child("rates/" + currency).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null) {
                                currencyRate = Double.valueOf(dataSnapshot.getValue().toString());
                                editor.putString("currencyRate", String.valueOf(currencyRate));
                                currencyTimestamp = Calendar.getInstance().getTimeInMillis();
                                editor.putLong("currencyTimestamp", currencyTimestamp);
                                editor.apply();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else if (preference.getKey().equals("defaultCount")) {
                    MainActivity.brands.child("packCount").setValue(stringValue);
                    MainActivity.userPrefs.child("packCount").setValue(stringValue);
                } else if (preference.getKey().equals("cigDaily")) {
                    MainActivity.userPrefs.child("cigDaily").setValue(stringValue);
                }
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
//        if (!preference.getKey().equals("force_english")) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof CheckBoxPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (forceEnglish) {
//            /* Start Locale */
//
//            String languageToLoad = "en"; // your language
//            Locale locale = new Locale(languageToLoad);
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config,
//                    getBaseContext().getResources().getDisplayMetrics());
//            /* End Locale */
//        } else {
//            /* Start Locale */
//            LocaleListCompat localeList = LocaleListCompat.getDefault();
//            String languageToLoad = localeList.get(0).toString().split("_")[0];
//            // your language
//            Locale locale = new Locale(languageToLoad);
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config,
//                    getBaseContext().getResources().getDisplayMetrics());
//            /* End Locale */
//        }
        String languageToLoad = languageSelected;
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_activity_settings);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            //}
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("cigDaily"));
            bindPreferenceSummaryToValue(findPreference("defaultBrand"));
            bindPreferenceSummaryToValue(findPreference("defaultPrice"));
            bindPreferenceSummaryToValue(findPreference("defaultCurrency"));
            bindPreferenceSummaryToValue(findPreference("defaultCount"));
//            bindPreferenceSummaryToValue(findPreference("force_english"));
            bindPreferenceSummaryToValue(findPreference("colored_navigation"));
            bindPreferenceSummaryToValue(findPreference("old_layout"));
            bindPreferenceSummaryToValue(findPreference("app_language"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
