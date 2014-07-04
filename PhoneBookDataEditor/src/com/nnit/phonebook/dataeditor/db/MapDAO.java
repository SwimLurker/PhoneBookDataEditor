package com.nnit.phonebook.dataeditor.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;





import com.nnit.phonebook.dataeditor.data.FLAG;
import com.nnit.phonebook.dataeditor.data.MapItem;


public class MapDAO {
	private DBManager dbManager;
	
	public MapDAO(){
		dbManager = new DBManager();
	}
	
	public List<MapItem> getAllMaps(){
		List<MapItem> result = new ArrayList<MapItem>();
		Cursor cursor = null;
		try{
			dbManager.openDatabase();
			cursor = dbManager.query("select Floor, Filename from Map", null);
				
			while(cursor.moveToNext()){
				int floorNo = cursor.getInt(0);
				String mapFilename = cursor.getString(1);
				
				MapItem mi = new MapItem();
				mi.setFlag(FLAG.NOTMODIFIED);
				mi.setFloor(floorNo);
				mi.setMapFilename(mapFilename);
				result.add(mi);
			}
			
			return result;
		}catch(SQLException e){
			Log.e("MapDAO", "Query map info error");
			e.printStackTrace();
			return null;
		}finally{
			if(cursor != null){
				cursor.close();
				cursor = null;
			}
			dbManager.closeDatabase();
		}
	}
	
	public MapItem getMapByFloorNo(int floor){
		MapItem result = null;
		Cursor cursor = null;
		try{
			dbManager.openDatabase();
			cursor = dbManager.query("select Filename from Map where Floor = ?", new String[]{Integer.toString(floor)});
				
			if(cursor.moveToNext()){
				String mapFilename = cursor.getString(0);
				
				result = new MapItem();
				result.setFlag(FLAG.NOTMODIFIED);
				result.setFloor(floor);
				result.setMapFilename(mapFilename);
			}
			
			return result;
		}catch(SQLException e){
			Log.e("MapDAO", "Query map info by floor no error");
			e.printStackTrace();
			return null;
		}finally{
			if(cursor != null){
				cursor.close();
				cursor = null;
			}
			dbManager.closeDatabase();
		}
	}

	public void deleteMapByFloorNo(int floor) {
		try{
			dbManager.openDatabase();
			dbManager.delete("Map","Floor = ?" , new String[]{Integer.toString(floor)});
		}catch(SQLException e){
			Log.e("MapDAO", "Delete map info error");
			e.printStackTrace();
		}finally{
			dbManager.closeDatabase();
		}
		
	}

	public void insertOrUpdateMap(MapItem mi) {
		String siteID = "1";
		String filename = mi.getMapFilename();
		File mapF = new File(mi.getMapFilename());
		String name = mapF.getName();
		name = name.substring(0, name.lastIndexOf('.')-1);
		int floor = mi.getFloor();
		
		Cursor cursor = null;
		boolean bInsert = false;
		try{
			
			dbManager.openDatabase();
			cursor = dbManager.query("select count(*) from Map where Floor = ?", new String[]{Integer.toString(floor)});
			
			if(cursor.moveToNext()){
				int count = cursor.getInt(0);
				if(count ==0){
					bInsert = true;
				}
			}
			
			if(bInsert){
				dbManager.insert("Map", new String[]{"SiteID", "Name", "Filename", "Floor"}, new String[]{siteID, name, filename, Integer.toString(floor)});
			}else{
				dbManager.update("Map", new String[]{"Name", "Filename"}, new String[]{name, filename}, "Floor = ?", new String[]{Integer.toString(floor)});
			}
			
			
		}catch(SQLException e){
			Log.e("MapDAO", "Insert/Update map info error");
			e.printStackTrace();
		}finally{
			if(cursor != null){
				cursor.close();
				cursor = null;
			}
			dbManager.closeDatabase();
		}
		
		
		
	}
	
}
