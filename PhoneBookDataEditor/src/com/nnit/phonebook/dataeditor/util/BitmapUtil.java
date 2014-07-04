package com.nnit.phonebook.dataeditor.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

public class BitmapUtil {
	public static Bitmap getImageThumbnail(String imagePath, int width, int height){
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		
		options.inJustDecodeBounds = false;
		
		int h = options.outHeight;
		int w = options.outWidth;
		
		if(width<=0){
			width = w;
		}
		if(height<=0){
			height = h;
		}
		
		int beWidth = w / width;
		int beHeight = h / height;
		
		int be = 1;
		if(beWidth < beHeight){
			be = beWidth;
		}else{
			be = beHeight;
		}
		
		if(be <= 0){
			be = 1;
		}
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
		
	}
	
	public static Bitmap getImageThumbnail(Bitmap bitmap, int width, int height){
		
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
		
	}
	
}
