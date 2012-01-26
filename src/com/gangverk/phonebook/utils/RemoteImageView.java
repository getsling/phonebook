package com.gangverk.phonebook.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;


public class RemoteImageView extends ImageView implements ImageCache.ImageCallback {
	private String currentUrl = null;
    
	private Context context;
//	private static final String LOG_TAG = "RemoteImageView";
	public RemoteImageView(Context context) {
		super(context);
		initialize(context);
	}
	
	public RemoteImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}
	
	public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}
	
	private void initialize(Context context) {
		this.context = context;
	}
	
	public void setImageFromUrl(String imgUrlString){
		currentUrl = imgUrlString;
		ImageCache ic = ImageCache.getInstance();
		ic.loadAsync(imgUrlString, this, context);
	}
	
	private void setResultingImage(Drawable result) {
		clearAnimation();
		setPadding(0,0,0,0);
		if(result != null) {
			setImageDrawable(result);
		}
	}
	

	@Override
	public void onImageLoaded(Drawable image, String url) {
		if(url.equals(currentUrl)) {
			setResultingImage(image);
		}
	}
}