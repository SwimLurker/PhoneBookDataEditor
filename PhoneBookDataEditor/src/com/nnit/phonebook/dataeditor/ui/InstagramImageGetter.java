package com.nnit.phonebook.dataeditor.ui;

public class InstagramImageGetter implements IOnlineImageGetter{

	private String imageListURL = "http://iconosquare.com/tag/onennit";
	@Override
	public String getImageURL() {
		return "http://scontent-b.cdninstagram.com/hphotos-xfp1/t51.2885-15/10471891_750299578353958_2100431122_n.jpg";
	}

}
