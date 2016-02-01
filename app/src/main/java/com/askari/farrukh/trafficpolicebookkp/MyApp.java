package com.askari.farrukh.trafficpolicebookkp;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by FARRU on 12/2/2015.
 */
public class MyApp extends Application {
    private static Tracker mTracker;

    private static GoogleAnalytics analytics;

    public static GoogleAnalytics analytics(){
        return analytics;
    }

    public static Tracker tracker(){
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = GoogleAnalytics.getInstance(this);
        mTracker = analytics.newTracker("UA-70910442-1");
    }
}
