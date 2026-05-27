package com.cyberpath.smartlearn;

import android.app.Application;

import com.cyberpath.smartlearn.data.remote.api.RetrofitClient;

public class SmartLearnApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitClient.init(this);
    }
}