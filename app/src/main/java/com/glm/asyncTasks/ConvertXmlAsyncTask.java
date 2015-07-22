package com.glm.asyncTasks;

import com.glm.bean.User;
import com.glm.utilities.Utilities;

import android.content.Context;
import android.os.AsyncTask;

public class ConvertXmlAsyncTask extends AsyncTask<User, Void, Void> {
	
	private Context context;
	private Utilities utilities;
	
	public ConvertXmlAsyncTask(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(User... params) {
		
		User user = (User)params[0];
		
		this.utilities = new Utilities(context);
		
		this.utilities.initXmlDB(user);
		this.utilities.exportData(user.getId());

		return null;
	}	
}
