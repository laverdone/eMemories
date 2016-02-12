/*
package com.glm.utilities;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.glm.bean.DiaryPicture;
import com.glm.ememories.ODTConst;
import com.glm.bean.Diary;
import com.glm.bean.Page;
import com.glm.bean.Row;
import com.glm.ememories.Const;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

*/
/**
 * Created by gianluca on 09/05/14.
 *
 * Classe che si occupa dell'esportazione del diario in
 * formato ODF, da usare poi nel cloud
 *
 *//*

public class ODFHelper {
    // Create a text document from a standard template (empty documents within the JAR)
    //TextDocument mODFTextDocument = null;
    */
/**contiene il path di esportazione*//*

    private String mPathToExprt=null;
    private Context mContext=null;
    private Diary mDiaryToExport=null;
    private Map<Long, Page> mSortedPages = null;
    private Map<Long, Row> mSortedRows = null;
    private ZipOutputStream mOut=null;
    */
/**
     * Costruttore
     * *//*

    public ODFHelper(Context context, Diary diary){
        try {
            mDiaryToExport=diary;
            mContext=context;
            mSortedPages = new TreeMap<Long, Page>(mDiaryToExport.getDiaryPages());

            if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
                mPathToExprt= Const.EXTDIR+mContext.getPackageName()+"/"+mDiaryToExport.getDiaryID() + "/";
            }else{
                mPathToExprt=Const.INTERNALDIR+mContext.getPackageName()+"/"+mDiaryToExport.getDiaryID() + "/";
            }

            //mODFTextDocument.addParagraph("Hello Diary");

            //mODFTextDocument.save(mPathToExprt+mDiaryToExport.getDiaryID()+".odt");

        } catch (Exception e) {
            Log.e(this.getClass().getCanonicalName(), "Error creating Document ODF: " + e);
        }
    }

    */
/**
     * Avvia l'esportazione del diario in formato
     * ODF per la sincronizzazione con il Cloud
     *
     * *//*

    public boolean startExportToODF(){
        if(mContext==null || mDiaryToExport==null) return false;
        try{
            boolean isFirstPage=true;
        	FileOutputStream dest = new FileOutputStream(mPathToExprt+mDiaryToExport.getDiaryID()+".odt");

            mOut = new ZipOutputStream(new BufferedOutputStream(dest));
			
			*/
/**Configurations2/accelerator/current.xml*//*

			ZipEntry entry = new ZipEntry("Configurations2/accelerator/current.xml");
            mOut.putNextEntry(entry);
            mOut.write("".getBytes());
			
			*/
/** META-INF/manifest.xml*//*

			entry = new ZipEntry("META-INF/manifest.xml");
            mOut.putNextEntry(entry);
            mOut.write(ODTConst.ODT_MANIFEST.toString().getBytes());
			
			*/
/** meta.xml*//*

			entry = new ZipEntry("meta.xml");
            mOut.putNextEntry(entry);
            mOut.write(ODTConst.ODT_META.toString().getBytes());
			
			*/
/** settings.xml*//*

			entry = new ZipEntry("settings.xml");
            mOut.putNextEntry(entry);
            mOut.write(ODTConst.ODT_SETTINGS.toString().getBytes());
			
			*/
/** settings.xml contiene la sezione page*//*

			entry = new ZipEntry("styles.xml");
            mOut.putNextEntry(entry);
            mOut.write(ODTConst.ODT_STYLES.toString().getBytes());
			
			
			*/
/**Content*//*

			entry = new ZipEntry("content.xml");
            mOut.putNextEntry(entry);
            mOut.write(ODTConst.ODT_START.toString().getBytes());

            for(Page oPage : mSortedPages.values()){
                addRowAndImage(isFirstPage, oPage);
                isFirstPage=false;
            }
            */
/**TODO LOOP FOR PAGE AND ROW*//*

            //mOut.write("<text:p text:style-name=\"P1\">HELLO WORK</text:p>".getBytes());
			//out.write("<text:p text:style-name=\"P6\">HELLO WORK 1</text:p>".getBytes());

			mOut.write(ODTConst.ODT_END.toString().getBytes());

            */
/**insert Image in ZIP *//*

            for(Page oPage : mSortedPages.values()){
                addImage(oPage);
            }

			*/
