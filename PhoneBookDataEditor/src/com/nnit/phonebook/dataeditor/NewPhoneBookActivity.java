package com.nnit.phonebook.dataeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.nnit.phonebook.dataeditor.data.DataManager;
import com.nnit.phonebook.dataeditor.data.DepartmentInfo;
import com.nnit.phonebook.dataeditor.data.FLAG;
import com.nnit.phonebook.dataeditor.data.PhoneBookItem;
import com.nnit.phonebook.dataeditor.data.PhoneBookItem.GENDER;
import com.nnit.phonebook.dataeditor.util.BitmapUtil;

public class NewPhoneBookActivity extends Activity{
	
	public static final int PICKPHOTO_BY_CAMERA = 1001;
	public static final int PICKPHOTO_BY_ALBUM = 1002;
	
	public static final String URI_SCHEMA_CONTENT = "content";
	public static final String URI_SCHEMA_FILE = "file";
	
	
	private PhoneBookItem newPbItem = null;
	private Bitmap newPhoto = null;
	
	private LayoutInflater inflater = null;
	
	private EditText initialsET;
	private EditText nameET;
	private EditText localnameET;
	private RadioButton genderMaleRB;
	private RadioButton genderFemaleRB;
	private EditText phoneET;
	private EditText titleET;
	private Spinner depNoSpinner;
	private Spinner depNameSpinner;
	private EditText managerET;
	
