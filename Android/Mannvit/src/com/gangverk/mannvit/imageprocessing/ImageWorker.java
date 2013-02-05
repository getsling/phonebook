package com.gangverk.mannvit.imageprocessing;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.gangverk.mannvit.BuildConfig;

public class ImageWorker {
	public static final String TAG = "ImageWorker";
	private ImageCache mImageCache;
	private Bitmap mLoadingBitmap;
	private static final int FADE_IN_TIME = 200;
	private boolean mFadeInBitmap = false;
	protected boolean mPauseWork = false;
	private boolean mExitTasksEarly = false;
	protected Context mContext;
	private int mResource;
	private final Object mPauseWorkLock = new Object();
	private boolean mCache = true;

	public ImageWorker(Context context, ImageCache imageCache, int resource, boolean cache) {
		mCache = cache;
		mContext = context;
		mImageCache = imageCache;
		mResource = resource;
		mLoadingBitmap = BitmapFactory.decodeResource(mContext.getResources(), mResource);
	}

	/**
	 * @param imageView Any imageView
	 * @return Retrieve the currently active work task (if any) associated with this imageView.
	 * null if there is no such task.
	 */
	private static BitmapFetchTask getBitmapFetchTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapFetchTask();
			}
		}
		return null;
	}

	public void loadImage(String url, ImageView imageView) {
		Bitmap bitmap = null;

		if(url==null) {
			return;
		}

		if(mImageCache != null) {
			bitmap = mImageCache.getBitmapFromMemCache(ImageCache.urlToKey(url));
		}

		if(bitmap != null) {
			// Bitmap found in memory cache
			imageView.setImageBitmap(bitmap);
		} else if (cancelPotentialWork(url, imageView)) {
			final BitmapFetchTask task = new BitmapFetchTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), mLoadingBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(url);
		}
	}

	/**
	 * If set to true, the image will fade-in once it has been loaded by the background thread.
	 */
	public void setImageFadeIn(boolean fadeIn) {
		mFadeInBitmap = fadeIn;
	}

	/**
	 * Cancels any pending work attached to the provided ImageView.
	 * @param imageView
	 */
	public static void cancelWork(ImageView imageView) {
		final BitmapFetchTask bitmapFetchTask = getBitmapFetchTask(imageView);
		if (bitmapFetchTask != null) {
			bitmapFetchTask.cancel(true);
			if (BuildConfig.DEBUG) {
				final String bitmapData = bitmapFetchTask.url;
				Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
			}
		}
	}

	/**
	 * Returns true if the current work has been canceled or if there was no work in
	 * progress on this image view.
	 * Returns false if the work in progress deals with the same data. The work is not
	 * stopped in that case.
	 */
	public static boolean cancelPotentialWork(String data, ImageView imageView) {
		final BitmapFetchTask bitmapFetchTask = getBitmapFetchTask(imageView);

		if (bitmapFetchTask != null) {
			final String bitmapData = bitmapFetchTask.url;
			if (bitmapData == null || !bitmapData.equals(data)) {
				bitmapFetchTask.cancel(true);
				if (BuildConfig.DEBUG) {
					Log.d(ImageWorker.class.getName(), "cancelPotentialWork - cancelled work for " + data);
				}
			} else {
				// The same work is already in progress.
				return false;
			}
		}
		return true;
	}

	private class BitmapFetchTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private String url;

		public BitmapFetchTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			this.url = urls[0];
			Bitmap bitmap = null;
			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {}
				}
			}
			// If the image cache is available and this task has not been cancelled by another
			// thread and the ImageView that was originally bound to this task is still bound back
			// to this task and our "exit early" flag is not set then try and fetch the bitmap from
			// the cache
			if(mImageCache != null && !isCancelled() && getAttachedImageView() != null && !mExitTasksEarly) {
				if(mCache) {
					bitmap = mImageCache.getBitmap(urls[0], true);
					if(bitmap != null) {
						mImageCache.addBitmap(urls[0], bitmap);
					}
				} else {
					bitmap = mImageCache.getBitmapFromUrl(urls[0]);
				}
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			// if cancel was called on this task or the "exit early" flag is set then we're done
			if (isCancelled() || mExitTasksEarly) {
				bitmap = null;
			}

			final ImageView imageView = getAttachedImageView();
			if (bitmap != null && imageView != null) {
				setImageBitmap(imageView, bitmap);
			}
		}	

		@Override
		protected void onCancelled() {
			super.onCancelled();
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}

		/**
		 * Returns the ImageView associated with this task as long as the ImageView's task still
		 * points to this task as well. Returns null otherwise.
		 */
		private ImageView getAttachedImageView() {
			final ImageView imageView = imageViewReference.get();
			final BitmapFetchTask bitmapFetchTask = getBitmapFetchTask(imageView);

			if (this == bitmapFetchTask) {
				return imageView;
			}

			return null;
		}
	}

	/**
	 * Called when the processing is complete and the final bitmap should be set on the ImageView.
	 *
	 * @param imageView
	 * @param bitmap
	 */
	@SuppressWarnings("deprecation")
	private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
		if (mFadeInBitmap) {
			// Transition drawable with a transparent drawable and the final bitmap
			final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
					new ColorDrawable(android.R.color.transparent),
					new BitmapDrawable(mContext.getResources(), bitmap)
			});
			// Set background to loading bitmap
			imageView.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), mLoadingBitmap));

			imageView.setImageDrawable(td);
			td.startTransition(FADE_IN_TIME);
		} else {
			imageView.setImageBitmap(bitmap);
		}
	}


	/**
	 * A custom Drawable that will be attached to the imageView while the work is in progress.
	 * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
	 * required, and makes sure that only the last started worker process can bind its result,
	 * independently of the finish order.
	 */
	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapFetchTask> bitmapFetchTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapFetchTask bitmapFetchTask) {
			super(res, bitmap);
			bitmapFetchTaskReference = new WeakReference<BitmapFetchTask>(bitmapFetchTask);
		}

		public BitmapFetchTask getBitmapFetchTask() {
			return bitmapFetchTaskReference.get();
		}
	}

	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
	}
}
