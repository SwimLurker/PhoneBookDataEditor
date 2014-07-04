package com.nnit.phonebook.dataeditor;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class EditorViewGroup extends ViewGroup{
	
	private final static int INVALID_SCREEN = -1;
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	
	
	private Scroller mScroller;
	private float mLastMotionX;
	private int mTouchState = TOUCH_STATE_REST;
	private int mCurrentScreen;
	private int mNextScreen = INVALID_SCREEN;
	
	public EditorViewGroup(Context context) {
		super(context);
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev){
		final int action = ev.getAction();
		final float x = ev.getX();
		switch(action){
			case MotionEvent.ACTION_DOWN:
				if(!mScroller.isFinished()){
					mScroller.abortAnimation();
				}
				mLastMotionX = x;
				break;
			case MotionEvent.ACTION_MOVE:
				if(mTouchState == TOUCH_STATE_SCROLLING){
					final int deltaX = (int)(mLastMotionX - x);
					mLastMotionX = x;
					if(deltaX < 0){
						if(getScrollX() > 0){
							scrollBy(Math.max(-1 * getScrollX(), deltaX), 0);
						}
					}else if(deltaX > 0){
						final int availableToScroll = getChildAt(getChildCount() -1).getRight() - getScrollX() - getWidth();
						if(availableToScroll > 0){
							scrollBy(Math.min(availableToScroll, deltaX), 0);
						}
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if(mTouchState == TOUCH_STATE_SCROLLING){
					snapToDestination();
				}
				mTouchState = TOUCH_STATE_REST;
				break;
			case MotionEvent.ACTION_CANCEL:
				mTouchState = TOUCH_STATE_REST;
				break;
		}
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
	}
	
	private void snapToDestination(){
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() +(screenWidth/2))/screenWidth;
		snapToScreen(whichScreen);
	}
	
	private void snapToScreen(int whichScreen){
		if(!mScroller.isFinished()) return;
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));
		boolean changingScreens = whichScreen != mCurrentScreen;
		mNextScreen = whichScreen;
		View focusedChild = getFocusedChild();
		if(focusedChild != null && changingScreens && focusedChild == getChildAt(mCurrentScreen)){
			focusedChild.clearFocus();
		}
		final int cx = getScrollX();
		final int newX = whichScreen * getWidth();
		final int delta = newX - cx;
		mScroller.startScroll(cx, 0, delta, 0, Math.abs(delta)*4);
		invalidate();
	}
	
	@Override
	public void computeScroll(){
		if(mScroller.computeScrollOffset()){
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}else if (mNextScreen != INVALID_SCREEN){
			mCurrentScreen = Math.max(0,  Math.min(mNextScreen, getChildCount() -1));
			mNextScreen = INVALID_SCREEN;
		}
	}
}
