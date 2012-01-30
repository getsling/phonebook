package com.gangverk.mannvit.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * The ImageCache class can be used to download images and store them in the
 * cache directory of the device. Multiple requests to the same URL will result
 * in a single download, until the cache timeout has passed.
 * Adapted from: http://squarewolf.nl/2010/11/android-image-cache/
 * @author Thomas Vervest
 */
public class ImageCache {
	private static final long CACHE_TIMEOUT = 60*60*24*3*1000;
	private final Object _lock = new Object();
	private HashMap<String, WeakReference<Drawable>> _cache;
	private HashMap<String, List<ImageCallback>> _callbacks;
	
	/**
	 * The ImageCallback interface defines a single method used to pass an image
	 * back to the calling object when it has been loaded.
	 */
	public static interface ImageCallback {
		/**
		 * The onImageLoaded method is called by the ImageCache when an image
		 * has been loaded.
		 * @param image The requested image in the form of a Drawable object.
		 * @param url The originally requested URL
		 */
		void onImageLoaded(Drawable image, String url);
	}

	private static ImageCache _instance = null;

	/**
	 * Gets the singleton instance of the ImageCache.
	 * @return The ImageCache.
	 */
	public synchronized static ImageCache getInstance() {
		if (_instance == null) {
			_instance = new ImageCache();
		}
		return _instance;
	}

	private ImageCache() {
		_cache = new HashMap<String, WeakReference<Drawable>>();
		_callbacks = new HashMap<String, List<ImageCallback>>();
	}

	private String getHash(String url) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(url.getBytes());
			return new BigInteger(digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException ex) {
			// this should never happen, but just to make sure return the url
			return url;
		}
	}

	private Drawable drawableFromCache(String url, String hash) {
		Drawable d = null;
		synchronized (_lock) {
			if (_cache.containsKey(hash)) {
				WeakReference<Drawable> ref = _cache.get(hash);
				if ( ref != null ) {
					d = ref.get();
					if (d == null)
						_cache.remove(hash);
				}
			}
		}
		return d;
	}
	private class ImgInfoContainer {
		String url;
		Context context;
		public ImgInfoContainer(String url, Context context) {
			this.url = url;
			this.context = context;
		}
	}
	private class ImgResultContainer {
		String hash;
		String url;
		Drawable drawable;
		public ImgResultContainer(String hash, String url, Drawable drawable) {
			this.hash = hash;
			this.url = url;
			this.drawable = drawable;
		}
	}
	
	private class DownloadImageTask extends AsyncTask<ImgInfoContainer, Void, ImgResultContainer> {

		protected ImgResultContainer doInBackground(ImgInfoContainer... info) {
			String url = info[0].url;
			Context context = info[0].context;
			
			String hash = getHash(url);
			Drawable d = null;
			try {
				d = drawableFromCache(url, hash);

				File f = new File(context.getCacheDir(), hash);
				boolean timeout = f.lastModified() + CACHE_TIMEOUT < new Date().getTime();
				if (d == null || timeout) {
					if (timeout)
						f.delete();
					if (!f.exists())
					{
						InputStream is = new URL(url).openConnection().getInputStream();
						if (f.createNewFile())
						{
							FileOutputStream fo = new FileOutputStream(f);
							byte[] buffer = new byte[256];
							int size;
							while ((size = is.read(buffer)) > 0) {
								fo.write(buffer, 0, size);
							}
							fo.flush();
							fo.close();
						}
					}
					d = Drawable.createFromPath(f.getAbsolutePath());

					synchronized (_lock) {
						_cache.put(hash, new WeakReference<Drawable>(d));
					}
				}
			} catch (Exception ex) {
				Log.e(getClass().getName(), ex.getMessage(), ex);
			}
			return new ImgResultContainer(hash, url, d);
		}
		
		protected void onPostExecute(ImgResultContainer result) {
			List<ImageCallback> callbacks;

			synchronized (_lock) {
				callbacks = _callbacks.remove(result.hash);
			}

			for (ImageCallback iter : callbacks) {
				iter.onImageLoaded(result.drawable, result.url);
			}
		}
	}

	/**
	 * Loads an image from the passed URL and calls the callback method when
	 * the image is done loading.
	 * @param url The URL of the target image.
	 * @param callback A ImageCallback object to pass the loaded image. If null,
	 * the image will only be pre-loaded into the cache.
	 * @param context The context of the new Drawable image.
	 */
	public void loadAsync(final String url, final ImageCallback callback, final Context context) {
		final String hash = getHash(url);

		synchronized (_lock) {
			List<ImageCallback> callbacks = _callbacks.get(hash);
			if (callbacks != null) {
				if (callback != null)
					callbacks.add(callback);
				return;
			}

			callbacks = new ArrayList<ImageCallback>();
			if (callback != null)
				callbacks.add(callback);
			_callbacks.put(hash, callbacks);
		}
		
		new DownloadImageTask().execute(new ImgInfoContainer(url, context));
	}
}