package com.glm.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import android.content.Context;
import android.util.Log;

import com.glm.bean.Diary;
import com.glm.bean.DiaryPicture;
import com.glm.bean.Page;
import com.glm.bean.Row;


public class DiaryHelper {
	private static Diary mCurrentDiary;
	private static Context mContext;

	/*public static Diary factoryDiaryBuilder(WritePageActivity activity, Context context, int template, long diaryid) {
		mContext=context;
		WPActivity=activity;
		mCurrentDiary=new Diary();
		if(diaryid==-1) {
			initNewDiary(template);
		}else{
			reloadDiary(diaryid);
		}
		return mCurrentDiary;
	}

	private static void reloadDiary(long diaryid) {
		mCurrentDiary.setDiaryID(diaryid);
		LoadDiaryAsyncTask asyncTask = new LoadDiaryAsyncTask(WPActivity, mContext,mCurrentDiary);
		asyncTask.execute();

	}*/
	/**
	 * Inizializza un nuovo Diario con tutte le proprietà necessarie
	 * 
	 * **/
	public static Diary initNewDiary(int template) {
		mCurrentDiary=new Diary();
		//Repository mReposotory = new Repository(mContext);
		long mDiaryID=0;
		Date mDate = new Date();
		SimpleDateFormat oDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		try{
			mDiaryID=Long.parseLong(oDateFormat.format(mDate));
		}catch(NumberFormatException e){
			Log.e(DiaryHelper.class.getCanonicalName(), "Number Format Error ");
            System.exit(1);
		}
		mCurrentDiary.setDiaryID(mDiaryID);
		mCurrentDiary.setDiaryTemplate(template);
		mCurrentDiary.setDiaryDTCreation(mDate);
		mCurrentDiary.setDiaryDTModify(mDate);
		mCurrentDiary.setDiaryPages(newPagesBuilder());
		return mCurrentDiary;
	}
	/**
	 * inizializza una pagina vuota del Nuovo Diario
	 * 
	 * **/
	private static Hashtable<Long, Page> newPagesBuilder() {
		Date mDate = new Date();
		SimpleDateFormat oDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		long mPageID=0;
		try{
			mPageID=Long.parseLong(oDateFormat.format(mDate));
		}catch(NumberFormatException e){
			mPageID=0;
		}
		Hashtable<Long, Page> mPages = new Hashtable<Long, Page>();
		Page mPage = new Page();
		mPage.setDiaryID(mCurrentDiary.getDiaryID());
		mPage.setPageID(mPageID);
		mPage.setPageDTCreation(new Date());
		mPage.setPageNumber(1);
		mPage.setDiaryImage(newImagesBuilder());
		mPage.setPageRows(newRowsBuilder());
		
		mPages.put(mPageID, mPage);
		
		return mPages;
	}
	/**
	 * Contiene un ArrayList Vuoto per le righe della pagina
	 * 
	 * */
	private static Hashtable<Long, Row> newRowsBuilder() {
		Hashtable<Long,Row> mRows = new Hashtable<Long, Row>();
		return mRows;
	}

	/**
	 * Contiene un ArrayList Vuoto per le immagini della pagina
	 * 
	 * */
	private static Hashtable<Long, DiaryPicture> newImagesBuilder() {
		Hashtable<Long,DiaryPicture> mImages = new Hashtable<Long, DiaryPicture>();
		return mImages;
	}

	
	/**
	 * Crea una pagina vuota
	 * 
	 * */
	public static Page factoryNewPageBuilder() {
		Date mDate = new Date();
		SimpleDateFormat oDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		long mPageID=0;
		try{
			mPageID=Long.parseLong(oDateFormat.format(mDate));
		}catch(NumberFormatException e){
			mPageID=0;
		}
		Page mPage = new Page();
		mPage.setPageID(mPageID);
		mPage.setPageDTCreation(new Date());
		mPage.setDiaryImage(newImagesBuilder());
		mPage.setPageRows(newRowsBuilder());
		return mPage;
	}
	/**
	 * aggiunge una pagina al diario passato
	 * 
	 * */
	public static Diary addPageToDiary(Diary mDiary, Page mPage){
		mCurrentDiary=mDiary;
		Hashtable<Long, Page> mPages = mCurrentDiary.getDiaryPages();
		int mPageNumber=mPages.size();
		mPage.setPageNumber(mPageNumber);
		mPage.setDiaryID(mCurrentDiary.getDiaryID());
		mPages.put(mPage.getPageID(), mPage);
		return mCurrentDiary;
	}
	/**
	 * carica immagini e path per la pagina passata
	 * risparmia memoria
	 * @param context 
	 * @param mPage 
	 * 
	 *
	public static Page factoryCurrentPageBuilder(WritePageActivity activity, Context context, Page currentPage, DiaryPageView mPage){
		CurrentPageAsyncTask asyncTask = new CurrentPageAsyncTask(activity, context,currentPage,mPage);
		asyncTask.execute();
		
		return currentPage;
	}*/
}
