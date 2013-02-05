package com.gangverk.mannvit.imageprocessing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public class ProcessingUtils {

	public static Bitmap decodeSampledBitmap(String filename, int width, int height, int rotate) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename,options);
		options.inSampleSize = calculateInSampleSize(options,width,height);
		options.inJustDecodeBounds = false;


		Bitmap original = BitmapFactory.decodeFile(filename,options);
		if(rotate!=0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			original = Bitmap.createBitmap(original, 0, 0, 
					original.getWidth(), original.getHeight(), 
					matrix, true);
		}
		return original;
	}

	public static Bitmap decodeSampledBitmap(InputStream is, Context context, String uniqueFileName, int width, int height) {
		OutputStream out = null;
		Bitmap bmp = null;
		try {
			out = context.openFileOutput(uniqueFileName, Context.MODE_PRIVATE);
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			bmp = decodeSampledBitmap(context.getFileStreamPath(uniqueFileName).getAbsolutePath(), width, height,0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
					context.deleteFile(uniqueFileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bmp;		 
	}

	/**
	 * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
	 * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
	 * the closest inSampleSize that will result in the final decoded bitmap having a width and
	 * height equal to or larger than the requested width and height. This implementation does not
	 * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
	 * results in a larger bitmap which isn't as useful for caching purposes.
	 *
	 * @param options An options object with out* params already populated (run through a decode*
	 *            method with inJustDecodeBounds==true
	 * @param reqWidth The requested width of the resulting bitmap
	 * @param reqHeight The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger
			// inSampleSize).

			final float totalPixels = width * height;

			// Anything more than 2x the requested pixels we'll sample down
			// further.
			final float totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
				inSampleSize++;
			}
		}
		return inSampleSize;
	}

	/**
	 * Get the memory class of this device (approx. per-app memory limit)
	 *
	 * @param context
	 * @return
	 */
	public static int getMemoryClass(Context context) {
		return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
	}

	/**
	 * Get the size in bytes of a bitmap.
	 * @param bitmap
	 * @return size in bytes
	 */
	@SuppressLint("NewApi")
	public static int getBitmapSize(Bitmap bitmap) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			return bitmap.getByteCount();
		}
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * This is a check to see if the user is connected to the internet.
	 * @param context Application context
	 * @return true if connected, false otherwise
	 */
	public static boolean checkConnection(Context context) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
			return false;
		}
		return true;
	}
}