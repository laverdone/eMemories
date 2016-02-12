package com.glm.asyncTasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;

public class BitmapLoadAsyncTask extends AsyncTask<Void, BitmapFactory.Options, Bitmap> {

	private BitmapFactory.Options mOptions;
	private String mFileName;
	private Bitmap mBitmap;
	private int mScaleFactor=0;
	private int mReqWidth;
	private int mReqHeight;
	
	public BitmapLoadAsyncTask(BitmapFactory.Options options, String filename,int scaleFactor) {
		mOptions=options;
		mFileName=filename;
		mScaleFactor=scaleFactor;
	}
	public BitmapLoadAsyncTask(Options options, String filename, int reqWidth,
			int reqHeight) {
		mOptions=options;
		mFileName=filename;
		mReqWidth=reqWidth;
		mReqHeight=reqHeight;

	}
	@Override
	protected Bitmap doInBackground(Void... params) {
		mOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mFileName, mOptions);
	    
		if(mScaleFactor==0){
			mOptions.inSampleSize = calculateInSampleSize(mOptions, mReqWidth, mReqHeight);
		}else{
			//int imageHeight = mOptions.outHeight/mScaleFactor;
			//int imageWidth = mOptions.outWidth/mScaleFactor;
			 // Calculate inSampleSize
			//mOptions.inSampleSize = calculateInSampleSize(mOptions, imageWidth, imageHeight);
			mOptions.inSampleSize = mScaleFactor;
		}

	    // Decode bitmap with inSampleSize set
		mOptions.inJustDecodeBounds = false;
		
		//OUT OF MEMORY
		mBitmap=BitmapFactory.decodeFile(mFileName, mOptions);
		return mBitmap;
	}
	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		this.cancel(true);
	}
	/**
	 * calcola il sample size
	 * */
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	    //Log.v(BitmapFactoryHelper.class.getClass().getCanonicalName(),"reqWidth: "+reqWidth+" reqHeight:"+reqHeight);
	    if (height > reqHeight || width > reqWidth) {
	
	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);
	
	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
	    }
	    //Log.v(BitmapFactoryHelper.class.getClass().getCanonicalName(),"Sample rate: "+inSampleSize+" outH:"+height+" outW:"+width);
	    return inSampleSize;
	}
}
