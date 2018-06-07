package com.tehflatch.liquid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.os.LocaleListCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Locale;

import static com.tehflatch.liquid.MainActivity.latestVersion;
import static com.tehflatch.liquid.MainActivity.showSpent;
import static com.tehflatch.liquid.MainActivity.show_banners;
import static com.tehflatch.liquid.MainActivity.show_interstitial;
import static com.tehflatch.liquid.MainActivity.show_reward_video;
import static com.tehflatch.liquid.MainActivity.useNewCigs;

public class SplashActivity extends AppCompatActivity {

    public static SharedPreferences.Editor editor;
    public static SharedPreferences settings;
    public FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        String languageSelected = settings.getString("app_language", LocaleListCompat.getDefault().get(0).toString().split("_")[0]);
        Locale locale = new Locale(languageSelected);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        editor = settings.edit();
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.default_configs);

        final long cacheExpiration = 7200;
//        // cacheExpirationSeconds is set to cacheExpiration here, indicating that any previously
//        // fetched and cached config would be considered expired because it would have been fetched
//        // more than cacheExpiration seconds ago. Thus the next fetch would go to the server unless
//        // throttling is in progress. The default expiration duration is 43200 (12 hours).
        // Use an activity context to get the rewarded video instance.
        if (isConnected) {
            mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        InitApp();
                    }

                }
            });
        } else {
            InitApp();
        }
    }

    private void InitApp() {
        mFirebaseRemoteConfig.activateFetched();
        latestVersion = Integer.parseInt(mFirebaseRemoteConfig.getString("latestVersion"));
        showSpent = mFirebaseRemoteConfig.getBoolean("showSpent");
        useNewCigs = mFirebaseRemoteConfig.getBoolean("useNewCigs");
        show_banners = mFirebaseRemoteConfig.getBoolean("show_banners");
        show_interstitial = mFirebaseRemoteConfig.getBoolean("show_interstitial");
        show_reward_video = mFirebaseRemoteConfig.getBoolean("show_reward_video");
        //old_layout = mFirebaseRemoteConfig.getBoolean("old_layout");
        editor.putBoolean("show_banners", show_banners);
        editor.putBoolean("show_interstitial", show_interstitial);
        editor.putBoolean("show_reward_video", show_reward_video);
        editor.putBoolean("showSpent", showSpent);
        editor.putBoolean("useNewCigs", useNewCigs);
        editor.apply();
//                    if (verCode < latestVersion) {
//                        Toast.makeText(SplashActivitythis, getString(R.string.update_application),
//                                Toast.LENGTH_SHORT).show();
//                    }

//                    database.getReference("users/" + dbuid + "/useNewCigs").setValue(useNewCigs);
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