	private ImageView photoIV;
	private ImageButton photoBtn;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_new_phonebook);
		
		
		
		inflater = LayoutInflater.from(this);
		
		TextView tv = (TextView)findViewById(R.id.textview_newphonebook_title);
		tv.setText("New PhoneBook Item");
		
		photoIV = (ImageView) findViewById(R.id.new_phonebook_photo);
		
		
		if(newPhoto == null){
			photoIV.setImageResource(R.drawable.photo);
		}else{
			photoIV.setImageBitmap(newPhoto);
		}
		
		
		initialsET = (EditText) findViewById(R.id.new_phonebook_initials);
		
		nameET = (EditText) findViewById(R.id.new_phonebook_name);
		
		localnameET = (EditText) findViewById(R.id.new_phonebook_localname);

		genderMaleRB = (RadioButton)findViewById(R.id.new_phonebook_gender_male);
		genderFemaleRB = (RadioButton)findViewById(R.id.new_phonebook_gender_female);
		
		genderMaleRB.setChecked(true);
		genderFemaleRB.setChecked(false);
		
		phoneET = (EditText) findViewById(R.id.new_phonebook_phone);
		
		titleET = (EditText) findViewById(R.id.new_phonebook_title);
		
		List <DepartmentInfo> departments = DataManager.getInstance().getAllDepartments();
		List<String> depNoList = new ArrayList<String>();
		List<String> depNameList = new ArrayList<String>();
		depNoList.add("Please select ...");
		depNameList.add("Please select ...");
		
		for(DepartmentInfo di: departments){
			depNoList.add(di.getDepartmentNO());
			depNameList.add(di.getDepartmentName());
		}
		
		depNoSpinner = (Spinner) findViewById(R.id.new_phonebook_departmentno);
		
		ArrayAdapter<String> depNoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depNoList);
		depNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		depNoSpinner.setAdapter(depNoAdapter);
		
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
		
		
		depNameSpinner = (Spinner) findViewById(R.id.new_phonebook_department);
		
		ArrayAdapter<String> depNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depNameList);
		depNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		depNameSpinner.setAdapter(depNameAdapter);
		
		
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
		
		managerET = (EditText) findViewById(R.id.new_phonebook_manager);
		
		photoBtn = (ImageButton)findViewById(R.id.new_phonebook_photo_btn);
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
		
		
		ImageButton okBtn =(ImageButton)findViewById(R.id.imagebtn_newphonebook_confirm);
		okBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String initials = initialsET.getText().toString().toUpperCase();
				if(initials.equals("")){
					Toast.makeText(NewPhoneBookActivity.this, "Please input initials", Toast.LENGTH_SHORT).show();
					return;
				}
				
				String name = nameET.getText().toString();
				if(name.equals("")){
					Toast.makeText(NewPhoneBookActivity.this, "Please input name", Toast.LENGTH_SHORT).show();
					return;
				}
				
				String localName = localnameET.getText().toString();
				if(localName.equals("")){
					Toast.makeText(NewPhoneBookActivity.this, "Please input local name", Toast.LENGTH_SHORT).show();
					return;
				}
				
				String phone = phoneET.getText().toString();
				if(phone.equals("")){
					Toast.makeText(NewPhoneBookActivity.this, "Please input phone number", Toast.LENGTH_SHORT).show();
					return;
				}
				
				String title = titleET.getText().toString();
				
				if(depNoSpinner.getSelectedItemPosition() == 0){
					Toast.makeText(NewPhoneBookActivity.this, "Please select department number", Toast.LENGTH_SHORT).show();
					return;
				}
				String depNo = (String)depNoSpinner.getSelectedItem();
				
				if(depNameSpinner.getSelectedItemPosition() == 0){
					Toast.makeText(NewPhoneBookActivity.this, "Please select department name", Toast.LENGTH_SHORT).show();
					return;
				}
				String depName = (String)depNameSpinner.getSelectedItem();
				
				String manager = managerET.getText().toString();
				
				newPbItem = new PhoneBookItem();
				
				newPbItem.setInitials(initials);
				newPbItem.setName(name);
				newPbItem.setLocalName(localName);
				newPbItem.setGender(genderMaleRB.isChecked()? GENDER.MALE: GENDER.FEMALE);
				newPbItem.setPhone(phone);
				newPbItem.setTitle(title);
				newPbItem.setDepartmentNo(depNo);
				newPbItem.setDepartment(depName);
				newPbItem.setManager(manager);
				newPbItem.setFlag(FLAG.NEW);
				
				
				Dialog dialog = new AlertDialog.Builder(NewPhoneBookActivity.this)
		        	.setIcon(R.drawable.ic_launcher)
		        	.setTitle("Do you want to save the phonebook info?")
		        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(!DataManager.getInstance().newPhoneBook(newPbItem)){
								Toast.makeText(NewPhoneBookActivity.this, "Create phonebook info failed!", Toast.LENGTH_SHORT);
								NewPhoneBookActivity.this.finish();
								return;
							}
							
							if(newPhoto!=null && !DataManager.getInstance().newPhoneBookPhoto(newPbItem.getInitials(), newPhoto)){
								Toast.makeText(NewPhoneBookActivity.this, "Create phonebook photo failed!", Toast.LENGTH_SHORT);
								NewPhoneBookActivity.this.finish();
								return;
							}
							
							setResult(MainActivity.ACTIVITY_RESULT_NEWPHONEBOOK_OK);
							NewPhoneBookActivity.this.finish();
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
					
				
			}
			
		});
		
		ImageButton cancelBtn =(ImageButton)findViewById(R.id.imagebtn_newphonebook_cancel);
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
		        		Resources r = getResources();
		        		newPhoto = BitmapUtil.getImageThumbnail(bit, (int)r.getDimension(R.dimen.photo_width), (int)r.getDimension(R.dimen.photo_height));
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
		        	Toast.makeText(this, "uri:" + imageUri.toString(), Toast.LENGTH_LONG).show();
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
			        	Toast.makeText(this, "Path:" + path, Toast.LENGTH_LONG).show();
			        	Resources r = getResources();
		        		try{
		        			newPhoto = BitmapUtil.getImageThumbnail(path, (int)r.getDimension(R.dimen.photo_width), (int)r.getDimension(R.dimen.photo_height));
		        		}catch(Exception e){
		        			e.printStackTrace();
		        		}
		        	}
		        	if(newPhoto!=null && !newPhoto.isRecycled()){
		        		photoIV.setImageBitmap(newPhoto);
		        	}
		        }
		        break;
		    }
	}
	
	@Override
	public void onBackPressed() {
		clickCancelButton();
	}
	
	private void clickCancelButton(){
		Dialog dialog = new AlertDialog.Builder(NewPhoneBookActivity.this)
    	.setIcon(R.drawable.ic_launcher)
    	.setTitle("Do you want to cancel the created info?")
    	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();		
				NewPhoneBookActivity.this.finish();
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

	private String getPathFromUri(Uri uri){
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = this.managedQuery(uri, proj, null, null, null);
		int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(columnIndex);
		
	}
	
	private void pickupPhoto() {
		CharSequence[] items = {"Camera", "Photo Album"};
		new AlertDialog.Builder(NewPhoneBookActivity.this)
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

}