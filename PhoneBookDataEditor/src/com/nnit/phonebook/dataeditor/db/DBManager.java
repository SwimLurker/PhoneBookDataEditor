package com.nnit.phonebook.dataeditor.db;


import java.io.File;

import com.nnit.phonebook.dataeditor.data.DataManager;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
	
	private SQLiteDatabase database;
	
	public void openDatabase(){
		String dbfile = DataManager.getInstance().getSeatDBFileAbsolutePath();
		checkDatabase(dbfile);
		this.database =  SQLiteDatabase.openOrCreateDatabase(dbfile, null);
		if(this.database == null){
			throw new SQLException("Can not open database:" + dbfile);
		}
	}
	
	public void closeDatabase(){
		if(this.database != null){
			this.database.close();
			this.database = null;
		}
	}
	
	public Cursor query(String sql, String[] selectionArgs){
		return this.database.rawQuery(sql, selectionArgs);
	}
	
	public void delete(String table, String whereClause, String[] whereArgs){
		this.database.delete(table, whereClause, whereArgs);
	}
	
	public void insert(String table, String[] columns, String[] values) {
		ContentValues cvs = new ContentValues();
		for(int i = 0; i<columns.length; i++){
			String column = columns[i];
			String value = values[i];
			cvs.put(column, value);
		}
		this.database.insert(table, null, cvs);
		
	}
	
	public void update(String table, String[] columns, String[] values, String whereClause, String[] whereArgs){
		ContentValues cvs = new ContentValues();
		for(int i = 0; i<columns.length; i++){
			String column = columns[i];
			String value = values[i];
			cvs.put(column, value);
		}
		
		this.database.update(table, cvs, whereClause, whereArgs);
	}
	
	@SuppressLint("NewApi")
	private void checkDatabase(String dbfile){
		if(!(new File(dbfile).exists())){
			throw new RuntimeException("database file not found");
		}
	}

	
	
	

}
