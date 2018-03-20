package com.king.demo.sence;

import com.bumptech.glide.Glide;
import com.king.demo.R;
import com.king.demo.sence.GravityGridView.GravityAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class SenceGridAdapter extends BaseAdapter implements GravityAdapter {

	private Context mContext;
	private int[] datas;
	
	public SenceGridAdapter(Context context, int[] datas) {
		mContext = context;
		this.datas = datas;
	}
	
	@Override
	public int getCount() {
		return datas.length;
	}

	@Override
	public Object getItem(int position) {
		return datas[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_img, null);
			holder.mIV = (ImageView) convertView.findViewById(R.id.item_iv);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Glide.with(mContext).load(datas[position]).asBitmap().centerCrop().into(holder.mIV);

		return convertView;
	}

	private static class ViewHolder {
		ImageView mIV;
	}

	@Override
	public int getLeft(View child) {
		if(child.getTag() instanceof ViewHolder){
			ViewHolder viewHolder = (ViewHolder) child.getTag();
			return viewHolder.mIV.getLeft();
		}
		return 0;
	}

	@Override
	public int getRight(View child) {
		if(child.getTag() instanceof ViewHolder){
			ViewHolder viewHolder = (ViewHolder) child.getTag();
			return viewHolder.mIV.getRight();
		}
		return 0;
	}

	@Override
	public int getTop(View child) {
		if(child.getTag() instanceof ViewHolder){
			ViewHolder viewHolder = (ViewHolder) child.getTag();
			return viewHolder.mIV.getTop();
		}
		return 0;
	}

	@Override
	public int getBottom(View child) {
		if(child.getTag() instanceof ViewHolder){
			ViewHolder viewHolder = (ViewHolder) child.getTag();
			return viewHolder.mIV.getBottom();
		}
		return 0;
	}
}
