package com.nnit.phonebook.dataeditor.ui;



import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MapView extends ImageView{

	public static final float MIN_SCALE = 0.2f;
	public static final float MAX_SCALE = 2f;
	
	private static Paint mapPaint = null;
	private static Paint penRectPaint = null;
	private static Paint seatRectPaint = null;
	
	private RectF penRect = new RectF();
	private RectF seatRect = new RectF();
	private int seatDirection = 0;
	
	
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	private Bitmap map = null;
	
	private enum MODE {
		NONE, DRAG, ZOOM
	}
	
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private MODE mode = MODE.NONE;
	private boolean isPenPressed = false;
	
	private PointF penStart = new PointF();
	private float beforeLength;
	private int bitmapWidth, bitmapHeight;
	private int mapViewWidth, mapViewHeight;
	
	private List<ISeatPositionChangedListener> seatPositionChangedListeners = new ArrayList<ISeatPositionChangedListener>();
	
	static {
		mapPaint = new Paint();

		penRectPaint = new Paint();
		penRectPaint.setColor(Color.RED);
		penRectPaint.setStrokeWidth(8);
		penRectPaint.setStyle(Paint.Style.STROKE);
		penRectPaint.setAlpha(100);
		PathEffect effects = new DashPathEffect(new float[]{5,5,5,5}, 1);
		penRectPaint.setPathEffect(effects);
		
		seatRectPaint = new Paint();
		seatRectPaint.setColor(Color.RED);
		seatRectPaint.setStrokeWidth(8);
		seatRectPaint.setStyle(Paint.Style.STROKE);
	}

	public MapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void setCurrentMatrix(Matrix matrix){
		this.matrix = matrix;
	}
	
	public void addSeatPositionListener(ISeatPositionChangedListener listener){
		seatPositionChangedListeners.add(listener);
	}
	
	public void removeSeatPositionListener(ISeatPositionChangedListener listener){
		seatPositionChangedListeners.remove(listener);
	}
	
	public void setMap(Bitmap bitmap){
		this.map = bitmap;
		if(bitmap != null){
			this.bitmapWidth = bitmap.getWidth();
			this.bitmapHeight = bitmap.getHeight();
		}
		invalidate();
	}
	
	
	public RectF getPenRect() {
		return penRect;
	}

	public void setPenRect(RectF penRect) {
		this.penRect = penRect;
	}
	
	public RectF getSeatRect() {
		return seatRect;
	}

	public void setSeatRect(RectF seatRect) {
		this.seatRect = seatRect;
	}
	
	public void setPenPressed(boolean penPressed){
		this.isPenPressed = penPressed;
	}
	
	public int getSeatDirection() {
		return seatDirection;
	}

	public void setSeatDirection(int seatDirection) {
		this.seatDirection = seatDirection;
	}

	@Override
	public void onLayout(boolean changed ,int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		mapViewWidth = right - left;
		mapViewHeight = bottom - top;
	}

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		if(mode == MODE.DRAG && isPenPressed){
			//draw map
			if(map !=null){
				canvas.drawBitmap(map, matrix, mapPaint);
			}
			//draw pen rect
			if(penRect !=null){
				canvas.drawRect(penRect, penRectPaint);
			}
		}else{
			Bitmap map1 = getFloorMapWithSeat();
			if(map1 !=null){
				canvas.drawBitmap(map1, matrix, mapPaint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		switch(event.getAction() & MotionEvent.ACTION_MASK){
			case MotionEvent.ACTION_DOWN:
				onTouchDown(event);
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				onPointerDown(event);
				break;
			case MotionEvent.ACTION_MOVE:
				onTouchMove(event);
				break;
			case MotionEvent.ACTION_POINTER_UP:
				onPointerUp(event);
				break;
			case MotionEvent.ACTION_UP:
				onTouchUp(event);
				break;
		}
		checkScale();
		//center();
		invalidate();
		return true;
	}
	
	public void zoomIn(){
		float p[] = new float[9];
		matrix.getValues(p);
		if ((p[0] * 1.25f) <= MAX_SCALE) {
			matrix.postScale(1.25f, 1.25f);
		}
		center();
		setCurrentMatrix(matrix);
		invalidate();
	}
	
	public void zoomOut(){
		float p[] = new float[9];
		matrix.getValues(p);
		if ((p[0] * 0.8f) >= MIN_SCALE) {
			matrix.postScale(0.8f, 0.8f);
		}
		center();
		setCurrentMatrix(matrix);
		invalidate();
	}
	
	public void Rotate(int degree){
		seatDirection = ((seatDirection + degree) % 360 +360) % 360;
		for(ISeatPositionChangedListener listener: seatPositionChangedListeners){
			listener.onSeatDirectionChanged(seatDirection);
		}
		invalidate();
	}
	
	public void locateSeatPosition(){
		
		RectF sRect = new RectF();
	
		matrix.mapRect(sRect, seatRect);
	
		float centerX = sRect.left + sRect.width() / 2;
		float centerY = sRect.top + sRect.height() / 2;

		float mapViewCenterX = mapViewWidth / 2;
		float mapViewCenterY = mapViewHeight / 2;

		float deltaX = 0, deltaY = 0;

		deltaX = mapViewCenterX - centerX;
		deltaY = mapViewCenterY - centerY;

		
		matrix.postTranslate(deltaX, deltaY);

		setCurrentMatrix(matrix);
		
		center();
		
		setCurrentMatrix(matrix);
		
		invalidate();
	}
	
	private void onTouchDown(MotionEvent event){	
		savedMatrix.set(matrix);
		start.set(event.getX(), event.getY());
		mode = MODE.DRAG;
		if(isPenPressed){
			penStart.set(event.getX(), event.getY());
			penRect.set(event.getX(), event.getY(), event.getX(), event.getY());
			seatRect.set(penRect);
			RectF r = new RectF();
			r.set(seatRect);
			seatDirection = 0;
			for(ISeatPositionChangedListener listener: seatPositionChangedListeners){
				listener.onSeatRectChanged(r);
				listener.onSeatDirectionChanged(seatDirection);
			}
		}
	}

	private void onTouchUp(MotionEvent event) {
		mode = MODE.NONE;
		if(isPenPressed){
			penRect.set(penStart.x, penStart.y, event.getX(), event.getY());		
			seatRect.set(penRect);
			Matrix mi = new Matrix();
			matrix.invert(mi);
			mi.mapRect(seatRect);
			
			RectF r = new RectF();
			r.set(seatRect);
			for(ISeatPositionChangedListener listener: seatPositionChangedListeners){
				listener.onSeatRectChanged(r);
			}
		}
		center();
	}
	
	private void onTouchMove(MotionEvent event) {
		if (mode == MODE.DRAG) {
			if(isPenPressed){
				penRect.set(penStart.x, penStart.y, event.getX(), event.getY());
				seatRect.set(penRect);
				Matrix mi = new Matrix();
				matrix.invert(mi);
				mi.mapRect(seatRect);
				
				RectF r = new RectF();
				r.set(seatRect);
				for(ISeatPositionChangedListener listener: seatPositionChangedListeners){
					listener.onSeatRectChanged(r);
				}
			}else{
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			}
		} else if (mode == MODE.ZOOM) {
			
			float afterLength = getDistance(event);
			if (afterLength > 10f) {
				midPoint(mid, event);
				matrix.set(savedMatrix);
				float scale = afterLength / beforeLength;
				matrix.postScale(scale, scale, mid.x, mid.y);
			}
		}
	}
	
	private void onPointerUp(MotionEvent event) {
		if(!isPenPressed){
			mode = MODE.NONE;
		}
	}

	private void onPointerDown(MotionEvent event) {
		if(!isPenPressed){
			if (event.getPointerCount() == 2) {
				beforeLength = getDistance(event);
				if (beforeLength > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event);
					mode = MODE.ZOOM;
				}
			}
		}
	}
	
	private float getDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);

		return FloatMath.sqrt(x * x + y * y);
	}
	
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);

		point.set(x / 2, y / 2);
	}
	
	protected void checkScale() {
		float p[] = new float[9];
		matrix.getValues(p);
		if (mode == MODE.ZOOM) {
			if (p[0] < MIN_SCALE) {
				matrix.setScale(MIN_SCALE, MIN_SCALE);
			}
			if (p[0] > MAX_SCALE) {
				matrix.set(savedMatrix);
			}
		}
	}

	protected void center() {
		center(true, true);
	}

	private void center(boolean horizontal, boolean vertical) {
		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, bitmapWidth, bitmapHeight);
		m.mapRect(rect);
		float height = rect.height();
		float width = rect.width();

		float deltaX = 0, deltaY = 0;

		if (vertical) {
			if (height < mapViewHeight) {
				deltaY = (mapViewHeight - height) / 2 - rect.top;
			} else if (rect.top > 0) {
				deltaY = -rect.top;
			} else if (rect.bottom < mapViewHeight) {
				deltaY = mapViewHeight - rect.bottom;
			}
		}

		if (horizontal) {
			if (width < mapViewWidth) {
				deltaX = (mapViewWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) {
				deltaX = -rect.left;
			} else if (rect.right < mapViewWidth) {
				deltaX = mapViewWidth - rect.right;
			}
		}
		matrix.postTranslate(deltaX, deltaY);

	}
	
	private Bitmap getFloorMapWithSeat() {
		Bitmap seatBmp = null;
		
		seatBmp = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(seatBmp);

		Matrix m1 = new Matrix();
		
		canvas.drawBitmap(map, m1, mapPaint);
		canvas.save();

		float x1 = seatRect.left, y1 = seatRect.top;
		float x2 = seatRect.right, y2 = seatRect.bottom;
		double r = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) / 2;

		float midPoint_x = (x1 + x2) / 2;
		float midPoint_y = (y1 + y2) / 2;

		float w = x2 - x1;
		float h = y2 - y1;
		
		canvas.translate(midPoint_x, midPoint_y);
		canvas.rotate(seatDirection);
		
		canvas.drawRect(new RectF(-w/2, -h/2, w/2, h/2), seatRectPaint);
		
		canvas.restore();

		return seatBmp;
	}
}
