package com.glm.utilities;

import java.util.concurrent.ExecutionException;

//import com.glm.asyncTasks.BitmapLoadAsyncTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapFactoryHelper {
	/**
	 * codifica la bitmap in sample size
	 * */
	/*public static Bitmap decodeSampledBitmapFromFile(String filename,int scaleFactor) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    Bitmap oBitmap=null; 
	    *//*options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(filename, options);
	    int imageHeight = options.outHeight/scaleFactor;
		int imageWidth = options.outWidth/scaleFactor;
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, imageWidth, imageHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;*//*
	    //TODO async task
	    BitmapLoadAsyncTask asyncTask = new BitmapLoadAsyncTask(options, filename,scaleFactor);
		asyncTask.execute();
		try {
			oBitmap = asyncTask.get();
			asyncTask.cancel(true);
			asyncTask=null;
			return oBitmap;
		} catch (InterruptedException e) {
			Log.e(BitmapFactoryHelper.class.getClass().getCanonicalName(),"InterruptedException during task async");
			return null;
		} catch (ExecutionException e) {
			Log.e(BitmapFactoryHelper.class.getClass().getCanonicalName(),"ExecutionException during task async");
			return null;
		}
		//return BitmapFactory.decodeFile(filename, options);
	}*/
	/**
	 * codifica la bitmap in sample size
	 * */
	/*public static Bitmap decodeSampledBitmapFromFile(String filename) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    Bitmap oBitmap=null; 
	    *//*options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(filename, options);
	    int imageHeight = options.outHeight/scaleFactor;
		int imageWidth = options.outWidth/scaleFactor;
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, imageWidth, imageHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;*//*
	    //TODO async task
	    BitmapLoadAsyncTask asyncTask = new BitmapLoadAsyncTask(options, filename,getScaleFactor(filename));
		asyncTask.execute();
		try {
			oBitmap = asyncTask.get();
			asyncTask.cancel(true);
			asyncTask=null;
			return oBitmap;
		} catch (InterruptedException e) {
			Log.e(BitmapFactoryHelper.class.getClass().getCanonicalName(),"InterruptedException during task async");
			return null;
		} catch (ExecutionException e) {
			Log.e(BitmapFactoryHelper.class.getClass().getCanonicalName(),"ExecutionException during task async");
			return null;
		}
		//return BitmapFactory.decodeFile(filename, options);
	}*/
	/**
	 * codifica la bitmap in sample size
	 * */
	/*private static int getScaleFactor(String filename) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(filename, options);
	    int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;
		
		if(imageWidth>1024){
			return 15;
		}else{
			return 6;	
		}
		
	    
	}*/
	/**
	 * codifica la bitmap in sample size
	 * */
	/*public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {
	    // First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		Bitmap oBitmap=null;    
	    BitmapLoadAsyncTask asyncTask = new BitmapLoadAsyncTask(options, filename, reqWidth, reqHeight);
		asyncTask.execute();
		try {
			oBitmap = asyncTask.get();
			asyncTask.cancel(true);
			asyncTask=null;
			return oBitmap;
		} catch (InterruptedException e) {
			Log.e(BitmapFactoryHelper.class.getClass().getCanonicalName(),"InterruptedException during task async");
			return null;
		} catch (ExecutionException e) {
			Log.e(BitmapFactoryHelper.class.getClass().getCanonicalName(),"ExecutionException during task async");
			return null;
		}
	}*/
}
