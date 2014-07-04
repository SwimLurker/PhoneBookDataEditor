package com.nnit.phonebook.dataeditor.ui;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

public class ImageLoader {
	
	private Object lock = new Object();
	private boolean bAllowLoad = true;
	private IImageGetter imageGetter = null;
	final Handler handler = new Handler();
	
	public ImageLoader(IImageGetter imageGetter) {
		super();
		this.imageGetter = imageGetter;
	}

	public void lock() {
		bAllowLoad = false;
	}

	public void unlock() {
		bAllowLoad = true;
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public void loadImage(Object[] parameters, OnImageLoadListener listener) {
		final Object[] _parameters = parameters;
		final OnImageLoadListener _listener = listener;

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!bAllowLoad) {
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException exp) {
							exp.printStackTrace();
						}
					}
				}

				if (bAllowLoad) {
					_loadImage(_parameters, _listener);
				}
			}
		}).start();
	}

	private void _loadImage(final Object[] parameters, final OnImageLoadListener listener) {
		try {
			final Bitmap bitmap = imageGetter.getBitmap(parameters);
			//Thread.sleep(5000);
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (bAllowLoad) {
						if(bitmap != null){
							listener.onImageLoaded(bitmap, parameters);
						}else{
							listener.onError(parameters);
						}
					}
				}
			});
			return;

		} catch (Exception exp) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onError(parameters);
				}
			});
			exp.printStackTrace();
		}
	}
	
	public interface OnImageLoadListener {
		public void onImageLoaded(Bitmap bitmap, Object[] parameters);

		public void onError(Object[] data);
	}
}
