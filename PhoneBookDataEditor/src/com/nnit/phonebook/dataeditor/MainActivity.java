package com.nnit.phonebook.dataeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;







import com.nnit.phonebook.dataeditor.R;
import com.nnit.phonebook.dataeditor.data.DataManager;
import com.nnit.phonebook.dataeditor.data.DepartmentInfo;
import com.nnit.phonebook.dataeditor.data.Filter;
import com.nnit.phonebook.dataeditor.data.MapItem;
import com.nnit.phonebook.dataeditor.data.PhoneBookItem;
import com.nnit.phonebook.dataeditor.data.PhoneBookItem.PhoneBookField;
import com.nnit.phonebook.dataeditor.ui.MapListAdapter;
import com.nnit.phonebook.dataeditor.ui.PhoneBookListAdapter;
import com.nnit.phonebook.dataeditor.ui.OpenFileDialog;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String SELECTED_PBITEM = "com.nnit.phonebook.dataeditor.SELECTED_PBITEM";
	public static final int VIEW_STATE_PHONEBOOKLIST = 1;
	public static final int VIEW_STATE_MAPLIST = 2;
	
	public static final int ACTIVITY_REQUEST_CODE_EDITPHONEBOOK_ACTIVITY = 101;
	public static final int ACTIVITY_REQUEST_CODE_NEWPHONEBOOK_ACTIVITY = 102;
	public static final int ACTIVITY_REQUEST_CODE_NEWMAP_ACTIVITY = 103;
	public static final int ACTIVITY_REQUEST_CODE_EDITSEATPOSITION_ACTIVITY = 104;
	
	public static final int ACTIVITY_RESULT_EDITPHONEBOOK_OK = 201;
	public static final int ACTIVITY_RESULT_NEWPHONEBOOK_OK = 202;
	public static final int ACTIVITY_RESULT_NEWMAP_OK = 203;
	public static final int ACTIVITY_RESULT_EDITSEATINFO_OK = 204;

	
	
	private LayoutInflater inflater;
	
	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private ImageView imageView;
	private ImageView[] imageViews;
	private ViewGroup main;
	private ViewGroup group;

	private Filter[] filters;
	
	private ListView pbListView;
	private PhoneBookListAdapter pbListAdapter;
	
	private ListView mapListView;
	private MapListAdapter mapListAdapter;
	
	private RelativeLayout titlebarPhonebookList;
	private RelativeLayout titlebarSelectPhonebookList;
	private RelativeLayout titlebarMapList;
	private RelativeLayout titlebarSelectMapList;
	
	
	private int viewState = VIEW_STATE_PHONEBOOKLIST;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		inflater = getLayoutInflater();

		pageViews = new ArrayList<View>();

		View phoneBookPageView = inflater.inflate(
				R.layout.pageview_phonebooklist, null);

		View mapPageView = inflater.inflate(R.layout.pageview_maplist, null);
		
		pageViews.add(phoneBookPageView);
		pageViews.add(mapPageView);
		
		imageViews = new ImageView[pageViews.size()];
		main = (ViewGroup) inflater.inflate(R.layout.activity_main, null);

		group = (ViewGroup) main.findViewById(R.id.viewGroup);
		viewPager = (ViewPager) main.findViewById(R.id.guidePages);

		for (int i = 0; i < pageViews.size(); i++) {
			imageView = new ImageView(this);
			imageView.setLayoutParams(new LayoutParams(20, 20));
			imageView.setPadding(200, 0, 200, 0);
			imageViews[i] = imageView;

			if (i == 0) {
				imageViews[i]
						.setBackgroundResource(R.drawable.page_indicator_focused_1);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.page_indicator);
			}
			group.addView(imageViews[i]);
		}

		setContentView(main);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.titlebar_main);

		if (!DataManager.getInstance().existData()) {
			showDialog(R.layout.dialog_datapackageselect);
		} else {
			if (!DataManager.getInstance().loadDataPackage()) {
				Toast.makeText(MainActivity.this, "Invalid Data Package!",
						Toast.LENGTH_SHORT).show();
			}
		}

		viewPager.setAdapter(new GuidePageAdapter());
		viewPager.setOnPageChangeListener(new GuidePageChangeListener());
		
		titlebarPhonebookList = (RelativeLayout) findViewById(R.id.titlebar_phonebooklist);
		titlebarSelectPhonebookList = (RelativeLayout) findViewById(R.id.titlebar_selectphonebooklist);
		titlebarMapList = (RelativeLayout) findViewById(R.id.titlebar_maplist);
		titlebarSelectMapList = (RelativeLayout) findViewById(R.id.titlebar_selectmaplist);

		updateViewState(VIEW_STATE_PHONEBOOKLIST, false);

		
		// title bar phone book list
		TextView tvTitle = (TextView) findViewById(R.id.textview_phonebooklist_title);

		ImageButton newBtn = (ImageButton) findViewById(R.id.imagebtn_phonebooklist_new);
		newBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setAction("com.nnit.phonebook.dataeditor.NewPhoneBookActivity");
				startActivityForResult(intent,
						ACTIVITY_REQUEST_CODE_NEWPHONEBOOK_ACTIVITY);	
			}

		});

		ImageButton searchBtn = (ImageButton) findViewById(R.id.imagebtn_phonebooklist_search);
		searchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showSearchByDialog();
			}

		});

		// title bar for select phone book list
		TextView tvEditListTitle = (TextView) findViewById(R.id.textview_selectphonebooklist_title);
		tvEditListTitle.setText("");

		ImageButton selectAllBtn = (ImageButton) findViewById(R.id.imagebtn_selectphonebooklist_selectall);
		selectAllBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				pbListAdapter.selectedAll();
				pbListAdapter.notifyDataSetChanged();
			}

		});

		ImageButton unSelectAllBtn = (ImageButton) findViewById(R.id.imagebtn_selectphonebooklist_unselectall);
		unSelectAllBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				pbListAdapter.unSelectedAll();
				pbListAdapter.notifyDataSetChanged();
			}

		});

		ImageButton deleteBtn = (ImageButton) findViewById(R.id.imagebtn_selectphonebooklist_delete);
		deleteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Dialog dialog = new AlertDialog.Builder(MainActivity.this)
						.setIcon(R.drawable.ic_launcher)
						.setTitle("Do you want to delete selected data?")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Set<String> initials = pbListAdapter
												.getDeletedInitals();
										DataManager.getInstance()
												.setPhoneBookDataDeleted(
														initials);
										updatePhoneBookList(DataManager.getInstance().getPhoneBookItemList(filters),true);
										dialog.dismiss();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();
			}

		});

		ImageButton cancelBtn = (ImageButton) findViewById(R.id.imagebtn_selectphonebooklist_cancel);
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				canclePhoneBookListEditMode();
			}

		});

		// phonebook list
		
		pbListView = (ListView) phoneBookPageView
				.findViewById(R.id.phonebook_list);

		updatePhoneBookList(DataManager.getInstance().getPhoneBookItemList(filters), false);

		pbListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				enterPhoneBookListEditMode(position);
				return true;
			}

		});

		pbListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (!pbListAdapter.isEditMode()) {
					Intent intent = new Intent();
					intent.putExtra(SELECTED_PBITEM,
							(PhoneBookItem) pbListAdapter.getItem(position));
					intent.setAction("com.nnit.phonebook.dataeditor.EditPhoneBookActivity");
					startActivityForResult(intent,
							ACTIVITY_REQUEST_CODE_EDITPHONEBOOK_ACTIVITY);
				} else {
					// pbListAdapter.setCheckedState(position,
					// !pbListAdapter.getCheckedState(position));
					// pbListAdapter.notifyDataSetChanged();
					// Toast.makeText(MainActivity.this, "aaaa",
					// Toast.LENGTH_SHORT).show();
				}

			}

		});
		
		//map list
		mapListView = (ListView) mapPageView.findViewById(R.id.map_list);
		updateMapList(DataManager.getInstance().getMapItemList(), false);
		mapListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				enterMapListEditMode(position);
				return true;
			}

		});
		// title bar map list
		TextView tvMapTitle = (TextView) findViewById(R.id.textview_maplist_title);
		tvMapTitle.setText("Seat Map List");

		ImageButton newMapBtn = (ImageButton) findViewById(R.id.imagebtn_maplist_new);
		newMapBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setAction("com.nnit.phonebook.dataeditor.NewMapActivity");
				startActivityForResult(intent,
						ACTIVITY_REQUEST_CODE_NEWMAP_ACTIVITY);	
			}

		});
		// title bar for select map list
		TextView tvEditListTitleMap = (TextView) findViewById(R.id.textview_selectmaplist_title);
		tvEditListTitleMap.setText("");

		ImageButton selectAllMapsBtn = (ImageButton) findViewById(R.id.imagebtn_selectall_map);
		selectAllMapsBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mapListAdapter.selectedAll();
				mapListAdapter.notifyDataSetChanged();
			}

		});

		ImageButton unSelectAllMapsBtn = (ImageButton) findViewById(R.id.imagebtn_unselectall_map);
		unSelectAllMapsBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mapListAdapter.unSelectedAll();
				mapListAdapter.notifyDataSetChanged();
			}

		});

		ImageButton deleteMapBtn = (ImageButton) findViewById(R.id.imagebtn_delete_map);
		deleteMapBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Dialog dialog = new AlertDialog.Builder(MainActivity.this)
						.setIcon(R.drawable.ic_launcher)
						.setTitle("Do you want to delete selected maps?")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Set<Integer> floors =  mapListAdapter.getDeletedMaps();
										DataManager.getInstance()
												.setMapDataDeleted(
														floors);
										updateMapList(DataManager.getInstance().getMapItemList(),true);
										dialog.dismiss();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();
			}

		});

		ImageButton cancelMapBtn = (ImageButton) findViewById(R.id.imagebtn_cancel_map);
		cancelMapBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				cancleMapListEditMode();
			}

		});

	}
	
	private void enterPhoneBookListEditMode(int selectedPosition) {
		pbListAdapter.setEditMode(true);
		if(selectedPosition != -1){
			pbListAdapter.setCheckedState(selectedPosition, true);
		}
		pbListAdapter.notifyDataSetChanged();
		updateViewState(VIEW_STATE_PHONEBOOKLIST, true);
	}
	
	private void canclePhoneBookListEditMode() {
		pbListAdapter.unSelectedAll();
		pbListAdapter.setEditMode(false);
		pbListAdapter.notifyDataSetChanged();
		updateViewState(VIEW_STATE_PHONEBOOKLIST, false);
	}
	
	private void enterMapListEditMode(int selectedPosition) {
		mapListAdapter.setEditMode(true);
		if(selectedPosition != -1){
			mapListAdapter.setCheckedState(selectedPosition, true);
		}
		mapListAdapter.notifyDataSetChanged();
		updateViewState(VIEW_STATE_MAPLIST, true);
	}
	
	private void cancleMapListEditMode() {
		mapListAdapter.unSelectedAll();
		mapListAdapter.setEditMode(false);
		mapListAdapter.notifyDataSetChanged();
		updateViewState(VIEW_STATE_MAPLIST, false);
	}

	@Override
	public void onBackPressed() {
		if(viewState == VIEW_STATE_PHONEBOOKLIST && pbListAdapter.isEditMode()){
			canclePhoneBookListEditMode();
		}else if(viewState == VIEW_STATE_MAPLIST && mapListAdapter.isEditMode()){
			cancleMapListEditMode();
		}else{
		
			if (DataManager.getInstance().isDataModified()) {
				Dialog dialog = new AlertDialog.Builder(MainActivity.this)
						.setIcon(R.drawable.ic_launcher)
						.setTitle(
								"Data has been modified,do you want to save the modification?")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										if(!DataManager.getInstance().saveModification()){
											Toast.makeText(MainActivity.this, "Save modification failed", Toast.LENGTH_SHORT).show();
										}
										dialog.dismiss();
										MainActivity.this.finish();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
	
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										MainActivity.this.finish();
									}
								}).show();
	
			} else {
				super.onBackPressed();
			}
		}
	}

	private void updatePhoneBookList(List<PhoneBookItem> pbItemList, boolean bEditMode) {
		if (pbListView == null) {
			pbListView = (ListView) findViewById(R.id.phonebook_list);
		}
		pbListAdapter = new PhoneBookListAdapter(this, pbItemList, bEditMode);
		pbListView.setAdapter(pbListAdapter);
	}

	private void updateMapList(List<MapItem> mapList, boolean bEditMode) {
		if (mapListView == null) {
			mapListView = (ListView) findViewById(R.id.map_list);
		}
		mapListAdapter = new MapListAdapter(this, mapListView, mapList, bEditMode);
		mapListView.setAdapter(mapListAdapter);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case ACTIVITY_REQUEST_CODE_EDITPHONEBOOK_ACTIVITY:
				if (resultCode == ACTIVITY_RESULT_EDITPHONEBOOK_OK) {
					updatePhoneBookList(DataManager.getInstance().getPhoneBookItemList(filters), false);
				}
				break;
			case ACTIVITY_REQUEST_CODE_NEWPHONEBOOK_ACTIVITY:
				if (resultCode == ACTIVITY_RESULT_NEWPHONEBOOK_OK) {
					updatePhoneBookList(DataManager.getInstance().getPhoneBookItemList(filters), false);
				}
				break;
			case ACTIVITY_REQUEST_CODE_NEWMAP_ACTIVITY:
				if (resultCode == ACTIVITY_RESULT_NEWMAP_OK) {
					updateMapList(DataManager.getInstance().getMapItemList(), false);
				}
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_open:
			showDialog(R.layout.dialog_datapackageselect);
			break;
		case R.id.menuitem_save:
			Dialog dialog = new AlertDialog.Builder(MainActivity.this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle("Do you want to save the data package to disk?")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (DataManager.getInstance().isDataModified()) {
										if(!DataManager.getInstance().saveModification()){
											Toast.makeText(MainActivity.this, "Save modification failed", Toast.LENGTH_SHORT).show();
											return;
										}
									}
									showDialog(R.layout.dialog_savefileselect);

								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
			break;
		case R.id.menuitem_about:
			showAboutDialog();
			break;
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		if (id == R.layout.dialog_datapackageselect) {

			Map<String, Integer> images = new HashMap<String, Integer>();
			images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
			images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
			images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
			images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_file);
			images.put("zip", R.drawable.filedialog_zipfile);

			Dialog dialog = OpenFileDialog.createDialog(id, this,
					"Select Data Package File",
					new OpenFileDialog.CallbackBundle() {
						@Override
						public void callback(Bundle bundle) {

							String fullFileName = bundle.getString("path");
							if (!DataManager.getInstance().loadDataPackage(
									fullFileName)) {
								Toast.makeText(MainActivity.this,
										"Invalid Data Package!",
										Toast.LENGTH_SHORT);
							} else {
								filters = null;
								updatePhoneBookList(DataManager.getInstance().getPhoneBookItemList(filters), false);
								updateMapList(DataManager.getInstance().getMapItemList(), false);
							}

						}
					}, ".zip", images);

			return dialog;
		} else if (id == R.layout.dialog_savefileselect) {
			Map<String, Integer> images = new HashMap<String, Integer>();
			images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
			images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
			images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
			images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_file);
			images.put("zip", R.drawable.filedialog_zipfile);

			Dialog dialog = OpenFileDialog.createDialog(id, this,
					"Select Save File", new OpenFileDialog.CallbackBundle() {
						@Override
						public void callback(Bundle bundle) {

							String fullFileName = bundle.getString("path");

							if (DataManager.getInstance().savePackageToDisk(
									fullFileName)) {
								Toast.makeText(MainActivity.this,
										"Save Data Package to " + fullFileName,
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(MainActivity.this,
										"Save Data Package failed",
										Toast.LENGTH_SHORT).show();
							}

						}
					}, ".zip", images);

			return dialog;

		}
		return null;
	}
	
	private void showSearchByDialog() {
    	final View dialogView = inflater.inflate(R.layout.dialog_searchby, null);
    	
    	List<DepartmentInfo> departments = DataManager.getInstance().getAllDepartments();
    	
    	List<String> depNameList = new ArrayList<String>();
		depNameList.add(0, "Please Select ...");
		for(DepartmentInfo di: departments){
			depNameList.add(di.getDepartmentName());
		}

		Spinner depNameSpinner = (Spinner)dialogView.findViewById(R.id.searchby_depName);
    	
		ArrayAdapter<String> depNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depNameList);
		depNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		depNameSpinner.setAdapter(depNameAdapter);
		
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle("Please input search criteria:")
        	.setView(dialogView)
        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText initials_et = (EditText) dialogView.findViewById(R.id.searchby_initials);
					String initials = initials_et.getText().toString();
					
					EditText name_et = (EditText) dialogView.findViewById(R.id.searchby_name);
					String name = name_et.getText().toString();
					
					EditText phone_et = (EditText) dialogView.findViewById(R.id.searchby_phone);
					String phone = phone_et.getText().toString();
					
					Spinner depName_spinner = (Spinner) dialogView.findViewById(R.id.searchby_depName);
					String depName = depName_spinner.getSelectedItemPosition() == 0? null:(String)depName_spinner.getSelectedItem();
					
					EditText manager_et = (EditText) dialogView.findViewById(R.id.searchby_manager);
					String manager = manager_et.getText().toString();
					
					filters = new Filter[] {
							new Filter(PhoneBookField.INITIALS, initials),
							new Filter(PhoneBookField.NAME, name),
							new Filter(PhoneBookField.PHONE, phone),
							new Filter(PhoneBookField.DEPARTMENT, depName),
							new Filter(PhoneBookField.MANAGER, manager) };
					updatePhoneBookList(DataManager.getInstance().getPhoneBookItemList(filters), false);
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
    
	private void showAboutDialog() {
    	final View dialogView = inflater.inflate(R.layout.dialog_about, null);
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle("About")
        	.setView(dialogView)
        	.setNegativeButton("Close", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
				}
			})
        	.show();
    }
	
	private void updateViewState(int viewState, boolean bSelectMode){
		this.viewState = viewState;
		if(viewState == VIEW_STATE_PHONEBOOKLIST){
			if(bSelectMode){
				titlebarPhonebookList.setVisibility(View.INVISIBLE);
				titlebarSelectPhonebookList.setVisibility(View.VISIBLE);
				titlebarMapList.setVisibility(View.INVISIBLE);
				titlebarSelectMapList.setVisibility(View.INVISIBLE);
			}else{
				titlebarPhonebookList.setVisibility(View.VISIBLE);
				titlebarSelectPhonebookList.setVisibility(View.INVISIBLE);
				titlebarMapList.setVisibility(View.INVISIBLE);
				titlebarSelectMapList.setVisibility(View.INVISIBLE);
			}
		}else if(viewState == VIEW_STATE_MAPLIST){
			if(bSelectMode){
				titlebarPhonebookList.setVisibility(View.INVISIBLE);
				titlebarSelectPhonebookList.setVisibility(View.INVISIBLE);
				titlebarMapList.setVisibility(View.INVISIBLE);
				titlebarSelectMapList.setVisibility(View.VISIBLE);
			}else{
				titlebarPhonebookList.setVisibility(View.INVISIBLE);
				titlebarSelectPhonebookList.setVisibility(View.INVISIBLE);
				titlebarMapList.setVisibility(View.VISIBLE);
				titlebarSelectMapList.setVisibility(View.INVISIBLE);
			}
		}
	}

	class GuidePageAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			// TODO Auto-generated method stub
			((ViewPager) arg0).removeView(pageViews.get(arg1));
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			// TODO Auto-generated method stub
			((ViewPager) arg0).addView(pageViews.get(arg1));
			return pageViews.get(arg1);
		}
	}

	class GuidePageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int index) {

			for (int i = 0; i < imageViews.length; i++) {
				if(index == 0){
					imageViews[index].setBackgroundResource(R.drawable.page_indicator_focused_1);
				}else{
					imageViews[index].setBackgroundResource(R.drawable.page_indicator_focused_2);
				}
				if (index != i) {
					imageViews[i]
							.setBackgroundResource(R.drawable.page_indicator);
				}
			}
			if(index == 0){
				updateViewState(VIEW_STATE_PHONEBOOKLIST, pbListAdapter.isEditMode());
			}else{
				updateViewState(VIEW_STATE_MAPLIST, mapListAdapter.isEditMode());
			}
		}

	}

}
