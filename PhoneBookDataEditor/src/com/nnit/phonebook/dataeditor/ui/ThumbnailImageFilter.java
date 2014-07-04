package com.nnit.phonebook.dataeditor.ui;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

public class ThumbnailImageFilter implements IImageFilter{

	private int thumbnailWidth, thumbnailHeight;
	
	public ThumbnailImageFilter(int thumbnailWidth, int thumbnailHeight) {
		super();
		this.thumbnailWidth = thumbnailWidth;
		this.thumbnailHeight = thumbnailHeight;
	}

	@Override
	public Bitmap getImage(Bitmap originalImage) {
		return ThumbnailUtils.extractThumbnail(originalImage, thumbnailWidth, thumbnailHeight);
	}

}
