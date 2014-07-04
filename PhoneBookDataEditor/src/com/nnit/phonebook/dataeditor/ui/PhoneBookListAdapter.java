package com.nnit.phonebook.dataeditor.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nnit.phonebook.dataeditor.R;
import com.nnit.phonebook.dataeditor.data.DataManager;
import com.nnit.phonebook.dataeditor.data.PhoneBookItem;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PhoneBookListAdapter extends BaseAdapter {
	private List<PhoneBookItem> pbItems = null;
	private LayoutInflater mInflater = null;
	private Context context = null;
	
	private boolean bEditMode = false;
	private HashMap<Integer, Boolean> isCheckedMap = null;

	public PhoneBookListAdapter(Context c, List<PhoneBookItem> items, boolean bEditMode) {
		this.context = c;
		this.pbItems = items;
		this.bEditMode = bEditMode;
		isCheckedMap = new HashMap<Integer, Boolean>();
		
		mInflater = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for(int i=0; i<pbItems.size(); i++){
			isCheckedMap.put(i, false);
		}
		
	}

	public void unSelectedAll(){
		for(int i=0; i<pbItems.size(); i++){
			isCheckedMap.put(i, false);
		}
	}
	
	public void selectedAll(){
		for(int i=0; i<pbItems.size(); i++){
			isCheckedMap.put(i, true);
		}
	}
	
	public void setCheckedState(int position, boolean checked){
		isCheckedMap.put(position, checked);
	}
	
	public boolean getCheckedState(int position) {
		return isCheckedMap.get(position);
	}
	
	@Override
	public int getCount() {
		return pbItems == null ? 0 : pbItems.size();
	}

	@Override
	public Object getItem(int position) {
		return pbItems == null ? null : pbItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PhoneBookItem pb = pbItems.get(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_phonebook,parent, false);
			
		}
		
		TextView tv1 = (TextView) convertView.findViewById(R.id.tv_list_text);
		tv1.setText(pb == null ? null : pb.getInitials() + "(" + pb.getName() + ")");
		ImageView iv = (ImageView) convertView.findViewById(R.id.iv_list_photo);
		setPhoto(iv, pb);
		
		CheckBox cb = (CheckBox) convertView.findViewById(R.id.cb_list_checked);
		cb.setVisibility(bEditMode?CheckBox.VISIBLE: CheckBox.INVISIBLE);	
		cb.setChecked(isCheckedMap.get(position));
	
		cb.setOnClickListener(new CheckBoxOnClickListener(position, isCheckedMap));
		return convertView;
	}
	
	private void setPhoto(ImageView iv, PhoneBookItem pb){
		FileInputStream fis = null;
		Resources resources = context.getResources();
		String initials = (pb == null ? null: pb.getInitials());
		
		try{
			String photoFilename = DataManager.getInstance().getPhotoFilenameByInitials(initials);
			if(photoFilename == null){
				iv.setImageResource(R.drawable.photo);
			}
			File f = new File(photoFilename);
		
			if(f.exists() && f.isFile()){
				fis = new FileInputStream(f);
				Bitmap bitmap = BitmapFactory.decodeStream(fis);
				iv.setImageBitmap(bitmap);
			}else{
				iv.setImageResource(R.drawable.photo); 
			}			
			
		}catch(Exception exp){
			iv.setImageResource(R.drawable.photo); 
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void setEditMode(boolean b) {
		this.bEditMode = b;
	}

	public boolean isEditMode() {
		return bEditMode;
	}

	class CheckBoxOnClickListener implements OnClickListener{
		private int position;
		private HashMap<Integer, Boolean> checkedMap;
		
		public CheckBoxOnClickListener(int pos, HashMap<Integer, Boolean> checkedMap){
			this.position = pos;
			this.checkedMap = checkedMap;
		}
		@Override
		public void onClick(View v) {
			CheckBox cb = (CheckBox)v;
			checkedMap.put(position, cb.isChecked());
		}
		
	}

	public Set<String> getDeletedInitals() {
		Set<String> result = new HashSet<String>();
		
		for(int i = 0; i<pbItems.size(); i++){
			if(isCheckedMap.get(i)){
				result.add(pbItems.get(i).getInitials());
			}
		}
		return result;
	}

}
