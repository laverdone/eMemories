package com.glm.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import com.glm.bean.Diary;
import com.glm.bean.DiaryPicture;
import com.glm.bean.HandWritePath;
import com.glm.bean.Page;
import com.glm.bean.Row;
import com.glm.labs.diary.ememories.Const;
//import com.glm.utilities.SynchHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Repository extends DBAdapter{

	public Repository(Context context) {
		super(context);
	}

	/**
	 * Salva il diario passato
	 * 
	 * */
	public boolean saveDiary(Diary diaryToDump) {
		
		boolean mResult = false;
		int iRowEffect=0;
		ContentValues mDiaryTable = new ContentValues();

		
		try {

            mDiaryTable.put(DiaryTable.DIARYID, diaryToDump.getDiaryID());
            mDiaryTable.put(DiaryTable.DIARYNAME, diaryToDump.getDiaryName());
            mDiaryTable.put(DiaryTable.DIARYTEMPLATE, diaryToDump.getDiaryTemplate());
            mDiaryTable.put(DiaryTable.DIARYDTCREATION,  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(diaryToDump.getDiaryDTCreation()));
            mDiaryTable.put(DiaryTable.DIARYDTMODIFY,  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(diaryToDump.getDiaryDTModify()));
            mDiaryTable.put(DiaryTable.CLOUDID, diaryToDump.getCloudDiaryID());
			//Apro il DB
			open();
			
			getmDb().insertOrThrow(DiaryTable.TABLE_NAME, null, mDiaryTable);

			mResult = saveDiaryPage(getmDb(), diaryToDump.getDiaryPages());
			
			
			close();
			//Chiudo il DB
			//mResult = true;
			if (mResult){
				Log.v(this.getClass().getCanonicalName(), "Dump diary success");
			}


		} catch (SQLiteException e) {
			mResult=false;
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "Errore nell' inserimento del Diary try to update");
			iRowEffect=getmDb().update(DiaryTable.TABLE_NAME, mDiaryTable, 
					DiaryTable.DIARYID + "=" + diaryToDump.getDiaryID(),null);
			if(iRowEffect==0){
				mResult=false;
			}else{
				mResult = saveDiaryPage(getmDb(), diaryToDump.getDiaryPages());
				//Log.i(this.getClass().getCanonicalName(), "Update Page to diary");
			}

		} catch (NullPointerException e) { 
			mResult=false;
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "NullPointerException under dump Diary");
		} catch (PersistenceException e) {
			mResult=false;
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "PersistenceException under dump Diary");
		}finally{
			close();
		}


		return mResult;
	}

	private boolean saveDiaryPage(SQLiteDatabase mDB, Hashtable<Long, Page> diaryPages) {
		ContentValues mPageTable = new ContentValues();
		
		
		long mLastPageID =0;
		int iRowEffect=0;
		Enumeration<Long> keys = diaryPages.keys();
		while( keys.hasMoreElements() ) {
			mLastPageID = (long) keys.nextElement();
			Page mPage = diaryPages.get(mLastPageID);
			mPageTable.put(PagesTable.DIARYID, mPage.getDiaryID());
			mPageTable.put(PagesTable.PAGEID, mPage.getPageID());
			mPageTable.put(PagesTable.PAGEHANDWRITE, mPage.getByteImageHW());
			mPageTable.put(PagesTable.PAGEPREVIEW, mPage.getByteImagePreviewPage());
			mPageTable.put(PagesTable.PAGENUMBER, mPage.getPageNumber());
			mPageTable.put(PagesTable.PAGELONG, mPage.getPageLong());
			mPageTable.put(PagesTable.PAGELAT, mPage.getPageLat());
			mPageTable.put(PagesTable.PAGEALT, mPage.getPageAlt());
			mPageTable.put(PagesTable.PAGEBOOKMARK, (mPage.isPageBookMark() ? 1 : 0) );
			mPageTable.put(PagesTable.PAGEDTCREATION, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(mPage.getPageDTCreation()));
			mPageTable.put(PagesTable.PAGELOC, mPage.getPageLoc());
			mPageTable.put(PagesTable.PAGEORIENTATION, mPage.getPageOrientation());
			try{				
				mDB.insertOrThrow(PagesTable.TABLE_NAME, null, mPageTable);	
				//Log.i(this.getClass().getCanonicalName(), "Add new Page to diary");
			}catch(SQLException e){
				//Log.w(this.getClass().getCanonicalName(), "Error inserting page try to update");
				
				iRowEffect=mDB.update(PagesTable.TABLE_NAME, mPageTable, 
								PagesTable.DIARYID + "=" + mPage.getDiaryID() + " and "+PagesTable.PAGEID+"="+mPage.getPageID(), 
									null);
				if(iRowEffect==0) return false;
				//Log.i(this.getClass().getCanonicalName(), "Update Page to diary");
			}finally{
				//Eseguo sempre se inserisco o updato
				//Inserisco le rows
				savePageRows(mDB, mPage);
				//Inserisco le images
				savePageImages(mDB, mPage);
				//Inserisco le images
				//savePagePaths(mDB, mPage);
				
			}
			
		}
		return true;
	}

	/**
	 * inserisco le immagini per la pagina passata nel DB
	 * @param mDB
	 * @param mPage pagina a cui le immagini sono legate.
	 *
	 * */
	private boolean savePageImages(SQLiteDatabase mDB, Page mPage) {
		if(mPage.getDiaryImage()==null) return true;
		int iRowEffect=0;
		ContentValues mImageTable = new ContentValues();
		Enumeration<?> keys = mPage.getDiaryImage().keys();
		while( keys.hasMoreElements() ) {
			DiaryPicture mDiaryPicture = mPage.getDiaryImage().get( (Long) keys.nextElement());
			if(mDiaryPicture==null) break;
			mImageTable.put(PictureTable.DIARYID, mPage.getDiaryID());
			mImageTable.put(PictureTable.PAGEID, mDiaryPicture.getPageID());
			mImageTable.put(PictureTable.PICTUREID, mDiaryPicture.getDiaryPictureID());
			mImageTable.put(PictureTable.PICTUREPREVIEW, mDiaryPicture.getByteImage());
			mImageTable.put(PictureTable.PICTUREH, mDiaryPicture.getDiaryPictureH());
			mImageTable.put(PictureTable.PICTUREW, mDiaryPicture.getDiaryPictureW());
			mImageTable.put(PictureTable.PICTUREX, mDiaryPicture.getDiaryPictureX());
			mImageTable.put(PictureTable.PICTUREY, mDiaryPicture.getDiaryPictureY());
			mImageTable.put(PictureTable.PICTUREROTATION, mDiaryPicture.getDiaryPictureRotation());
			mImageTable.put(PictureTable.PICTUREURI, mDiaryPicture.getDiaryImageURI());
			mImageTable.put(PictureTable.PICTUREHAND, (mDiaryPicture.isDiaryHandImage() ? 1 : 0) );
			try{
				mDB.insertOrThrow(PictureTable
						.TABLE_NAME, null, mImageTable);
				Log.i(this.getClass().getCanonicalName(), "Add new Image to Page");
			}catch(SQLException e){
				//Log.w(this.getClass().getCanonicalName(), "Error inserting Image try to update");
				
				iRowEffect=mDB.update(PictureTable.TABLE_NAME, mImageTable, 
						PictureTable.DIARYID + "=" + mPage.getDiaryID() 
						+ " and " + 
						PictureTable.PAGEID+"="+mDiaryPicture.getPageID() 
						+ " and " +
						PictureTable.PICTUREID+"="+mDiaryPicture.getDiaryPictureID() , 
									null);
				if(iRowEffect==0) return false;
				//Log.i(this.getClass().getCanonicalName(), "Update Image to diary");
			}	
			mDiaryPicture=null;
		}
		return true;
	}
	/**
	 * inserisco le righe per la pagina passata nel DB
	 * @param mDB 
	 * @param mPage pagina a cui le righe sono legate.
	 * 
	 * */
	private boolean savePageRows(SQLiteDatabase mDB, Page mPage) {
		int iRowEffect=0;
		ContentValues mRowTable = new ContentValues();
		Enumeration<Long> keys = mPage.getPageRows().keys();
		while( keys.hasMoreElements()) {
			Row mRow = mPage.getPageRows().get( (Long) keys.nextElement());
			if(mRow==null) break;
			mRowTable.put(RowsTable.DIARYID, mPage.getDiaryID());
			mRowTable.put(RowsTable.PAGEID, mRow.getPageID());
			mRowTable.put(RowsTable.ROWID, mRow.getRowID());
			mRowTable.put(RowsTable.ROWNUMBER, mRow.getRowNumber());
			mRowTable.put(RowsTable.ROWTEXT, mRow.getRowText());
			mRowTable.put(RowsTable.ROWPOSX, mRow.getRowPosX());
			mRowTable.put(RowsTable.ROWPOSY, mRow.getRowPosY());
			try{
				mDB.insertOrThrow(RowsTable.TABLE_NAME, null, mRowTable);
				Log.i(this.getClass().getCanonicalName(), "Add new Row to Page: "+mRow.getRowText());
			}catch(SQLException e){
				//Log.w(this.getClass().getCanonicalName(), "Error inserting Row try to update");
				
				iRowEffect=mDB.update(RowsTable.TABLE_NAME, mRowTable, 
						RowsTable.DIARYID + "=" + mPage.getDiaryID() 
						+ " and " + 
						RowsTable.PAGEID+"="+mRow.getPageID() 
						+ " and " +
						RowsTable.ROWID+"="+mRow.getRowID() , 
									null);
				if(iRowEffect==0) return false;
				//Log.i(this.getClass().getCanonicalName(), "Update Row to diary: "+mRow.getRowText());
			}	
			mRow=null;
		}
		return true;
	}

    /**
     * Metodo che legge tutte le informazioni dal DB e popola il bean del Diary
     * @param currentDiary
     *
     *
     * */
    public Diary reloadDiaryForExport(Diary currentDiary) {
        Diary mCurrentDiary=currentDiary;
        try {
            open();
            //Load Data from Diary
            mCurrentDiary=loadDiary(mCurrentDiary);
            //Load Data from Pages
            mCurrentDiary=loadPages(mCurrentDiary,true,false,true);
            close();
        } catch (PersistenceException e) {
            Log.e(this.getClass().getCanonicalName(),"PersistenceException during reload Diary");
            e.printStackTrace();
        }catch(SQLException e){
            Log.e(this.getClass().getCanonicalName(),"SQLException during reload Diary");
        }
        return mCurrentDiary;
    }

	/**
	 * Metodo che legge tutte le informazioni dal DB e popola il bean del Diary
	 * @param currentDiary 
	 * 
	 * 
	 * */
	public Diary reloadDiary(Diary currentDiary) {
		Diary mCurrentDiary=currentDiary;
		try {
			open();
			//Load Data from Diary
			mCurrentDiary=loadDiary(mCurrentDiary);
			//Load Data from Pages
			mCurrentDiary=loadPages(mCurrentDiary,true,false,false);
			close();
		} catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(),"PersistenceException during reload Diary");
			e.printStackTrace();
		}catch(SQLException e){
			Log.e(this.getClass().getCanonicalName(),"SQLException during reload Diary");
		}
		return mCurrentDiary;
	}

	/**
	 * Carica i dettagli del diary
	 * 
	 * @param currentDiary da caricare
	 * */
	private Diary loadDiary(Diary currentDiary) {
		Cursor oCursor = getmDb().query(DiaryTable.TABLE_NAME, DiaryTable.COLUMNS, DiaryTable.DIARYID + "=" + currentDiary.getDiaryID() 
				, null, null, null, null);
		while(oCursor != null && oCursor.moveToNext()) {
			SimpleDateFormat iso8601Format = new SimpleDateFormat(
		            "dd/MM/yyyy HH:mm:ss",Locale.ENGLISH);
		
			currentDiary.setDiaryName(oCursor.getString(oCursor.getColumnIndex(DiaryTable.DIARYNAME)));
			currentDiary.setDiaryTemplate(oCursor.getInt(oCursor.getColumnIndex(DiaryTable.DIARYTEMPLATE)));
            currentDiary.setCloudDiaryID(oCursor.getString(oCursor.getColumnIndex(DiaryTable.CLOUDID)));
			try {
				currentDiary.setDiaryDTCreation(iso8601Format.parse(oCursor.getString(oCursor.getColumnIndex(DiaryTable.DIARYDTCREATION))));
				currentDiary.setDiaryDTModify(iso8601Format.parse(oCursor.getString(oCursor.getColumnIndex(DiaryTable.DIARYDTMODIFY))));
			} catch (ParseException e) {
				Log.e(this.getClass().getCanonicalName(),"error get date diary");
                e.printStackTrace();
				return null;
			}
		}
		if(oCursor!=null) oCursor.close();
		return currentDiary;
	}
	/**
	 * Carica le pagine per il diario passato
	 * 
	 * @param  loadRow indica se caricare o no le roghe della pagina
	 * @param  loadOnePage indica se di caricare solo una pagina del diario
	 * */
	private Diary loadPages(Diary currentDiary,boolean loadRow, boolean loadOnePage,boolean loadImages) {
		Hashtable<Long, Page> mPages = new Hashtable<Long, Page>();
		Cursor oCursor = getmDb().query(PagesTable.TABLE_NAME, PagesTable.COLUMNS, PagesTable.DIARYID + "=" + currentDiary.getDiaryID()
				, null, null, null, null);
		while(oCursor != null && oCursor.moveToNext()) {
			SimpleDateFormat iso8601Format = new SimpleDateFormat(
		            "dd/MM/yyyy HH:mm:ss",Locale.ENGLISH);
			Page page = new Page();
			page.setDiaryID(currentDiary.getDiaryID());
			page.setPageID(oCursor.getLong(oCursor.getColumnIndex(PagesTable.PAGEID)));
			page.setByteImageHW(oCursor.getBlob(oCursor.getColumnIndex(PagesTable.PAGEHANDWRITE)));
			page.setByteImagePreviewPage(oCursor.getBlob(oCursor.getColumnIndex(PagesTable.PAGEPREVIEW)));
			page.setPageAlt(oCursor.getDouble(oCursor.getColumnIndex(PagesTable.PAGEALT)));
			page.setPageLat(oCursor.getDouble(oCursor.getColumnIndex(PagesTable.PAGELAT)));
			page.setPageLong(oCursor.getDouble(oCursor.getColumnIndex(PagesTable.PAGELONG)));
			page.setPageNumber(oCursor.getInt(oCursor.getColumnIndex(PagesTable.PAGENUMBER)));
			page.setPageLoc(oCursor.getString(oCursor.getColumnIndex(PagesTable.PAGELOC)));
			page.setPageOrientation(oCursor.getInt(oCursor.getColumnIndex(PagesTable.PAGEORIENTATION)));
			if(oCursor.getInt(oCursor.getColumnIndex(PagesTable.PAGEBOOKMARK))==1){
				page.setPageBookMark(true);
			} else { 
				page.setPageBookMark(false);
			}
			try {
				page.setPageDTCreation(iso8601Format.parse(oCursor.getString(oCursor.getColumnIndex(PagesTable.PAGEDTCREATION))));
			} catch (ParseException e) {
				e.printStackTrace();
				page.setPageDTCreation(null);
			}
			//Load Data from Rows
			if(loadRow) loadRows(page);
			//Load Data from Picture
			if(loadImages) loadPicture(page,6);
			//Load Data from Picture
			//page=loadPaths(page);
			
			mPages.put(page.getPageID(), page);
			//Forzo l'uscita dal ciclo per caricare solo la prima pagina
			if(loadOnePage) break;
			page=null;
		}
		if(oCursor!=null) oCursor.close();
		currentDiary.setDiaryPages(mPages);
		return currentDiary;
	}
	
	/**
	 * inserisce le rige nella pagina selezionata
	 * 
	 * */
	private void loadRows(Page page) {
		Hashtable<Long, Row> mRows = new Hashtable<Long, Row>();
		Cursor oCursor = getmDb().query(RowsTable.TABLE_NAME, RowsTable.COLUMNS, RowsTable.DIARYID + "=" + page.getDiaryID() +
				" and " + RowsTable.PAGEID + "="+page.getPageID()
				, null, null, null, null);
		while(oCursor != null && oCursor.moveToNext()) {
			Row row = new Row();
			row.setRowID(oCursor.getLong(oCursor.getColumnIndex(RowsTable.ROWID)));
			row.setPageID(oCursor.getLong(oCursor.getColumnIndex(RowsTable.PAGEID)));
			row.setRowNumber(oCursor.getInt(oCursor.getColumnIndex(RowsTable.ROWNUMBER)));
			row.setRowText(oCursor.getString(oCursor.getColumnIndex(RowsTable.ROWTEXT)));
			row.setRowPosX(oCursor.getInt(oCursor.getColumnIndex(RowsTable.ROWPOSX)));
			row.setRowPosY(oCursor.getInt(oCursor.getColumnIndex(RowsTable.ROWPOSY)));
			mRows.put(row.getRowID(), row);
			row=null;
		}
		if(oCursor!=null) oCursor.close();
		page.setPageRows(mRows);
		//return page;
	}
	/**
	 * inserisce le immagini nella pagina selezionata
	 * 
	 * */
	private Page loadPicture(Page page, int calculatedInSamplesize) {
		
		Hashtable<Long, DiaryPicture> mPictures = new Hashtable<Long, DiaryPicture>();
		if(page==null) return null;
		//Aggiungo la HandWrite della pagina alle immagini
		DiaryPicture picture = new DiaryPicture();
		picture.setPageID(page.getPageID());
		picture.setDiaryPictureX(0);
		picture.setDiaryPictureY(0);
		picture.setDiaryPictureW(1000);
		picture.setDiaryPictureH(1000);
		picture.setDiaryPictureRotation(0);
		picture.setDiaryImageURI(null);
		picture.setDiaryHandImage(true);
		picture.setByteImage(page.getByteImageHW());
		mPictures.put(picture.getDiaryPictureID(), picture);
		//Fine Aggiungo la HandWrite della pagina alle immagini
		Cursor oCursor = getmDb().query(PictureTable.TABLE_NAME, PictureTable.COLUMNS, PictureTable.DIARYID + "=" + page.getDiaryID() +
				" and " + PictureTable.PAGEID + "="+page.getPageID()
				, null, null, null, null);
		while(oCursor != null && oCursor.moveToNext()) {
			
			picture = new DiaryPicture();
			picture.setDiaryPictureID(oCursor.getLong(oCursor.getColumnIndex(PictureTable.PICTUREID)));
			picture.setPageID(oCursor.getLong(oCursor.getColumnIndex(PictureTable.PAGEID)));
			picture.setByteImage(oCursor.getBlob(oCursor.getColumnIndex(PictureTable.PICTUREPREVIEW)));
			picture.setDiaryPictureX(oCursor.getInt(oCursor.getColumnIndex(PictureTable.PICTUREX)));
			picture.setDiaryPictureY(oCursor.getInt(oCursor.getColumnIndex(PictureTable.PICTUREY)));
			picture.setDiaryPictureW(oCursor.getInt(oCursor.getColumnIndex(PictureTable.PICTUREW)));
			picture.setDiaryPictureH(oCursor.getInt(oCursor.getColumnIndex(PictureTable.PICTUREH)));
			picture.setDiaryPictureRotation(oCursor.getInt(oCursor.getColumnIndex(PictureTable.PICTUREROTATION)));
			picture.setDiaryImageURI(oCursor.getString(oCursor.getColumnIndex(PictureTable.PICTUREURI)));
			if(oCursor.getInt(oCursor.getColumnIndex(PictureTable.PICTUREHAND))==1){
				picture.setDiaryHandImage(true);
			} else { 
				picture.setDiaryHandImage(false);
			}
			//Potrei applicare i filtri a RUN-TIME
			/**Decode to Save Memory*/
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize=calculatedInSamplesize;
			if(oCursor.getBlob(oCursor.getColumnIndex(PictureTable.PICTUREPREVIEW))!=null)
				picture.setBitmapImage(BitmapFactory.decodeByteArray(oCursor.getBlob(oCursor.getColumnIndex(PictureTable.PICTUREPREVIEW)),0,oCursor.getBlob(oCursor.getColumnIndex(PictureTable.PICTUREPREVIEW)).length,options));
			mPictures.put(picture.getDiaryPictureID(), picture);
		}
		if(oCursor!=null) oCursor.close();
		page.setDiaryImage(mPictures);
		return page;
	}
	
	/**
	 * carica le immagini solo per la pagina corrente
	 * 
	 * 
	 * */
	public Page reloadImageForPage(Page page, int calculatedInSamplesize){
		Page currentPage=null;
		try {
			open();
			currentPage=loadPicture(page,calculatedInSamplesize);
			close();
		} catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(),"Error Load Image");
			close();
		}
		
		return currentPage;
	}
	
