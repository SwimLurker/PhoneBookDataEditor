package com.nnit.phonebook.dataeditor.data;

import com.nnit.phonebook.dataeditor.data.PhoneBookItem.PhoneBookField;


public class Filter {
	private PhoneBookItem.PhoneBookField field;
	private String value;
	
	public Filter(PhoneBookField field, String value) {
		super();
		this.field = field;
		this.value = value;
	}



	public boolean match(PhoneBookItem pb) {
		return Matcher.match(pb, field, value);
	}
	
}
