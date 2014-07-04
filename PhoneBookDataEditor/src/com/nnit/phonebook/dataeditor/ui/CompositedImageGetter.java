package com.nnit.phonebook.dataeditor.ui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class CompositedImageGetter implements IImageGetter{
	
	private List<IImageFilter> filters = null;
	private IImageGetter originalImageGetter = null;
	
	public CompositedImageGetter(IImageGetter originalImageGetter) {
		super();
		this.filters = new ArrayList<IImageFilter>();
		this.originalImageGetter = originalImageGetter;
	}
	
	public IImageGetter getOriginalImageGetter() {
		return originalImageGetter;
	}

	public void setOriginalImageGetter(IImageGetter originalImageGetter) {
		this.originalImageGetter = originalImageGetter;
	}

	public void addFilter(IImageFilter filter){
		filters.add(filter);
	}

	@Override
	public Bitmap getBitmap(Object[] parameters) {
		
		Bitmap seatImage = originalImageGetter.getBitmap(parameters);
		
		for(IImageFilter filter: filters){
			seatImage = filter.getImage(seatImage);
		}
		return seatImage;
	}
}