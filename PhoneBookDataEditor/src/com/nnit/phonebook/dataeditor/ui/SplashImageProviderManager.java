package com.nnit.phonebook.dataeditor.ui;

import com.nnit.phonebook.dataeditor.R;

import android.content.Context;

public class SplashImageProviderManager {
	public static String RANDOM_PROVIDER = "random";
	public static String ONLINE_PROVIDER = "online";
	
	private static SplashImageProviderManager _instance = null;
	private Context context = null;
	
	private SplashImageProviderManager(Context context){
		this.context = context;
	}

	public static SplashImageProviderManager getInstance(Context context){
		if(_instance == null){
			_instance = new SplashImageProviderManager(context);
		}
		return _instance;
	}
	
	public ISplashImageProvider getSplashImageProvider(String providerName){
		if(providerName.equalsIgnoreCase(RANDOM_PROVIDER)){
			return new RandomSplashImageProvider(context.getResources(), 
					new int[]{R.drawable.splash, R.drawable.splash1, R.drawable.splash2, R.drawable.splash3, R.drawable.splash4});
		}else if(providerName.equalsIgnoreCase(ONLINE_PROVIDER)){
			return new OnlineSplashImageProvider(context, OnlineSplashImageProvider.ONLINE_SERVICE_PROVIDER_INSTAGRAM);
		}else{
			return new DefaultSplashImageProvider(context.getResources());
		}
	}
}
