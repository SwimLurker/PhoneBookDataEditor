package com.nnit.phonebook.dataeditor.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.nnit.phonebook.dataeditor.data.FLAG;
import com.nnit.phonebook.dataeditor.data.SeatInfo;

public class SeatDAO {
	private DBManager dbManager;
	
	public SeatDAO(){
		dbManager = new DBManager();
	}
	public List<SeatInfo> getAllSeats(){
		List<SeatInfo> result = new ArrayList<SeatInfo>();
		Cursor cursor = null;
		try{
			dbManager.openDatabase();
			cursor = dbManager.query("select Seat.Initials, Seat.X, Seat.Y, Seat.Width, Seat.Height, Seat.Direction, Map.Floor from Seat, Map where Seat.MapID = Map.ID and Seat.Status='OK'", null);
				
			while(cursor.moveToNext()){
				String initials = cursor.getString(0);
				int x = cursor.getInt(1);
				int y = cursor.getInt(2);
				int width = cursor.getInt(3);
				int height = cursor.getInt(4);
				int direction = cursor.getInt(5);
				int floorNo = cursor.getInt(6);
				
				SeatInfo si = new SeatInfo();
				si.setFlag(FLAG.NOTMODIFIED);
				si.setInitials(initials);
				si.setX(x);
				si.setY(y);
				si.setWidth(width);
				si.setHeight(height);
				si.setDirection(direction);
				si.setFloorNo(floorNo);
				
				result.add(si);
			}
			
			return result;
		}catch(SQLException e){
			Log.e("SeatDAO", "Query seat info error");
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
	
	public void deleteSeatByInitials(String initials) {
		try{
			dbManager.openDatabase();
			dbManager.delete("Seat","Initials = ?" , new String[]{initials});
		}catch(SQLException e){
			Log.e("SeatDAO", "Delete seat info error");
			e.printStackTrace();
		}finally{
			dbManager.closeDatabase();
		}
			
	}
	public void insertOrUpdateSeatInfo(SeatInfo si) {
		
		int floor = si.getFloorNo();
		int mapID= -1;
		String initials = si.getInitials();
		int x = si.getX();
		int y = si.getY();
		int width = si.getWidth();
		int height = si.getHeight();
		int direction = si.getDirection();
		String status = "OK";
		
		Cursor cursor = null;
		boolean bInsert = false;
		try{
			
			dbManager.openDatabase();
			cursor = dbManager.query("select ID from Map where Floor = ?", new String[]{Integer.toString(floor)});
			if(cursor.moveToNext()){
				mapID = cursor.getInt(0);
			}else{
				Log.e("SeatDAO", "Map is not exist");
				return;
			}
			
			cursor.close();
			cursor = null;
			
			cursor = dbManager.query("select count(*) from Seat where Initials = ?", 
					new String[]{initials});
			
			if(cursor.moveToNext()){
				int count = cursor.getInt(0);
				if(count ==0){
					bInsert = true;
				}
			}
			
			if(bInsert){
				dbManager.insert("Seat", new String[]{"MapID", "Initials", "X", "Y", "Width", "Height", "Direction", "Status"}, 
						new String[]{Integer.toString(mapID), initials, Integer.toString(x), Integer.toString(y), Integer.toString(width)
						, Integer.toString(height), Integer.toString(direction), status});
			}else{
				dbManager.update("Seat", new String[]{"MapID", "X", "Y", "Width", "Height", "Direction", "Status"},
						new String[]{Integer.toString(mapID), Integer.toString(x), Integer.toString(y), Integer.toString(width)
						, Integer.toString(height), Integer.toString(direction), status}, "Initials = ?", new String[]{initials});
			}
			
			
		}catch(SQLException e){
			Log.e("SeatDAO", "Insert/Update seat info error");
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
