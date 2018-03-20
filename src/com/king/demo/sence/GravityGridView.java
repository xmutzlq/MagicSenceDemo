package com.king.demo.sence;

import com.king.demo.util.DimensionsUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class GravityGridView extends GridView implements SensorEventListener {
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private SensorEventListener mListener;
	private GravityAdapter mAdapter;
	private Body mBody;
	/** Grid列数*/
	private int mNumColumns;
	/** Left边界*/
	private int mBoundLeft;
	/** Right边界*/
	private int mBoundRight;
	/** 最近的时间*/
	private Long mLastTime;
	/** 动画是否可用*/
	private boolean isAnimEnabled;
	
	public GravityGridView(Context context) {
		super(context);
		init();
	}
	public GravityGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public GravityGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		onInit();
		mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		initAnim();
		isAnimEnabled = false;
	}
	/**
	 * 初始化动画
	 */
	private void initAnim(){
		if(mBody == null) {
			mBody = new Body();
		}
		mBody.init();
		invalidateViews();
		abortRestoreAnim();
	}
	
	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		initRowLocal(true);
	}
	
	private void initRowLocal(boolean isLayoutChildren){
		if(mAdapter != null && getChildCount() > 0 && isAnimEnabled){
			mBoundLeft = getLeft() - getLeft(getChildAt(0));
			int indexRight = getChildCount() > mNumColumns ? mNumColumns - 1: getChildCount() - 1;
			View rightChild = getChildAt(indexRight);
			mBoundRight = getWidth() + (rightChild.getWidth() - getRight(rightChild));
			if(getChildCount() < mBody.mRowLocals.size()){
				mBody.mRowLocals.subList(getChildCount() - 1, mBody.mRowLocals.size()).clear();
				mBody.mRowScrollers.subList(getChildCount() - 1, mBody.mRowScrollers.size()).clear();
			}
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				Rect rect = new Rect(child.getLeft(),child.getTop(),child.getRight(),child.getBottom());
				if(isLayoutChildren && i < mNumColumns){
					if(mBody.mRowSourceLocals.size() <= mNumColumns){
						mBody.mRowSourceLocals.add(rect);
					}else{
						mBody.mRowSourceLocals.set(i, rect);
					}
				}
				if(mBody.mRowLocals.size() < i + 1){
					mBody.mRowLocals.add(rect.left);
					OvershootInterpolator interpolator = new OvershootInterpolator(1f);
					mBody.mRowScrollers.add(new Scroller(getContext(),interpolator));
				}else{
					int offset = mBody.mRowLocals.get(i) - getChildAt(i).getLeft();
					child.offsetLeftAndRight(offset);
				}
			}
		}else{
			mBoundLeft = 0;
			mBoundRight = getWidth();
		}
	}
	
	@Override
	public void setNumColumns(int numColumns) {
		if(mNumColumns != numColumns){
			mNumColumns = numColumns;
			initAnim();
		}
		super.setNumColumns(numColumns);
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		mAdapter = null;
		if(adapter instanceof GravityAdapter){
			mAdapter = (GravityAdapter) adapter;
			if(!isRegister() && isAnimEnabled){
				registerListener();
			}
		}else if(isRegister()){
			unregisterListener();
		}
		initAnim();
		super.setAdapter(adapter);
	}
	
	protected void onInit(){}
	
	private boolean isRegister(){
		return mListener != null;
	}
	
	private void registerListener(){
		mListener = this;
		mSensorManager.registerListener(mListener, mSensor,SensorManager.SENSOR_DELAY_GAME);
	}
	
	private void unregisterListener(){
		mSensorManager.unregisterListener(mListener);
		mListener = null;
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setAnimEnabled(true);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		isAnimEnabled = false;
		if(isRegister()){
			unregisterListener();
		}
		super.onDetachedFromWindow();
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		float angle = event.values[SensorManager.DATA_X];
		if(Math.abs(mBody.mAngle - angle) > 1.5){
			mBody.mAngle = angle;
		}
		if(Math.abs(mBody.mAngle) < 3){
			startRestoreAnim();
			mLastTime = null;
		}else{
			computeGravityAnimLocal();
		}
	}
	
	/**
	 * 计算重力驱动动画
	 * @param angle
	 */
	private void computeGravityAnimLocal(){
		if(!checkAnimEnabled()){
			return;
		}
		if(!isFinishedRestoreAnim()){
			abortRestoreAnim();
		}
		if(mLastTime == null){
			mLastTime = System.currentTimeMillis();
			mBody.mSpeed = 0;
		}
		mBody.mAcc = mBody.mAngle / Math.abs(mBody.mAngle);
		mBody.mSpeed -= mBody.mAcc; 
		if(Math.abs(mBody.mSpeed) > 10){
			mBody.mSpeed = mBody.mSpeed / Math.abs(mBody.mSpeed) * 10;
		}
		if (mBody.mSpeed < 0) {
			int bound = mBoundLeft;
			for (int i = 0;i < mBody.mRowLocals.size() && i >= 0; i++) {
				View child = getChildAt(i);
				if(child != null){
					if(i % mNumColumns != 0){
						bound -= getLeft(child);
					}
					int offset = (int) ((computeSpeed(i, true)) * computeTime());
					int left = mBody.mRowLocals.get(i);
					if(left + offset < bound){
						offset = bound - left;
					}
					mBody.mRowLocals.set(i, mBody.mRowLocals.get(i) + offset);
					if(i % mNumColumns != mNumColumns - 1){
						bound = mBody.mRowLocals.get(i) + getRight(child);
					}else{
						bound = mBoundLeft;
					}
				}
			}
		} else {
			int bound = mBoundRight;
			for (int i = mBody.mRowLocals.size() - 1;i < mBody.mRowLocals.size() && i >= 0; i--) {
				View child = getChildAt(i);
				if(child != null){
					if(i % mNumColumns != mNumColumns - 1 && i != mBody.mRowLocals.size() - 1){
						bound += child.getWidth() - getRight(child);
					}
					int offset = (int) (computeSpeed(i, false) * computeTime());
					int right = mBody.mRowLocals.get(i) + child.getWidth();
					if(right + offset > bound){
						offset = bound - right;
					}
					mBody.mRowLocals.set(i, mBody.mRowLocals.get(i) + offset);
					if(i % mNumColumns != 0){
						bound = mBody.mRowLocals.get(i) + getLeft(child);
					}else{
						bound = mBoundRight;
					}
				}
			}
		}
		invalidate();
		mLastTime = System.currentTimeMillis();
	}
	
	private float computeSpeed(int index,boolean isLeft){
		if(isLeft){
			return mBody.mSpeed - (mNumColumns - index % mNumColumns - 1) * 2;
		}else{
			return mBody.mSpeed + (index % mNumColumns) * 2;
		}
	}
	
	private long computeTime(){
		return (System.currentTimeMillis() - mLastTime) / 10;
	}
	
	private void abortRestoreAnim(){
		for (Scroller mScroller : mBody.mRowScrollers) {
			mScroller.abortAnimation();
		}
	}
	
	private boolean isFinishedRestoreAnim(){
		return mBody.mRowScrollers.size() > 0 && mBody.mRowScrollers.get(0).isFinished();
	}
	
	private boolean checkRestoreLocals(){
		boolean isChange = mBody.mRowSourceLocals.get(0).left != mBody.mRowLocals.get(0);
		boolean isNeedInvalidate = false;
		if(!isChange){
			for (int i = 0; i < mBody.mRowLocals.size(); i++) {
				if(mBody.mRowSourceLocals.get(i % mNumColumns).left != mBody.mRowLocals.get(i)){
					mBody.mRowLocals.set(i,mBody.mRowSourceLocals.get(i % mNumColumns).left);
					isNeedInvalidate = true;
				}
			}
		}
		if(isNeedInvalidate){
			invalidate();
		}
		return isChange;
	}
	
	/**
	 * 播放恢复动画
	 */
	private void startRestoreAnim(){
		if(!checkAnimEnabled()){
			return;
		}
		if(!isFinishedRestoreAnim() || !checkRestoreLocals()){
			return;
		}
		for (int i = 0; i < mBody.mRowScrollers.size(); i++) {
			Scroller mScroller = mBody.mRowScrollers.get(i);
			mScroller.startScroll(mBody.mRowLocals.get(i), 0, mBody.mRowSourceLocals.get(i % mNumColumns).left - mBody.mRowLocals.get(i), 0, 700);
		}
		invalidate();
	}
	/**
	 * 计算恢复动画
	 */
	private void computeRestoreAnimLocal(){
		if(!checkAnimEnabled()){
			return;
		}
		if(!isFinishedRestoreAnim()){
			for (int i = 0; i < mBody.mRowScrollers.size(); i++) {
				Scroller mScroller = mBody.mRowScrollers.get(i);
				if(mScroller.computeScrollOffset()){
					mBody.mRowLocals.set(i, mScroller.getCurrX());
				}
			}
			invalidate();
		}
	}
	/**
	 * 设置子View位置
	 */
	private void setChildLocal(){
		if(!checkAnimEnabled()){
			return;
		}
		for (int i = 0;i < mBody.mRowLocals.size() && i >= 0; i++) {
			View child = getChildAt(i);
			if(child != null){
				child.offsetLeftAndRight(mBody.mRowLocals.get(i) - child.getLeft());
			}
		}
	}
	/**
	 * 检测动画是否可执行
	 * @return
	 */
	private boolean checkAnimEnabled(){
		if(getChildCount() != mBody.mRowLocals.size()){
			initRowLocal(false);
		}
		return isAnimEnabled && mBody.mRowLocals.size() > 0 && getChildCount() > 0 && mBody.mRowSourceLocals.size() > 0;
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		//递归还原动画运算
		computeRestoreAnimLocal();
		setChildLocal();
		super.dispatchDraw(canvas);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
	public int getLeft(View child){
		if(mAdapter != null){
			return mAdapter.getLeft(child) - DimensionsUtil.DIPToPX(3);
		}
		return 0;
	}
	
	public int getRight(View child){
		if(mAdapter != null){
			return mAdapter.getRight(child) + DimensionsUtil.DIPToPX(3);
		}
		return 0;
	}
	
	public int getTop(View child){
		if(mAdapter != null){
			return mAdapter.getTop(child);
		}
		return 0;
	}
	
	public int getBottom(View child){
		if(mAdapter != null){
			return mAdapter.getBottom(child);
		}
		return 0;
	}
	
	private void setAnimEnabled(boolean isAnimEnabledTemp){
		if(isAnimEnabled != isAnimEnabledTemp){
			isAnimEnabled = isAnimEnabledTemp;
			if(isAnimEnabled){
				if(mAdapter instanceof GravityAdapter && !isRegister()){
					registerListener();
				}
			}else if(isRegister()){
				unregisterListener();
			}
			initAnim();
		}
	}
	
	/**
	 * 适配器必须实现这个接触才能支持重力动画
	 */
	public interface GravityAdapter{
		public abstract int getLeft(View child);
		public abstract int getRight(View child);
		public abstract int getTop(View child);
		public abstract int getBottom(View child);
	}
}
