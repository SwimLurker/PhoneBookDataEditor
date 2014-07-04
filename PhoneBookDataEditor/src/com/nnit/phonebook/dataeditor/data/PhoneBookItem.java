package com.nnit.phonebook.dataeditor.data;

import java.io.Serializable;


public class PhoneBookItem implements Serializable{
	public enum GENDER{
		MALE, FEMALE, UNKNOWN
	};
	private String initials = null;
	private String name = null;
	private String localName = null;
	private GENDER gender = GENDER.UNKNOWN;
	private String phone = null;
	private String departmentNo = null;
	private String department = null;
	private String title = null;
	private String manager = null;
	private FLAG flag = FLAG.NOTMODIFIED;
	
	public String getInitials() {
		return initials;
	}
	public void setInitials(String initials) {
		this.initials = initials;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public GENDER getGender() {
		return gender;
	}
	public void setGender(GENDER gender) {
		this.gender = gender;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getDepartmentNo() {
		return departmentNo;
	}
	public void setDepartmentNo(String departmentNo) {
		this.departmentNo = departmentNo;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getManager() {
		return manager;
	}
	public void setManager(String manager) {
		this.manager = manager;
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
	
	public enum PhoneBookField {
		INITIALS, NAME, LOCALNAME, GENDER, PHONE, DEPARTMENTNO, DEPARTMENT, TITLE, MANAGER

	}

	public String toJSONString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{\"Initials\": \"");
		sb.append(initials);
		sb.append("\", \"Name\": \"");
		sb.append(name);
		sb.append("\", \"LocalName\": \"");
		sb.append(localName);
		sb.append("\", \"Gender\": \"");
		sb.append(gender == GENDER.MALE? "Male": "Female");
		sb.append("\", \"Phone\": \"");
		sb.append(phone);
		sb.append("\", \"DepartmentNO\": \"");
		sb.append(departmentNo);
		sb.append("\", \"Department\": \"");
		sb.append(department);
		sb.append("\", \"Title\": \"");
		sb.append(title);
		sb.append("\", \"Manager\": \"");
		sb.append(manager);
		sb.append("\" }");
		return sb.toString();
	}

}
