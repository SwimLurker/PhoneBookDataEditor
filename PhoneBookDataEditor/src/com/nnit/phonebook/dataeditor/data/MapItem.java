package com.nnit.phonebook.dataeditor.data;

public class MapItem {
	private int floor;
	private String mapFilename;
	private FLAG flag = FLAG.NOTMODIFIED;
	public int getFloor() {
		return floor;
	}
	public void setFloor(int floor) {
		this.floor = floor;
	}
	public String getMapFilename() {
		return mapFilename;
	}
	public void setMapFilename(String mapFilename) {
		this.mapFilename = mapFilename;
	}
	public FLAG getFlag() {
		return flag;
	}
	public void setFlag(FLAG flag) {
		this.flag = flag;
	}
	public boolean isDeleted() {
		return flag == FLAG.DELETED;
	}

	public boolean isNewCreated() {
		return flag == FLAG.NEW;
	}
	
	public boolean isModified(){
		return flag == FLAG.MODIFIED;
	}
	
}
