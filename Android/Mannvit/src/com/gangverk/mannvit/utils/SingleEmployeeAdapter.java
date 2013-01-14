package com.gangverk.mannvit.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.SimpleAdapter;

import com.gangverk.mannvit.R;

public class SingleEmployeeAdapter extends SimpleAdapter {
	private List<? extends Map<String, ?>> mData;
	private LayoutInflater mInflater;
	private ArrayList<Integer> phonePositionArray;
	private View.OnClickListener callButtonListener = null;
	
	public SingleEmployeeAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
		phonePositionArray = new ArrayList<Integer>();
		mData = data;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for(int i = 0; i<mData.size();i++) {
			String description = mData.get(i).get("description").toString();
			if(description.equals(context.getString(R.string.phone)) || description.equals(context.getString(R.string.mobile))) {
				phonePositionArray.add(new Integer(i));
			}
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(phonePositionArray.contains(position)) {
			convertView = mInflater.inflate(R.layout.single_employee_phone_item, parent, false);
			DontPressWithParentImgButton btnCall = (DontPressWithParentImgButton)convertView.findViewById(R.id.btnCall);
			btnCall.setOnClickListener(callButtonListener);
		}
		return super.getView(position, convertView, parent);
	}
	
	public void setCallButtonListener(OnClickListener callButtonListener) {
		this.callButtonListener = callButtonListener;
	}
}