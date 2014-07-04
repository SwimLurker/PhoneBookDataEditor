package com.nnit.phonebook.dataeditor.ui;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nnit.phonebook.dataeditor.R;
import com.nnit.phonebook.dataeditor.data.DataManager;
import com.nnit.phonebook.dataeditor.data.MapItem;
import com.nnit.phonebook.dataeditor.util.BitmapUtil;

public class MapListAdapter extends BaseAdapter {
	private List<MapItem> mapItems = null;
	private LayoutInflater mInflater = null;
	private Context context = null;
	private ListView listview = null;
	private ImageLoader imageLoader = null;
	
	private boolean bEditMode = false;
	private HashMap<Integer, Boolean> isCheckedMap = null;
	
	
	private HashSet<String> loadingBitmaps = null;
	
	
	
	ImageLoader.OnImageLoadListener imageLoadListener = new ImageLoader.OnImageLoadListener() {
		
		@Override
		public void onImageLoaded(Bitmap bitmap, Object[] parameters) {
			String bitmapFilename = (String) parameters[0];
			int position =(Integer)parameters[1];
			View view = listview.findViewWithTag(position);
			if(view != null){
				ImageView iv = (ImageView)view.findViewById(R.id.iv_list_map);
				if(bitmap!= null){
					iv.setImageBitmap(bitmap);
					DataManager.getInstance().updateMapThumbnailPhotoCache(bitmapFilename, bitmap);
					Log.i("OnImageLoadListener", "bitmap loaded:" + bitmapFilename);
				}
			}
		}
		
		@Override
		public void onError( Object[] parameters) {
			String bitmapFilename = (String) parameters[0];
			int position =(Integer)parameters[1];
			View view = listview.findViewWithTag(position);
			if(view != null){
				ImageView iv = (ImageView)view.findViewById(R.id.iv_list_map);
				iv.setImageResource(R.drawable.map);
				Log.i("OnImageLoadListener", "bitmap loaded error:" + bitmapFilename);
			}
		}
	};
	
	
	public MapListAdapter(Context c, ListView listview, List<MapItem> items, boolean bEditMode) {
		this.context = c;
		this.listview = listview;
		this.mapItems = items;
		this.bEditMode = bEditMode;
		isCheckedMap = new HashMap<Integer, Boolean>();
		
		mInflater = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for(int i=0; i<mapItems.size(); i++){
			isCheckedMap.put(i, false);
		}
		Resources r = context.getResources();
		imageLoader = new ImageLoader(
				new ThumbnailImageGetter((int)r.getDimension(R.dimen.map_width_thumbnail_small), 
						(int)r.getDimension(R.dimen.map_height_thumbnail_small)));
		loadingBitmaps = new HashSet<String>();
	}

	public void unSelectedAll(){
		for(int i=0; i<mapItems.size(); i++){
			isCheckedMap.put(i, false);
		}
	}
	
	public void selectedAll(){
		for(int i=0; i<mapItems.size(); i++){
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
		return mapItems == null ? 0 : mapItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mapItems == null ? null : mapItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MapItem map = mapItems.get(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_map,parent, false);
			
		}
		convertView.setTag(position);
		
		TextView tv1 = (TextView) convertView.findViewById(R.id.tv_list_text_map);
		tv1.setText(map == null ? null : map.getMapFilename() + "(Floor " + map.getFloor() +")" );
		ImageView iv = (ImageView) convertView.findViewById(R.id.iv_list_map);
		iv.setImageResource(R.drawable.map);
		
		String mapFilename = DataManager.getInstance().getMapFilenameByFloor(map.getFloor());
		if(loadingBitmaps.contains(mapFilename)){
			Bitmap bitmap = DataManager.getInstance().getMapThumbnailPhotoFromCache(mapFilename);
			if(bitmap!=null){
				iv.setImageBitmap(bitmap);
			}
		}else{
			loadingBitmaps.add(mapFilename);
			imageLoader.loadImage(new Object[]{mapFilename, position}, imageLoadListener);
		}
		CheckBox cb = (CheckBox) convertView.findViewById(R.id.cb_list_checked_map);
		cb.setVisibility(bEditMode?CheckBox.VISIBLE: CheckBox.INVISIBLE);	
		cb.setChecked(isCheckedMap.get(position));
	
		cb.setOnClickListener(new CheckBoxOnClickListener(position, isCheckedMap));
		
		return convertView;
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

	public Set<Integer> getDeletedMaps() {
		Set<Integer> result = new HashSet<Integer>();
		
		for(int i = 0; i<mapItems.size(); i++){
			if(isCheckedMap.get(i)){
				result.add(mapItems.get(i).getFloor());
			}
		}
		return result;
	}
}
