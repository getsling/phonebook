package com.gangverk.mannvit.imageprocessing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.gangverk.mannvit.BuildConfig;
import com.jakewharton.DiskLruCache;

public class ImageCache {
	public static final String TAG = "ImageCache";
	private DiskLruCache mDiskCache;
	private LruCache<String, Bitmap> mMemoryCache;
	private Context context;
	private static final int APP_VERSION = 1;
	private static final int VALUE_COUNT = 1;

	private static final int DEFAULT_MEM_CACHE_SIZE= 10 * 1024 * 1024; // 5MB 
	private static final int DEFAULT_DISK_CACHE_SIZE = 20 * 1024 * 1024; // 10MB
	private static final int DEFAULT_BITMAP_WIDTH = 150;
	private static final int DEFAULT_BITMAP_HEIGHT = 150;
	private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
	private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;

	private ImageCacheParams cacheParams;

	public static final int IO_BUFFER_SIZE = 8 * 1024;

	public ImageCache(Context context, String uniqueName) {
		init(context, new ImageCacheParams(uniqueName));
	}

	public ImageCache(Context context, ImageCacheParams cacheParams) {
		init(context, cacheParams);
	}

	private void init(Context context, ImageCacheParams cacheParams) {
		this.context = context;
		this.cacheParams = cacheParams;
		// Set up disk cache
		if(cacheParams.diskCacheEnabled) {
			try {
				final File diskCacheDir = getDiskCacheDir(context, cacheParams.uniqueName );
				mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, cacheParams.diskCacheSize);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Set up memory cache
		if(cacheParams.memoryCacheEnabled) {
			mMemoryCache = new LruCache<String, Bitmap>(cacheParams.memCacheSize) {
				/**
				 * Measure item size in bytes rather than units which is more practical for a bitmap
				 * cache
				 */
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					return ProcessingUtils.getBitmapSize(bitmap);
				}
			};
		}
	}

	/**
	 * Find and return an existing ImageCache stored in a {@link RetainFragment}, if not found a new
	 * one is created using the supplied params and saved to a {@link RetainFragment}.
	 *
	 * @param activity The calling {@link FragmentActivity}
	 * @param cacheParams The cache parameters to use if creating the ImageCache
	 * @return An existing retained ImageCache object or a new one if one did not exist
	 */
	public static ImageCache findOrCreateCache(
			final FragmentActivity activity, ImageCacheParams cacheParams) {

		// Search for, or create an instance of the non-UI RetainFragment
		final RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(
				activity.getSupportFragmentManager());

		// See if we already have an ImageCache stored in RetainFragment
		ImageCache imageCache = (ImageCache) mRetainFragment.getObject();

		// No existing ImageCache, create one and store it in RetainFragment
		if (imageCache == null) {
			imageCache = new ImageCache(activity, cacheParams);
			mRetainFragment.setObject(imageCache);
		}

		return imageCache;
	}


