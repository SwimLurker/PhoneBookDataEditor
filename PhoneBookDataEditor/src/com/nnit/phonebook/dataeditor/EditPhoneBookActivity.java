package com.nnit.phonebook.dataeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.nnit.phonebook.dataeditor.data.DataManager;
import com.nnit.phonebook.dataeditor.data.DepartmentInfo;
import com.nnit.phonebook.dataeditor.data.PhoneBookItem;
import com.nnit.phonebook.dataeditor.data.PhoneBookItem.GENDER;
import com.nnit.phonebook.dataeditor.data.SeatInfo;
import com.nnit.phonebook.dataeditor.ui.ImageLoader;
import com.nnit.phonebook.dataeditor.ui.SeatPositionImageDecorator;
import com.nnit.phonebook.dataeditor.ui.ThumbnailImageDecorator;
import com.nnit.phonebook.dataeditor.util.BitmapUtil;
import com.nnit.phonebook.dataeditor.ui.RealSizeImageGetter;
import com.nnit.phonebook.dataeditor.ui.CompositedImageGetter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class EditPhoneBookActivity extends Activity{
	
	//public static final String TARGET_INITIALS = "com.nnit.phonebook.dataeditor.TARGET_INITIALS";
	public static final String SELECTED_INITIALS = "com.nnit.phonebook.dataeditor.SELECTED_INITIALS";
	
	public static final int PICKPHOTO_BY_CAMERA = 1001;
	public static final int PICKPHOTO_BY_ALBUM = 1002;
	
	public static final String URI_SCHEMA_CONTENT = "content";
	public static final String URI_SCHEMA_FILE = "file";
	
	
	private PhoneBookItem pbItem = null;
	private PhoneBookItem newPbItem = null;
	private Bitmap photo = null;
	private Bitmap newPhoto = null;
	
	//private SeatInfo seatInfo = null;
	
	private LayoutInflater inflater = null;
	private Resources resources = null;
	
	private TextView initialsTV;
	private EditText nameET;
	private EditText localnameET;
	private RadioButton genderMaleRB;
	private RadioButton genderFemaleRB;
	private EditText phoneET;
	private EditText titleET;
	private Spinner depNoSpinner;
	private Spinner depNameSpinner;
	private EditText managerET;
	private ImageView seatIV;
	private ImageButton seatBtn;
	private ImageView photoIV;
	private ImageButton photoBtn;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_edit_phonebook);
		
		pbItem = (PhoneBookItem) getIntent().getSerializableExtra(MainActivity.SELECTED_PBITEM);
		
		SeatInfo seatInfo = pbItem == null? null: DataManager.getInstance().getSeatInfoByInitial(pbItem.getInitials());
		
		inflater = LayoutInflater.from(this);
		resources = getResources();
		
		int width = (int)resources.getDimension(R.dimen.map_width_thumbnail_small);
		int height = (int)resources.getDimension(R.dimen.map_height_thumbnail_small);
		
		
		CompositedImageGetter imageGetter = new CompositedImageGetter(new RealSizeImageGetter());
		if(seatInfo != null){
			imageGetter.addDecorator(new SeatPositionImageDecorator(seatInfo.getX(), seatInfo.getY(), seatInfo.getWidth(), seatInfo.getHeight(), seatInfo.getDirection()));
		}
		imageGetter.addDecorator(new ThumbnailImageDecorator(width, height));
		
		
		TextView tv = (TextView)findViewById(R.id.textview_editphonebook_title);
		tv.setText("Edit PhoneBook Item");
		
		String initials = pbItem.getInitials();
		photoIV = (ImageView) findViewById(R.id.edit_phonebook_photo);
		
		photo = getPhotoBitmap(initials);
		if(photo == null){
			photoIV.setImageResource(R.drawable.photo);
		}else{
			photoIV.setImageBitmap(photo);
		}
		newPhoto = null;
		
		photoBtn = (ImageButton)findViewById(R.id.edit_phonebook_photo_btn);
		photoBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				pickupPhoto();
			}
			
		});
		
		photoIV.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				pickupPhoto();
			}
			
		});
		
		
		
		initialsTV = (TextView) findViewById(R.id.edit_phonebook_initials);
		initialsTV.setText(pbItem.getInitials());
		
		nameET = (EditText) findViewById(R.id.edit_phonebook_name);
		nameET.setText(pbItem.getName());
		
		localnameET = (EditText) findViewById(R.id.edit_phonebook_localname);
		localnameET.setText(pbItem.getLocalName());
		
		genderMaleRB = (RadioButton)findViewById(R.id.edit_phonebook_gender_male);
		genderFemaleRB = (RadioButton)findViewById(R.id.edit_phonebook_gender_female);
		
		if(pbItem.getGender() == PhoneBookItem.GENDER.MALE){
			genderMaleRB.setChecked(true);
			genderFemaleRB.setChecked(false);
			
		}else if(pbItem.getGender() == PhoneBookItem.GENDER.FEMALE){
			genderMaleRB.setChecked(false);
			genderFemaleRB.setChecked(true);
		}
		
		phoneET = (EditText) findViewById(R.id.edit_phonebook_phone);
		phoneET.setText(pbItem.getPhone());
		
		titleET = (EditText) findViewById(R.id.edit_phonebook_title);
		titleET.setText(pbItem.getTitle());
		
		List <DepartmentInfo> departments = DataManager.getInstance().getAllDepartments();
		List<String> depNoList = new ArrayList<String>();
		List<String> depNameList = new ArrayList<String>();
		for(DepartmentInfo di: departments){
			depNoList.add(di.getDepartmentNO());
			depNameList.add(di.getDepartmentName());
		}
		
		depNoSpinner = (Spinner) findViewById(R.id.edit_phonebook_departmentno);
		
		ArrayAdapter<String> depNoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depNoList);
		depNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		depNoSpinner.setAdapter(depNoAdapter);
		for(int i = 0; i < depNoList.size(); i++){
			String depNo = depNoList.get(i);
			if(depNo.equalsIgnoreCase(pbItem.getDepartmentNo())){
				depNoSpinner.setSelection(i);
				break;
			}
		}
		depNoSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				depNameSpinner.setSelection(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				depNameSpinner.setSelected(false);
			}
			
		});
		
		
		depNameSpinner = (Spinner) findViewById(R.id.edit_phonebook_department);
		
		ArrayAdapter<String> depNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depNameList);
		depNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		depNameSpinner.setAdapter(depNameAdapter);
		
		for(int i = 0; i < depNameList.size(); i++){
			String depName = depNameList.get(i);
			if(depName.equalsIgnoreCase(pbItem.getDepartment())){
				depNameSpinner.setSelection(i);
				break;
			}
		}
		
		depNameSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				depNoSpinner.setSelection(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				depNoSpinner.setSelected(false);
			}
			
		});
		
		managerET = (EditText) findViewById(R.id.edit_phonebook_manager);
		managerET.setText(pbItem.getManager());
		
		seatIV = (ImageView)findViewById(R.id.edit_phonebook_seatpos);
		
		setMapImageThumbnail();
		
		seatIV.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showEditSeatPositionActivity(pbItem.getInitials());
						
			}

			
			
		});
		
		seatBtn = (ImageButton)findViewById(R.id.edit_phonebook_seatpos_btn);
		seatBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				showEditSeatPositionActivity(pbItem.getInitials());
			}
			
		});
		
		
		ImageButton okBtn =(ImageButton)findViewById(R.id.imagebtn_editphonebook_confirm);
		okBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				newPbItem = new PhoneBookItem();
				
				newPbItem.setInitials(initialsTV.getText().toString());
				newPbItem.setName(nameET.getText().toString());
				newPbItem.setLocalName(localnameET.getText().toString());
				newPbItem.setGender(genderMaleRB.isChecked()? GENDER.MALE: GENDER.FEMALE);
				newPbItem.setPhone(phoneET.getText().toString());
				newPbItem.setTitle(titleET.getText().toString());
				newPbItem.setDepartmentNo((String)depNoSpinner.getSelectedItem());
				newPbItem.setDepartment((String)depNameSpinner.getSelectedItem());
				newPbItem.setManager(managerET.getText().toString());
				
				if(dataModified(pbItem, newPbItem) || newPhoto!=null){
					Dialog dialog = new AlertDialog.Builder(EditPhoneBookActivity.this)
		        	.setIcon(R.drawable.ic_launcher)
		        	.setTitle("Data has been modified,do you want to apply the modification?")
		        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(!DataManager.getInstance().updatePhoneBook(pbItem.getInitials(), newPbItem)){
								Toast.makeText(EditPhoneBookActivity.this, "Update phonebook info failed!", Toast.LENGTH_SHORT);
							}
							
							if(!DataManager.getInstance().updatePhoneBookPhoto(pbItem.getInitials(), newPhoto)){
								Toast.makeText(EditPhoneBookActivity.this, "Update phonebook photo failed!", Toast.LENGTH_SHORT);
							}
							dialog.dismiss();	
							
							setResult(MainActivity.ACTIVITY_RESULT_EDITPHONEBOOK_OK);
							EditPhoneBookActivity.this.finish();
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
		
		ImageButton cancelBtn =(ImageButton)findViewById(R.id.imagebtn_editphonebook_cancel);
		cancelBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				clickCancelButton();				
			}
			
		});

	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	        Intent intent) {
	    super.onActivityResult(requestCode, resultCode, intent);
	    switch (requestCode) {
		    case PICKPHOTO_BY_CAMERA:
		        if (resultCode == RESULT_OK) {
		        	if(newPhoto!=null && !newPhoto.isRecycled()){
		        		newPhoto.recycle();
		        	}
		        	Bundle bundle = intent.getExtras();
		        	Bitmap bit = (Bitmap)bundle.get("data");
		        	if(bit!=null){
		        		
		        		newPhoto = BitmapUtil.getImageThumbnail(bit, (int)resources.getDimension(R.dimen.photo_width), (int)resources.getDimension(R.dimen.photo_height));
		        	}
		        	if(newPhoto!=null && !newPhoto.isRecycled()){
		        		photoIV.setImageBitmap(newPhoto);
		        	}
		        }
		        break;
		    case PICKPHOTO_BY_ALBUM:
		    	if (resultCode == RESULT_OK) {
		        	if(newPhoto!=null && !newPhoto.isRecycled()){
		        		newPhoto.recycle();
		        	}
		        	Uri imageUri = intent.getData();
		        	if(imageUri != null){
		        		String schema = imageUri.getScheme();
		        		String path = null;
			        	if(URI_SCHEMA_CONTENT.equalsIgnoreCase(schema)){
			        		path = getPathFromUri(imageUri);
			        	}else if(URI_SCHEMA_FILE.equalsIgnoreCase(schema)){
			        		int index = path.indexOf(URI_SCHEMA_FILE);
			        		path = path.substring(index + URI_SCHEMA_CONTENT.length() + 3);
			        	}else{
			        		path = imageUri.getPath();
			        	}
		        		try{
		        			newPhoto = BitmapUtil.getImageThumbnail(path, (int)resources.getDimension(R.dimen.photo_width), (int)resources.getDimension(R.dimen.photo_height));
		        		}catch(Exception e){
		        			e.printStackTrace();
		        		}
		        	}
		        	if(newPhoto!=null && !newPhoto.isRecycled()){
		        		photoIV.setImageBitmap(newPhoto);
		        	}
		        }
		        break;
		    case MainActivity.ACTIVITY_REQUEST_CODE_EDITSEATPOSITION_ACTIVITY:
				if (resultCode == MainActivity.ACTIVITY_RESULT_EDITSEATINFO_OK) {
					setMapImageThumbnail();
				}
				break;
		    }
	}

	@Override
	public void onBackPressed() {
		clickCancelButton();
			
	}
	
	private void clickCancelButton(){
		newPbItem = new PhoneBookItem();
		
		newPbItem.setInitials(initialsTV.getText().toString());
		newPbItem.setName(nameET.getText().toString());
		newPbItem.setLocalName(localnameET.getText().toString());
		newPbItem.setGender(genderMaleRB.isChecked()? GENDER.MALE: GENDER.FEMALE);
		newPbItem.setPhone(phoneET.getText().toString());
		newPbItem.setTitle(titleET.getText().toString());
		newPbItem.setDepartmentNo((String)depNoSpinner.getSelectedItem());
		newPbItem.setDepartment((String)depNameSpinner.getSelectedItem());
		newPbItem.setManager(managerET.getText().toString());
		
		if(dataModified(pbItem, newPbItem)|| newPhoto!=null){
			Dialog dialog = new AlertDialog.Builder(EditPhoneBookActivity.this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle("Data has been modified,do you want to cancel the modification?")
        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();		
					EditPhoneBookActivity.this.finish();
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
	
	protected boolean dataModified(PhoneBookItem pbItem,
			PhoneBookItem newPbItem) {
		return !pbItem.getName().equals(newPbItem.getName()) ||
				!pbItem.getLocalName().equals(newPbItem.getLocalName()) ||
				!pbItem.getDepartment().equals(newPbItem.getDepartment()) ||
				!pbItem.getDepartmentNo().equals(newPbItem.getDepartmentNo()) ||
				!pbItem.getGender().equals(newPbItem.getGender()) ||
				!pbItem.getPhone().equals(newPbItem.getPhone()) ||
				!pbItem.getTitle().equals(newPbItem.getTitle()) ||
				!pbItem.getManager().equals(newPbItem.getManager()) ;
	}


	private Bitmap getPhotoBitmap(String initials){
		FileInputStream fis = null;
		try{
			String photoFilename = DataManager.getInstance().getPhotoFilenameByInitials(initials);
			if(photoFilename != null){
				File f = new File(photoFilename);
		
				if(f.exists() && f.isFile()){
					fis = new FileInputStream(f);
					Bitmap bitmap = BitmapFactory.decodeStream(fis);
					return bitmap;
				}			
			}
		}catch(Exception exp){
			exp.printStackTrace();
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	
	private String getPathFromUri(Uri uri){
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = this.managedQuery(uri, proj, null, null, null);
		int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(columnIndex);
		
	}
	
	private void pickupPhoto(){
		CharSequence[] items = {"Camera", "Photo Album"};
		new AlertDialog.Builder(EditPhoneBookActivity.this)
			.setTitle("Pick Photo From")
			.setItems(items, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == 0){ //Camera
						try{
							Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							startActivityForResult(cameraIntent, PICKPHOTO_BY_CAMERA);
						}catch (ActivityNotFoundException e){
							e.printStackTrace();
						}
					}else if(which == 1){ //Photo Album
						try{
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							intent.setType("image/*");
							startActivityForResult(Intent.createChooser(intent, "Please select photo"), PICKPHOTO_BY_ALBUM);
							//startActivityForResult(intent, PICKPHOTO_BY_ALBUM);
						}catch (ActivityNotFoundException e){
							e.printStackTrace();
						}
					}
				}
				
			})
			.create().show();
	}
	
	private void showEditSeatPositionActivity(String initials) {
		Intent intent = new Intent();
		intent.putExtra(SELECTED_INITIALS, initials);
		intent.setAction("com.nnit.phonebook.dataeditor.EditSeatPositionActivity");
		startActivityForResult(intent, MainActivity.ACTIVITY_REQUEST_CODE_EDITSEATPOSITION_ACTIVITY);		
	}
	
	private void setMapImageThumbnail() {
		SeatInfo seatInfo = pbItem == null? null: DataManager.getInstance().getSeatInfoByInitial(pbItem.getInitials());
		if(seatInfo == null) return;
		
		CompositedImageGetter imageGetter = new CompositedImageGetter(new RealSizeImageGetter());
		
		if(seatInfo != null){
			imageGetter.addDecorator(new SeatPositionImageDecorator(seatInfo.getX(), seatInfo.getY(), seatInfo.getWidth(), seatInfo.getHeight(), seatInfo.getDirection()));
		}
		
		int width = (int)resources.getDimension(R.dimen.map_width_thumbnail_small);
		int height = (int)resources.getDimension(R.dimen.map_height_thumbnail_small);
		imageGetter.addDecorator(new ThumbnailImageDecorator(width, height));
		
		ImageLoader imageLoader = new ImageLoader(imageGetter);
		
		
		
		ImageLoader.OnImageLoadListener listener = new ImageLoader.OnImageLoadListener() {
			
			@Override
			public void onImageLoaded(Bitmap bitmap, Object[] parameters) {
				// TODO Auto-generated method stub
				seatIV.setImageBitmap(bitmap);
			}
			
			@Override
			public void onError(Object[] parameters) {
				seatIV.setImageResource(R.drawable.map);
			}
		};
		
		String filename = DataManager.getInstance().getMapFilenameByFloor(seatInfo.getFloorNo());
		if(filename != null){
			imageLoader.loadImage(new Object[]{filename}, listener);	
		}
			
	}
	
	
}
