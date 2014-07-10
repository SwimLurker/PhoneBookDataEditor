package com.nnit.phonebook.dataeditor.ui;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

public class ThumbnailImageDecorator implements IImageDecorator{

	private int thumbnailWidth, thumbnailHeight;
	
	public ThumbnailImageDecorator(int thumbnailWidth, int thumbnailHeight) {
		super();
		this.thumbnailWidth = thumbnailWidth;
		this.thumbnailHeight = thumbnailHeight;
	}

	@Override
	public Bitmap decorateImage(Bitmap originalImage) {
		return ThumbnailUtils.extractThumbnail(originalImage, thumbnailWidth, thumbnailHeight);
	}

}
