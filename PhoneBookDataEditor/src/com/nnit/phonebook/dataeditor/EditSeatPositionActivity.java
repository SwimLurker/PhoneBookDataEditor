package com.nnit.phonebook.dataeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import com.nnit.phonebook.dataeditor.data.DataManager;
import com.nnit.phonebook.dataeditor.data.PhoneBookItem;
import com.nnit.phonebook.dataeditor.data.SeatInfo;
import com.nnit.phonebook.dataeditor.data.PhoneBookItem.GENDER;
import com.nnit.phonebook.dataeditor.ui.CompositedImageGetter;
import com.nnit.phonebook.dataeditor.ui.ImageLoader;
import com.nnit.phonebook.dataeditor.ui.MapView;
import com.nnit.phonebook.dataeditor.ui.RealSizeImageGetter;
import com.nnit.phonebook.dataeditor.ui.SeatPositionImageFilter;
import com.nnit.phonebook.dataeditor.ui.ThumbnailImageFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EditSeatPositionActivity extends Activity {

	public static final float MIN_SCALE = 0.2f;
	public static final float MAX_SCALE = 2f;

	private PointF start = new PointF();
	private PointF mid = new PointF();
	private int mapViewWidth, mapViewHeight;
	private int bitmapWidth, bitmapHeight;
	private float beforeLength;
	private int floorPanelHeight, infoPanelHeight;
	private boolean calculateMapSize = false;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	private static Paint mapPaint = null;
	private static Paint textPaint = null;
	private static Paint positionPaint = null;

	static {
		mapPaint = new Paint();
		mapPaint.setColor(Color.RED);
		mapPaint.setStrokeWidth(10);

		textPaint = new Paint();
		textPaint.setTextSize(40);
		textPaint.setColor(Color.BLACK);
		textPaint.setTypeface(Typeface.DEFAULT);

		positionPaint = new Paint();
		positionPaint.setColor(Color.RED);
		positionPaint.setStrokeWidth(10);
	}

	private enum MODE {
		NONE, DRAG, ZOOM, PEN
	}
	
	private enum PENMODE{
		RELEASE, DOWN, MOVE
	}

	private MODE mode = MODE.NONE;
	
	private String initials = null;
	private SeatInfo seatInfo = null;
	private SeatInfo newSeatInfo = null;
	
	private Resources resources = null;

	private Spinner floorSpinner;
	private MapView mapIV;
	private EditText xET;
	private EditText yET;
	private EditText widthET;
	private EditText heightET;
	private EditText directionET;
	private Bitmap mapImage;
	
	private ToggleButton fullscreenBtn = null;
	private ToggleButton penBtn = null;
	private ImageButton rotateBtn = null;
	private ImageButton counterRotateBtn = null;
	private ImageButton zoomInBtn = null;
	private ImageButton zoomOutBtn = null;
	private ImageButton locateBtn = null;
	
	private LinearLayout floorPanelLayout = null;
	private LinearLayout infoPanelLayout = null;
	
	private int seatPosFloor;
	private int seatPosX, seatPosY, seatPosWidth, seatPosHeight, seatPosDirection;
	private int penStartPosX, penStartPosY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_edit_seatposition);

		initials = (String) getIntent().getSerializableExtra(
				EditPhoneBookActivity.SELECTED_INITIALS);

		seatInfo = DataManager.getInstance().getSeatInfoByInitial(initials);

		resources = getResources();
		floorPanelHeight = (int) resources.getDimension(R.dimen.map_floorpanel_height);
		infoPanelHeight = (int) resources.getDimension(R.dimen.map_infopanel_height);
		calculateMapSize = true;
		
		
		mapIV = (MapView) findViewById(R.id.edit_seatpos_map);
		xET = (EditText) findViewById(R.id.edit_seatpos_x);
		yET = (EditText) findViewById(R.id.edit_seatpos_y);
		widthET = (EditText) findViewById(R.id.edit_seatpos_width);
		heightET = (EditText) findViewById(R.id.edit_seatpos_height);
		directionET = (EditText) findViewById(R.id.edit_seatpos_direction);
		
		floorSpinner = (Spinner) findViewById(R.id.edit_seatpos_floor);
		List<Integer> mapsFloors = DataManager.getInstance().getMapFloors();

		ArrayAdapter<Integer> floorNoAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, mapsFloors);
		floorNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		floorSpinner.setAdapter(floorNoAdapter);

		if (seatInfo != null) {
			seatPosFloor = seatInfo.getFloorNo();
			seatPosX = seatInfo.getX();
			seatPosY = seatInfo.getY();
			seatPosWidth = seatInfo.getWidth();
			seatPosHeight = seatInfo.getHeight();
			seatPosDirection = seatInfo.getDirection();
			
		} else {
			seatPosFloor = mapsFloors.size() == 0? -1: mapsFloors.get(0);
			seatPosX = 0;
			seatPosY = 0;
			seatPosWidth = 0;
			seatPosHeight = 0;
			seatPosDirection = 0;
		}
		
		xET.setText(Integer.toString(seatPosX));
		yET.setText(Integer.toString(seatPosY));
		widthET.setText(Integer.toString(seatPosWidth));
		heightET.setText(Integer.toString(seatPosHeight));
		directionET.setText(Integer.toString(seatPosDirection));
		
		OnFocusChangeListener ocl = new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					if (v.getId() == R.id.edit_seatpos_x){
						seatPosX = Integer.parseInt(xET.getText().toString());
					}else if(v.getId() == R.id.edit_seatpos_y){
						seatPosY = Integer.parseInt(yET.getText().toString());
					}else if(v.getId() == R.id.edit_seatpos_width){
						seatPosWidth = Integer.parseInt(widthET.getText().toString());
					}else if(v.getId() == R.id.edit_seatpos_height){
						seatPosHeight = Integer.parseInt(heightET.getText().toString());
					}else if(v.getId() == R.id.edit_seatpos_direction){
						seatPosDirection = Integer.parseInt(directionET.getText().toString());
					}
					updateMapImage();
				}		
			}
			
		};
		
		xET.setOnFocusChangeListener(ocl);
		yET.setOnFocusChangeListener(ocl);
		widthET.setOnFocusChangeListener(ocl);
		heightET.setOnFocusChangeListener(ocl);
		directionET.setOnFocusChangeListener(ocl);
		
		
		for (int i = 0; i < mapsFloors.size(); i++) {
			Integer floor = mapsFloors.get(i);
			if (floor == seatPosFloor) {
				floorSpinner.setSelection(i);
				break;
			}
		}

		floorSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				seatPosFloor = (Integer)floorSpinner.getSelectedItem();
				updateMapImage();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});


		mapIV.setImageMatrix(matrix);

		mapIV.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView view = (ImageView) v;
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					onTouchDown(event);
					break;
				case MotionEvent.ACTION_UP:
					onTouchUp(event);
					break;
				case MotionEvent.ACTION_POINTER_UP:
					onPointerUp(event);
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					onPointerDown(event);
					break;
				case MotionEvent.ACTION_MOVE:
					onTouchMove(event);
					break;
				}
				view.setImageMatrix(matrix);

				checkScale();
				center();

				return true;
			}
		});

		mapIV.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						if (calculateMapSize) {
							Rect frame = new Rect();
							getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
							int top = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
							mapViewHeight = frame.bottom - top ;
							Resources r = getResources();
							int titleLayoutHeight = (int) r.getDimension(R.dimen.activity_title_height);
							
							mapViewHeight = mapViewHeight - (titleLayoutHeight +floorPanelHeight+infoPanelHeight);
							mapViewWidth = frame.width();
							calculateMapSize = false;
						}
					}
				});
		
		zoomInBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_zoomin);
		zoomInBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				float p[] = new float[9];
				matrix.getValues(p);
				if ((p[0] * 1.25f) <= MAX_SCALE) {
					matrix.postScale(1.25f, 1.25f);
				}
				center();
				mapIV.setImageMatrix(matrix);

			}

		});

		zoomOutBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_zoomout);
		zoomOutBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				float p[] = new float[9];
				matrix.getValues(p);
				if ((p[0] * 0.8f) >= MIN_SCALE) {
					matrix.postScale(0.8f, 0.8f);
				}
				center();
				mapIV.setImageMatrix(matrix);
			}

		});

		locateBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_locate);

		locateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				float p[] = new float[9];
				matrix.getValues(p);
				float currentScale = p[0];

				RectF seatRect = new RectF(seatPosX, seatPosY, seatPosX + seatPosWidth, seatPosY + seatPosHeight);
				RectF mapRect = new RectF(0, 0, bitmapWidth, bitmapHeight);

				Matrix m = new Matrix();
				m.set(matrix);
				m.mapRect(seatRect);
				m.mapRect(mapRect);

				float centerX = seatRect.left + seatRect.width() / 2;
				float centerY = seatRect.top + seatRect.height() / 2;

				float mapViewCenterX = mapViewWidth / 2;
				float mapViewCenterY = mapViewHeight / 2;

				float deltaX = 0, deltaY = 0;

				deltaX = mapViewCenterX - centerX;
				deltaY = mapViewCenterY - centerY;

				Matrix m1 = new Matrix();
				m1.set(matrix);

				float p3[] = new float[9];

				m1.getValues(p3);

				// m1.setScale(p[0], p[0]);
				// matrix.setTranslate(deltaX, deltaY);
				m1.postTranslate(deltaX, deltaY);

				float p2[] = new float[9];

				m1.getValues(p2);

				matrix.set(m1);
				mapIV.setImageMatrix(matrix);
				
				center();
				mapIV.setImageMatrix(matrix);
			}

		});
		
		floorPanelLayout = (LinearLayout) findViewById(R.id.edit_seatpos_floorpanellayout);
		infoPanelLayout = (LinearLayout) findViewById(R.id.edit_seatpos_infopanellayout);
		
		
		fullscreenBtn = (ToggleButton) findViewById(R.id.edit_seatpos_map_fullscreen);
		fullscreenBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(fullscreenBtn.isChecked()){
					floorPanelLayout.setVisibility(View.GONE);
					infoPanelLayout.setVisibility(View.GONE);
					floorPanelHeight = 0;
					infoPanelHeight = 0;
					calculateMapSize = true;
				}else{
					floorPanelLayout.setVisibility(View.VISIBLE);
					infoPanelLayout.setVisibility(View.VISIBLE);
					floorPanelHeight = (int) resources.getDimension(R.dimen.map_floorpanel_height);
					infoPanelHeight = (int) resources.getDimension(R.dimen.map_infopanel_height);	
					calculateMapSize = true;
				}
				center();
				mapIV.setImageMatrix(matrix);
			}
			
		});

		penBtn = (ToggleButton) findViewById(R.id.edit_seatpos_map_pen);
		penBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(penBtn.isChecked()){
					mode = MODE.PEN;
				}else{
					mode = MODE.NONE;
				}	
			}
			
		});

		rotateBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_rotate_clockwise);
		rotateBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				seatPosDirection = ((seatPosDirection + 30) % 360 +360) % 360;
				directionET.setText(Integer.toString(seatPosDirection));
				updateMapImage();
			}
			
		});
		
		counterRotateBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_rotate_counterclockwise);
		counterRotateBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				seatPosDirection = ((seatPosDirection - 30) % 360 +360) % 360;
				directionET.setText(Integer.toString(seatPosDirection));
				updateMapImage();
			}
			
		});
		
		ImageButton okBtn =(ImageButton)findViewById(R.id.imagebtn_editseatpos_confirm);
		okBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				seatPosX = Integer.parseInt(xET.getText().toString());
				seatPosY = Integer.parseInt(yET.getText().toString());
				seatPosWidth = Integer.parseInt(widthET.getText().toString());
				seatPosHeight = Integer.parseInt(heightET.getText().toString());
				seatPosDirection = Integer.parseInt(directionET.getText().toString());
				seatPosFloor = (Integer)floorSpinner.getSelectedItem();
				
				newSeatInfo = new SeatInfo();
				
				newSeatInfo.setInitials(initials);
				newSeatInfo.setX(seatPosX);
				newSeatInfo.setY(seatPosY);
				newSeatInfo.setWidth(seatPosWidth);
				newSeatInfo.setHeight(seatPosHeight);
				newSeatInfo.setDirection(seatPosDirection);
				newSeatInfo.setFloorNo(seatPosFloor);
				
				if(dataModified(seatInfo, newSeatInfo)){
					Dialog dialog = new AlertDialog.Builder(EditSeatPositionActivity.this)
		        	.setIcon(R.drawable.ic_launcher)
		        	.setTitle("Data has been modified,do you want to apply the modification?")
		        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(!DataManager.getInstance().updateSeatInfo(initials, newSeatInfo)){
								Toast.makeText(EditSeatPositionActivity.this, "Update seat info failed!", Toast.LENGTH_SHORT);
							}
							
							dialog.dismiss();	
							
							setResult(MainActivity.ACTIVITY_RESULT_EDITSEATINFO_OK);
							EditSeatPositionActivity.this.finish();
							//update main activity layout
							
						}
					})
		        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();		
						}
					})
		        	.show();
					
				}else{
					finish();
				}	
			}

			
			
		});
		
		ImageButton cancelBtn =(ImageButton)findViewById(R.id.imagebtn_editseatpos_cancel);
		cancelBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				clickCancelButton();				
			}
			
		});
	}
	
	@Override
	public void onBackPressed() {
		seatPosX = Integer.parseInt(xET.getText().toString());
		seatPosY = Integer.parseInt(yET.getText().toString());
		seatPosWidth = Integer.parseInt(widthET.getText().toString());
		seatPosHeight = Integer.parseInt(heightET.getText().toString());
		seatPosDirection = Integer.parseInt(directionET.getText().toString());
		seatPosFloor = (Integer)floorSpinner.getSelectedItem();
		
		newSeatInfo = new SeatInfo();
		
		newSeatInfo.setInitials(initials);
		newSeatInfo.setX(seatPosX);
		newSeatInfo.setY(seatPosY);
		newSeatInfo.setWidth(seatPosWidth);
		newSeatInfo.setHeight(seatPosHeight);
		newSeatInfo.setDirection(seatPosDirection);
		newSeatInfo.setFloorNo(seatPosFloor);
		
		if(dataModified(seatInfo, newSeatInfo)){
			Dialog dialog = new AlertDialog.Builder(EditSeatPositionActivity.this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle("Data has been modified,do you want to save the modification?")
        	.setPositiveButton("Save",new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(!DataManager.getInstance().updateSeatInfo(initials, newSeatInfo)){
						Toast.makeText(EditSeatPositionActivity.this, "Update seat info failed!", Toast.LENGTH_SHORT);
					}
					
					dialog.dismiss();	
					
					setResult(MainActivity.ACTIVITY_RESULT_EDITSEATINFO_OK);
					EditSeatPositionActivity.this.finish();
				}
			})
        	.setNegativeButton("Not Save", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();		
					EditSeatPositionActivity.this.finish();
				}
			})
        	.show();
			
		}else{
			finish();
		}
			
	}
	
	private boolean dataModified(SeatInfo seatInfo, SeatInfo newSeatInfo) {
		return seatInfo == null ||
				seatInfo.getFloorNo()!=newSeatInfo.getFloorNo() ||
				seatInfo.getX() != newSeatInfo.getX() ||
				seatInfo.getY() != newSeatInfo.getY() ||
				seatInfo.getWidth() != newSeatInfo.getWidth() ||
				seatInfo.getHeight() != newSeatInfo.getHeight() ||
				seatInfo.getDirection() != newSeatInfo.getDirection();
	}
	
	private void clickCancelButton(){
		seatPosX = Integer.parseInt(xET.getText().toString());
		seatPosY = Integer.parseInt(yET.getText().toString());
		seatPosWidth = Integer.parseInt(widthET.getText().toString());
		seatPosHeight = Integer.parseInt(heightET.getText().toString());
		seatPosDirection = Integer.parseInt(directionET.getText().toString());
		seatPosFloor = (Integer)floorSpinner.getSelectedItem();
		
		newSeatInfo = new SeatInfo();
		
		newSeatInfo.setInitials(initials);
		newSeatInfo.setX(seatPosX);
		newSeatInfo.setY(seatPosY);
		newSeatInfo.setWidth(seatPosWidth);
		newSeatInfo.setHeight(seatPosHeight);
		newSeatInfo.setDirection(seatPosDirection);
		newSeatInfo.setFloorNo(seatPosFloor);
		
		if(dataModified(seatInfo, newSeatInfo)){
			Dialog dialog = new AlertDialog.Builder(EditSeatPositionActivity.this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle("Data has been modified,do you want to cancel the modification?")
        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();		
					EditSeatPositionActivity.this.finish();
				}
			})
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();		
				}
			})
        	.show();
			
		}else{
			finish();
		}
	}
	

	private void onTouchDown(MotionEvent event) {
		if(mode != MODE.PEN){
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = MODE.DRAG;
		}else{
			penStartPosX = (int) event.getX();
			penStartPosY = (int) event.getY();
			
			float[] point = new float[]{penStartPosX, penStartPosY};
			float[] newPoint = new float[]{0, 0};
			Matrix m = new Matrix();
			matrix.invert(m);
			m.mapPoints(newPoint, point);
			
			seatPosX = (int) newPoint[0];
			seatPosY = (int) newPoint[1];
			
			seatPosWidth = 0;
			seatPosHeight = 0;
			seatPosDirection = 0;
			updateSeatPosControls();
			updateMapImage();
			mapIV.setDrawSeatPos(true);
			mapIV.setSeatPos(penStartPosX, penStartPosY, seatPosWidth, seatPosHeight);
			mapIV.invalidate();
		}
	}

	
	private void onTouchUp(MotionEvent event) {
		if(mode != MODE.PEN){
			mode = MODE.NONE;
		}else{
			
			int oldx = seatPosX;
			int oldy = seatPosY;
			int x = (int) event.getX();
			int y = (int) event.getY();
			
			RectF rect = new RectF(penStartPosX, penStartPosY, x, y);
			Matrix m = new Matrix();
			matrix.invert(m);
			m.mapRect(rect);
			
			seatPosX = (int) rect.left;
			seatPosY = (int) rect.top;
			seatPosWidth = (int)rect.width();
			seatPosHeight = (int)rect.height();
			
			updateSeatPosControls();
			updateMapImage();
			mapIV.setDrawSeatPos(false);
		}
	}

	private void onPointerUp(MotionEvent event) {
		if(mode != MODE.PEN){
			mode = MODE.NONE;
		}
	}

	private void onPointerDown(MotionEvent event) {
		if(mode != MODE.PEN){
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

	private void onTouchMove(MotionEvent event) {
		if (mode == MODE.DRAG) {
			matrix.set(savedMatrix);
			matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
		} else if (mode == MODE.ZOOM) {
			float afterLength = getDistance(event);
			if (afterLength > 10f) {
				matrix.set(savedMatrix);
				float scale = afterLength / beforeLength;
				matrix.postScale(scale, scale, mid.x, mid.y);
			}
		}else if(mode == MODE.PEN){
			int x = (int) event.getX();
			int y = (int) event.getY();
			
			RectF rect = new RectF(penStartPosX, penStartPosY, x, y);
			Matrix m = new Matrix();
			matrix.invert(m);
			m.mapRect(rect);
			
			seatPosX = (int) rect.left;
			seatPosY = (int) rect.top;
			seatPosWidth = (int)rect.width();
			seatPosHeight = (int)rect.height();
			
			int drawX = Math.min(penStartPosX, x);
			int drawY = Math.min(penStartPosY, y);
			int drawWidth = Math.abs(x - penStartPosX);
			int drawHeight = Math.abs(y - penStartPosY);
			
			
			updateSeatPosControls();
			mapIV.setSeatPos(drawX, drawY, drawWidth, drawHeight);
			mapIV.invalidate();
			//updateMapImage();
		}
	}

	private float getDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);

		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);

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

	private int getStatusBarHeight() {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			Log.e("MapActivity", "get status bar height failed");
			e.printStackTrace();
		}
		return sbar;
	}
	
	private void updateSeatPosControls() {
		xET.setText(Integer.toString(seatPosX));
		yET.setText(Integer.toString(seatPosY));
		widthET.setText(Integer.toString(seatPosWidth));
		heightET.setText(Integer.toString(seatPosHeight));
		directionET.setText(Integer.toString(seatPosDirection));
	}
	

	
	private void updateMapImage() {
		
		CompositedImageGetter imageGetter = new CompositedImageGetter(new RealSizeImageGetter());
		
		imageGetter.addFilter(new SeatPositionImageFilter(seatPosX, seatPosY, seatPosWidth, seatPosHeight, seatPosDirection));
		
		ImageLoader imageLoader = new ImageLoader(imageGetter);
		
		String mapFilename = DataManager.getInstance().getMapFilenameByFloor(seatPosFloor);
		
		if(mapFilename != null){
			ImageLoader.OnImageLoadListener listener = new ImageLoader.OnImageLoadListener() {
				
				@Override
				public void onImageLoaded(Bitmap bitmap, Object[] parameters) {
					// TODO Auto-generated method stub
					mapImage = bitmap;
					mapIV.setImageBitmap(bitmap);
					bitmapWidth = bitmap.getWidth();
					bitmapHeight = bitmap.getHeight();
				}
				
				@Override
				public void onError(Object[] parameters) {
				}
			};
			imageLoader.loadImage(new Object[]{mapFilename}, listener);
		}
			
	}
}
