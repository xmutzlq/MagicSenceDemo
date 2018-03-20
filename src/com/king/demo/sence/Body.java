package com.king.demo.sence;

import java.util.ArrayList;

import android.graphics.Rect;
import android.widget.Scroller;

public class Body {
	/** 手机倾斜角度*/
	public float mAngle;
	/** 加速度*/
	public float mAcc;
	/** 速度*/
	public float mSpeed;
	/** 一行View的X原始位置*/
	public ArrayList<Rect> mRowSourceLocals;
	/** 一行View的X当前位置*/
	public ArrayList<Integer> mRowLocals;
	/** 一行View的X当前位置*/
	public ArrayList<Scroller> mRowScrollers;
	/** 最近的时间*/
	public Long mLastTime;
	
	public String name;
	
	public void init() {
		mAngle = 0;
		mAcc = 0;
		mSpeed = 0;
		mRowLocals = new ArrayList<Integer>();
		mRowSourceLocals = new ArrayList<Rect>();
		mRowScrollers = new ArrayList<Scroller>();
		mLastTime = null;
	}
	
	public long computeTime(){
		return (System.currentTimeMillis() - mLastTime) / 10;
	}
	
	public boolean isFinishedRestoreAnim(){
		return (mRowScrollers.size() > 0 && mRowScrollers.get(0).isFinished());
	}
	
	public void abortRestoreAnim(){
		for (Scroller mScroller : mRowScrollers) {
			mScroller.abortAnimation();
		}
	}
}
