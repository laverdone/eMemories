package com.glm.asyncTasks;

import com.glm.bean.Page;
import com.glm.db.DiaryRepositoryHelper;
import android.content.Context;
import android.os.AsyncTask;


public class DeletePictureAsyncTask extends  AsyncTask<Object, Void, Boolean> {
	
	private Page mPage;
	private Context mContext;
	private long mCurrentPictureID;
	public DeletePictureAsyncTask(Page page,Context applicationContext) {
		mContext = applicationContext;
		mPage	 = page;
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		mCurrentPictureID = (Long) params[0];
		return DiaryRepositoryHelper.deleteImage(mContext, mPage,mCurrentPictureID);
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

}
