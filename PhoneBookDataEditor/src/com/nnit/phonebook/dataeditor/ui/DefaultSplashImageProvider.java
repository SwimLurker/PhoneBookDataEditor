package com.nnit.phonebook.dataeditor.ui;

import com.nnit.phonebook.dataeditor.R;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class DefaultSplashImageProvider implements ISplashImageProvider{

	private Resources resources = null;
	
	public DefaultSplashImageProvider(Resources resources) {
		super();
		this.resources = resources;
	}


	@Override
	public Drawable getSplashImage() {
		return resources.getDrawable(R.drawable.splash);
	}

}
