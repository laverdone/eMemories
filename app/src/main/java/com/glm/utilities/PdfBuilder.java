package com.glm.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.glm.bean.Diary;
import com.glm.bean.DiaryPicture;
import com.glm.bean.Page;
import com.glm.bean.Row;

import com.glm.labs.diary.ememories.Const;
import com.lowagie.text.Font;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

public class PdfBuilder {
    /**Documento PDF da generare*/
    private Document mPDFDiaryBuilded;
    /**Diario da esportare in PDF*/
    private Diary mDiaryToBuild;
    /**Context Application*/
    private Context mContext;
    /**Path di esportazione PDF*/
    private String mPathToPdf;
    /**Font PDF*/
    private Typeface oFont;
    /**font copiato a runtime*/
    private Font eMemoriesFont;
    /**Dimensioni pagina*/
    private int mPageWidth;
    private int mPageHeigth;
    /**File PDF export*/
    private File mFilePDF;
    public PdfBuilder(Context context,Diary diaryToBuild,int pageWidth, int pageHeigth){
        mContext=context;
        mDiaryToBuild=diaryToBuild;
        mPageWidth=pageWidth;
        mPageHeigth=pageHeigth;
        mPDFDiaryBuilded = new Document(new Rectangle(0, 0, mPageWidth, mPageHeigth));
        if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
            mPathToPdf= Const.EXTDIR+mContext.getPackageName()+"/"+mDiaryToBuild.getDiaryID() + "/";
        }else{
            mPathToPdf=Const.INTERNALDIR+mContext.getPackageName()+"/"+mDiaryToBuild.getDiaryID() + "/";
        }
    }


    /**
     * Costruisce il PDF per il diario
     *
     * */
    public boolean pdfBuilder(){
        mFilePDF = new File(mPathToPdf, mDiaryToBuild.getDiaryID()+".pdf");
        try {
            FileOutputStream fOut = new FileOutputStream(mFilePDF);
            PdfWriter.getInstance(mPDFDiaryBuilded,fOut);

            mPDFDiaryBuilded.open();
            mPDFDiaryBuilded.setPageSize(new Rectangle(0, 0, mPageWidth, mPageHeigth));
            mPDFDiaryBuilded.getPageSize().setBackgroundColor(harmony.java.awt.Color.getColor("#496885"));

            applyTemplate();

            Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) mDiaryToBuild.getDiaryPages();
            Map<Long, Page> sortedPages = new TreeMap<Long, Page>(mPages);

            for(Page oPpage : sortedPages.values()){
                Paragraph oPagePDF = new Paragraph();
                oPagePDF.setFont(eMemoriesFont);

                addTextToPage(oPagePDF,oPpage);

                //addImagesToPage(oPagePDF,mPage);

                mPDFDiaryBuilded.add(oPagePDF);
                mPDFDiaryBuilded.newPage();

            }
            /*Enumeration<Long> keys = mDiaryToBuild.getDiaryPages().keys();
            while( keys.hasMoreElements() ) {
                Page mPage = mDiaryToBuild.getDiaryPages().get(keys.nextElement());

                Paragraph oPagePDF = new Paragraph();
                oPagePDF.setFont(eMemoriesFont);

                addTextToPage(oPagePDF,mPage);

                //addImagesToPage(oPagePDF,mPage);

                mPDFDiaryBuilded.add(oPagePDF);
                mPDFDiaryBuilded.newPage();
            }*/


            mPDFDiaryBuilded.close();

            fOut.flush();
            fOut.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(this.getClass().getCanonicalName(),"FileNotFoundException in exportation PDF");
            return false;
        } catch (DocumentException e) {
            e.printStackTrace();
            Log.e(this.getClass().getCanonicalName(),"DocumentException in exportation PDF");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(this.getClass().getCanonicalName(),"IOException in exportation PDF");
            return false;
        }

        return true;
    }

    /**
     * Aggiunge le immagini alla pagina pdf
     * */
    private void addImagesToPage(Paragraph pagePDF, Page page) {
        if(page.getDiaryImage()==null) return;
        Enumeration<Long> keys = page.getDiaryImage().keys();
        while( keys.hasMoreElements() ) {
            DiaryPicture oPicture = page.getDiaryImage().get(keys.nextElement());


            Image img = null;
            try {
                img = Image.getInstance(oPicture.getDiaryImageURI());
                /*Ridimensiono l' immagine*/
                img.scaleAbsolute(oPicture.getDiaryPictureH()/Const.SAMPLESIZEIMAGE, oPicture.getDiaryPictureW()/Const.SAMPLESIZEIMAGE);
                img.setAbsolutePosition(oPicture.getDiaryPictureX(),oPicture.getDiaryPictureY());
                pagePDF.add(img);


            } catch (BadElementException e) {
                e.printStackTrace();
                Log.e(this.getClass().getCanonicalName(),"BadElementException inserting image to PDF");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(this.getClass().getCanonicalName(),"IOException inserting image to PDF");
            }
        }
        //Add HandWrite if Present
        Image img = null;
        try {
            File otmp = new File(mPathToPdf+"/Pictures/h"+page.getPageID()+Const.PAGE_PREVIEW_EXT);
            if(otmp.exists()){
                img = Image.getInstance(mPathToPdf+"/Pictures/h"+page.getPageID()+Const.PAGE_PREVIEW_EXT);
                /*Ridimensiono l' immagine*/
                img.scaleAbsolute(mPageWidth,mPageHeigth);
                img.setAbsolutePosition(0,0);
                pagePDF.add(img);
            }
            otmp=null;
        } catch (BadElementException e) {
            e.printStackTrace();
            Log.e(this.getClass().getCanonicalName(),"BadElementException inserting image to PDF");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(this.getClass().getCanonicalName(),"IOException inserting image to PDF");
        }
    }

    /**
     * Aggiunge il testo alla pagina PDF
     *
     *
     * */
    private void addTextToPage(Paragraph pagePDF, Page page) {
        if(page.getPageRows()==null) return;

        Enumeration<Long> keys = page.getPageRows().keys();
        while( keys.hasMoreElements() ) {
            Row oRow = page.getPageRows().get(keys.nextElement());
            Image img = null;
            try {
                img = Image.getInstance(mPathToPdf + "/Pictures/" + page.getPageID() + Const.CAMERA_PREVIEW_EXT);
                    /*Ridimensiono l' immagine*/
                img.scaleAbsolute(mPageWidth,mPageHeigth);
                img.setAbsolutePosition(0,0);
                pagePDF.add(img);
                //pagePDF.add(oRow.getRowText()+"\n");
            } catch (BadElementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * Applica il template selezionato
     *
     * TODO utilizzare Drawable per creare la pagina con un gradiente ed essere più leggera
     * */
    private void applyTemplate() {
        if(mDiaryToBuild==null) return;

        Log.v(this.getClass().getCanonicalName(),"Loading font...");
        oFont        = Typeface.createFromAsset(mContext.getAssets(), "template/" + mDiaryToBuild.getDiaryTemplate() + "/font.ttf");
        try {
            InputStream in = mContext.getAssets().open("template/" + mDiaryToBuild.getDiaryTemplate() + "/font.ttf");
            OutputStream out = null;
            File outFile = new File(mPathToPdf+"eMemories.ttf");


            out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;


            FontFactory.register(mPathToPdf+"eMemories.ttf", "eMemories"+mDiaryToBuild.getDiaryTemplate());
            eMemoriesFont = FontFactory.getFont("eMemories"+mDiaryToBuild.getDiaryTemplate(), BaseFont.IDENTITY_H);
            eMemoriesFont.setColor(harmony.java.awt.Color.black);
            //if(mDiaryToBuild.getDiaryTemplate()==3){
            //    eMemoriesFont.setColor(harmony.java.awt.Color.white);
            //}
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(this.getClass().getCanonicalName(),"Error coping Font to "+mPathToPdf);
            eMemoriesFont = new Font(Font.TIMES_ROMAN);
            eMemoriesFont.setColor(harmony.java.awt.Color.black);
        }


    }

    /**
     * Ritorna il file PDF Prodotto
     *
     * */
    public File getPdfFile(){
        return mFilePDF;
    }


    /**


    ################ OLD METOD###################

    **/

	private static Font catfont = new Font(Font.TIMES_ROMAN, 18, Font.BOLD);

	private static Font smallfont = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);


	/**
	 * Questo metodo esegue la costruzione della pagina pdf
	 * @author coluzza
	 * @param document
	 * @throws DocumentException
	 */
	public void execute(int idNote, SparseArray<Page> pages, String noteName, String noteDesc) {

		try {

			File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/AppGianoNotes/" + idNote + "/PdfNotes");

			if(!dir.exists()) {
				dir.mkdirs();
			}

			/*In questo modo ho sempre un solo file pdf*/
			File[] files = dir.listFiles();

			if(files != null) {

				for(File f : files) {

					f.delete();

					break;
				}
			}

			/*Creo il documento*/
			Document document = new Document();

			/*Prendo un' istanza del PdfWriter*/
			PdfWriter.getInstance(document, new FileOutputStream(dir.getPath() + "/myAppNotesPdf_" + idNote + ".pdf"));

			/*Apro il documento*/
			document.open();

			/*Aggiundo i metadati*/
			addMetaData(document);

			addFirstPage(document, noteName, noteDesc);

			/*Scorrendo lo sparseArray creo una pagina in pdf per ogni pagina del note*/
			for(int i = 1; i <= pages.size(); i++) {

				Page p = pages.get(i);

				document.newPage();

				/*Aggiungo il title e la location*/
				/*addTitlePage(document, p.getTitle(), p.getLocation());

				Aggiungo l' immagine
				String image = p.getImage();
				if(image != null)
					addImage(document, image);

				Aggiungo il contenuto
				String content = p.getText();
				if(content != null)
					addContent(document, content, p.getNumber());*/

			}
			document.close();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	private void addFirstPage(Document document, String noteName, String noteDesc) throws DocumentException {

		Paragraph preface = new Paragraph();

		preface.add(new Paragraph(noteName, catfont));

		addEmptyLine(preface, 1);

		preface.add(new Paragraph(noteDesc,
				smallfont));

		preface.setAlignment(Element.ALIGN_CENTER);

		document.add(preface);

	}


	public static void addContent(Document document, String content, int numberPage) throws DocumentException {

		Paragraph para = new Paragraph();

		para.add(new Paragraph("Text of page " + numberPage));

		addEmptyLine(para, 1);

		para.add(new Paragraph(content));

		document.add(para);
	}

	public static void addTitlePage(Document document, String title, String location)
			throws DocumentException {
		Paragraph preface = new Paragraph();

		// Will create: Report generated by: _name, _date
		preface.add(new Paragraph("Report generated by: " + System.getProperty("user.name") + ", " + new Date(),
				smallfont));

		// Lets write a big header
		if(!(title.equals("")) && !(location.equals(""))) {

			preface.add(new Paragraph(title + "," + location, catfont));
			addEmptyLine(preface, 1);

		} else if (!(title.equals("")) && (location.equals(""))) {

			preface.add(new Paragraph(title, catfont));
			addEmptyLine(preface, 1);

		} else if ((title.equals("")) && !(location.equals(""))) {

			preface.add(new Paragraph(location, catfont));
			addEmptyLine(preface, 1);

		}


		preface.setAlignment(Element.ALIGN_LEFT);

		addEmptyLine(preface, 2);

		document.add(preface);
	}


	/**
	 * This method create an empty line
	 * @author coluzza
	 * @param paragraph
	 * @param number
	 */
	public static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}


	/**
	 * iText allows to add metadata to the PDF which can be viewed in your Adobe
	 * Reader under File -> Properties
	 * @param document
	 * @author coluzza
	 */
	public static void addMetaData(Document document) {
		document.addTitle("Creating PDF");
		document.addSubject("Using DroidText");
		document.addKeywords("Java, PDF, iText");
	}

	public static void addImage(Document document, String image) throws DocumentException {

		try {

			Paragraph pImg = new Paragraph();

			/*Creo l' immagine*/
			Image img = Image.getInstance(new URL(image));

			/*Ridimensiono l' immagine*/
			img.scaleAbsolute(200f, 150f);

			pImg.add(img);

			addEmptyLine(pImg, 2);

			document.add(pImg);

		} catch (BadElementException e) {

			//Log.v(TAG, "BadElementException");
			e.printStackTrace();

		} catch (MalformedURLException e) {

			//Log.v(TAG, "MalformedURLException");
			e.printStackTrace();

		} catch (IOException e) {

			//Log.v(TAG, "IOException");
			e.printStackTrace();

		}
	}
}
