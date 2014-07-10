package com.nnit.phonebook.dataeditor.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

public class SeatPositionImageDecorator implements IImageDecorator {

	private int x, y, w, h, d;
	

	public SeatPositionImageDecorator(int x, int y, int width, int height, int direction) {
		super();
		this.x = x;
		this.y = y;
		this.w = width;
		this.h = height;
		this.d = direction;
	}

	@Override
	public Bitmap decorateImage(Bitmap mapImage) {
		Bitmap seatBmp = null;

		int bitmapWidth = mapImage.getWidth();
		int bitmapHeight = mapImage.getHeight();

		seatBmp = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(seatBmp);

		Matrix m1 = new Matrix();
		// m1.setScale(0.1f, 0.1f);
		Paint mapPaint = new Paint();
		mapPaint.setColor(Color.RED);
		mapPaint.setStrokeWidth(10);

		canvas.drawBitmap(mapImage, m1, mapPaint);

		RectF rect = new RectF(x, y, x+w, y+h);

		canvas.save();

		float x1 = rect.left, y1 = rect.top;
		float x2 = rect.right, y2 = rect.bottom;
		double r = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) / 2;

		float midPoint_x = (x1 + x2) / 2;
		float midPoint_y = (y1 + y2) / 2;

		float w = x2 - x1;
		float h = y2 - y1;
		canvas.translate(midPoint_x, midPoint_y);
		canvas.rotate(d);

		canvas.drawLine(-w / 2, h / 2, w / 2, h / 2, mapPaint);
		canvas.drawLine(w / 2, h / 2, w / 2, -h / 2, mapPaint);
		canvas.drawLine(w / 2, -h / 2, -w / 2, -h / 2, mapPaint);
		canvas.drawLine(-w / 2, -h / 2, -w / 2, h / 2, mapPaint);

		canvas.restore();

		return seatBmp;
	}

}
