package com.glm.utilities.com.glm.utilities.export;

import android.os.Environment;
import android.util.Log;

import com.glm.bean.Diary;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


/**
 * Created by gianluca on 08/08/13.
 */
public class PdfHelper {
    private Diary mDiary;
    private String mPath;
    private File mFilePDF;
    private FileOutputStream mOutputStream;
    private Document mPdfDoc;
    /**
     * converte un diario in un PDF
     *
     * */
    public boolean convertToeBook(Diary diaryToConvert){
        mDiary=diaryToConvert;
        mPdfDoc = new Document();
        mPath=Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePDF = new File(mPath+mDiary.getDiaryID());
        try {
            mOutputStream = new FileOutputStream(mFilePDF);
            PdfWriter.getInstance(mPdfDoc,mOutputStream);
            mPdfDoc.open();

            //TODO Ciclare tutte le pagine
            Paragraph oBody = new Paragraph("");
            //mPdfDoc.newPage();
        } catch (FileNotFoundException e) {
            Log.e(this.getClass().getCanonicalName(), "Error File not Found");
            e.printStackTrace();
        } catch (DocumentException e) {
            Log.e(this.getClass().getCanonicalName(), "Error DocumentException during generation PDF");
            e.printStackTrace();
        }
        return true;
    }
}
