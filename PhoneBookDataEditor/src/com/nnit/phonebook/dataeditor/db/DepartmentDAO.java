package com.nnit.phonebook.dataeditor.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.nnit.phonebook.dataeditor.data.DepartmentInfo;

public class DepartmentDAO {
	private DBManager dbManager;
	
	public DepartmentDAO(){
		dbManager = new DBManager();
	}
	
	public List<DepartmentInfo> getAllDepartments(){
		List<DepartmentInfo> result = new ArrayList<DepartmentInfo>();
		Cursor cursor = null;
		try{
			dbManager.openDatabase();
			cursor = dbManager.query("select Name, Number, ManagerInitials from Department", null);
				
			while(cursor.moveToNext()){
				String depName = cursor.getString(0);
				String depNo = cursor.getString(1);
				String manager = cursor.getString(2);
				
				DepartmentInfo dep = new DepartmentInfo();
				dep.setDepartmentName(depName);
				dep.setDepartmentNO(depNo);
				dep.setManager(manager);
				result.add(dep);
			}
			
			return result;
		}catch(SQLException e){
			Log.e("DepartmentDAO", "Query department info error");
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
}
