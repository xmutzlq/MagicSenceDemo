package com.king.demo.sence;

import com.king.demo.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class SenceActivity extends Activity implements android.view.View.OnClickListener {

	public final static int[] mBottomIcon = {R.drawable.ico_b6, R.drawable.ico_b2, R.drawable.ico_b3, 
			R.drawable.ico_b4, R.drawable.ico_b5, R.drawable.ico_b1, R.drawable.ico_b7};
	private GravityFrameLayout mGravityFrameLayout;
	private GravityGridView mGravityGridView;
	private SenceGridAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_sences);
		mGravityFrameLayout = (GravityFrameLayout) findViewById(R.id.mark_lay);
		mGravityFrameLayout.setOnClickListener(this);
		mGravityFrameLayout.setIsNeedSence(true);

		mAdapter = new SenceGridAdapter(this, mBottomIcon);
		mGravityGridView = (GravityGridView) findViewById(R.id.sence_grid);
		mGravityGridView.setAdapter(mAdapter);
	}

	@Override
	public void onClick(View v) {
		mGravityFrameLayout.rock(GravityFrameLayout.MAX_DEGREES);
	}
}
