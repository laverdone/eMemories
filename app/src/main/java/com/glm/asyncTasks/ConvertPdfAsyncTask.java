package com.glm.asyncTasks;

import com.glm.bean.Page;
import com.glm.utilities.PdfBuilder;

import android.os.AsyncTask;
import android.util.SparseArray;

public class ConvertPdfAsyncTask extends AsyncTask<Object, Void, Void> {
	
	private PdfBuilder pdfB;

	@Override
	protected Void doInBackground(Object... params) {
		
		int idNote = (Integer) params[0];
		
		@SuppressWarnings("unchecked")
		SparseArray<Page> pages = (SparseArray<Page>) params[1];
		
		String noteName = (String) params[2];
		
		String noteDesc = (String) params[3];
		
		//pdfB = new PdfBuilder();

		//pdfB.execute(idNote, pages, noteName, noteDesc);
		
		return null;	
	}
}
