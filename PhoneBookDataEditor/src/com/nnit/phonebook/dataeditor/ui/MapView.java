package com.nnit.phonebook.dataeditor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MapView extends ImageView{

	private static Paint mapPaint = null;
	private static Paint positionPaint = null;
	
	private int seatPosX, seatPosY, seatPosWidth, seatPosHeight;
	private boolean drawSeatPos = false;
	

	static {
		mapPaint = new Paint();
		mapPaint.setColor(Color.RED);
		mapPaint.setStrokeWidth(10);

		positionPaint = new Paint();
		positionPaint.setColor(Color.RED);
		positionPaint.setStrokeWidth(10);
	}

	public MapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}	
	
	
	public boolean isDrawSeatPos() {
		return drawSeatPos;
	}

	public void setDrawSeatPos(boolean drawSeatPos) {
		this.drawSeatPos = drawSeatPos;
	}
	
	public void setSeatPos(int x, int y, int w, int h){
		this.seatPosX = x;
		this.seatPosY = y;
		this.seatPosWidth = w;
		this.seatPosHeight = h;
	}

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		if(drawSeatPos){
			canvas.drawLine(seatPosX, seatPosY, seatPosX + seatPosWidth, seatPosY, positionPaint);
			canvas.drawLine(seatPosX + seatPosWidth, seatPosY, seatPosX + seatPosWidth, seatPosY + seatPosHeight, positionPaint);
			canvas.drawLine(seatPosX + seatPosWidth, seatPosY + seatPosHeight, seatPosX, seatPosY + seatPosHeight, positionPaint);
			canvas.drawLine(seatPosX, seatPosY + seatPosHeight, seatPosX, seatPosY, positionPaint);
			
		}
	}

}
