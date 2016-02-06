package com.glm.asyncTasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class BitmapHWWorkerAsyncTask extends AsyncTask<Bitmap, String, Boolean> {

	private Bitmap mBitmap;
	private String mFileName;
	private File mFile=null;
	private FileOutputStream out = null;
	private View mPage=null;

	private CompressFormat mCompress=null;
	public BitmapHWWorkerAsyncTask(Bitmap bitmap, String filename,CompressFormat compress) {
		mBitmap=bitmap;
		mFileName=filename;
		mCompress=compress;
	}
	
	@Override
	protected Boolean doInBackground(Bitmap... arg0) {
		   
		try {
			if(mPage!=null) mBitmap=mPage.getDrawingCache(true);
			if(mBitmap!=null){
				mFile = new File(mFileName);
			    out = new FileOutputStream(mFile);
			    if(mCompress==null){
			    	 mCompress=Bitmap.CompressFormat.JPEG;
			    	 Log.v(this.getClass().getCanonicalName(),"Compress JPEG");
			    }
			    mBitmap.compress(mCompress, 90, out);	
				out.close();
				out=null;
				Log.v(this.getClass().getCanonicalName(),"Page Preview saving image: "+mFileName);
			}else{
				Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
			}
		} catch (IOException e) {
			Log.e(this.getClass().getCanonicalName(),"Error saving image");
		}
		
		return true;
	}
}
