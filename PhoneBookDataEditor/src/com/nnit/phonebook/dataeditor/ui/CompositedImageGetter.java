package com.nnit.phonebook.dataeditor.ui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class CompositedImageGetter implements IImageGetter{
	
	private List<IImageDecorator> decorators = null;
	private IImageGetter originalImageGetter = null;
	
	public CompositedImageGetter(IImageGetter originalImageGetter) {
		super();
		this.decorators = new ArrayList<IImageDecorator>();
		this.originalImageGetter = originalImageGetter;
	}
	
	public IImageGetter getOriginalImageGetter() {
		return originalImageGetter;
	}

	public void setOriginalImageGetter(IImageGetter originalImageGetter) {
		this.originalImageGetter = originalImageGetter;
	}

	public void addDecorator(IImageDecorator decorator){
		decorators.add(decorator);
	}

	@Override
	public Bitmap getBitmap(Object[] parameters) {
		
		Bitmap seatImage = originalImageGetter.getBitmap(parameters);
		
		for(IImageDecorator decorator: decorators){
			seatImage = decorator.decorateImage(seatImage);
		}
		return seatImage;
	}
}