/**mimetype*//*

			entry = new ZipEntry("mimetype");
            mOut.putNextEntry(entry);
            mOut.write(ODTConst.ODT_MIMETYPE.toString().getBytes());
            mOut.close();
            //mODFTextDocument.setLocale(Locale.getDefault());
            // Append text to the end of the document.
            //mODFTextDocument.addText("This is my very first ODF test");
            //mODFTextDocument.newParagraph("THIS IS NEW PARAGRAPH");

            // Save document
            //mODFTextDocument.save(mPathToExprt+mDiaryToExport.getDiaryID()+".odt");
        } catch (Exception e) {
            Log.e(this.getClass().getCanonicalName(), "Error creating Document ODF: " + e);
            return false;
        }


        return true;
    }


    */
/**
     * Aggiunge una riga al documento ODF e le immagini solo come riferimento
     *
     * *//*

    private void addRowAndImage(boolean firstPage, Page page){
        boolean isFirstRow=true;
        String _ImageODT="";
        if(page.getPageRows()!=null) {
            mSortedRows = new TreeMap<Long, Row>(page.getPageRows());
            for (Row oRow : mSortedRows.values()) {
                try {
                    if (!firstPage && isFirstRow)
                        mOut.write(ODTConst.ODT_TEXT_PAGE_BREAK_BEFORE.replaceAll("%TEXT_HERE%", oRow.getRowText()).getBytes());
                    else
                        mOut.write(ODTConst.ODT_TEXT_NORMAL.replaceAll("%TEXT_HERE%", oRow.getRowText()).getBytes());
                } catch (IOException e) {
                    Log.e(this.getClass().getCanonicalName(), "IO Error during generation ODT Row");
                }
                isFirstRow = false;
            }
        }
        if(page.getDiaryImage()!=null) {

            TreeMap<Long, DiaryPicture> sortedImages = new TreeMap<Long, DiaryPicture>(page.getDiaryImage());

            for (DiaryPicture oPicture : sortedImages.values()) {
                try {

                    _ImageODT = ODTConst.ODT_IMAGE.replaceAll("%N_IMAGE%", String.valueOf(oPicture.getDiaryPictureID()));
                    _ImageODT = _ImageODT.replaceAll("%IMAGE_X%", String.valueOf(oPicture.getDiaryPictureX() * ODTConst.ODT_PIXEL_TO_CM_FACTOR));
                    _ImageODT = _ImageODT.replaceAll("%IMAGE_Y%", String.valueOf(oPicture.getDiaryPictureY() * ODTConst.ODT_PIXEL_TO_CM_FACTOR));
                    _ImageODT = _ImageODT.replaceAll("%IMAGE_W%", String.valueOf(oPicture.getDiaryPictureW() * ODTConst.ODT_PIXEL_TO_CM_FACTOR));
                    _ImageODT = _ImageODT.replaceAll("%IMAGE_H%", String.valueOf(oPicture.getDiaryPictureH() * ODTConst.ODT_PIXEL_TO_CM_FACTOR));
                    _ImageODT = _ImageODT.replaceAll("%IMAGE%", String.valueOf(oPicture.getDiaryPictureID()) + ".png");

                    mOut.write(_ImageODT.getBytes());
                } catch (IOException e) {
                    Log.e(this.getClass().getCanonicalName(), "IO Error during generation ODT Image");
                }

            }
        }
    }

    */
/**
     * Aggiunge i byte dell'immagini al documento ODF
     *
     * *//*

    private void addImage(Page page){
        FileInputStream in;
        int BUFFER=2048;
        BufferedInputStream origin;
        byte data[] = new byte[BUFFER];
        if(page.getDiaryImage()!=null) {
            TreeMap<Long, DiaryPicture> sortedImages = new TreeMap<Long, DiaryPicture>(page.getDiaryImage());

            for (DiaryPicture oPicture : sortedImages.values()) {
                try {
                    in = new FileInputStream(oPicture.getDiaryImageURI());
                    origin = new BufferedInputStream(in, BUFFER);
                    ZipEntry entry = new ZipEntry("Pictures/" + String.valueOf(oPicture.getDiaryPictureID()) + ".png");
                    mOut.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        mOut.write(data, 0, count);
                    }
                    origin.close();
                } catch (IOException e) {
                    Log.e(this.getClass().getCanonicalName(), "IO Error during generation ODT Image");
                }
            }
        }
    }

}
*/
