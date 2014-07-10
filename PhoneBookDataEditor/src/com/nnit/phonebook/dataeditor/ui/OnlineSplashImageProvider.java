package com.nnit.phonebook.dataeditor.ui;

import java.net.URL;

import com.nnit.phonebook.dataeditor.R;
import com.nnit.phonebook.dataeditor.data.DataManager;
import com.nnit.phonebook.dataeditor.util.NetworkUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class OnlineSplashImageProvider implements ISplashImageProvider{
	public static String ONLINE_SERVICE_PROVIDER_INSTAGRAM = "instagram";
	
	private Context context = null;
	private String onlineSP = null;
	
	public OnlineSplashImageProvider(Context context, String onlineServiceProvider){
		this.context = context;
		this.onlineSP = onlineServiceProvider;
	}
	
	@Override
	public Drawable getSplashImage() {
		Drawable result = null;
		try{
			boolean isOnline = NetworkUtil.checkNetworkAvailable(context);
			if(isOnline){
				IOnlineImageGetter onlineImageGetter = null;
				if(onlineSP.equalsIgnoreCase(ONLINE_SERVICE_PROVIDER_INSTAGRAM)){
					onlineImageGetter = new InstagramImageGetter();
				}else{
					throw new Exception("Unsupported online service provider");
				}
				
				String url = onlineImageGetter.getImageURL();
				result = Drawable.createFromStream(new URL(url).openStream(), DataManager.getInstance().getTempDirAbsolutePath()+"instagram.png");
			}
		}catch(Exception exp){
			exp.printStackTrace();
		}
		if(result == null){
			result = context.getResources().getDrawable(R.drawable.splash);
		}
		return result;
	}

}
