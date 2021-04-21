package com.glm.db;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.glm.bean.Diary;
import com.glm.bean.DiaryPicture;
import com.glm.bean.Page;
import com.glm.labs.diary.ememories.Const;

//import com.glm.utilities.SynchHelper;

public class DiaryRepositoryHelper {
	private static Repository mRepository;
	private static Context mContext;
    private static SharedPreferences mPrefs;
    private static boolean isCloudEnebled=false;
	/**
	 * Salva il Diario passato completo
	 * 
	 * */
	public static boolean dumpDiary(Context context, Diary diaryToDump){
	    boolean result=false;
        mContext=context;
        mPrefs = mContext.getSharedPreferences(Const.CLOUD_COLLECTION, Context.MODE_PRIVATE);
       /* isCloudEnebled=mPrefs.getBoolean("isCloudEnabled",false);



        if(isCloudEnebled) {
            //TODO Synch To Cloud
            SynchHelper mCloud = new SynchHelper(mContext);
            mCloud.synchDiaryToCloud(diaryToDump);
        }*/
        Log.v(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"Initial Diary Dump");
        mRepository = new Repository(mContext);
        result=mRepository.saveDiary(diaryToDump);
		return result;
	}
	/**
	 * Popola un diario esistente
	 * 
	 * *//*
	public static Diary populateDiary(Context context, int currentDiaryID){
		mContext=context;
		return DiaryHelper.factoryDiaryBuilder(mContext, -1, currentDiaryID);
	}*/
	/**
	 * Ritorna la pagina corrente con immagini e path
	 *
	public static Page populateCurrentPage(Context context, Page currentPage,DiaryPageView page){
		return DiaryHelper.factoryCurrentPageBuilder(null, context, currentPage, page);
	}*/
	
	public static ArrayList<Diary> getDiaries(Context context){
		
		mContext=context;
		mRepository = new Repository(mContext);
		Log.v(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"List of Diaries");
		
		return mRepository.listDiaries();
	}
	/**
	 * Cancello una riga dal diario
	 * @param mPageID 
	 * @param mDiaryID 
	 * */
	public static boolean removeRow(Context context, long rowID, long mDiaryID, long mPageID) {
		mContext=context;
		mRepository = new Repository(mContext);
		return mRepository.deleteRow(rowID,mDiaryID, mPageID);
	}
	
	/**
	 * Salva la Pagina Corrente
     *
	 * */
	public static Boolean dumpPage(Context context, Page pageToDump) {
		mContext=context;
		mRepository = new Repository(mContext);
		Log.v(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"Initial Page Dump");
		return mRepository.saveCurrentPage(pageToDump);
	}
	/**
	 * Salva la HandWrite della Pagina Corrente
	 *
	 * */
	public static Boolean dumpHandWritePage(Context context, Page pageToDump,byte[] imageHW) {
		mContext=context;
		mRepository = new Repository(mContext);
		Log.v(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"dumpHandWritePage");
		return mRepository.saveCurrentHandWritePage(pageToDump,imageHW);
	}

	/**
	 * Salva l'anteprima della Pagina Corrente
	 *
	 * */
	public static Boolean dumpPagePreviewPage(Context context, Page pageToDump, byte[] imageHW) {
		mContext=context;
		mRepository = new Repository(mContext);
		Log.v(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"dumpPagePreviewPage");
		return mRepository.saveCurrentPagePreviewPage(pageToDump,imageHW);
	}

	/**
	 * Salva la HandWrite della Pagina Corrente
	 *
	 * */
	public static Boolean dumpPageImage(Context context, DiaryPicture oPicture,byte[] imageHW) {
		mContext=context;
		mRepository = new Repository(mContext);
		Log.v(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"################dumpPageImage###########");
		return mRepository.saveDiaryPicturePage(oPicture,imageHW);
	}
	/**
	 * Cancella un'immagine dal DB
	 * 
	 * */
	public static Boolean deleteImage(Context context, Page page,
			long pictureID) {
		mContext=context;
		mRepository = new Repository(mContext);
		Log.v(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"Initial Page Dump");
		return mRepository.deleteImage(page,pictureID);
	}
    /**
     * Aggiorna un'immagine dal DB
     *
     * */
	public static Boolean updatePicturePosition(Context context, DiaryPicture pictureToSave) {
		mContext=context;
		mRepository = new Repository(mContext);
		Log.v(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"update Picture Position");
		return mRepository.updatePicturePosition(pictureToSave);
	}

    /**
     * cancella un diario e tutte le foto e handwrite
     * */
    public static Boolean deleteDiary(Context context, final Diary diary) {
        mContext=context;
        mRepository = new Repository(mContext);
        Log.v(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"Remove diary");
        boolean bDelete= mRepository.deleteDiary(diary);

        //CANCELLO ANCHE LA CARTELLA E IL SUO CONTENUTO
        File fFolderDiaryToDelete = new File(Environment.getExternalStorageDirectory().getPath() + "/"+mContext.getPackageName()+"/"+diary.getDiaryID());
        File fFolderToDelete = new File(Environment.getExternalStorageDirectory().getPath() + "/"+mContext.getPackageName()+"/"+diary.getDiaryID() + "/Pictures/");
        if(fFolderToDelete.isDirectory()){
            for(File removeFile : fFolderToDelete.listFiles()){
                if(!removeFile.delete()){
                    Log.e(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"Error removing file: "+removeFile.getAbsoluteFile());
                }
            }
            if(!fFolderToDelete.delete()){
                Log.e(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"Error removing file: "+fFolderToDelete.getAbsoluteFile());
            }
            if(!fFolderDiaryToDelete.delete()){
                Log.e(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"Error removing file: "+fFolderDiaryToDelete.getAbsoluteFile());
            }
        }
        /*mPrefs = mContext.getSharedPreferences(Const.CLOUD_COLLECTION, Context.MODE_PRIVATE);
        isCloudEnebled=mPrefs.getBoolean("isCloudEnabled",false);

        if(isCloudEnebled){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Cancellazione del Documento sul Cloud
                    SynchHelper mCloud = new SynchHelper(mContext);
                    mCloud.deleteDocFromCloud(diary);
                }
            }).start();

        }*/
        return bDelete;
    }

    /**
     * funzione di ricerca
     *
     *
     * */
    public static ArrayList<Page> searchText(Context context, String search) {
        mContext=context;
        mRepository = new Repository(mContext);
        return mRepository.searchText(search);
    }


}
