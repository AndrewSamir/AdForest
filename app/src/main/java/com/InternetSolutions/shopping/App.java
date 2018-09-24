package com.InternetSolutions.shopping;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.InternetSolutions.shopping.helper.LocaleHelper;
import com.InternetSolutions.shopping.utills.NoInternet.AppLifeCycleManager;

public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Fresco.initialize(this);
		AppLifeCycleManager.init(this);
	}
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
		MultiDex.install(this);
	}
}
