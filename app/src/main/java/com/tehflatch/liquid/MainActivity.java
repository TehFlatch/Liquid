package com.tehflatch.liquid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.os.LocaleListCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "MainActivity";
    public static FirebaseUser anonymUser;
    public static FirebaseAnalytics mAnalytics;
    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor editor;
    public static SharedPreferences settings;
    public static String hoursString, minutesString, minuteString, hourString, dailyString, noCigSmoked, timeSinceString, yesString, noString, areYouSureString, cigDeleteString;
    public static Calendar calendar = Calendar.getInstance();
    public static String brandName, currency;
    public static long currencyTimestamp;
    public static double currencyRate;
    public static boolean isBaseCurrency;
    public static double pricePerCig, price;
    public static FirebaseDatabase database;
    public static DatabaseReference cigarettes, brands, newCigarettes, userPrefs, currencies;
    public static DatabaseReference cigsToday, cigsMonth;
    public static int latestVersion;
    public static boolean showSpent;
    public static String check;
    public static String version;
    public static int verCode;
    public static boolean forceEnglish = false;
    public static boolean coloredNavigation = false;
    public static boolean useNewCigs = true;
    public static Calendar todayStart, yesterdayStart, thisMonthStart;
    public static String monthlySpentString, todaySpentString;
    public static long monthSmoked, yesterdaySmoked;
    public static int timeOffset;
    public static AlertDialog dialog;
    public static long interstitialTimestamp = 0, rewardTimestamp = 0;
    public static MenuItem reset;
    public static boolean rewardVideoTriggered = false;
    public static boolean show_banners, show_interstitial, show_reward_video;
    public static String rewardVideoID;
    public static InterstitialAd mInterstitial;
    public static boolean isConnected;
    public static boolean old_layout;
    public static String currentView = "";
    @SuppressLint("StaticFieldLeak")
    public static View tempView;
    private static RewardedVideoAd mRewardedVideoAd;
    public boolean isFABOpen = false;
    public ConstraintLayout fab_background;
    public FloatingActionButton mainFab, fabMultiSig, fabDateCig, fabSingleCig;
    public CardView fabSingleLabel, fab1Label, fab2Label;
    public TextView navTitle, navSubTitle;
    public GoogleSignInClient mGoogleSignInClient;
    public String dbuid;
    public boolean canLogin;
    public NavigationView navigationView;
    public MenuItem navSignout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static Context appContext;
    public static int popupShownInVersion;
    public static String languageSelected;
    private Boolean showWhatsNew = true;

    public static void dumpListToDatabase() {
        if (cigsToday != null) {
            String[] brandsToDump = settings.getString("brandsToDump", "").split(",");
            String[] timestampsToDump = settings.getString("timestampsToDump", "").split(",");
            String[] cigCount = settings.getString("cigCount", "").split(",");
            int cigsToDump = settings.getInt("cigsToDump", 0);
            if (cigsToDump > 0) {
                for (int c = 0; c < cigsToDump; c++) {
                    cigsToday.child(cigCount[c]).setValue(new Cigarette(brandsToDump[c], timestampsToDump[c]));
                }
                editor.remove("cigsToDump");
                editor.remove("cigCount");
                editor.remove("brandsToDump");
                editor.remove("timestampsToDump");
                editor.apply();
            }
        }
    }

    public static void videoRewardTriggered() {
        loadRewardVideo();
        newCigarettes.orderByChild("addedOn").startAt(todayStart.getTimeInMillis() - timeOffset, "addedOn")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        rewardVideoTriggered = true;
                        for (DataSnapshot cig : dataSnapshot.getChildren()) {
                            cig.getRef().removeValue();
                        }
                        rewardVideoTriggered = false;
                        rewardTimestamp = Calendar.getInstance().getTimeInMillis();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static void loadRewardVideo() {
        if (show_reward_video) {
            Log.d(TAG, "loadRewardVideo: LOAD init");
            mRewardedVideoAd.loadAd(rewardVideoID,
                    new AdRequest.Builder().build());
        }
    }

    public static void loadInterstitial() {
        if (show_interstitial) {
            final AdRequest interstitialRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
            mInterstitial.loadAd(interstitialRequest);
        }
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appContext = getApplicationContext();
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        isConnected = activeNetwork != null && activeNetwork.isConnected();
        mAnalytics = FirebaseAnalytics.getInstance(this);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        //forceEnglish = settings.getBoolean("force_english", false);
        languageSelected = settings.getString("app_language", LocaleListCompat.getDefault().get(0).toString().split("_")[0]);
        coloredNavigation = settings.getBoolean("colored_navigation", true);
        useNewCigs = settings.getBoolean("useNewCigs", true);
        show_interstitial = settings.getBoolean("show_interstitial", true);
        show_reward_video = settings.getBoolean("show_reward_video", true);
        showSpent = settings.getBoolean("showSpent", true);
        popupShownInVersion = settings.getInt("popupShownInVersion", 0);
        showWhatsNew = settings.getBoolean("showWhatsNew", true);
//        if (forceEnglish) {
//            /* Start Locale */
//            String languageToLoad = "en"; // your language
//            Locale locale = new Locale(languageToLoad);
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config,
//                    getBaseContext().getResources().getDisplayMetrics());
//            /* End Locale */
//        } else {
            /* Start Locale */
        //LocaleListCompat localeList = LocaleListCompat.getDefault();
        //String languageToLoad = localeList.get(0).toString().split("_")[0]; // your language
        String languageToLoad = languageSelected;
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
            /* End Locale */
//        }
        if (coloredNavigation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
            }
        }
        check = getString(R.string.check_currency);
        Date now = new Date();
        timeOffset = Calendar.getInstance().getTimeZone().getOffset(now.getTime()) / 1000;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verCode = pInfo != null ? pInfo.versionCode : 0;
        version = pInfo != null ? pInfo.versionName : "";
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        } else {
            Log.d(TAG, "onCreate: database already exists");
        }
        mInterstitial = new InterstitialAd(this);
        mInterstitial.setAdUnitId(getString(R.string.interstitial_ad_id));

        mInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                loadInterstitial();
            }
        });
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.d(TAG, "onRewardedVideoAdLoaded: LOADED AD");
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                loadRewardVideo();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                videoRewardTriggered();
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }
        });


        brandName = "No Brand";
        currency = settings.getString("defaultCurrency", "USD");
        pricePerCig = 0.00;
        price = 0.00;
        hoursString = getString(R.string.hours);
        hourString = getString(R.string.hour);
        minutesString = getString(R.string.minutes);
        minuteString = getString(R.string.minute);
        noCigSmoked = getString(R.string.no_cig_smoked);
        timeSinceString = getString(R.string.time_since);
        rewardVideoID = getString(R.string.rewarded_video_ad_id);

        yesString = getString(R.string.yes);
        noString = getString(R.string.no);
        areYouSureString = getString(R.string.are_you_sure);
        cigDeleteString = getString(R.string.delete_cig);

        /*Calendars*/
        todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);

        yesterdayStart = Calendar.getInstance();
        yesterdayStart.set(Calendar.HOUR_OF_DAY, 0);
        yesterdayStart.set(Calendar.MINUTE, 0);
        yesterdayStart.set(Calendar.SECOND, 0);
        yesterdayStart.set(Calendar.DAY_OF_MONTH, todayStart.get(Calendar.DAY_OF_MONTH) - 1);

        thisMonthStart = Calendar.getInstance();
        thisMonthStart.set(Calendar.HOUR_OF_DAY, 0);
        thisMonthStart.set(Calendar.MINUTE, 0);
        thisMonthStart.set(Calendar.SECOND, 0);
        thisMonthStart.set(Calendar.DAY_OF_MONTH, 1);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        navTitle = navigationView.getHeaderView(0).findViewById(R.id.nav_title);
        navSubTitle = navigationView.getHeaderView(0).findViewById(R.id.nav_subtitle);
        navSignout = navigationView.getMenu().getItem(6);

        navTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected) {
                    if (canLogin) {
                        googleSignInLink();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.connect_internet),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (user == null) {
            anonymousSignIn();
        } else {
            updateUI(user);
        }
        if (verCode < latestVersion) {
            Toast.makeText(this, getString(R.string.update_application),
                    Toast.LENGTH_SHORT).show();
        }
        //popupShownInVersion = 1;
        if (showWhatsNew && isInstallFromUpdate() && popupShownInVersion < verCode) {
            editor.putInt("popupShownInVersion", verCode);
            editor.apply();
            AlertDialog.Builder whatsNew = new AlertDialog.Builder(this);
            whatsNew.setMessage(String.format(getString(R.string.whats_new_text)))
                    .setTitle(getString(R.string.whats_new));
            whatsNew.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            whatsNew.setNeutralButton(getString(R.string.never_show), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showWhatsNew = false;
                    editor.putBoolean("showWhatsNew", false);
                    editor.apply();
                }
            });
            whatsNew.create().show();
        }
        final String thisYear = String.valueOf(calendar.get(Calendar.YEAR));
        final String thisMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        final String thisDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    updateUI(user);
                    dbuid = user.getUid();
                    userPrefs = database.getReference("users/" + dbuid + "/userPrefs");
                    userPrefs.keepSynced(true);
                    fetchUserPrefs();
                    Crashlytics.setUserIdentifier(dbuid);
                    mAnalytics.setUserProperty("UserUID", dbuid);
                    mAnalytics.setUserProperty("VersionCode", String.valueOf(verCode));
                    mAnalytics.setUserProperty("VersionName", String.valueOf(version));
                    mAnalytics.setUserProperty("Currency", currency);
                    mAnalytics.setUserProperty("AppLanguage", languageSelected);
                    mAnalytics.setUserProperty("UseOldLayout", String.valueOf(old_layout));
                    Log.d(TAG, "dbuid: " + dbuid);
                    //monthPrices = database.getReference("users/" + dbuid + "/monthPrices");
                    database.getReference("users/" + dbuid + "/lastActive").setValue(thisDay + "/" + thisMonth + "/" + thisYear);
                    database.getReference("users/" + dbuid + "/lastActiveTimestamp").setValue(Calendar.getInstance().getTimeInMillis() - timeOffset);
                    database.getReference("users/" + dbuid + "/version").setValue(version);
                    database.getReference("users/" + dbuid + "/build").setValue(verCode);

                    currencies = database.getReference("currencies");

                    FirebaseInstanceId.getInstance().getToken();
                    FirebaseMessaging.getInstance().subscribeToTopic("allNews");
                    newCigarettes = database.getReference("users/" + dbuid + "/newCigs");
                    cigarettes = database.getReference("users/" + dbuid + "/cigarettes");
                    cigsToday = cigarettes.child(thisYear).child(thisMonth).child(thisDay);
                    cigsMonth = cigarettes.child(thisYear).child(thisMonth);

                    brands = database.getReference("users/" + dbuid + "/brands");
                    brands.child("default").setValue(settings.getString("defaultBrand", "No Brand"));
                    brands.child("price").setValue(settings.getString("defaultPrice", "1.00"));
                    brands.child("currency").setValue(settings.getString("defaultCurrency", "USD"));
                    brands.child("packCount").setValue(settings.getString("defaultCount", "20"));

                    brandName = settings.getString("defaultBrand", "No Brand");
                    currency = settings.getString("defaultCurrency", "USD");
                    if (!currency.matches("[a-zA-Z.? ]*")) {
                        currency = "USD";
                    }
                    currencyTimestamp = settings.getLong("currencyTimestamp", 0);
                    currencyRate = Double.parseDouble(settings.getString("currencyRate", "1.00"));
                    isBaseCurrency = currency.equals("USD");
                    //   /([!@#$%^&*()_=+<,>./?`~\[\]{}-])/g


                    if (!isBaseCurrency) {
                        currencies.child("rates/" + currency).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                    currencyRate = Double.valueOf(dataSnapshot.getValue().toString());
                                    editor.putString("currencyRate", String.valueOf(currencyRate));
                                    currencyTimestamp = Calendar.getInstance().getTimeInMillis();
                                    editor.putLong("currencyTimestamp", currencyTimestamp);
                                    editor.apply();
                                } else {
                                    Toast.makeText(MainActivity.this, getString(R.string.check_currency), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        currencyRate = 1.00;
                    }

                    price = Float.valueOf(settings.getString("defaultPrice", "1.00"));
                    int numberCigs = Integer.valueOf(settings.getString("defaultCount", "20"));
                    pricePerCig = price / numberCigs;
                    loadRewardVideo();
                    loadInterstitial();
                    try {
                        Fragment overview = new Overview();
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container, overview).commit();
                        Overview.CallbackDatabaseReference();
                        navigationView.getMenu().getItem(0).setChecked(true);
                    } catch (Exception ex) {
                        Log.e(TAG, "onAuthStateChanged: Fragment ", ex);
                    }
                    dumpListToDatabase();
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        reset = findViewById(R.id.reset_today);
        try {
            Fragment overview = new Overview();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, overview).commit();
            Overview.CallbackDatabaseReference();
        } catch (Exception ex) {
            Log.e(TAG, "onAuthStateChanged: Fragment ", ex);
        }
        fab_background = findViewById(R.id.fab_background);
        mainFab = findViewById(R.id.fab);
        fab_background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFABOpen) closeFABMenu();
            }
        });
        fabMultiSig = findViewById(R.id.fab_multi);
        fabMultiSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final NumberPicker numberPicker = new NumberPicker(MainActivity.this);
                numberPicker.setMaxValue(15);
                numberPicker.setMinValue(2);
                numberPicker.setWrapSelectorWheel(false);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
                params.gravity = Gravity.CENTER;
                numberPicker.setLayoutParams(params);
                FrameLayout fl = new FrameLayout(MainActivity.this);
                fl.addView(numberPicker);
                fl.setLayoutParams(params);
                closeFABMenu();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
                builder.setTitle(getString(R.string.multiple_cigarettes));
                builder.setView(fl);
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NewCigarette.AddCigarette(numberPicker.getValue());
                        closeFABMenu();
                        Snackbar.make(view, getString(R.string.success_add_multi_cigarettes), Snackbar.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                builder.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setDividerColor(numberPicker, getColor(R.color.accent));
                }
            }
        });

        fabDateCig = findViewById(R.id.fab_date);
        fabDateCig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempView = view;
                DialogFragment newFragment = new CigaretteDatePicker();
                newFragment.show(getFragmentManager(), "datePicker");
                closeFABMenu();
            }
        });
        fabSingleCig = findViewById(R.id.fabSingleCig);
        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) showFABMenu();
            }
        });
        fabSingleCig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Overview.OnFabClickAddCigarete(getString(R.string.time_since), getString(R.string.no_cig_smoked), getString(R.string.no_database_conn), MainActivity.this)) {
                    closeFABMenu();
                    Snackbar.make(view, getString(R.string.success_add_one_cigarette), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.undo), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Overview.OnFabClickAddCigaretteUndo();
                                }
                            }).show();
                }
            }
        });
        fabSingleLabel = findViewById(R.id.fabSingleLabel);
        fab1Label = findViewById(R.id.fab1Label);
        fab2Label = findViewById(R.id.fab2Label);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.reset_dialog_text))
                .setTitle(getString(R.string.are_you_sure));
        // Add the buttons
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mRewardedVideoAd.isLoaded()) {
                    long timestampNow = Calendar.getInstance().getTimeInMillis();
                    if (rewardTimestamp + 60000 <= timestampNow) {
                        mRewardedVideoAd.show();
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                    }
                } else if (!show_reward_video) {
                    videoRewardTriggered();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        dialog = builder.create();



    }

    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void updateUI(FirebaseUser user) {
        if (user != null) {
            if (user.getProviders() != null && user.getProviders().contains("google.com")) {
                String displayName = user.getDisplayName();
                for (UserInfo userInfo : user.getProviderData()) {
                    if (userInfo.getDisplayName() != null) {
                        displayName = userInfo.getDisplayName();
                    }
                }
                navTitle.setText(displayName);
                navSubTitle.setText(user.getEmail());
                if (navSignout != null) {
                    navSignout.setVisible(true);
                }
                canLogin = false;
                mAnalytics.setUserProperty("SignInMethod", "Google");
            } else {
                if (navSignout != null) {
                    navSignout.setVisible(false);
                }
                canLogin = true;
                navTitle.setText(R.string.sign_in);
                navSubTitle.setText("");
                mAnalytics.setUserProperty("SignInMethod", "Anonymous");
            }
        } else {
            if (navSignout != null) {
                navSignout.setVisible(false);
            }
            canLogin = true;
            navTitle.setText("");
            navSubTitle.setText("");
        }
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public void anonymousSignIn() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        anonymUser = mAuth.getCurrentUser();
                        if (!task.isSuccessful()) {
                            Log.w("FirebaseAuth", "signInAnonymously", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void googleSignInLink() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseUser userToLink = mAuth.getCurrentUser();
        if (userToLink != null) {
            userToLink.linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCredential:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(MainActivity.this, getString(R.string.welcome_login), Toast.LENGTH_SHORT).show();
                                updateUI(user);
//                                fetchUserPrefs(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage(getString(R.string.account_exists))
                                        .setTitle(getString(R.string.are_you_sure));
                                // Add the buttons
                                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                mAuth = FirebaseAuth.getInstance();
                                                updateUI(mAuth.getCurrentUser());
//                                                fetchUserPrefs(mAuth.getCurrentUser());
                                                Toast.makeText(MainActivity.this, getString(R.string.welcome_login), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        updateUI(mAuth.getCurrentUser());
                                    }
                                });
                                AlertDialog linkAccount = builder.create();
                                linkAccount.show();
//                                Log.w(TAG, "signInWithCredential:failure", task.getException());
//                                Toast.makeText(MainActivity.this, "Existing account",
//                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        }
    }

    public void fetchUserPrefs() {
        userPrefs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot pref : dataSnapshot.getChildren()) {
                    if (pref.getValue() != null) {
                        switch (pref.getKey()) {
                            case "default": {
                                editor.putString("defaultBrand", pref.getValue().toString());
                                break;
                            }
                            case "price": {
                                editor.putString("defaultPrice", pref.getValue().toString());
                                break;
                            }
                            case "currency": {
                                editor.putString("defaultCurrency", pref.getValue().toString());
                                break;
                            }
                            case "packCount": {
                                editor.putString("defaultCount", pref.getValue().toString());
                                break;
                            }
                            case "cigDaily": {
                                editor.putString("cigDaily", pref.getValue().toString());
                                break;
                            }
                            case "forceEnglish": {
                                editor.putBoolean("force_english", (Boolean) pref.getValue());
                                break;
                            }
                            case "coloredNavigation": {
                                editor.putBoolean("colored_navigation", (Boolean) pref.getValue());
                                break;
                            }
                        }
                    }
                }
                editor.apply();
                //Applying user configs to preferences
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void googleSignOut() {
        mAuth.signOut();
        updateUI(null);
        anonymousSignIn();
    }

    @Override
    public void onBackPressed() {
        if (isFABOpen) {
            closeFABMenu();
        } else {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else if (currentView.equals("statistics")) {
                //TODO Remove this part
                Fragment overview = new Overview();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, overview);
                transaction.commit();
                Overview.CallbackDatabaseReference();
                navigationView.getMenu().getItem(0).setChecked(true);
            } else if (!currentView.equals("overview")) {
                Fragment overview = new Overview();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, overview);
                transaction.commit();
                Overview.CallbackDatabaseReference();
                navigationView.getMenu().getItem(0).setChecked(true);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        //navigationView.getMenu().getItem(0).getSubMenu().getItem(0).setChecked(true);
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent startSettings = new Intent(this, SettingsActivity.class);
            startSettings.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
            startSettings.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            startActivityFromChild(this, startSettings, 1);
        } else if (id == R.id.nav_overview) {
            Fragment overview = new Overview();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, overview);
            transaction.commit();
            Overview.CallbackDatabaseReference();
            item.setChecked(true);
        } else if (id == R.id.nav_statistics) {
            Fragment statistics = new Statistics();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, statistics);
            transaction.commit();
            item.setChecked(true);
        } else if (id == R.id.nav_history) {
            Fragment statistics = new History();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, statistics);
            transaction.commit();
            item.setChecked(true);
        } else if (id == R.id.reset_today) {
            if (mRewardedVideoAd.isLoaded()) {
                dialog.show();
            } else if (!show_reward_video) {
                videoRewardTriggered();
            } else {
                Toast.makeText(this, getString(R.string.try_later), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_rate) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.tehflatch.ashtray"));
            startActivity(intent);
        } else if (id == R.id.nav_signout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(getString(R.string.signout_confirm))
                    .setTitle(getString(R.string.are_you_sure));
            // Add the buttons
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    googleSignOut();
                }
            });
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    updateUI(mAuth.getCurrentUser());
                }
            });
            AlertDialog signoutConfirm = builder.create();
            signoutConfirm.show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFABMenu() {
        isFABOpen = true;

        fab_background.setVisibility(View.VISIBLE);
        fab_background.animate().alpha(0.8f).setDuration(250);

        mainFab.animate().rotation(360).setDuration(250);
        mainFab.animate().alpha(0).setDuration(250);

        fabSingleCig.setVisibility(View.VISIBLE);
        fabSingleCig.animate().rotation(360).setDuration(250);
        fabSingleCig.animate().alpha(1).setDuration(250);

        fabMultiSig.animate().translationY(-getResources().getDimension(R.dimen.fab1_margin)).setDuration(250);
        fabMultiSig.animate().alpha(1).setDuration(250);

        fabDateCig.animate().translationY(-getResources().getDimension(R.dimen.fab2_margin)).setDuration(250);
        fabDateCig.animate().alpha(1).setDuration(250);

        fabSingleLabel.setVisibility(View.VISIBLE);
        fabSingleLabel.animate().alpha(1).setDuration(250);
        fabSingleLabel.animate().translationY(-getResources().getDimension(R.dimen.fab_label_main_margin)).setDuration(250);
        fabSingleLabel.animate().scaleX(1).setDuration(250);
        fabSingleLabel.animate().scaleY(1).setDuration(250);

        fab1Label.setVisibility(View.VISIBLE);
        fab1Label.animate().alpha(1).setDuration(250);
        fab1Label.animate().translationY(-getResources().getDimension(R.dimen.fab1_label_margin)).setDuration(250);
        fab1Label.animate().scaleX(1).setDuration(250);
        fab1Label.animate().scaleY(1).setDuration(250);

        fab2Label.setVisibility(View.VISIBLE);
        fab2Label.animate().alpha(1).setDuration(250);
        fab2Label.animate().translationY(-getResources().getDimension(R.dimen.fab2_label_margin)).setDuration(250);
        fab2Label.animate().scaleX(1).setDuration(250);
        fab2Label.animate().scaleY(1).setDuration(250);
    }

    private void closeFABMenu() {
        isFABOpen = false;

        fab_background.animate().alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                fab_background.setVisibility(View.GONE);
            }
        });

        mainFab.animate().rotation(45).setDuration(250);
        mainFab.animate().alpha(1).setDuration(250);

        fabSingleCig.animate().rotation(0).setDuration(250);
        fabSingleCig.animate().alpha(0).setDuration(250).withEndAction(new Runnable() {
            @Override
            public void run() {
                fabSingleCig.setVisibility(View.GONE);
            }
        });

        fabMultiSig.animate().translationY(0).setDuration(250);
        fabMultiSig.animate().alpha(0).setDuration(250);

        fabDateCig.animate().translationY(0).setDuration(250);
        fabDateCig.animate().alpha(0).setDuration(250);

        fabSingleLabel.animate().alpha(0).setDuration(250);
        fabSingleLabel.animate().translationY(0).setDuration(250);
        fabSingleLabel.animate().scaleX(0).setDuration(250);
        fabSingleLabel.animate().scaleY(0).setDuration(250).withEndAction(new Runnable() {
            @Override
            public void run() {
                fabSingleLabel.setVisibility(View.GONE);
            }
        });

        fab1Label.animate().alpha(0).setDuration(250);
        fab1Label.animate().translationY(0).setDuration(250);
        fab1Label.animate().scaleX(0).setDuration(250);
        fab1Label.animate().scaleY(0).setDuration(250).withEndAction(new Runnable() {
            @Override
            public void run() {
                fab1Label.setVisibility(View.GONE);
            }
        });

        fab2Label.animate().alpha(0).setDuration(250);
        fab2Label.animate().translationY(0).setDuration(250);
        fab2Label.animate().scaleX(0).setDuration(250);
        fab2Label.animate().scaleY(0).setDuration(250).withEndAction(new Runnable() {
            @Override
            public void run() {
                fab2Label.setVisibility(View.GONE);
            }
        });
    }

    private boolean isInstallFromUpdate() {
        try {
            long firstInstallTime = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
            long lastUpdateTime = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime;
            return firstInstallTime != lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class CigaretteDatePicker extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());

            // Create a new instance of DatePickerDialog and return it
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            long timestamp = cal.getTimeInMillis();
            NewCigarette.AddCigaretteCustomDate(1, timestamp);
            Snackbar.make(tempView, getString(R.string.success_add_date_cigarette), Snackbar.LENGTH_LONG).show();
        }
    }
}
