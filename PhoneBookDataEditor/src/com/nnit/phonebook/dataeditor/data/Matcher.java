package com.nnit.phonebook.dataeditor.data;

import com.nnit.phonebook.dataeditor.data.PhoneBookItem.PhoneBookField;

public class Matcher {

	public static boolean match(PhoneBookItem pb, PhoneBookField field,
			String value) {
		boolean result = true;
		switch (field){
			case INITIALS:
				result = matchByInitials(pb, (String)value);
				break;		
			case NAME:
				result = matchByName(pb, (String)value);
				break;
			case PHONE:
				result = matchByPhone(pb, (String)value);
				break;
			case DEPARTMENTNO:
				result = matchByDepNo(pb, (String)value);
				break;
			case DEPARTMENT:
				result = matchByDepartment(pb, (String)value);
				break;
			case MANAGER:
				result = matchByManager(pb, (String)value);
				break;
		}
		return result;
	}
	
	private static boolean matchByInitials(PhoneBookItem pb, String initials){		
		return match(pb.getInitials(), initials);
	}
	
	private static boolean matchByName(PhoneBookItem pb, String name){
		return contains(pb.getName(), name);
	}
	
	private static boolean matchByPhone(PhoneBookItem pb, String phone){
		return contains(pb.getPhone(),phone);
	}
	
	private static boolean matchByDepNo(PhoneBookItem pb, String depNo){
		return matchEquals(pb.getDepartmentNo(), depNo);
	}
	
	private static boolean matchByDepartment(PhoneBookItem pb, String department){
		return matchEquals(pb.getDepartment(), department);
	}
	
	private static boolean matchByManager(PhoneBookItem pb, String manager){
		return matchEquals(pb.getManager(), manager);
	}
	
	private static boolean match(String target, String pattern){
		if(pattern == null || pattern.trim().equals("")){
			return true;
		}
		if(target == null){
			return false;
		}
		String patternStr = getPatternString(pattern);
		
		boolean result =  target.toUpperCase().matches(patternStr);
		return result;
	}
	
	
	private static boolean matchEquals(String target, String pattern){
		if(pattern == null || pattern.trim().equals("")){
			return true;
		}
		if(target == null){
			return false;
		}
		return target.equalsIgnoreCase(pattern);
	}
	
	private static boolean contains(String target, String pattern){
		if(pattern == null || pattern.trim().equals("")){
			return true;
		}
		if(target == null){
			return false;
		}
		
		return target.toUpperCase().indexOf(pattern.toUpperCase())!=-1;
	}
	
	private static String getPatternString(String str){
		StringBuffer result = new StringBuffer();
		for(char c : str.toUpperCase().toCharArray()){
			if(c == '*'){
				result.append("([A-Z]*)");
			}else{
				result.append(c);
			}
		}
		result.append("([A-Z]*)");
		return result.toString();
	}
}
