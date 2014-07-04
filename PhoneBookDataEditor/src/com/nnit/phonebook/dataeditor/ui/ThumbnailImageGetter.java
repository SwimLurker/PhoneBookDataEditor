package com.nnit.phonebook.dataeditor.ui;

import android.graphics.Bitmap;

import com.nnit.phonebook.dataeditor.util.BitmapUtil;

public class ThumbnailImageGetter implements IImageGetter{
	
	private int thumbnailWidth, thumbnailHeight;
	

	public ThumbnailImageGetter(int thumbnailWidth, int thumbnailHeight) {
		super();
		this.thumbnailWidth = thumbnailWidth;
		this.thumbnailHeight = thumbnailHeight;
	}


	@Override
	public Bitmap getBitmap(Object[] parameters) {
		String bitmapFilename = (String)parameters[0];
		return BitmapUtil.getImageThumbnail(bitmapFilename, thumbnailWidth, thumbnailHeight);
	}
	
}
