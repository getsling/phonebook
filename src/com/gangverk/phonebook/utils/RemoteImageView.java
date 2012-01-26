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
	
	/*private void startSpinner() {
		setImageDrawable(context.getResources().getDrawable(R.drawable.spinner));
		// make spinner reasonably large - remember to remove padding below
		int spinnerWidth = 20;
		measure(10000,10000);
		int imgWidth = getMeasuredWidth();
		int imgHeight = getMeasuredHeight();
		setScaleType(ScaleType.FIT_CENTER);
		setPadding(imgWidth/2-spinnerWidth/2, imgHeight/2-spinnerWidth/2, imgWidth/2-spinnerWidth/2, imgHeight/2-spinnerWidth/2);
		Animation spinnerRotation = AnimationUtils.loadAnimation(context,R.anim.clockwise_rotation);
		spinnerRotation.setRepeatCount(Animation.INFINITE);
		startAnimation(spinnerRotation);
	}*/
	
	public void setImageFromUrl(String imgUrlString){
		currentUrl = imgUrlString;
		ImageCache ic = ImageCache.getInstance();
		ic.loadAsync(imgUrlString, this, context);
	}
	
	private void setResultingImage(Drawable result) {
		clearAnimation();
		setPadding(0,0,0,0);
		setImageDrawable(result);
	}
	

	@Override
	public void onImageLoaded(Drawable image, String url) {
		if(url.equals(currentUrl)) {
			setResultingImage(image);
		}
	}
}