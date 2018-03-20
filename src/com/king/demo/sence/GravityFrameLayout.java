package com.king.demo.sence;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class GravityFrameLayout extends FrameLayout implements SensorEventListener{
	private static int SENSOR_STATE_OPEN = 1;
	private static int SENSOR_STATE_CLOSE = 0;
	/** 最大角度*/
	public static final float MAX_DEGREES = 20f;
	/** 固定摩擦力对的加速度损耗*/
	private static final float CONSUME_SPEED = 1.5f;
	/** 速度灵敏度*/
	private static final int SCALE_DEGREES = 15;
	/** 最小感应角度*/
	private static final float MIN_DEGREES = 0.3f;
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private SensorEventListener mListener;
	
	/** 加速度*/
	private float mDegreesAcc;
	/** 角速度*/
	private float mDegreesSpeed;
	/** 手机倾斜角度*/
	private Float mDegrees;
	
	private int mSenceState = SENSOR_STATE_CLOSE;
	private boolean isNeedSence;
	private boolean isClickPlaying;
	
	public GravityFrameLayout(Context context) {
		super(context);
		init();
	}
	public GravityFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public GravityFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		onInit();
		mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	protected void onInit(){
		mDegreesAcc = 0;
		mDegreesSpeed = 0;
	}
	
	private boolean isRegister(){
		return mListener != null;
	}
	
	private void registerListener(){
		mDegrees = null;
		mListener = this;
		mSensorManager.registerListener(mListener, mSensor,SensorManager.SENSOR_DELAY_GAME);
	}
	
	private void unregisterListener(){
		mSensorManager.unregisterListener(mListener);
		mListener = null;
	}
	
	public void setIsNeedSence(boolean isNeedSence) {
		this.isNeedSence = isNeedSence;
		mSenceState = isNeedSence ? SENSOR_STATE_OPEN : SENSOR_STATE_CLOSE;
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if(!isRegister()){
			registerListener();
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		if(isRegister()){
			unregisterListener();
		}
		super.onDetachedFromWindow();
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if(visibility == View.VISIBLE){
			if(!isRegister() && isShown()){
				registerListener();
			}
		}else{
			if(isRegister()){
				unregisterListener();
			}
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(!isNeedSence) return; 
		if(getChildCount() == 1){
			float angle = event.values[SensorManager.DATA_X];
			if(mDegrees == null){
				mDegrees = MAX_DEGREES * 0.8f;
			}
			mDegreesAcc = angle - mDegrees;
			if(mDegreesSpeed > 0){
				mDegreesAcc -= CONSUME_SPEED;
			}else if(mDegreesSpeed < 0){
				mDegreesAcc += CONSUME_SPEED;
			}
			mDegreesSpeed += mDegreesAcc;
			if(Math.abs(mDegreesSpeed) < 0.2f){
				mDegreesSpeed = 0;
			}
			float tempDegrees = mDegrees;
			tempDegrees += mDegreesSpeed / SCALE_DEGREES;
			if(Math.abs(mDegrees - tempDegrees) > 0.5f || Math.abs(angle - mDegrees) > 0.5f){
				mDegrees = tempDegrees;
			}else if(Math.abs(angle - mDegrees) > MIN_DEGREES){
				if(mDegrees > angle){
					mDegrees -= MIN_DEGREES;
				}else{
					mDegrees += MIN_DEGREES;
				}
			}else{
				isNeedSence = mSenceState == SENSOR_STATE_OPEN;
				if(isClickPlaying) {
					isClickPlaying = false;
					mDegrees = 0.0f;
					invalidate();
				}
			}
			if(Math.abs(mDegrees) > MAX_DEGREES){
				mDegrees = mDegrees / Math.abs(mDegrees) * MAX_DEGREES;
			}
			invalidate();
		}
	}
	
	public void rock(float degrees) {
		mDegrees = degrees / 10 * MAX_DEGREES / 2;
		isNeedSence = true;
		isClickPlaying = true;
		invalidate();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
	private float getDegrees(){
		return mDegrees / 10 * MAX_DEGREES / 2;
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		canvas.save();
		if(getChildCount() == 1 && mDegrees != null){
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
			canvas.rotate(getDegrees(), getWidth() / 2, getChildAt(0).getPaddingTop());
		}
		super.dispatchDraw(canvas);
		canvas.restore();
	}
}
