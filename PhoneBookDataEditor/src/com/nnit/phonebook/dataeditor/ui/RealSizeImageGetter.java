package com.nnit.phonebook.dataeditor.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RealSizeImageGetter implements IImageGetter{
	
	@Override
	public Bitmap getBitmap(Object[] parameters) {
		String bitmapFilename = (String)parameters[0];
		FileInputStream fis = null;
		try {
			if (bitmapFilename != null) {
				File f = new File(bitmapFilename);

				if (f.exists() && f.isFile()) {
					fis = new FileInputStream(f);
					Bitmap bitmap = BitmapFactory.decodeStream(fis);
					return bitmap;
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	
}
