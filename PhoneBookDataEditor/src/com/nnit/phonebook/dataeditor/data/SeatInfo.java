package com.nnit.phonebook.dataeditor.data;

import java.io.Serializable;


public class SeatInfo implements Serializable {
	private String initials;
	private int x, y, width, height;
	private int direction;
	private int floorNo;
	private FLAG flag;
	
	public String getInitials() {
		return initials;
	}
	
	public void setInitials(String initials) {
		this.initials = initials;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getDirection() {
		return direction;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public int getFloorNo() {
		return floorNo;
	}
	
	public void setFloorNo(int floorNo) {
		this.floorNo = floorNo;
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