	private boolean writeImageBitmapToFile(Bitmap bmp, DiskLruCache.Editor editor) {
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(editor.newOutputStream(0),IO_BUFFER_SIZE);
			return bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (IOException e) {
			Log.e(TAG, "Error in downloadBitmap - " + e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (final IOException e) {
					Log.e(TAG, "Error in downloadBitmap - " + e);
				}
			}
		}
		return false;
	}

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 *
	 * @param context The context to use
	 * @param uniqueName A unique directory name to append to the cache dir
	 * @return The cache dir
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {

		// Check if media is mounted or storage is built-in, if so, try and use external cache dir
		// otherwise use internal cache dir
		String storageState = Environment.getExternalStorageState();
		String cachePath = null;
		File externalCacheDir = context.getExternalCacheDir();
		if(externalCacheDir != null && storageState.equals(Environment.MEDIA_MOUNTED)) {		
			cachePath = externalCacheDir.getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		Log.d("DiskCacheDir", cachePath + File.separator + uniqueName);
		return new File(cachePath + File.separator + uniqueName + File.separator);
	}

	/**
	 * Fetches the bitmap associated with a key in the disk cache
	 * 
	 * @param key The key associated with a file that points to a bitmap
	 * @return The bitmap fetched from the cache, null if it doesn't exist.
	 */
	public Bitmap getBitmapFromDiskCache(String key) {
		Bitmap bitmap = null;
		DiskLruCache.Snapshot snapshot = null;
		try {
			snapshot = mDiskCache.get(key);
			if (snapshot == null) {
				return null;
			}
			final InputStream in = snapshot.getInputStream(0);
			if (in != null) {
				final BufferedInputStream buffIn = new BufferedInputStream(in, IO_BUFFER_SIZE);
				bitmap = ProcessingUtils.decodeSampledBitmap(buffIn, context,key, cacheParams.bitmapWidth, cacheParams.bitmapHeight);              
			}   
		} catch ( IOException e ) {
			e.printStackTrace();
		} finally {
			if ( snapshot != null ) {
				snapshot.close();
			}
		}
		if ( BuildConfig.DEBUG ) {
			Log.d( "cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
		}
		return bitmap;
	}

	/**
	 * Fetches the bitmap from the mem cache. The bitmap is ready there.
	 * 
	 * @param key The key associated with the bitmap
	 * @return The bitmap
	 */
	public Bitmap getBitmapFromMemCache(String key) {
		if(mMemoryCache != null && key!=null) {
			final Bitmap memBitmap = mMemoryCache.get(key);
			if(memBitmap != null && BuildConfig.DEBUG) {
				Log.d("Image fetched from memCache", key);
			}
			return memBitmap;
		}
		return null;
	}

	/**
	 * Fetches an image from the internet with specific url. Converts it into bitmap.
	 * 
	 * @param urlString The url to the image
	 * @return A processed bitmap
	 */
	public Bitmap getBitmapFromUrl(String urlString) {
		HttpURLConnection urlConnection = null;
		URL url = null;
		Bitmap bmp = null;
		try {
			url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			final InputStream in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
			bmp = ProcessingUtils.decodeSampledBitmap(in, context,urlToKey(urlString), cacheParams.bitmapWidth, cacheParams.bitmapHeight);
			if(BuildConfig.DEBUG) {
				if(bmp != null) {
					Log.d(TAG, "Successfully fetched bitmap from " + urlString);
				} else {
					Log.e(TAG, "Last resort method failed. No bitmap fetched from " + urlString);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return bmp;
	}

	/**
	 * Fetches bitmap located on a specific url if it exists there. Takes in url as parameter as
	 * opposed to getBitmapFromMemCache and getBitmapFromDiskCache 
	 * 
	 * @param url
	 * @return
	 */
	public Bitmap getBitmap(String url, boolean skipMemCheck) {
		Bitmap bmp = null;
		String key = urlToKey(url);
		if(key != null) {
			if(!skipMemCheck)
				bmp = getBitmapFromMemCache(key);
			if(bmp == null) {
				bmp = getBitmapFromDiskCache(key);
			}
			if(bmp == null) {
				bmp = getBitmapFromUrl(url);
			}
		}
		return bmp;
	}

	/**
	 * We use the url as a key to the cache. Should be unique enough.
	 * 
	 * @param url The url that is to be converted to a key
	 * @return The key from the url
	 */
	public static String urlToKey (String url) {
		String key = String.valueOf(url.hashCode());
		return key;

	}

	/**
	 * Adds a bitmap to both the memcache and the diskcache. If you already have a 
	 * processed bitmap you can pass it as a variable. If bitmap is null then the 
	 * image has to be fetched from the internet. In that case, always run from a separate thread
	 * 
	 * @param url The url to the image. Used only as a key if a bitmap is provided
	 * @param bitmap Can be null. The bitmap to add to the cache
	 */
	public void addBitmap(String url, Bitmap bitmap) {
		if(url == null) {
			return;
		}
		String key = urlToKey(url);

		if(mMemoryCache != null && key != null && mMemoryCache.get(key) == null) {
			putToMemCache(key, bitmap);
		}

		if(mDiskCache != null && key != null && !containsKey(key)) {
			putToDiskCache(url, bitmap);
		}
	}

	/**
	 * Puts an image to the disk cache. 
	 * 
	 * @param url
	 * @param bitmap
	 * @return
	 */
	private boolean putToDiskCache(String url, Bitmap bitmap) {
		if(bitmap != null) {
			DiskLruCache.Editor editor = null;
			try {
				editor = mDiskCache.edit(urlToKey(url));
				if ( editor == null ) {
					return false;
				}
				if(writeImageBitmapToFile(bitmap, editor)) {               
					mDiskCache.flush();
					editor.commit();
					if ( BuildConfig.DEBUG ) {
						Log.d( "cache_test_DISK_", "image put on disk cache " + urlToKey(url));
					}
					return true;
				} else {
					editor.abort();
					if ( BuildConfig.DEBUG ) {
						Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + urlToKey(url));
					}
					return false;
				}   
			} catch (IOException e) {
				if ( BuildConfig.DEBUG ) {
					Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + urlToKey(url));
				}
				try {
					if ( editor != null ) {
						editor.abort();
					}
				} catch (IOException ignored) {
				}           
			}
		}
		return false;
	}

	/**
	 * If the bitmap parameter is not null then it puts that bitmap to the memcache.
	 * Else, puts an image from the disk cache to the memcache. Does nothing if the image is
	 * not in the diskcache.
	 * 
	 * @param url Only used as a key here.
	 * @param bitmap The bitmap to put into the memcache. Can be null in order to use diskcache.
	 */
	private void putToMemCache(String key, Bitmap bitmap) {
		if(bitmap == null && key!=null) {
			bitmap = getBitmapFromDiskCache(key);
		}
		if(bitmap!=null && key!=null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * Checks if a certain key exists in the cache
	 * 
	 * @param key The key to search for
	 * @return true if it exists, otherwise false
	 */
	public boolean containsKey(String key) {

		boolean contained = false;
		DiskLruCache.Snapshot snapshot = null;
		try {
			snapshot = mDiskCache.get(key);
			contained = snapshot != null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if ( snapshot != null ) {
				snapshot.close();
			}
		}
		return contained;
	}

	public File getCacheFolder() {
		return mDiskCache.getDirectory();
	}

	/**
	 * A holder class that contains cache parameters.
	 */
	public static class ImageCacheParams {
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		public int bitmapWidth = DEFAULT_BITMAP_WIDTH;
		public int bitmapHeight = DEFAULT_BITMAP_HEIGHT;
		public String uniqueName;
		public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
		public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;


		public ImageCacheParams(String uniqueName) {
			this.uniqueName = uniqueName;
		}
	}

}
