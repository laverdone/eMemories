package com.glm.asyncTasks;

import com.glm.db.DiaryRepositoryHelper;

import android.content.Context;
import android.os.AsyncTask;

public class RowLoadAsyncTask extends AsyncTask<Integer, Void, Boolean>{
	private int mRowID;
	private Context mContext;
	
	public RowLoadAsyncTask(Context applicationContext) {
		mContext = applicationContext;
	}

	@Override
	protected Boolean doInBackground(Integer... params) {
		mRowID = (int) params[0];
		return DiaryRepositoryHelper.removeRow(mContext, mRowID,0,0);
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