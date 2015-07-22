package com.glm.asyncTasks;

import com.glm.db.DiaryRepositoryHelper;

import android.content.Context;
import android.os.AsyncTask;

public class RowAsyncTask extends AsyncTask<Long, Void, Boolean>{
	private long mRowID,mDiaryID,mPageID;
	private Context mContext;
	
	public RowAsyncTask(Context applicationContext) {
		mContext = applicationContext;
	}

	@Override
	protected Boolean doInBackground(Long... params) {
		mRowID = (long) params[0];
		mDiaryID = (long) params[1];
		mPageID = (long) params[2];
		return DiaryRepositoryHelper.removeRow(mContext, mRowID,mDiaryID,mPageID);
	}
	
	/**
     * Questo metodo viene eseguito prima del task separato
     * @author coluzza
     */
    @Override
    protected void onPreExecute() { 
      super.onPreExecute();
    }
}