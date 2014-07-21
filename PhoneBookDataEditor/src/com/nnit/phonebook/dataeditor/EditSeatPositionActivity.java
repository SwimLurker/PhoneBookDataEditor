package com.nnit.phonebook.dataeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.nnit.phonebook.dataeditor.data.DataManager;
import com.nnit.phonebook.dataeditor.data.SeatInfo;
import com.nnit.phonebook.dataeditor.ui.ISeatPositionChangedListener;
import com.nnit.phonebook.dataeditor.ui.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EditSeatPositionActivity extends Activity implements ISeatPositionChangedListener {	
	private String initials = null;
	private SeatInfo seatInfo = null;
	private SeatInfo newSeatInfo = null;
	
	private Spinner floorSpinner;
	private MapView mapIV;
	private EditText xET;
	private EditText yET;
	private EditText widthET;
	private EditText heightET;
	private EditText directionET;
	private ToggleButton fullscreenBtn = null;
	private ToggleButton penBtn = null;
	private ImageButton rotateBtn = null;
	private ImageButton counterRotateBtn = null;
	private ImageButton zoomInBtn = null;
	private ImageButton zoomOutBtn = null;
	private ImageButton locateBtn = null;
	
	private LinearLayout floorPanelLayout = null;
	private LinearLayout infoPanelLayout = null;
	
	private Resources resources = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		resources = getResources();
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_edit_seatposition);

		initials = (String) getIntent().getSerializableExtra(EditPhoneBookActivity.SELECTED_INITIALS);

		seatInfo = DataManager.getInstance().getSeatInfoByInitial(initials);
		
		mapIV = (MapView) findViewById(R.id.edit_seatpos_map);
		
		mapIV.addSeatPositionListener(this);
		
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

		int seatPosFloor, seatPosX, seatPosY, seatPosWidth, seatPosHeight, seatPosDirection;
		
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
		
		
		updateSeatPositionInfo();
		
		OnFocusChangeListener ocl = new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					if (v.getId() == R.id.edit_seatpos_x || 
							v.getId() == R.id.edit_seatpos_y ||
							v.getId() == R.id.edit_seatpos_width || 
							v.getId() == R.id.edit_seatpos_height || 
							v.getId() == R.id.edit_seatpos_direction){
						updateSeatPositionInfo();
					}
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
				int seatPosFloor = (Integer)floorSpinner.getSelectedItem();
				Bitmap mapImage = getFloorMapImage(seatPosFloor);
				if(mapImage != null){
					mapIV.setMap(mapImage);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});


		
		zoomInBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_zoomin);
		zoomInBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mapIV.zoomIn();
			}

		});

		zoomOutBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_zoomout);
		zoomOutBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mapIV.zoomOut();
			}

		});

		locateBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_locate);
		locateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mapIV.locateSeatPosition();
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
				}else{
					floorPanelLayout.setVisibility(View.VISIBLE);
					infoPanelLayout.setVisibility(View.VISIBLE);
				}
			}
			
		});

		penBtn = (ToggleButton) findViewById(R.id.edit_seatpos_map_pen);
		penBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mapIV.setPenPressed(penBtn.isChecked());	
			}
			
		});

		rotateBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_rotate_clockwise);
		rotateBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mapIV.Rotate(30);
			}
			
		});
		
		counterRotateBtn = (ImageButton) findViewById(R.id.edit_seatpos_map_rotate_counterclockwise);
		counterRotateBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mapIV.Rotate(-30);
			}
			
		});
		
		ImageButton okBtn =(ImageButton)findViewById(R.id.imagebtn_editseatpos_confirm);
		okBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				newSeatInfo = getNewSeatInfo();
				
				if(dataModified(seatInfo, newSeatInfo)){
					Dialog dialog = new AlertDialog.Builder(EditSeatPositionActivity.this)
		        	.setIcon(R.drawable.ic_launcher)
		        	.setTitle(resources.getString(R.string.info_save_modification))
		        	.setPositiveButton(resources.getString(R.string.lable_okbtn),new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(!DataManager.getInstance().updateSeatInfo(initials, newSeatInfo)){
								Toast.makeText(EditSeatPositionActivity.this, resources.getString(R.string.error_save_seatinfo), Toast.LENGTH_SHORT).show();
							}else{
								setResult(MainActivity.ACTIVITY_RESULT_EDITSEATINFO_OK);
							}							
							dialog.dismiss();								
							EditSeatPositionActivity.this.finish();
							
						}
					})
		        	.setNegativeButton(resources.getString(R.string.lable_cancelbtn), new DialogInterface.OnClickListener() {
						
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
	public void onSeatRectChanged(RectF seatRect) {
		xET.setText(Integer.toString((int)seatRect.left));
		yET.setText(Integer.toString((int)seatRect.top));
		widthET.setText(Integer.toString((int)seatRect.width()));
		heightET.setText(Integer.toString((int)seatRect.height()));
	}

	@Override
	public void onSeatDirectionChanged(int direction) {
		directionET.setText(Integer.toString(direction));
	}
	
	private SeatInfo getNewSeatInfo() {
		int seatPosX = Integer.parseInt(xET.getText().toString());
		int seatPosY = Integer.parseInt(yET.getText().toString());
		int seatPosWidth = Integer.parseInt(widthET.getText().toString());
		int seatPosHeight = Integer.parseInt(heightET.getText().toString());
		int seatPosDirection = Integer.parseInt(directionET.getText().toString());
		int seatPosFloor = (Integer)floorSpinner.getSelectedItem();
		
		SeatInfo result = new SeatInfo();
		
		result.setInitials(initials);
		result.setX(seatPosX);
		result.setY(seatPosY);
		result.setWidth(seatPosWidth);
		result.setHeight(seatPosHeight);
		result.setDirection(seatPosDirection);
		result.setFloorNo(seatPosFloor);
		
		return result;
	}
	
	private void updateSeatPositionInfo() {
		int seatPosX = Integer.parseInt(xET.getText().toString());
		int seatPosY = Integer.parseInt(yET.getText().toString());
		int seatPosWidth = Integer.parseInt(widthET.getText().toString());
		int seatPosHeight = Integer.parseInt(heightET.getText().toString());
		int seatPosDirection = Integer.parseInt(directionET.getText().toString());
		mapIV.setSeatRect(new RectF(seatPosX, seatPosY, seatPosX + seatPosWidth, seatPosY + seatPosHeight));
		mapIV.setSeatDirection(seatPosDirection);	
		mapIV.invalidate();
	}

	@Override
	public void onBackPressed() {
		int seatPosX = Integer.parseInt(xET.getText().toString());
		int seatPosY = Integer.parseInt(yET.getText().toString());
		int seatPosWidth = Integer.parseInt(widthET.getText().toString());
		int seatPosHeight = Integer.parseInt(heightET.getText().toString());
		int seatPosDirection = Integer.parseInt(directionET.getText().toString());
		int seatPosFloor = (Integer)floorSpinner.getSelectedItem();
		
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
        	.setTitle(resources.getString(R.string.info_save_modification))
        	.setPositiveButton(resources.getString(R.string.lable_savebtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(!DataManager.getInstance().updateSeatInfo(initials, newSeatInfo)){
						Toast.makeText(EditSeatPositionActivity.this, resources.getString(R.string.error_save_seatinfo), Toast.LENGTH_SHORT).show();
					}
					
					dialog.dismiss();	
					
					setResult(MainActivity.ACTIVITY_RESULT_EDITSEATINFO_OK);
					EditSeatPositionActivity.this.finish();
				}
			})
        	.setNegativeButton(resources.getString(R.string.lable_notsavebtn), new DialogInterface.OnClickListener() {
				
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
		newSeatInfo = getNewSeatInfo();
		
		if(dataModified(seatInfo, newSeatInfo)){
			Dialog dialog = new AlertDialog.Builder(EditSeatPositionActivity.this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.info_cancel_modification))
        	.setPositiveButton(resources.getString(R.string.lable_okbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();		
					EditSeatPositionActivity.this.finish();
				}
			})
        	.setNegativeButton(resources.getString(R.string.lable_cancelbtn), new DialogInterface.OnClickListener() {
				
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

	private Bitmap getFloorMapImage(int floor) {
		String mapFilename = DataManager.getInstance().getMapFilenameByFloor(floor);
		FileInputStream fis = null;
		try {
			if (mapFilename != null) {
				File f = new File(mapFilename);

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