/*	*//**
	 * carica le linee solo per la pagina corrente
	 * 
	 * @deprecated
	 * *//*
	public Page reloadPathsForPage(Page page){
		Page currentPage=null;
		try {
			open();
			currentPage=loadPaths(page);
			close();
		} catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(),"Error Load Paths");
			close();
		}
		return currentPage;
	}*/
	/**
	 * ritorna un elenco di riaries presenti in archivio
	 * 
	 * */
	public ArrayList<Diary> listDiaries() {
		ArrayList<Diary> mDiaries = new ArrayList<Diary>();
		try{
			open();
			Cursor oCursor = getmDb().query(DiaryTable.TABLE_NAME, DiaryTable.COLUMNS, null	, null, null, null, null);
			while(oCursor != null && oCursor.moveToNext()) {
				Diary mDiary = new Diary();
				SimpleDateFormat iso8601Format = new SimpleDateFormat(
			            "dd/MM/yyyy HH:mm:ss");
                //yyyy-MMM-dd HH:mm:ss
                //EEE MMM dd HH:mm:ss Z yyyy
				mDiary.setDiaryID(oCursor.getLong(oCursor.getColumnIndex(DiaryTable.DIARYID)));
				mDiary.setDiaryName(oCursor.getString(oCursor.getColumnIndex(DiaryTable.DIARYNAME)));
				mDiary.setDiaryTemplate(oCursor.getInt(oCursor.getColumnIndex(DiaryTable.DIARYTEMPLATE)));
                mDiary.setCloudDiaryID(oCursor.getString(oCursor.getColumnIndex(DiaryTable.CLOUDID)));

                try {
					mDiary.setDiaryDTCreation(iso8601Format.parse(oCursor.getString(oCursor.getColumnIndex(DiaryTable.DIARYDTCREATION))));
					mDiary.setDiaryDTModify(iso8601Format.parse(oCursor.getString(oCursor.getColumnIndex(DiaryTable.DIARYDTMODIFY))));
					mDiary=loadPages(mDiary,false,true,false);
				} catch (ParseException e) {
					Log.e(this.getClass().getCanonicalName(),"set Date Diary Error: "+oCursor.getString(oCursor.getColumnIndex(DiaryTable.DIARYDTCREATION)));
                    e.printStackTrace();
					mDiary.setDiaryDTCreation(null);
					mDiary.setDiaryDTModify(null);
                    mDiary=loadPages(mDiary,false,true,false);
				}
				mDiaries.add(mDiary);
				mDiary=null;
			}
			if(oCursor!=null) oCursor.close();
			close();
		}catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(),"PersistenceException during Listing Diaries");
			e.printStackTrace();
		}catch(SQLException e){
			Log.e(this.getClass().getCanonicalName(),"listDiaries SQLException during listing Diaries");
		}
		return mDiaries;
	}
	/***
	 * Cancella una riga dal diario in base all'id arrivato
	 * @param mPageID 
	 * @param mDiaryID 
	 * 
	 * @param rowID da cancellare
	 * **/
	public boolean deleteRow(double rowID, long mDiaryID, long mPageID) {
		try{
			open();
			getmDb().delete(RowsTable.TABLE_NAME, RowsTable.ROWID+"="+rowID+" AND "+
													RowsTable.DIARYID+"="+mDiaryID+" AND "+
														RowsTable.PAGEID+"="+mPageID, null);
			close();
			Log.v(this.getClass().getCanonicalName(),"deleting row success");
			return true;
		}catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(),"PersistenceException during Listing Diaries");
			e.printStackTrace();
		}catch(SQLException e){
			Log.e(this.getClass().getCanonicalName(),"deleteRow SQLException during listing Diaries");
		}
		return false;
	}

    /***
     * Cancella il diario in base all'id arrivato
     * @param diaryToDelete
     *
     * **/
    public boolean deleteDiary(Diary diaryToDelete) {
        try{
            open();
            getmDb().delete(DiaryTable.TABLE_NAME, DiaryTable.DIARYID + "=" + diaryToDelete.getDiaryID(), null);
            getmDb().delete(PagesTable.TABLE_NAME, PagesTable.DIARYID+"="+diaryToDelete.getDiaryID(), null);
            getmDb().delete(PictureTable.TABLE_NAME, PictureTable.DIARYID+"="+diaryToDelete.getDiaryID(), null);
            getmDb().delete(RowsTable.TABLE_NAME, RowsTable.DIARYID+"="+diaryToDelete.getDiaryID(), null);
            close();
            Log.v(this.getClass().getCanonicalName(),"deleting Diary success");
            return true;
        }catch (PersistenceException e) {
            Log.e(this.getClass().getCanonicalName(),"PersistenceException during deleteDiary");
            e.printStackTrace();
        }catch(SQLException e){
            Log.e(this.getClass().getCanonicalName(),"SQLException during deleteDiary");
        }
        return false;
    }

    /***
     * Cancella una pagina dal diario in base all'id arrivato
     * @param pageToDelete
     *
     * **/
    public boolean deletePage(Page pageToDelete) {
        try{
            open();
            getmDb().delete(PagesTable.TABLE_NAME, PagesTable.PAGEID+"="+pageToDelete.getPageID(), null);
            getmDb().delete(PictureTable.TABLE_NAME, PictureTable.PAGEID+"="+pageToDelete.getPageID(), null);
            getmDb().delete(RowsTable.TABLE_NAME, RowsTable.PAGEID+"="+pageToDelete.getPageID(), null);
            close();
            Log.v(this.getClass().getCanonicalName(),"deleting Page success");
            return true;
        }catch (PersistenceException e) {
            Log.e(this.getClass().getCanonicalName(),"PersistenceException during deletePage");
            e.printStackTrace();
        }catch(SQLException e){
            Log.e(this.getClass().getCanonicalName(),"SQLException during deletePage");
        }
        return false;
    }

	/***
	 * Cancella una riga dal diario in base all'id arrivato
	 * @param page
	 * @param pircureID
	 *
	 * **/
	public boolean deleteImage(Page page, long pircureID) {
		try{
			open();
			getmDb().delete(PictureTable.TABLE_NAME, PictureTable.PICTUREID+"="+pircureID+" AND "+
													PictureTable.PAGEID+"="+page.getPageID(), null);
			close();
			Log.v(this.getClass().getCanonicalName(),"deleting image success");
			return true;
		}catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(),"PersistenceException during Listing Diaries");
			e.printStackTrace();
		}catch(SQLException e){
			Log.e(this.getClass().getCanonicalName(),"deleteImage SQLException during listing Diaries");
		}
		return false;
	}
	
	/**
	 * save during draw
	 * @deprecated
	 * *//*
	public void savePathsPage(HandWritePath path) {
		try {
			open();
			ContentValues mPathTable = new ContentValues();
			HandWritePath mPath = path;
			if(mPath==null) return;
			mPathTable.put(PathsTable.DIARYID, mPath.getDiaryID());
			mPathTable.put(PathsTable.PAGEID, mPath.getPageID());
			mPathTable.put(PathsTable.PATHID, mPath.getmPathID());

			mPathTable.put(PathsTable.PATHX, mPath.getmPoint()[0]);
			mPathTable.put(PathsTable.PATHY, mPath.getmPoint()[1]);
			
			Log.v(this.getClass().getCanonicalName(), "save X:"+mPath.getmPoint()[0]+" save Y: "+mPath.getmPoint()[1]);
					
			mPathTable.put(PathsTable.PATHCOLOR, mPath.getmPaint().getColor());
			mPathTable.put(PathsTable.PATHSTROKEWIDTH, mPath.getmPaint().getStrokeWidth());
			try{
				getmDb().insertOrThrow(PathsTable.TABLE_NAME, null, mPathTable);
				//Log.i(this.getClass().getCanonicalName(), "Add new Path to Page");
			}catch(SQLException e){
				Log.e(this.getClass().getCanonicalName(), "Error Insert new Paths");
			}	
			mPath=null;
			close();
		} catch (PersistenceException e1) {
			e1.printStackTrace();
            Log.e(this.getClass().getCanonicalName(),"Error")
		}
		
	}*/

	/**
	 * Salva la hand write della pagina corrente
	 * */
	public Boolean saveCurrentHandWritePage(Page pageToDump,byte[] imageHW) {
		int iRowEffect=0;

		ContentValues mPageTable = new ContentValues();
		Page mPage =pageToDump;
		mPageTable.put(PagesTable.DIARYID, mPage.getDiaryID());
		mPageTable.put(PagesTable.PAGEID, mPage.getPageID());
		mPageTable.put(PagesTable.PAGEHANDWRITE, imageHW);
		/*mPageTable.put(PagesTable.PAGENUMBER, mPage.getPageNumber());
		mPageTable.put(PagesTable.PAGELONG, mPage.getPageLong());
		mPageTable.put(PagesTable.PAGELAT, mPage.getPageLat());
		mPageTable.put(PagesTable.PAGEALT, mPage.getPageAlt());
		mPageTable.put(PagesTable.PAGEBOOKMARK, (mPage.isPageBookMark() ? 1 : 0) );
		mPageTable.put(PagesTable.PAGEDTCREATION, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(mPage.getPageDTCreation()));
		mPageTable.put(PagesTable.PAGELOC, mPage.getPageLoc());
		mPageTable.put(PagesTable.PAGEORIENTATION, mPage.getPageOrientation());*/
		try{
			open();
			getmDb().insertOrThrow(PagesTable.TABLE_NAME, null, mPageTable);
			Log.i(this.getClass().getCanonicalName(), "Add new Preview to diary");
		}catch(SQLException e){
			//Log.w(this.getClass().getCanonicalName(), "Error inserting page try to update");

			iRowEffect=getmDb().update(PagesTable.TABLE_NAME, mPageTable,
					PagesTable.DIARYID + "=" + mPage.getDiaryID() + " and "+PagesTable.PAGEID+"="+mPage.getPageID(),
					null);
			if(iRowEffect==0) return false;
			//Log.i(this.getClass().getCanonicalName(), " Page to diary");
		} catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(), "Error during open DB");
			return false;
		}finally{
			close();
		}

		return true;
	}
	/**
	 * Salva l'anteprima della pagina corrente
	 * */
	public Boolean saveCurrentPagePreviewPage(Page pageToDump,byte[] imageHW) {
		int iRowEffect=0;

		ContentValues mPageTable = new ContentValues();
		Page mPage =pageToDump;
		mPageTable.put(PagesTable.DIARYID, mPage.getDiaryID());
		mPageTable.put(PagesTable.PAGEID, mPage.getPageID());
		mPageTable.put(PagesTable.PAGEPREVIEW, imageHW);
		/*mPageTable.put(PagesTable.PAGENUMBER, mPage.getPageNumber());
		mPageTable.put(PagesTable.PAGELONG, mPage.getPageLong());
		mPageTable.put(PagesTable.PAGELAT, mPage.getPageLat());
		mPageTable.put(PagesTable.PAGEALT, mPage.getPageAlt());
		mPageTable.put(PagesTable.PAGEBOOKMARK, (mPage.isPageBookMark() ? 1 : 0) );
		mPageTable.put(PagesTable.PAGEDTCREATION, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(mPage.getPageDTCreation()));
		mPageTable.put(PagesTable.PAGELOC, mPage.getPageLoc());
		mPageTable.put(PagesTable.PAGEORIENTATION, mPage.getPageOrientation());*/
		try{
			open();
			getmDb().insertOrThrow(PagesTable.TABLE_NAME, null, mPageTable);
			Log.i(this.getClass().getCanonicalName(), "Add new Preview to diary");
		}catch(SQLException e){
			//Log.w(this.getClass().getCanonicalName(), "Error inserting page try to update");

			iRowEffect=getmDb().update(PagesTable.TABLE_NAME, mPageTable,
					PagesTable.DIARYID + "=" + mPage.getDiaryID() + " and "+PagesTable.PAGEID+"="+mPage.getPageID(),
					null);
			if(iRowEffect==0) return false;
			//Log.i(this.getClass().getCanonicalName(), " Page to diary");
		} catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(), "Error during open DB");
			return false;
		}finally{
			close();
		}

		return true;
	}

	/**
	 * Salva l'anteprima della pagina corrente
	 * */
	public Boolean saveDiaryPicturePage(DiaryPicture pictureToSave,byte[] imageHW) {

		if (pictureToSave == null) return true;
		ContentValues mImageTable = new ContentValues();
		mImageTable.put(PictureTable.PAGEID, pictureToSave.getPageID());
		mImageTable.put(PictureTable.PICTUREID, pictureToSave.getDiaryPictureID());
		//BLOB Immagine
		mImageTable.put(PictureTable.PICTUREPREVIEW, pictureToSave.getByteImage());
		mImageTable.put(PictureTable.PICTUREH, pictureToSave.getDiaryPictureH());
		mImageTable.put(PictureTable.PICTUREW, pictureToSave.getDiaryPictureW());
		mImageTable.put(PictureTable.PICTUREX, pictureToSave.getDiaryPictureX());
		mImageTable.put(PictureTable.PICTUREY, pictureToSave.getDiaryPictureY());
		mImageTable.put(PictureTable.PICTUREROTATION, pictureToSave.getDiaryPictureRotation());
		mImageTable.put(PictureTable.PICTUREURI, pictureToSave.getDiaryImageURI());
		mImageTable.put(PictureTable.PICTUREHAND, (pictureToSave.isDiaryHandImage() ? 1 : 0));
		try {
			open();
			getmDb().update(PictureTable.TABLE_NAME, mImageTable,
					PictureTable.PAGEID + "=" + pictureToSave.getPageID()
							+ " and " +
							PictureTable.PICTUREID + "=" + pictureToSave.getDiaryPictureID(),
					null);
			Log.i(this.getClass().getCanonicalName(), "saveDiaryPicturePage Image");
			close();
		} catch (SQLException e) {
			Log.e(this.getClass().getCanonicalName(), "SQLException saveDiaryPicturePage Image");
			close();
		} catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(), "PersistenceException saveDiaryPicturePage Image");
			close();
		}
		return true;
	}
	/**
	 * Salva la pagina corrente
	 * */
	public Boolean saveCurrentPage(Page pageToDump) {
		
		int iRowEffect=0;
		
		ContentValues mPageTable = new ContentValues();
		Page mPage =pageToDump;
		mPageTable.put(PagesTable.DIARYID, mPage.getDiaryID());
		mPageTable.put(PagesTable.PAGEID, mPage.getPageID());
		if(mPage.getByteImagePreviewPage()!=null) mPageTable.put(PagesTable.PAGEPREVIEW, mPage.getByteImagePreviewPage());
		if(mPage.getByteImageHW()!=null) mPageTable.put(PagesTable.PAGEHANDWRITE, mPage.getByteImageHW());
		mPageTable.put(PagesTable.PAGENUMBER, mPage.getPageNumber());
		mPageTable.put(PagesTable.PAGELONG, mPage.getPageLong());
		mPageTable.put(PagesTable.PAGELAT, mPage.getPageLat());
		mPageTable.put(PagesTable.PAGEALT, mPage.getPageAlt());
		mPageTable.put(PagesTable.PAGEBOOKMARK, (mPage.isPageBookMark() ? 1 : 0) );
		mPageTable.put(PagesTable.PAGEDTCREATION, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(mPage.getPageDTCreation()));
		mPageTable.put(PagesTable.PAGELOC, mPage.getPageLoc());
		mPageTable.put(PagesTable.PAGEORIENTATION, mPage.getPageOrientation());
		try{	
			open();
			getmDb().insertOrThrow(PagesTable.TABLE_NAME, null, mPageTable);	
			Log.i(this.getClass().getCanonicalName(), "Add new Page to diary");
		}catch(SQLException e){
			//Log.w(this.getClass().getCanonicalName(), "Error inserting page try to update");
			
			iRowEffect=getmDb().update(PagesTable.TABLE_NAME, mPageTable, 
							PagesTable.DIARYID + "=" + mPage.getDiaryID() + " and "+PagesTable.PAGEID+"="+mPage.getPageID(), 
								null);
			if(iRowEffect==0) return false;
			//Log.i(this.getClass().getCanonicalName(), " Page to diary");
		} catch (PersistenceException e) {
			Log.e(this.getClass().getCanonicalName(), "Error during open DB");
		}finally{
			//Eseguo sempre se inserisco o updato
			//Inserisco le rows
			savePageRows(getmDb(), mPage);
			//Inserisco le images
			savePageImages(getmDb(), mPage);
			//Inserisco le images
			//savePagePaths(mDB, mPage);
			close();
		}
		
		return true;
	}


	/**
	 * aggiorna le coordinate di un'immagine 
	 * @param
	 * 
	 * 
	 * */
	public boolean updatePicturePosition(DiaryPicture pictureToSave) {
		if(pictureToSave==null) return true;
		ContentValues mImageTable = new ContentValues();
		mImageTable.put(PictureTable.PAGEID, pictureToSave.getPageID());
		mImageTable.put(PictureTable.PICTUREID, pictureToSave.getDiaryPictureID());
		mImageTable.put(PictureTable.PICTUREH, pictureToSave.getDiaryPictureH());
		mImageTable.put(PictureTable.PICTUREW, pictureToSave.getDiaryPictureW());
		mImageTable.put(PictureTable.PICTUREX, pictureToSave.getDiaryPictureX());
		mImageTable.put(PictureTable.PICTUREY, pictureToSave.getDiaryPictureY());
		mImageTable.put(PictureTable.PICTUREROTATION, pictureToSave.getDiaryPictureRotation());
		mImageTable.put(PictureTable.PICTUREURI, pictureToSave.getDiaryImageURI());
		mImageTable.put(PictureTable.PICTUREHAND, (pictureToSave.isDiaryHandImage() ? 1 : 0) );
		try{
			open();
			getmDb().update(PictureTable.TABLE_NAME, mImageTable, 
					PictureTable.PAGEID+"="+pictureToSave.getPageID() 
					+ " and " +
					PictureTable.PICTUREID+"="+pictureToSave.getDiaryPictureID() , 
					null);
			Log.i(this.getClass().getCanonicalName(), "Update Picture Position Image");
            close();
		}catch(SQLException e){
			Log.e(this.getClass().getCanonicalName(), "Update Picture Position Image");
            close();
		} catch (PersistenceException e) {
            Log.e(this.getClass().getCanonicalName(), "PersistenceException Picture Position Image");
            close();
		}
		return true;
	}
    /**
     * Funzione di ricerca
     *
     * */
    public ArrayList<Page> searchText(String search) {
        ArrayList<Page> results = new ArrayList<Page>();
        Cursor oCursor = null;
        try{
            open();
            long lPageID=0l;
            oCursor = getmDb().query(true, DiarySearchTable.TABLE_NAME, DiarySearchTable.COLUMNS, DiarySearchTable.PAGETEXT + " MATCH ?", new String[]{search}, null, null, null, null);

            while(oCursor != null && oCursor.moveToNext()) {
                Page mTmpPage = new Page();

                mTmpPage.setDiaryID(oCursor.getLong(oCursor.getColumnIndex(DiarySearchTable.DIARYID)));
                mTmpPage.setPageID(oCursor.getLong(oCursor.getColumnIndex(DiarySearchTable.PAGEID)));
                if(lPageID!=oCursor.getLong(oCursor.getColumnIndex(DiarySearchTable.PAGEID))){
                    results.add(mTmpPage);
                    lPageID=mTmpPage.getPageID();
                }

            }
            close();
        }catch (PersistenceException e) {
            Log.e(this.getClass().getCanonicalName(),"PersistenceException during search");
            close();
        }catch(SQLException e){
            Log.e(this.getClass().getCanonicalName(),"SQLException during search");
            close();
        }
        return results;
    }
}