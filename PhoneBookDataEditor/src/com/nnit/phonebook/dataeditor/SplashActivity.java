package com.nnit.phonebook.dataeditor;

import com.nnit.phonebook.dataeditor.data.DataManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class SplashActivity extends Activity {
	private final int SPLASH_DISPLAY_LENGTH = 3000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_splash);
		
		final ImageView image = (ImageView)findViewById(R.id.splash_image);
		
		final AnimationSet as = new AnimationSet(false);
		
		final Animation ani1 = new ScaleAnimation(1f,1.1f, 1f, 1.1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.3f);
		final Animation ani2 = new AlphaAnimation(1.0f, 0.3f);
		
		as.addAnimation(ani1);
		as.addAnimation(ani2);
		
		ani1.setDuration(SPLASH_DISPLAY_LENGTH);
		ani1.setFillAfter(true);
		ani1.setStartOffset(500);
		
		ani2.setDuration(SPLASH_DISPLAY_LENGTH);
		ani2.setFillAfter(true);
		ani2.setStartOffset(2000);
		
		//as.setFillAfter(true);
		
		image.setAnimation(as);

		
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				//preload data 
				if (DataManager.getInstance().existData()) {
					DataManager.getInstance().loadDataPackage();
				}
				Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
				SplashActivity.this.startActivity(mainIntent);
				SplashActivity.this.finish();
				
			}
			
		}, SPLASH_DISPLAY_LENGTH);
		
		as.startNow();
	}
	
}
