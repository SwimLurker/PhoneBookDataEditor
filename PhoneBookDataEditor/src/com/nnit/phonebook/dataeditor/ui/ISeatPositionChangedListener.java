package com.nnit.phonebook.dataeditor.ui;

import android.graphics.RectF;

public interface ISeatPositionChangedListener {
	public void onSeatRectChanged(RectF seatRect);
	public void onSeatDirectionChanged(int direction);
}
