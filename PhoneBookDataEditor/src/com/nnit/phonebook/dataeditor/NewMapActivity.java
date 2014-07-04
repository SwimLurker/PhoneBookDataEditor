package com.nnit.phonebook.dataeditor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nnit.phonebook.dataeditor.data.DataManager;
import com.nnit.phonebook.dataeditor.data.FLAG;
import com.nnit.phonebook.dataeditor.data.MapItem;
import com.nnit.phonebook.dataeditor.ui.ImageLoader;
import com.nnit.phonebook.dataeditor.ui.NumberPicker;
import com.nnit.phonebook.dataeditor.ui.OpenFileDialog;
import com.nnit.phonebook.dataeditor.ui.RealSizeImageGetter;
import com.nnit.phonebook.dataeditor.ui.ThumbnailImageGetter;

public class NewMapActivity extends Activity{
	
	private MapItem newMapItem = null;
	private Bitmap newMap = null;
	
	private LayoutInflater inflater = null;
	
	private NumberPicker floorNP;
	private TextView filenameTV;
	
	private ImageView mapIV;
	
	private ImageLoader imageLoader = null;
	private ImageLoader thumbnailImageLoader = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_new_map);
		
		
		
		inflater = LayoutInflater.from(this);
		
		Resources r = getResources();
		
		imageLoader = new ImageLoader(new RealSizeImageGetter());
		
		
		thumbnailImageLoader = new ImageLoader(
				new ThumbnailImageGetter((int)r.getDimension(R.dimen.map_width_thumbnail_small), 
						(int)r.getDimension(R.dimen.map_height_thumbnail_small)));
		
		TextView tv = (TextView)findViewById(R.id.textview_newmap_title);
		tv.setText("New Map Item");
		
		mapIV = (ImageView) findViewById(R.id.new_map_image);
		
		
		if(newMap == null){
			mapIV.setImageResource(R.drawable.new_image);
		}else{
			mapIV.setImageBitmap(newMap);
		}
		
		mapIV.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showDialog(R.layout.dialog_mapselect);
			}
			
		});
		
        floorNP = (NumberPicker) findViewById(R.id.new_map_floor);
        floorNP.setMaxValue(36);
        floorNP.setMinValue(1);
        floorNP.setValue(28);
        floorNP.setFocusable(true);
        floorNP.setFocusableInTouchMode(true);
		
		filenameTV = (TextView) findViewById(R.id.new_map_filename);
		
	
		/*
		ImageButton imageBtn = (ImageButton)findViewById(R.id.new_map_image_btn);
		imageBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				showDialog(R.layout.dialog_mapselect);
			}
			
		});
		*/
		
		ImageButton okBtn =(ImageButton)findViewById(R.id.imagebtn_newmap_confirm);
		okBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				newMapItem = checkInput();

				if(newMapItem != null){
					Dialog dialog = new AlertDialog.Builder(NewMapActivity.this)
			        	.setIcon(R.drawable.ic_launcher)
			        	.setTitle("Do you want to save the map info?")
			        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(createMapItem()){
									dialog.dismiss();	
									setResult(MainActivity.ACTIVITY_RESULT_NEWMAP_OK);
									NewMapActivity.this.finish();
								}else{
									dialog.dismiss();	
								
								}
							}
						})
			        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();		
							}
						})
			        	.show();
				}
				
			}
			
		});
		
		ImageButton cancelBtn =(ImageButton)findViewById(R.id.imagebtn_newmap_cancel);
		cancelBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {		
				if(newMap != null){
					Dialog dialog = new AlertDialog.Builder(NewMapActivity.this)
			    	.setIcon(R.drawable.ic_launcher)
			    	.setTitle("Do you want to cancel the created info?")
			    	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();		
							NewMapActivity.this.finish();
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
					NewMapActivity.this.finish();
				}
			}
			
		});

	}
	
	@Override
	protected Dialog onCreateDialog(int id) {

		if (id == R.layout.dialog_mapselect) {

			Map<String, Integer> images = new HashMap<String, Integer>();
			images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
			images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
			images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
			images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_file);
			images.put("png", R.drawable.filedialog_pngfile);
			images.put("jpg", R.drawable.filedialog_jpgfile);

			Dialog dialog = OpenFileDialog.createDialog(id, this,
					"Select Map Image File",
					new OpenFileDialog.CallbackBundle() {
						@Override
						public void callback(Bundle bundle) {

							String fullFileName = bundle.getString("path");
							filenameTV.setText(fullFileName);
							
							ImageLoader.OnImageLoadListener thumbnailImageLoadListener = new ImageLoader.OnImageLoadListener() {
								
								@Override
								public void onImageLoaded(Bitmap bitmap, Object[] parameters) {
									if(mapIV != null){
										if(bitmap!=null){
											mapIV.setImageBitmap(bitmap);
											String bitmapFilename = (String)parameters[0];
											DataManager.getInstance().updateMapThumbnailPhotoCache(bitmapFilename, bitmap);
										}
									}
								}
								
								@Override
								public void onError(Object[] parameters) {
									if(mapIV != null){
										mapIV.setImageResource(R.drawable.new_image);
									}
								}
							};
							
							thumbnailImageLoader.loadImage(new Object[]{fullFileName}, thumbnailImageLoadListener);
							
							ImageLoader.OnImageLoadListener imageLoadListener = new ImageLoader.OnImageLoadListener() {
								
								@Override
								public void onImageLoaded(Bitmap bitmap, Object[] parameters) {
									if(mapIV != null){
										if(bitmap!=null){
											newMap = bitmap;
										}
									}
								}
								
								@Override
								public void onError(Object[] parameters) {
									if(mapIV != null){
										newMap = null;
									}
								}
							};
							imageLoader.loadImage(new Object[]{fullFileName}, imageLoadListener);
							
						}
					}, ".png|.jpg", images);

			return dialog;
		}
		return null;
	}
	@Override
	public void onBackPressed() {
		if(newMap != null){
			newMapItem = checkInput();
			if(newMapItem != null){
				Dialog dialog = new AlertDialog.Builder(NewMapActivity.this)
		    	.setIcon(R.drawable.ic_launcher)
		    	.setTitle("You have selected map file, do you want to save the created info?")
		    	.setPositiveButton("Save",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();		
						if(createMapItem()){
							setResult(MainActivity.ACTIVITY_RESULT_NEWMAP_OK);
							NewMapActivity.this.finish();
						}
					}
				})
		    	.setNegativeButton("Not Save", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();	
						NewMapActivity.this.finish();
					}
				})
		    	.show();
			}else{
				super.onBackPressed();
			}
			
		}else{
			super.onBackPressed();
		}
	
	}
	
	private MapItem checkInput(){
		int floor = floorNP.getValue();
		if(floor <= 0){
			Toast.makeText(NewMapActivity.this, "Please input valid floor number", Toast.LENGTH_SHORT).show();
			return null;
		}
		
		String filename = filenameTV.getText().toString();
		if(filename.equals("")){
			Toast.makeText(NewMapActivity.this, "Please select map file", Toast.LENGTH_SHORT).show();
			return null;
		}
		
		File f = new File(filename);
		if((!f.exists())||(!f.isFile())){
			Toast.makeText(NewMapActivity.this, "Please select valid map file", Toast.LENGTH_SHORT).show();
			return null;
		}
		
		MapItem result = new MapItem();
		
		result.setFloor(floor);
		result.setMapFilename(f.getName());
		result.setFlag(FLAG.NEW);
		
		return result;
		
	}
	
	private boolean createMapItem() {
		
		if(!DataManager.getInstance().newMapInfo(newMapItem)){
			Toast.makeText(NewMapActivity.this, "Create map failed!", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(!DataManager.getInstance().newMapImage(newMapItem,newMap)){
			Toast.makeText(NewMapActivity.this, "Create map image failed!", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
		
	}

}