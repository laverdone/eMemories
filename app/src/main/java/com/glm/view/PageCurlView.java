package com.glm.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.Shader;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.VelocityTracker;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.glm.asyncTasks.DeletePictureAsyncTask;
import com.glm.asyncTasks.RowAsyncTask;
import com.glm.bean.Diary;
import com.glm.bean.DiaryPicture;
import com.glm.bean.HandWritePath;
import com.glm.bean.Page;
import com.glm.bean.Row;
import com.glm.db.DiaryRepositoryHelper;

import com.glm.labs.diary.ememories.Const;
import com.glm.labs.diary.ememories.R;
import com.glm.labs.diary.ememories.WriteActivity;
import com.glm.utilities.BitmapFactoryHelper;
import com.glm.utilities.InputConnetionEM;

/**
 *
 * @author Moritz 'Moss' Wundke (b.thax.dcg@gmail.com)
 *
 */
public class PageCurlView extends TextureView implements TextureView.SurfaceTextureListener,
        GestureDetector.OnGestureListener {



    private boolean mDeleteMode=false;
    private int iUndo=1;
    /**TAG*/
    private final String TAG=this.getClass().getCanonicalName();
    private Paint mPaint;
    private ArrayList<HandWritePath> _graphics = new ArrayList<HandWritePath>();
    private HandWritePath path;
    private Hashtable<Long, Row> mRows;
    private String mFont="fonts/";
    private Typeface oFont;
    private Resources res = getResources();
    float fontSize = res.getDimension(R.dimen.font_size);
    float fontSizeDate = res.getDimension(R.dimen.font_size_date);
    private volatile boolean mWritable=false;
    private boolean mThreadDrawing=true;
    private SurfaceHolder mSurfaceHolder;
    /**thread per catturare il long click solo sull'evento DOWN*/
    //private LongTouchThread oLongTouchThread = new LongTouchThread();
    private GestureDetector mGestureDetector;
    /**finger size */
    private boolean mAutoFingerSize=false;
    /**usati per disegnare il background senza flikering*/
    private Canvas mCanvas=null;
    /**Bitmap per la pagina di background*/
    private Bitmap mBitmapForPage = null;
    private Matrix identityMatrix=null;
    private Canvas mCanvasOldPath=null;

    /**angolo di rotazione */
    private float mAngle=0f;

    /**Bitmap per canvas testo*/
    private Bitmap mTextBitmap = null;
    /**contiene le immagini della pagina se presenti*/
    private Hashtable<Long, DiaryPicture> mImages=null;
    private Map<Long, DiaryPicture> sortedImages=null;
    /**identifica se ho cliccato su un'immagine della pagina*/
    private boolean isPirtureClick=false;
    /**identifica se ho long cliccato su un'immagine della pagina*/
    private boolean isPirtureLongClick=false;
    /**identifica se durante lo spostamento ho spostato un'immagine nell'area di cancellazione*/
    private boolean isPirtureDelete=false;
    private DiaryPicture mPictureToMove=null;
    private Canvas mCanvasForMovePicture = null;
    private Bitmap mPictureBitmapForCanvas = null;
    private Matrix MatrixPictureCanvas=null;

    /**Canva nella quale disegno le path e poi salvo come immagine*/
    private Canvas mCanvasPath=null;
    private Bitmap bmpPath=null;

    private DrawingThread _thread;

    /**contiene il bitmap della pagia corrente*/
    //private Bitmap mBmpPage;
    /**Pagina corrente che edito*/
    private Page mCurrentPage;
    private int mCurrentColor=Color.BLUE;
    private float mCurrentStrokeWidth=6;
    private Context mContext;
    private int iWidth=0;
    private int iHeight=0;
    private Diary mCurrentDiary;
    private boolean bScaled=true;
    private int mTextColor=Color.BLACK;
    /**identifica se disegnare il background e le vecchie linee per velocizzare la scrittura*/
    private boolean mDrawBackground=true;
    private String sPathImage;

    /**
     *
     * keyboard variable
     *
     * */
    /**identifica la riga corrente*/
    public int iCurrentRow=0;
    /**Identifica il testo di una riga*/
    private ArrayList<String> aRows = new ArrayList<String>();
    /**identifica l'oggetto Paint che contiene una riga*/
    private ArrayList<Paint> aPaint = new ArrayList<Paint>();
    /**identifica se una riga ha una posizione decisa dall'utente NOT USED*/
    private ArrayList<Boolean> aRowCustomPos = new ArrayList<Boolean>();

    /**prende la dimensione della view solo una volta*/
    private boolean mOneShot=true;

    private float newFingerDistance=0;
    private float oldFingerDistance=0;
    private int mImageZoom=1;

    private long touchTime =0;

    /**identificano le coordinate precedenti per applicare il quadTo*/
    private float mPrevX=0f;
    private float mPrevY=0f;

    //Gestione dello scroll
    private int mSoftKeyHeight=0;
    private int mSystemHeight=0;
    //0=top 1=top-keyboard
    private int iLastOffset=0;

    //Edit della riga
    private EditText oRowEdit =null;










    // Debug text paint stuff
    private Paint mTextPaint;
    private TextPaint mTextPaintShadow;

    /** Px / Draw call */
    private int mCurlSpeed;

    /** Fixed update time used to create a smooth curl animation */
    private int mUpdateRate;

    /** The initial offset for x and y axis movements */
    private int mInitialEdgeOffset;

    /** The mode we will use */
    private int mCurlMode;

    /** Simple curl mode. Curl target will move only in one axis. */
    public static final int CURLMODE_SIMPLE = 0;

    /** Dynamic curl mode. Curl target will move on both X and Y axis. */
    public static final int CURLMODE_DYNAMIC = 1;

    /** Enable/Disable debug mode */
    private boolean bEnableDebugMode = false;

    /** Handler used to auto flip time based */
    private FlipAnimationHandler mAnimationHandler;

    /** Maximum radius a page can be flipped, by default it's the width of the view */
    private float mFlipRadius;

    /** Point used to move */
    private Vector2D mMovement;

    /** The finger position */
    private Vector2D mFinger;

    /** Movement point form the last frame */
    private Vector2D mOldMovement;

    /** Page curl edge */
    private Paint mCurlEdgePaint;

    /** Our points used to define the current clipping paths in our draw call */
    private Vector2D mA, mB, mC, mD, mE, mF, mOldF, mOrigin;

    /** Left and top offset to be applied when drawing */
    private int mCurrentLeft, mCurrentTop;

    /** If false no draw call has been done */
    private boolean bViewDrawn;

    /** Defines the flip direction that is currently considered */
    private boolean bFlipRight;

    /** If TRUE we are currently auto-flipping */
    private boolean bFlipping;

    /** TRUE if the user moves the pages */
    private boolean bUserMoves;

    /** Used to control touch input blocking */
    private boolean bBlockTouchInput = false;

    /** Enable input after the next draw event */
    private boolean bEnableInputAfterDraw = false;

    /** LAGACY The current foreground */
    private Bitmap mForeground;

    /** LAGACY The current background */
    private Bitmap mBackground;

    /** LAGACY List of pages, this is just temporal */
    private ArrayList<Bitmap> mPages;

    /** LAGACY Current selected page */
    private int mIndex = 0;

    public void init(Diary diary, Page page){

        mCurrentDiary=diary;
        mCurrentPage=page;
        if(mCurrentPage==null) return;
        //if(_thread==null) _thread = new DrawingThread(this, this);
        //setDrawingCacheEnabled(true);

        //Imposto la vecchia handwrite
        _graphics= new ArrayList<HandWritePath>();//mCurrentPage.getmPath();
        mRows=mCurrentPage.getPageRows();

        //setWillNotDraw(false);
        //setVisibility(View.VISIBLE);
        //setZOrderOnTop(true);
        mPictureToMove=null;
        setPage(mCurrentPage);
        newPage();
        /**refresh page*/
        drawTextOnSurface();
        requestFocus();


    }

    VelocityTracker velocity = VelocityTracker.obtain();

    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(this.getClass().getCanonicalName(), "ACTION_DOWN: "+event+"keyboard: "+event.getKeyCode());
        if(event.getKeyCode()==KeyEvent.KEYCODE_DEL){
            deleteCharRowString(iCurrentRow);
            drawTextOnSurface();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if(keyCode==KeyEvent.KEYCODE_DEL){
                deleteCharRowString(iCurrentRow);

            }else if(keyCode==KeyEvent.KEYCODE_ENTER){
                iCurrentRow++;
                init(iCurrentRow);
            }else{
                char pressedKey = (char) event.getUnicodeChar();
                setRowString(iCurrentRow, pressedKey);
                //Forzo il riinsetimento dell'ultima riga
                //if(!mInsertLastRow) mInsertLastRow=true;
                //sPageText+=String.valueOf(pressedKey);
                //Log.d(this.getClass().getCanonicalName(), "ACTION_DOWN: "+pressedKey+"keyboard: "+keyCode);
            }

            drawTextOnSurface();
            //iPosX++;
            //Log.d(this.getClass().getCanonicalName(), "ACTION_DOWN keyboard: "+keyCode);
            return false;
        }
        //Log.d(this.getClass().getCanonicalName(), "ACTION_DOWN keyboard: "+keyCode);
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
    }



    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        //TODO CONVERT TO PAGE CURL
        InputConnetionEM connection = new InputConnetionEM(this, false);
        //TODO connection.setView(this);

        outAttrs.imeOptions = EditorInfo.IME_MASK_ACTION;

        return connection;
    }

    public void drawTextOnSurface() {


        if(mTextBitmap==null) return;
        if(mWritable) return;
        Canvas c=lockCanvas();
        if(c==null) {
            Log.v(this.getClass().getCanonicalName(),"Canvas NULL on drawTextOnSurface");
            return;
        }



        Canvas mTextCanvas = new Canvas(mTextBitmap);

        synchronized (c) {

            if(mTextBitmap!=null)mTextBitmap.eraseColor(Color.TRANSPARENT);

            /*if(mOneShot){
                iWidth=getWidth();
                //Cambia se apro la tastiera.
                iHeight=getHeight();
                mOneShot=false;
            }*/
            //mTextBitmap	= Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
            //mTextBitmap.eraseColor(Color.TRANSPARENT);

            //Toast.makeText(mContext, "Paper Size: "+getWidth()+" - "+getHeight()+" Original Size: "+iWidth+" - "+iHeight, Toast.LENGTH_SHORT).show();

            identityMatrix = new Matrix();

            float lastX=0,lastY=0;
            int nRow=aPaint.size();
            int iCurrentOffset=getInitialOffsetRow();

            for(int i=0;i<nRow;i++){
                //Log.v(this.getClass().getCanonicalName(),"#############INIZIO##################### ");
                //Log.v(this.getClass().getCanonicalName(),"First iCurrentOffset:"+iCurrentOffset);

                if(mRows.get((long) i)!=null){
                    if(mRows.get((long) i).getRowPosY()>iHeight) {
                        //End of Page
                        Log.v(this.getClass().getCanonicalName(),"END OF PAGE");
                        return;
                    }
                    updateRowToDiary(i,getRowString(i),mRows.get((long) i).getRowPosX(), mRows.get((long) i).getRowPosY());
                    mTextCanvas.drawText(getRowString(i), mRows.get((long) i).getRowPosX(), mRows.get((long) i).getRowPosY(),
                            getRow(i));
                    lastX=mRows.get((long) i).getRowPosX();
                    lastY=mRows.get((long) i).getRowPosY();
                    //Log.v(this.getClass().getCanonicalName(),"case 1");

                }else{
                    try{
                        if(aRowCustomPos.get(i)){
                            int iPosX = 0;
                            int iPosY = 0;
                            updateRowToDiary(i,getRowString(i), iPosX, iPosY);
                            mTextCanvas.drawText(getRowString(i), iPosX, iPosY,
                                    getRow(i));
                            lastX= iPosX;
                            lastY= iPosY;
                            //Log.v(this.getClass().getCanonicalName(),"case 2->1");
                        }else{
                            if(iCurrentOffset>iHeight) {
                                //End of Page
                                Log.v(this.getClass().getCanonicalName(),"END OF PAGE");
                                return;
                            }
                            updateRowToDiary(i,getRowString(i),getMarginSX(), iCurrentOffset);
                            mTextCanvas.drawText(getRowString(i), getMarginSX(), iCurrentOffset,
                                    getRow(i));
                            lastX=getMarginSX();
                            lastY=iCurrentOffset;
                            //Log.v(this.getClass().getCanonicalName(),"case 2->2");
                        }
                    }catch (IndexOutOfBoundsException e){
                        Log.e(this.getClass().getCanonicalName(),"IndexOutOfBoundsException ");
                        return;
                    }
                }
                iCurrentOffset+=getOffsetRow();
                iLastOffset=iCurrentOffset;
                //Log.v(this.getClass().getCanonicalName(),"Stringa: "+getRowString(i)+" afther iCurrentOffset:"+iCurrentOffset+" - getOffsetRow: "+getOffsetRow()+" - getMarginSX():"+getMarginSX());
                //Log.v(this.getClass().getCanonicalName(),"#############FINE##################### ");
            }

            Paint oCursor = new Paint();
            oCursor.setColor(getRow(0).getColor());
            oCursor.setAntiAlias(true);
            oCursor.setStrokeWidth(6);
            oCursor.setAntiAlias(true);
            oCursor.setTypeface(oFont);
            oCursor.setFakeBoldText(true);
            oCursor.setTextSize(fontSize);
            if((nRow-1)<0) return;
            //mTextCanvas.drawLine((lastX+oCursor.measureText(getRowString(nRow-1))), lastY+2, (lastX+oCursor.measureText(getRowString(nRow-1))+30), lastY+2, oCursor);
            mTextCanvas.drawCircle(lastX+oCursor.measureText(getRowString(nRow-1))+10,lastY+2,3,oCursor);
            //oCursor=null;


            refresh(c);
        }
        if (c != null) {
            try{
                unlockCanvasAndPost(c);
                //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
            }catch(IllegalArgumentException e){
                //TODO
                Log.e(this.getClass().getCanonicalName(),"drawTextOnSurface thread error canvas");
            }
            c=null;
            //mTextBitmap.recycle();
            //mTextBitmap=null;
            mTextCanvas=null;
            //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost Canvas");
        }
    }


    /**
     * metodo utilizzato per disegnare sullo schermo richiamato in modalità sincrona dal thread interno.
     *
     * **/
    protected void refresh(Canvas canvas) {

        int _Initializer = _graphics.size()-iUndo;
        if(canvas!=null && _Initializer<=0){
            //Questa bitmap contiene il backgroud
            if(mBitmapForPage !=null) canvas.drawBitmap(mBitmapForPage, identityMatrix, null);
            //Disegno lo spostamento dell'immagine
            if(mPictureBitmapForCanvas!=null) canvas.drawBitmap(mPictureBitmapForCanvas, identityMatrix, null);
            //Disegno le vecchie path
            if(bmpPath!=null) canvas.drawBitmap(bmpPath, identityMatrix, null);
            if(mTextBitmap!=null) canvas.drawBitmap(mTextBitmap, identityMatrix, null);
            //Log.v(this.getClass().getCanonicalName(),"draw background no path");
            mOneShot=true;
        }

        if(canvas!=null && _Initializer>0){
            //Log.v(this.getClass().getCanonicalName(),"draw background with path");
            if(mWritable){
                //Questa bitmap contiene il backgroud
                if(mBitmapForPage !=null && mOneShot) canvas.drawBitmap(mBitmapForPage, identityMatrix, null);
                try{
                    if(mCanvas!=null) mCanvas.drawPath(_graphics.get(_Initializer), _graphics.get(_Initializer).getmPaint());
                }catch (IndexOutOfBoundsException e){
                    Log.w(this.getClass().getCanonicalName(),"out of bound");
                }
                //Disegno lo spostamento dell'immagine
                if(mPictureBitmapForCanvas!=null && mOneShot) canvas.drawBitmap(mPictureBitmapForCanvas, identityMatrix, null);
                //Disegno le vecchie path
                if(bmpPath!=null) canvas.drawBitmap(bmpPath, identityMatrix, null);
                //Questo nel caso in cui sto scrivento con path
                if(mTextBitmap!=null && mOneShot) canvas.drawBitmap(mTextBitmap, identityMatrix, null);
                if(mOneShot) mOneShot=false;
            }else{
                //Questa bitmap contiene il backgroud
                if(mBitmapForPage !=null) canvas.drawBitmap(mBitmapForPage, identityMatrix, null);
                if(mCanvas!=null) mCanvas.drawPath(_graphics.get(_Initializer), _graphics.get(_Initializer).getmPaint());
                //Disegno lo spostamento dell'immagine
                if(mPictureBitmapForCanvas!=null && mOneShot) canvas.drawBitmap(mPictureBitmapForCanvas, identityMatrix, null);
                //Disegno le vecchie path
                if(bmpPath!=null) canvas.drawBitmap(bmpPath, identityMatrix, null);
                //Questo nel caso in cui sto scrivento con path
                if(mTextBitmap!=null && mOneShot) canvas.drawBitmap(mTextBitmap, identityMatrix, null);
                if(mOneShot) mOneShot=true;
            }
        }
    }
    /**
     * carica le immagini della pagina salvata.
     *
     * */
    private void drawPicturePage() {
        PageCurlView.this.invalidate();
        if(mCurrentPage==null
                || mCurrentPage.getDiaryImage()==null
                || mCurrentPage.getDiaryImage().size()==0){
            Log.v(this.getClass().getCanonicalName(),"NO Pucture on Page");
            return;
        }
        mImages = (Hashtable<Long, DiaryPicture>) mCurrentPage.getDiaryImage();
        sortedImages = new TreeMap<Long, DiaryPicture>(mImages);
        if(mPictureBitmapForCanvas==null){
            Log.v(this.getClass().getCanonicalName(),"mPictureBitmapForCanvas is NULL created");
            mPictureBitmapForCanvas = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
            MatrixPictureCanvas = new Matrix();
        }

        if(mCanvasForMovePicture ==null){
            Log.v(this.getClass().getCanonicalName(),"mCanvasForMovePicture is NULL created");
            mCanvasForMovePicture = new Canvas(mPictureBitmapForCanvas);
        }
        //Cancello la vecchia immagine
        mPictureBitmapForCanvas.eraseColor(Color.TRANSPARENT);

        for(DiaryPicture oPicture : sortedImages.values()){
            if(mCanvasForMovePicture!=null &&
                    oPicture!=null){
                Log.v(this.getClass().getCanonicalName(),"Draw old Picture");
                if(oPicture.getBitmapImage()==null){
                    mImages.remove(oPicture.getDiaryPictureID());
                    continue;
                }
               /* Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postTranslate(-oPicture.getBitmapImage().getWidth() / 2, -oPicture.getBitmapImage().getHeight() / 2); // Centers image
                matrix.postRotate(oPicture.getDiaryPictureRotation());
                matrix.postTranslate(oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY());
                mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), matrix, null);*/
                mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY(), mPaint);
            }
        }
        PageCurlView.this.invalidate();
    }



    /***
     * Scrive il testo della pagina.
     *
     *
     * */
    private void writeTextPage(Canvas canvas) {
        int nRow=mRows.size();
        Paint TextPaintPage = new Paint();
        TextPaintPage.setColor(mTextColor);
        TextPaintPage.setAntiAlias(true);
        TextPaintPage.setTypeface(oFont);

        TextPaintPage.setFakeBoldText(true);

        TextPaintPage.setTextSize(fontSize);

        for(int i=0;i<nRow;i++){
            //Toast.makeText(mContext, "Text Paint Size:"+getRow(i).measureText(getRowString(i), 0, getRowString(i).length()), Toast.LENGTH_SHORT).show();
            if(mRows.get((long) i)!=null && canvas!=null){
                canvas.drawText(mRows.get((long) i).getRowText(), mRows.get((long) i).getRowPosX(), mRows.get((long) i).getRowPosY(),
                        TextPaintPage);
            }
        }

        TextPaintPage=null;
    }

    /**
     * scrive la data sulla pagina
     * @param canvas
     *
     * @pram Canvas
     * */
    private void drawDate(Canvas canvas) {
        if(mCurrentPage==null) return;
        SimpleDateFormat sDF = new SimpleDateFormat("dd");
        String sDay=" - ";
        String sMonth=" - ";
        sDay=sDF.format(mCurrentPage.getPageDTCreation());
        sDF.applyPattern("MMMM");
        sMonth=sDF.format(mCurrentPage.getPageDTCreation());
        Paint TextPaintDay = new Paint();
        if(mCurrentDiary.getDiaryTemplate()==6){
            TextPaintDay.setColor(Const.COLOR_DATE_ALTERNATE);
        }else{
            TextPaintDay.setColor(Const.COLOR_DATE);
        }

        TextPaintDay.setAntiAlias(true);
        TextPaintDay.setTypeface(oFont);

        TextPaintDay.setFakeBoldText(true);

        if(canvas!=null){
            if(mCurrentPage.getPageOrientation()== ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                TextPaintDay.setTextSize((float) (fontSizeDate*1));
                canvas.drawText(sDay, (iWidth* Const.DATE_DAY_OFFSET_X)/Const.DEFAULT_WIDTH, ((iHeight*Const.DATE_DAY_OFFSET_Y)/Const.DEFAULT_HEIGTH)+15, TextPaintDay);
                TextPaintDay.setTextSize(fontSizeDate*0.5f);
                canvas.drawText(sMonth, (iWidth*Const.DATE_MONTH_OFFSET_X)/Const.DEFAULT_WIDTH, ((iHeight*Const.DATE_MONTH_OFFSET_Y)/Const.DEFAULT_HEIGTH)+15, TextPaintDay);
                canvas.drawLine(0f,((iHeight*Const.DATE_MONTH_OFFSET_Y)/Const.DEFAULT_HEIGTH)+10, iWidth, ((iHeight*Const.DATE_MONTH_OFFSET_Y)/Const.DEFAULT_HEIGTH)+10,TextPaintDay);
            }else{
                TextPaintDay.setTextSize((float) (fontSizeDate*1.5));
                canvas.drawText(sDay, (iWidth*Const.DATE_DAY_OFFSET_X)/Const.DEFAULT_WIDTH, (iHeight*Const.DATE_DAY_OFFSET_Y)/Const.DEFAULT_HEIGTH, TextPaintDay);
                TextPaintDay.setTextSize(fontSizeDate*0.5f);
                canvas.drawText(sMonth, (iWidth*Const.DATE_MONTH_OFFSET_X)/Const.DEFAULT_WIDTH, (iHeight*Const.DATE_MONTH_OFFSET_Y)/Const.DEFAULT_HEIGTH, TextPaintDay);
                canvas.drawLine(0f,((iHeight*Const.DATE_MONTH_OFFSET_Y)/Const.DEFAULT_HEIGTH)+10, iWidth, ((iHeight*Const.DATE_MONTH_OFFSET_Y)/Const.DEFAULT_HEIGTH)+10,TextPaintDay);
            }

        }

    }
    /**
     * ferma il thread e lo elimina?
     *
     * */
    public void stopDrawingThread(){
        freeBitmap();
        if(_thread!=null &&
                _thread.isAlive()){
            boolean retry = true;
            //_thread.setRunning(false);
            //_thread.setStopThread(false);
            _thread.interrupt();
            Log.v(this.getClass().getCanonicalName(),"setWritable _thread interupted");
        }
        _thread=null;

        System.gc();
    }

    /***
     * definisce se writable o no
     * */
    public void setWritable(boolean writable) {
        if(mCurrentPage==null) return;
        mWritable=writable;
        _graphics=new ArrayList<HandWritePath>();
        mRows=mCurrentPage.getPageRows();
        if(!mWritable){
            if(_thread!=null &&
                    _thread.isAlive()){
                _thread.interrupt();
                //_thread.setRunning(mWritable);
                Log.v(this.getClass().getCanonicalName(),"Interrupt Thread END Drawing");
            }

            _thread=null;
        }else{
            ((LinearLayout) getParent()).removeView(oRowEdit);
            if(_thread!=null &&
                    !_thread.isAlive()){
                Log.v(this.getClass().getCanonicalName(),"Interrupt Thread and Create new Thread for Drawing");
                _thread.interrupt();
                //_thread.setRunning(mWritable);
                _thread=null;
                _thread = new DrawingThread();
                //_thread.setRunning(mWritable);
                _thread.start();
            }else{
                Log.v(this.getClass().getCanonicalName(),"Create new Thread for Drawing");
                _thread = new DrawingThread();
                //_thread.setRunning(mWritable);
                _thread.start();
            }

        }
    }
    /**
     * imposta il background della pagina corrente
     *
     * */
    private void setImagePage(Bitmap oBmpPage){
        //if(mBmpPage!=null) mBmpPage.recycle();
        //mBmpPage=oBmpPage;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        findIfTouchPicture(motionEvent.getX(), motionEvent.getY());

        if(mPictureToMove!=null){
            //Apri l'immagine selezionata
            Intent intentGallery = new Intent();
            intentGallery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentGallery.setAction(Intent.ACTION_VIEW);
            intentGallery.setDataAndType(Uri.parse("file://" + mPictureToMove.getDiaryImageURI()), "image/*");
            mContext.getApplicationContext().startActivity(intentGallery);
            Log.v(this.getClass().getCanonicalName(), "onSingleTapUp Via GestureDetector");
            mPictureToMove=null;
        }else{
            if(!findIfTouchOnText(motionEvent.getX(), motionEvent.getY())){
                ((LinearLayout) getParent()).removeView(oRowEdit);
                //mostro la tastiera
                if(!isWritable()) toggleSoftKeyboard();
            }



        }
        return false;
    }

    /**flip/flop tastiera si/no*/
    private boolean isSoftKeyShow=false;

    /**
     * Mostra nasconte la tastiera soft
     * */
    private void toggleSoftKeyboard() {
        requestFocus();
        if(!isSoftKeyShow){
            // show the keyboard so we can enter text
            InputMethodManager imm = (InputMethodManager) mContext
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            //isSoftKeyShow=imm.showSoftInput(oSurface, InputMethodManager.SHOW_FORCED);
            isSoftKeyShow=true;
            if(isWritable()) setWritable(false);
        }else{
            InputMethodManager imm = (InputMethodManager) mContext
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN,0);
            //imm.hideSoftInputFromInputMethod(oSurface.getWindowToken(), 0);
            //getWindow().setSoftInputMode(
            //	      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            isSoftKeyShow=false;
            postInvalidate();
            getRootView().postInvalidate();
        }
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        //TODO LongPress for editing row.
        findIfTouchPicture(motionEvent.getX(), motionEvent.getY());
        if(mPictureToMove!=null){
            Log.v(this.getClass().getCanonicalName(),"Long Press Via GestureDetector");
            isPirtureLongClick=true;
            Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {

        findIfTouchPicture(motionEvent.getX(), motionEvent.getY());
        if(mPictureToMove!=null){
            Log.v(this.getClass().getCanonicalName(),"onFling Press Via GestureDetector");
            PictureRotateTask updatePictureAngle = new PictureRotateTask(mPictureToMove);
            updatePictureAngle.execute();
        }
        return false;
    }
    /**
     * Ritorna se è writable o no
     * */
    public boolean isWritable() {
        return mWritable;
    }

    public void setDeleteMode(boolean deleteMode) {
        mDeleteMode = deleteMode;
    }

    public boolean isDeleted() {
        return mDeleteMode;
    }

    public void setMarginTop(int marginTop) {
        mSystemHeight = marginTop;
    }

    class DrawingThread extends Thread {
        /*//private TextureView _surfaceHolder;
        //private TextureHandWrite _panel;
        //private volatile boolean _run = mWritable;
        //private volatile boolean _runThread = mThreadDrawing;

        public DrawingThread(TextureView surfaceHolder, TextureHandWrite panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
        }

        public void setRunning(boolean run) {
            _run = run;
        }

        public void setStopThread(boolean run) {
            _runThread = run;
        }*/

        @Override
        public void run() {
            while (!isAvailable()) {
                try {
                    sleep(1000);
                    Log.e(this.getClass().getCanonicalName(),"TextureView is not Avaiable wait...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Canvas c= null;
          /*  c = _surfaceHolder.lockCanvas();
            synchronized (_surfaceHolder) {
                _panel.refresh(c);
            }
            if (c != null) {
                _surfaceHolder.unlockCanvasAndPost(c);
                //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost Canvas");
            }*/
            //createCanvasBackground();

            if(isInterrupted()) return;
            while (mWritable) {
                if(isInterrupted()) return;
                if(mWritable){
                    c = null;
                    try {
                        c = lockCanvas();
                        //Log.v(this.getClass().getCanonicalName(),"H: "+c.getHeight()+" W: "+c.getWidth());
                        if(c==null){
                            Log.e(this.getClass().getCanonicalName(),"canvas _surfaceHolder is Null");
                            interrupt();
                            return;
                        }
                        synchronized (c) {

                            c.drawColor(Color.TRANSPARENT);
                            refresh(c);
                            c.drawColor(Color.TRANSPARENT);
                        }
                    }finally{
                        try{
                            if (c != null)
                                unlockCanvasAndPost(c);
                        }catch (IllegalArgumentException e){
                            Log.e(this.getClass().getCanonicalName(),"thread unlockCanvasAndPost error");
                            e.printStackTrace();
                            interrupt();
                            return;
                        }
                    }

                }
            }
            if (c != null) {
                try{
                    unlockCanvasAndPost(c);
                    //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
                }catch(IllegalArgumentException e){
                    //TODO
                    Log.e(this.getClass().getCanonicalName(),"run thread error canvas");
                    e.printStackTrace();
                    interrupt();
                    return;
                }
            }
        }
    }



    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
        Log.v(this.getClass().getCanonicalName(),"SurfaceTexture Visible");
        iWidth=getWidth();
        iHeight=getHeight();
        //drawPicturePage();

        createCanvasBackground();
        drawTextOnSurface();
        //addThreadForRefresh();
        //Riterdo disegno immagini
            /* Timer aTask = new Timer();
             aTask.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.v(this.getClass().getCanonicalName(),"Delay Picture DRAW");
                    drawPicturePage();
                }
            },2000);*/
        postInvalidate();
        //mDrawBackground=false;
    }
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        boolean retry = true;
        //if(_thread!=null) _thread.setRunning(false);
        surface.release();
        Log.v(this.getClass().getCanonicalName(), "surfaceDestroyed _thread interupted");
        if(_thread==null) return false;
        _thread.interrupt();
        _thread=null;

        return false;
    }
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
        //Log.v(this.getClass().getCanonicalName(), "onSurfaceTextureSizeChanged CHANGED");

    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //Log.v(this.getClass().getCanonicalName(), "onSurfaceTextureUpdated UPDATE");
        //postInvalidate();
    }



    /*@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
    	//paintBackground(holder);
    	Log.v(this.getClass().getCanonicalName(), "SURFACE MODIFY");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(getVisibility()==View.VISIBLE){
			iWidth=getWidth();
			iHeight=getHeight();
			createCanvasBackground();
			drawTextOnSurface();
			addThreadForRefresh();
			//mDrawBackground=false;
			Log.v(this.getClass().getCanonicalName(),"surfaceCreated Visible");
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        if(_thread!=null) _thread.setRunning(false);
        else return;
        while (retry) {
            try {
                _thread.join();
                _thread.interrupt();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
        Log.v(this.getClass().getCanonicalName(), "surfaceDestroyed _thread interupted");
		_thread=null;
	}*/


    /**
     * Avvia il thread per il refresh
     * */
    private void addThreadForRefresh() {
        if(!isAvailable()) return;
        if(_thread!=null &&
                !_thread.isAlive()){
            //_thread.setRunning(mWritable);
            _thread.start();
            Log.v(this.getClass().getCanonicalName(),"addThreadForRefresh _thread started");
        }else{
            _thread = new DrawingThread();
            // _thread.setRunning(mWritable);
            _thread.start();
            Log.v(this.getClass().getCanonicalName(),"addThreadForRefresh new _thread started");
        }
        // Log.v(this.getClass().getCanonicalName(),"Surface Created _thread started");
        // postInvalidate();
    }

    /**
     * ritorna l'immagine con tutte le path per salvare l'immagine
     *
     *
     * */
    public Bitmap getHandWritePath(){
        return bmpPath;
    }
    /**
     * libera memoria
     * */
    public void freeBitmap(){
        if(bmpPath!=null) bmpPath.recycle();
        if(mBitmapForPage !=null) mBitmapForPage.recycle();
        if(mTextBitmap!=null) mTextBitmap.recycle();
        //if(mBmpPage!=null) mBmpPage.recycle();
        if(mPictureBitmapForCanvas!=null) mPictureBitmapForCanvas.recycle();
        mPictureToMove=null;
        //mBmpPage=null;
        bmpPath=null;
        mCanvasPath=null;
        mBitmapForPage =null;
        mTextBitmap=null;
        mDrawBackground=true;
        mCanvas=null;
        mPictureBitmapForCanvas=null;
        mCanvasForMovePicture=null;
        mCurrentPage=null;
        //System.gc();
        //mSurfaceHolder.unlockCanvasAndPost(mSurfaceHolder.lockCanvas());

        Log.v(this.getClass().getCanonicalName(), "########### free bitmap memory ##############");
        Log.v(this.getClass().getCanonicalName(), "########### free memory is "+
                Runtime.getRuntime().freeMemory()+" total is "+
                Runtime.getRuntime().totalMemory()+" ##############");

    }

    /**
     * Imposta il colore della linea
     *
     *
     * */
    public void setColor(int currentColor) {
        mCurrentColor=currentColor;
        mPaint.setColor(currentColor);
    }

    /**
     * Imposta lo spessore della linea
     *
     *
     * */
    public void setStrokeWidth(int strokeWidth) {
        mCurrentStrokeWidth=strokeWidth;
        mPaint.setStrokeWidth(mCurrentStrokeWidth);
    }


    /**
     * canella i vecchi path.
     *
     *
     * */
    public void resetPage() {
        setWritable(false);
        _graphics.clear();
        mRows.clear();
    }

    /**
     * Imposto al pagina corrente in modo da preparare la
     * SurfaceView per l'hand write
     *
     * */
    public void setPage(Page page) {
        mDrawBackground=true;
        //if(mBmpPage!=null) mBmpPage.recycle();
        //if(mBitmapForPage !=null) mBitmapForPage.recycle();
        //mCanvas=null;
        //if(iWidth>0 && iHeight>0){
        //    createCanvasBackground();
        //}
        mCurrentPage=page;
        applyTemplate();
        //applyPaperPreview(mCurrentPage);
    }
    /**
     * Ritorno la pagina corrente in modo da preparare il
     * dump del diario
     *
     * */
    public Page getPage() {
        return mCurrentPage;
    }
    /**
     * Applica il template selezionato
     *
     * TODO utilizzare Drawable per creare la pagina con un gradiente ed essere più leggera
     * */
    private void applyTemplate() {
        if(mCurrentDiary==null || mCurrentPage==null) return;

        Log.v(this.getClass().getCanonicalName(),"Loading font...");
        oFont        = Typeface.createFromAsset(mContext.getAssets(), "template/"+mCurrentDiary.getDiaryTemplate()+"/font.ttf");
        if(mCurrentDiary.getDiaryTemplate()==3 ||
                mCurrentDiary.getDiaryTemplate()==6){
            mTextColor=Color.WHITE;
        }
		/*Bitmap preview=null;
		try {
			//BitmapFactory.Options options = new BitmapFactory.Options();
			//options.inSampleSize=Const.SAMPLESIZEIMAGE;

            //preview = BitmapFactory.decodeStream(mContext.getAssets().open("template/"+mCurrentDiary.getDiaryTemplate()+"/paper.jpg"),null, options);

            //Drawable  mDrawable = getResources().getDrawable(R.drawable.paper_1);
            //mDrawable.draw(cavas);
			//setImagePage(preview);
			//preview=null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setImagePage(null);
		}*/


    }

    /**
     * crea una canvas con le vecchie linee e background da usare poi nel refresh
     *
     * */
    public void createCanvasBackground(Canvas canvas){
        Canvas c=canvas;
        if(mWritable) return;


        if(c==null) return;
        if(!mDrawBackground || iWidth<=0) return;
        //if(iWidth<=0 || iHeight<=0) return;


        if(mTextBitmap!=null) mTextBitmap.recycle();
        mTextBitmap	= Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
        mTextBitmap.eraseColor(Color.TRANSPARENT);

        if(mBitmapForPage!=null) mBitmapForPage.recycle();
        mBitmapForPage = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
        mBitmapForPage.eraseColor(Color.TRANSPARENT);

        if(mCanvas==null){
            mCanvas = new Canvas(mBitmapForPage);
        }

        identityMatrix = new Matrix();

        if(mCanvas!=null){
                /*    //TODO NON SCRIVO IL BACKGROUND
                    if(bScaled){
                        mBmpPage=Bitmap.createScaledBitmap(mBmpPage, iWidth, iHeight, true);
                        bScaled=false;
                    }
                    if(mBmpPage!=null) {
                        //mCanvas.drawBitmap(mBmpPage, 0, 0, mPaint);

                    }*/
            Paint p = new Paint();
            p.setDither(true);
            switch (mCurrentDiary.getDiaryTemplate()){
                case 1:
                    //#496885 -> #506B85
                    p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#496885"),Color.parseColor("#506B85"), Shader.TileMode.REPEAT));
                    mCanvas.drawPaint(p);
                    drawLinesPaper(mCanvas);
                    break;
                case 2:
                    //#4D3939 -> #5E4949
                    p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#4D3939"),Color.parseColor("#5E4949"),Shader.TileMode.REPEAT));
                    mCanvas.drawPaint(p);
                    drawLinesPaper(mCanvas);
                    break;
                case 3:
                    //#331616 -> #472323
                    p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#331616"),Color.parseColor("#472323"),Shader.TileMode.REPEAT));
                    mCanvas.drawPaint(p);
                    drawLinesPaper(mCanvas);
                    break;
                case 4:
                    p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#FFEB10"),Color.parseColor("#BFB439"),Shader.TileMode.REPEAT));
                    mCanvas.drawPaint(p);
                    drawLinesPaper(mCanvas);
                    break;
                case 5:
                    p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#FFFFFF"),Color.parseColor("#BAAEBD"),Shader.TileMode.REPEAT));
                    mCanvas.drawPaint(p);
                    drawGridsPaper(mCanvas);
                    break;
                case 6:
                    p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#A60000"),Color.parseColor("#A64B00"),Shader.TileMode.REPEAT));
                    mCanvas.drawPaint(p);
                    drawLinesPaper(mCanvas);
                    break;
                default:

                    break;
            }


        }

        if(mCurrentDiary!=null && mContext!=null && mCurrentPage!=null){
            //le vecchie path da usare in un task async
            Bitmap immutable = BitmapFactoryHelper.decodeSampledBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/" + mContext.getPackageName() + "/" + mCurrentDiary.getDiaryID() + "/Pictures/h" + mCurrentPage.getPageID() + Const.PAGE_PREVIEW_EXT, 1);
            if(immutable!=null){
                bmpPath = immutable.copy(Bitmap.Config.ARGB_8888, true);
                immutable.recycle();
                immutable=null;
            }
        }
        //Disegna la Data della pagina
        drawDate(mCanvas);
        //Disegna il testo della pagina
        //writeTextPage(mCanvas);
        drawPicturePage();

        //Disegno le path
			/*if(bmpPath!=null){
	            mCanvasOldPath = new Canvas();
	            mCanvasOldPath.setBitmap(bmpPath);
	            //mCanvas.drawBitmap(bmpPath, 0, 0, mPaint);
	        }*/
        //TODO Disegna le photos della pagina
        //drawPicturePage(mCanvas);
			/*for (int i = _Initializer; i >= 0; i--) {
				HandWritePath path = _graphics.get(i);
				if(mCanvas!=null){
					mCanvas.drawPath(path, path.getmPaint());
				}
				path=null;
			}*/
        mDrawBackground=true;

    }

    /**
     * crea una canvas con le vecchie linee e background da usare poi nel refresh
     *
     * */
    public void createCanvasBackground(){

        if(mWritable) return;
        Canvas c=lockCanvas();

        if(c==null) return;
        synchronized (c) {
            if(!mDrawBackground || iWidth<=0) return;
            //if(iWidth<=0 || iHeight<=0) return;
            //Log.v(this.getClass().getCanonicalName(), "############ createCanvasBackground ######### "+iWidth+" - "+iHeight);
            //Log.e(this.getClass().getCanonicalName(), "############ createCanvasBackground ######### "+iWidth+" - "+iHeight);

            if(mTextBitmap!=null) mTextBitmap.recycle();
            mTextBitmap	= Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
            mTextBitmap.eraseColor(Color.TRANSPARENT);

            if(mBitmapForPage!=null) mBitmapForPage.recycle();
            mBitmapForPage = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
            mBitmapForPage.eraseColor(Color.TRANSPARENT);

            if(mCanvas==null){
                mCanvas = new Canvas(mBitmapForPage);
            }

            identityMatrix = new Matrix();

            if(mCanvas!=null){

                Paint p = new Paint();
                p.setDither(true);
                switch (mCurrentDiary.getDiaryTemplate()){
                    case 1:
                        //#496885 -> #506B85
                        p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#496885"),Color.parseColor("#506B85"),Shader.TileMode.REPEAT));
                        mCanvas.drawPaint(p);
                        drawLinesPaper(mCanvas);
                        break;
                    case 2:
                        //#4D3939 -> #5E4949
                        p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#4D3939"),Color.parseColor("#5E4949"),Shader.TileMode.REPEAT));
                        mCanvas.drawPaint(p);
                        drawLinesPaper(mCanvas);
                        break;
                    case 3:
                        //#331616 -> #472323
                        p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#331616"),Color.parseColor("#472323"),Shader.TileMode.REPEAT));
                        mCanvas.drawPaint(p);
                        drawLinesPaper(mCanvas);
                        break;
                    case 4:
                        p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#FFEB10"),Color.parseColor("#BFB439"),Shader.TileMode.REPEAT));
                        mCanvas.drawPaint(p);
                        drawLinesPaper(mCanvas);
                        break;
                    case 5:
                        p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#FFFFFF"),Color.parseColor("#BAAEBD"),Shader.TileMode.REPEAT));
                        mCanvas.drawPaint(p);
                        drawGridsPaper(mCanvas);
                        break;
                    case 6:
                        p.setShader(new LinearGradient(0,0,0, iHeight,Color.parseColor("#A60000"),Color.parseColor("#A64B00"),Shader.TileMode.REPEAT));
                        mCanvas.drawPaint(p);
                        drawLinesPaper(mCanvas);
                        break;
                    default:

                        break;
                }


            }

            if(mCurrentDiary!=null && mContext!=null && mCurrentPage!=null){
                //le vecchie path da usare in un task async
                Bitmap immutable = BitmapFactoryHelper.decodeSampledBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/"+mContext.getPackageName()+"/"+mCurrentDiary.getDiaryID() + "/Pictures/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT,1);
                if(immutable!=null){
                    if(bmpPath!=null) bmpPath.recycle();
                    bmpPath = immutable.copy(Bitmap.Config.ARGB_8888, true);
                    immutable.recycle();
                    immutable=null;
                }
            }
            //Disegna la Data della pagina
            drawDate(mCanvas);
            //Disegna il testo della pagina
            //writeTextPage(mCanvas);
            drawPicturePage();




            // Always refresh offsets
            mCurrentLeft = getLeft();
            mCurrentTop = getTop();

            // Translate the whole canvas
            //canvas.translate(mCurrentLeft, mCurrentTop);

            // We need to initialize all size data when we first draw the view
            if ( !bViewDrawn ) {
                bViewDrawn = true;
                onFirstDrawEvent(c);
            }

            c.drawColor(Color.WHITE);

            // Curl pages
            //DoPageCurl();

            // TODO: This just scales the views to the current
            // width and height. We should add some logic for:
            //  1) Maintain aspect ratio
            //  2) Uniform scale
            //  3) ...
            Rect rect = new Rect();
            rect.left = 0;
            rect.top = 0;
            rect.bottom = getHeight();
            rect.right = getWidth();

            // First Page render
            Paint paint = new Paint();

            // Draw our elements
            drawForeground(c, rect, paint);
            drawBackground(c, rect, paint);
            drawCurlEdge(c);

            // Draw any debug info once we are done
            if ( bEnableDebugMode )
                drawDebug(c);

            // Check if we can re-enable input
            if ( bEnableInputAfterDraw )
            {
                bBlockTouchInput = false;
                bEnableInputAfterDraw = false;
            }

            // Restore canvas
            //canvas.restore();




            mDrawBackground=true;
            refresh(c);
        }
        if (c != null) {
            try {
                unlockCanvasAndPost(c);
                //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
            } catch (IllegalArgumentException e) {
                //TODO
                Log.e(this.getClass().getCanonicalName(), "createCanvasBackground thread error canvas");
            }
            c = null;
            //mTextBitmap.recycle();
            //mTextBitmap=null;
            //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost Canvas");
        }
    }
    /**
     * disegna le linee del foglio
     * */
    private void drawLinesPaper(Canvas mCanvas) {
        //Diseegno le linee per il foglio
        Paint mLines = new Paint();
        mLines.setAntiAlias(true);
        if(mCurrentDiary.getDiaryTemplate()==4){
            mLines.setColor(Color.BLUE);
        }else{
            mLines.setColor(Color.WHITE);
        }
        mLines.setStrokeWidth(1);
        mLines.setAlpha(100);
        int iLines=4;
        int iOffsetPage=0;
        while (iOffsetPage<iHeight){
            iOffsetPage=(getOffsetRow()*iLines)+5;
            iLines++;
            mCanvas.drawLine(10,iOffsetPage, iWidth-10, iOffsetPage,mLines);
        }
    }

    /**
     * disegna le linee del foglio
     * */
    private void drawGridsPaper(Canvas mCanvas) {
        //Diseegno le linee per il foglio
        Paint mLines = new Paint();
        mLines.setAntiAlias(true);
        mLines.setColor(Color.BLACK);
        mLines.setStrokeWidth(1);
        mLines.setAlpha(100);
        int ihLines=4;
        int iOffsetPage=0;
        while (iOffsetPage<iHeight){
            iOffsetPage=(getOffsetRow()*ihLines)+5;
            ihLines++;
            mCanvas.drawLine(10,iOffsetPage, iWidth-10, iOffsetPage,mLines);
        }
        int ivLines=1;
        int ivOffset=getMarginSX()+20;

        while(ivOffset<iWidth){
            ivOffset=(getMarginSX()+20)*ivLines;
            ivLines++;
            mCanvas.drawLine(ivOffset,((iHeight*Const.DATE_MONTH_OFFSET_Y)/Const.DEFAULT_HEIGTH)+10, ivOffset, iHeight-10,mLines);
        }
    }

    /**
     * Disegna una pagina vuota
     *
     * @deprecated
     * */
    public void drawEmptyPage(){
        if(mWritable) return;
        //Reimposto il oneShot
        mOneShot=true;
        Canvas c=lockCanvas();
        if(c==null) {
            Log.v(this.getClass().getCanonicalName(),"Canvas is NULL, not drawing");
            return;
        }
        synchronized (c) {
            if(!mDrawBackground || iWidth<=0) return;
            //if(iWidth<=0 || iHeight<=0) return;
            Log.v(this.getClass().getCanonicalName(), "############ drawEmptyPage ######### "+iWidth+" - "+iHeight);

            if(mTextBitmap!=null) mTextBitmap.recycle();
            mTextBitmap	= Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
            mTextBitmap.eraseColor(Color.TRANSPARENT);

            if(mBitmapForPage!=null) mBitmapForPage.recycle();
            mBitmapForPage = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
            mBitmapForPage.eraseColor(Color.TRANSPARENT);
            mCanvas = new Canvas(mBitmapForPage);

            identityMatrix = new Matrix();

		/*	if(mCanvas!=null && mBmpPage!=null){
				mBmpPage=Bitmap.createScaledBitmap(mBmpPage, iWidth, iHeight, true);
				if(mBmpPage!=null) {
					mCanvas.drawBitmap(mBmpPage, 0, 0, mPaint);
				}
			}*/
            //le vecchie path da usare in un task async
            Bitmap immutable = BitmapFactoryHelper.decodeSampledBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/"+mContext.getPackageName()+"/"+mCurrentDiary.getDiaryID() + "/Pictures/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT,1);
            if(immutable!=null){
                if(bmpPath!=null) bmpPath.recycle();
                bmpPath = immutable.copy(Bitmap.Config.ARGB_8888, true);
                immutable.recycle();
                //immutable=null;
            }
            //Disegna la Data della pagina
            drawDate(mCanvas);
            //Disegna il testo della pagina
            //writeTextPage(mCanvas);
            drawPicturePage();
            refresh(c);
        }
        if (c != null) {
            try{
                unlockCanvasAndPost(c);
                //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
            }catch(IllegalArgumentException e){
                //TODO
                Log.e(this.getClass().getCanonicalName(),"drawEmptyPage thread error canvas");
            }
            c=null;
            //mTextBitmap.recycle();
            //mTextBitmap=null;
            //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost Canvas");
        }
    }
    /**
     * Applica il template selezionato
     *
     *
     * */
    public void applyPaperPreview(Page page) {
        String sPathImage=Environment.getExternalStorageDirectory().getPath() + "/"+mContext.getPackageName()+"/"+page.getDiaryID() + "/Pictures";
        Bitmap preview = BitmapFactoryHelper.decodeSampledBitmapFromFile(sPathImage + "/" + page.getPageID() + Const.CAMERA_PREVIEW_EXT, 1); //BitmapFactory.decodeFile(sPathImage+"/"+page.getPageID()+Const.PAGE_PREVIEW_EXT);
        setImagePage(preview);
        preview=null;
    }

    /**
     * Applica il template selezionato
     *
     * @deprecated
     * */
    public void applyPaperTemplate() {
        try {
            Bitmap paper = BitmapFactory.decodeStream(mContext.getAssets().open("template/"+mCurrentDiary.getDiaryTemplate()+"/paper.jpg"));
            setImagePage(paper);
        } catch (IOException e) {
            Log.e(this.getClass().getCanonicalName(),"Error Apply template");
        }
    }

    /**
     * Imposta la dimensione delle path in base al dito o no
     * */
    public void setAutoFingerSize(boolean fingerSize) {
        mAutoFingerSize=fingerSize;
    }
    /**
     * Ritorna se autofinger
     * */
    public boolean getAutoFingerSize() {
        return mAutoFingerSize;
    }


    /**
     * #############################################*
     * #############################################*
     * #############################################*
     * ###############KEYBOARD METHOD###############*
     * #############################################*
     * #############################################*
     * #############################################*
     * */

    /**
     * cancella un carattere dalla stringa corrente
     *
     * */
    public void deleteCharRowString(int iRow) {

        try{
            String sCurrentText=aRows.get(iRow);
            sCurrentText=sCurrentText.substring(0,sCurrentText.length()-1);
            aRows.add(iRow, sCurrentText);
        }catch (StringIndexOutOfBoundsException e) {
            //Questa eccezione indica che i caratteri di riga sono finiti
            //quindi passo alla riga precedente
            aRows.remove(iCurrentRow);
            aPaint.remove(iCurrentRow);
            removeRowToDiary(iCurrentRow);
            iCurrentRow--;

            if(iCurrentRow<0){
                //Toast.makeText(mContext, mContext.getString(R.string.empty_page), Toast.LENGTH_SHORT).show();
                iCurrentRow=0;
                init(iCurrentRow);
            }
            Log.i(TAG,"Delete prev. row");

        }catch(ArrayIndexOutOfBoundsException e){

            //Toast.makeText(mContext, mContext.getString(R.string.empty_page), Toast.LENGTH_SHORT).show();
            if(iCurrentRow<0){
                iCurrentRow=0;
                init(iCurrentRow);
            }
        }
    }
    /**
     * Rimuove la riga al bean del diario
     *
     * */
    private void removeRowToDiary(int rowToRemove) {
        if(iCurrentRow>0){
            long mRowID=rowToRemove;
            Log.v(this.getClass().getCanonicalName(),"Delete Row String");
            mRows.remove(mRowID);
            aRowCustomPos.remove(rowToRemove);
            RowAsyncTask asyncTask = new RowAsyncTask(mContext);
            asyncTask.execute(mRowID,mCurrentDiary.getDiaryID(),mCurrentPage.getPageID());
        }
    }
    /**
     * Inizializza la riga che comincio a scrivere
     *
     *
     * */
    public void init(int iRow) {
        Log.v(this.getClass().getCanonicalName(),"init New Row:"+iCurrentRow);
        Paint TextPaintPage = new Paint();
        TextPaintPage.setColor(mTextColor);
        TextPaintPage.setAntiAlias(true);
        TextPaintPage.setTypeface(oFont);

        TextPaintPage.setFakeBoldText(true);

        TextPaintPage.setTextSize(fontSize);
        aPaint.add(iRow,TextPaintPage);

        String sPageText = "";
        aRows.add(iRow,sPageText);
		/*identifica se l'utente ha cliccato per scrivere proprio in quel punto*/
        boolean mTouchWrite = false;
        aRowCustomPos.add(iRow, mTouchWrite);
    }
    /**
     * setta la stringa in base all'id della riga passato e al carattere da aggiungere
     *
     * */
    public void setRowString(int iRow,char cChar){
        String sCurrentText=aRows.get(iRow);
        //Toast.makeText(mContext, "Text Paint Size:"+
        //		(int) getRow(iRow).measureText(sCurrentText+cChar, 0, (sCurrentText+cChar).length()), Toast.LENGTH_SHORT).show();
        int mPageWidth=0;

        int mTestSize=(int) getRow(iRow).measureText(sCurrentText+cChar, 0, (sCurrentText+cChar).length());
        if(mRows.get(iRow)!=null){
            mPageWidth=(iWidth-(getMarginRX()+mRows.get(iRow).getRowPosX()));
        }else{
            mPageWidth=(iWidth-(getMarginRX()));
        }
        if(mTestSize>=mPageWidth){
            //Passo automaticamente alla riga successica
            //Devo controllare ed andare a capo con una parola intera

            iCurrentRow++;
            init(iCurrentRow);
            iRow=iCurrentRow;
            aRows.add(iRow,findWord(sCurrentText)+String.valueOf(cChar));
        }else{
            aRows.add(iRow,sCurrentText+cChar);
        }

    }
    /**
     * Ritorna il margine in base alla risoluzione
     * in base alla proporzione:
     *
     * DEFAULT_WIDTH:DEFAULT_MARGIN_RX=iWidth:x
     *
     * */
    private int getMarginRX(){

        return (iWidth*Const.DEFAULT_MARGIN_RX)/Const.DEFAULT_WIDTH;
    }
    /**
     * Ritorna il margine in base alla risoluzione
     * in base alla proporzione:
     *
     * DEFAULT_HEIGTH:DEFAULT_MARGIN_SX=iHeight:x
     *
     * */
    private int getMarginSX(){

        return (iWidth*Const.DEFAULT_MARGIN_SX)/Const.DEFAULT_WIDTH;
    }
    /**
     * Ritorna il margine in base alla risoluzione
     * in base alla proporzione:
     *
     * DEFAULT_HEIGTH:DEFAULT_OFFSET_ROW=iHeight:x
     *
     * */
    private int getOffsetRow(){

        return (iHeight*Const.DEFAULT_OFFSET_ROW)/Const.DEFAULT_HEIGTH;
    }
    /**
     * Ritorna il margine in base alla risoluzione
     * in base alla proporzione:
     *
     * DEFAULT_HEIGTH:DEFAULT_OFFSET_ROW=iHeight:x
     *
     * */
    private int getInitialOffsetRow(){

		/*offsett di default*/
        int mTemplateOffsetFirstRow = Const.DEFAULT_OFFSET_TOP;
        return (iHeight* mTemplateOffsetFirstRow)/Const.DEFAULT_HEIGTH;
    }
    /**
     * Ritorna l'oggetto Paint in base all'id della riga passato
     *
     *
     * */
    private Paint getRow(int iRow){

        Paint TextPaintPage = new Paint();
        TextPaintPage.setColor(Color.BLACK);
        TextPaintPage.setAntiAlias(true);
        TextPaintPage.setTypeface(oFont);

        TextPaintPage.setFakeBoldText(true);

        TextPaintPage.setTextSize(fontSize);

        return aPaint.get(iRow);
    }
    /**
     * identifica la parola intera e va a capo tutta togliendola dalla riga corrente
     *
     * */
    private String findWord(String sCurrentText) {
        String sWord="";
        if(sCurrentText.endsWith(" ")){
            Log.v(TAG,"nuova parola non devo fare nulla");
            return "";
        }else{
            if(sCurrentText.indexOf(" ")==-1){
                Log.v(TAG,"nessun spazio sulla riga non devo fare nulla");
                return "";
            }else{
                sWord=sCurrentText.substring(sCurrentText.lastIndexOf(" ")+1,sCurrentText.length());
                sCurrentText = sCurrentText.substring(0,sCurrentText.lastIndexOf(" "));
                aRows.add(iCurrentRow-1,sCurrentText);
                return sWord;
            }
        }
    }

    /**
     * Ritorna la stringa in base all'id della riga passato
     *
     * */
    private String getRowString(int iRow){

        return aRows.get(iRow);
    }
    /**
     * Aggiunge la riga al bean del diario sull'onDraw
     *
     * */
    private void updateRowToDiary(int iRow,String rowText,int posX,int posY) {
        if(mCurrentPage==null) return;
        long mRowID=0;
        mRowID=iRow;
        Row mRow = new Row();
        mRow.setPageID(mCurrentPage.getPageID());
        mRow.setRowNumber(iRow);
        mRow.setRowText(rowText);
        mRow.setRowPosX(posX);
        mRow.setRowPosY(posY);
        mRow.setRowID(mRowID);
        mRows.put(mRowID, mRow);
        mRow=null;
    }
    /**
     *
     * Precarica la pagina con il testo presente nel bean
     *
     * */
    private void setRowsString(){
        Map<Long, Row> sortedRows = new TreeMap<Long, Row>(mRows);
        int iRow=0;

        for(Row oRow : sortedRows.values()){
            init(iRow);
            aRows.add(iRow,oRow.getRowText());
            iCurrentRow=iRow;
            //Log.v(this.getClass().getCanonicalName(),"row add: "+iRow);
            iRow++;
        }
        //NewPageView.this.invalidate();
    }
    /**
     * inizializza per una nuova pagina
     *
     * */
    public void newPage(){
        aRows.clear();
        aPaint.clear();

        mRows=(Hashtable<Long, Row>) mCurrentPage.getPageRows();

        if(mRows.size()>0){
            //Devo inserire il popolamento della pagina solo se ci sono righe
            Log.v(this.getClass().getCanonicalName(),"Load non Empty Page");
            setRowsString();
        }else{
            iCurrentRow=0;
            Log.v(this.getClass().getCanonicalName(),"Add empty Page");
            init(iCurrentRow);
        }

    }
    /**
     * Aggiunge un'immagine alla pagina dopo averla catturata
     * @deprecated
     * */
    public void addPicture(DiaryPicture oPicture, Bitmap bitmap) {
        if(mWritable) return;
        Canvas c=lockCanvas();
        if(c==null) return;
		/*if(mOneShot){
			iWidth=getWidth();
			//Cambia se apro la tastiera.
			iHeight=getHeight();
			mOneShot=false;
		}*/
        //mTextBitmap	= Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
        //mTextBitmap.eraseColor(Color.TRANSPARENT);
        synchronized (c) {
            if(mCanvasForMovePicture==null){
                mCanvasForMovePicture = new Canvas();
                mCanvasForMovePicture.setBitmap(mPictureBitmapForCanvas);
                Log.v(this.getClass().getCanonicalName(), "Create Canvas for move picture addPicture");
            }
            mCanvasForMovePicture.drawBitmap(bitmap, oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY(), mPaint);
            refresh(c);
        }
        if (c != null) {
            try{
                unlockCanvasAndPost(c);
                //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
            }catch(IllegalArgumentException e){
                //TODO
                Log.e(this.getClass().getCanonicalName(),"addPicture thread error canvas");
            }
            c=null;
        }
    }
    /**
     * identifica se sul touch ho cliccato su un'immagine
     *
     * @param xFinger
     * @param yFinger
     * */
    private void findIfTouchPicture(float xFinger, float yFinger) {
        if(mImages==null || sortedImages==null) return;
        for(DiaryPicture oPicture : sortedImages.values()){
            if(oPicture.getBitmapImage()==null) continue;
            //Log.v(this.getClass().getCanonicalName(),"oPicture X: "+oPicture.getDiaryPictureX()+" Y: "+oPicture.getDiaryPictureY());
            if((xFinger>=oPicture.getDiaryPictureX()
                    && xFinger<=(oPicture.getDiaryPictureX()+ oPicture.getBitmapImage().getWidth()))
                    && (yFinger>=oPicture.getDiaryPictureY()
                    && yFinger<=(oPicture.getDiaryPictureY()+ oPicture.getBitmapImage().getHeight()))
                    ){
                //La coordinata X Y rientra nell'immagine
                isPirtureClick=true;
                mPictureToMove=oPicture;


                //Toast.makeText(mContext, "Bitmap: Cliccabled", Toast.LENGTH_SHORT).show();

            }
        }
    }

    /**
     * Se l'utente clicca sul testo di una riga creo una textview per la modifica.
     *
     *
     * */
    private boolean findIfTouchOnText(float xFinger, float yFinger) {
        int nRows=mRows.size()-1;
        int iRowX=0;
        int iRowY=0;
        for(long i=0;i<nRows;i++){
            if(mRows.get(i)==null) return false;
            iRowX=mRows.get(i).getRowPosX();
            iRowY=mRows.get(i).getRowPosY();
            if(Math.round(Math.abs(iRowY-yFinger))<=10){
                final Row oRowToEdit = mRows.get(i);
                //Toast.makeText(mContext, "Touch on Text: "+mRows.get(i).getRowText()
                //        , Toast.LENGTH_LONG).show();

                oRowEdit.setText(mRows.get(i).getRowText());

                RelativeLayout.LayoutParams oLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

                //oLayout.setMargins(iRowX,iHeight-(mSoftKeyHeight+mSystemHeight),iRowX,0);
                oLayout.setMargins(iRowX,-iHeight,iRowX,0);
                oRowEdit.setLayoutParams(oLayout);
                oRowEdit.setSingleLine(true);
                oRowEdit.setBackgroundColor(Color.DKGRAY);

                oRowEdit.setAlpha(0.7f);
                ((LinearLayout) getParent()).removeView(oRowEdit);
                ((LinearLayout) getParent()).addView(oRowEdit);
                oRowEdit.requestFocus();
                InputMethodManager imm = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

                oRowEdit.setOnKeyListener(new OnKeyListener() {

                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        if (i == KeyEvent.KEYCODE_ENTER) {
                            InputMethodManager imm = (InputMethodManager) mContext
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
                            if(oRowEdit.getText().toString().length()==0){
                                //Delete Row
                                removeRowToDiary(oRowToEdit.getRowNumber());
                                aRows.remove(oRowToEdit.getRowNumber());
                                oRowToEdit.setRowText(oRowEdit.getText().toString());
                                ((LinearLayout) getParent()).removeView(oRowEdit);
                                mRows.remove(oRowToEdit.getRowID());
                            }else{
                                aRows.set(oRowToEdit.getRowNumber(), oRowEdit.getText().toString());
                                oRowToEdit.setRowText(oRowEdit.getText().toString());
                                ((LinearLayout) getParent()).removeView(oRowEdit);
                                mRows.put(oRowToEdit.getRowID(), oRowToEdit);
                            }

                            drawTextOnSurface();
                            postInvalidate();
                        }else if (i == KeyEvent.KEYCODE_BACK) {
                            ((LinearLayout) getParent()).removeView(oRowEdit);

                        }

                        return false;
                    }
                });
                return true;
            }
        }
        return false;
    }
    /**
     * carica le immagini della pagina salvata.
     *
     * @param pictureToMove
     * @param moveY
     * @param moveX
     *
     * */
    private void movePicture(DiaryPicture pictureToMove, float moveX, float moveY) {
        if(mWritable) return;
        Canvas c=lockCanvas();
        synchronized (c) {
            if(mPictureBitmapForCanvas==null){
                mPictureBitmapForCanvas = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
                MatrixPictureCanvas = new Matrix();
                Log.v(this.getClass().getCanonicalName(),"Create Bitmap for move picture");
            }


            //Cancello la vecchia immagine
            mPictureBitmapForCanvas.eraseColor(Color.TRANSPARENT);

            if(mCanvasForMovePicture ==null){
                mCanvasForMovePicture = new Canvas();
                mCanvasForMovePicture.setBitmap(mPictureBitmapForCanvas);
                Log.v(this.getClass().getCanonicalName(), "Create Canvas for move picture");
            }

            //if(bmpPath!=null) mCanvasForMovePicture.drawBitmap(bmpPath, MatrixPictureCanvas, null);
            //if(mBitmapForPage !=null) mCanvasForMovePicture.drawBitmap(mBitmapForPage, MatrixPictureCanvas, null);
            //if(mTextBitmap!=null) mCanvasForMovePicture.drawBitmap(mTextBitmap, MatrixPictureCanvas, null);

            if(mCanvasForMovePicture!=null &&
                    pictureToMove!=null){
                //Rect fromRect1 = new Rect(0, 0, , bgrH);
              /*  Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postTranslate(-pictureToMove.getBitmapImage().getWidth() / 2, -pictureToMove.getBitmapImage().getHeight() / 2); // Centers image
                matrix.postRotate(mAngle);
                matrix.postTranslate(moveX-(pictureToMove.getBitmapImage().getWidth()/2), moveY-(pictureToMove.getBitmapImage().getHeight()/2));
                mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), matrix, null);*/
                mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), pictureToMove.getDiaryPictureX(), pictureToMove.getDiaryPictureY(), mPaint);
                //SE PORTO L'IMMAGINE IN BASSO OLTRE IL BORDO CANCELLO L'IMMAGINE.
                if((moveY+(pictureToMove.getBitmapImage().getHeight() / 2.5))>iHeight){
                    //Toast.makeText(mContext, "DELETE IMAGE", Toast.LENGTH_LONG).show();
                    isPirtureDelete=true;
                }
                //mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), moveX-(pictureToMove.getBitmapImage().getWidth()/2), moveY-(pictureToMove.getBitmapImage().getHeight()/2), new Paint());

                /*mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), new Rect((int)moveX-(pictureToMove.getBitmapImage().getWidth()/2),
                                                                                (int)moveY-(pictureToMove.getBitmapImage().getHeight()/2),
                                                                                pictureToMove.getBitmapImage().getWidth(), pictureToMove.getBitmapImage().getHeight()),
                                                                                new Rect((int)moveX-(pictureToMove.getBitmapImage().getWidth()/2),
                                                                                        (int)moveY-(pictureToMove.getBitmapImage().getHeight()/2), pictureToMove.getBitmapImage().getWidth(),
                                                                                        pictureToMove.getBitmapImage().getHeight()), mPaint);*/

                pictureToMove.setDiaryPictureX((int) moveX-(pictureToMove.getBitmapImage().getWidth()/2));
                pictureToMove.setDiaryPictureY((int) moveY-(pictureToMove.getBitmapImage().getHeight()/2));
                mImages.put(pictureToMove.getDiaryPictureID(), pictureToMove);
            }
            //Metto le altre immagini
            for(DiaryPicture oPicture : sortedImages.values()){
                if(oPicture.getBitmapImage()==null) continue;
                if(mCanvasForMovePicture!=null &&
                        oPicture!=null
                        && oPicture!=pictureToMove){
                	/* Matrix matrix = new Matrix();
                     matrix.reset();
                     matrix.postTranslate(-oPicture.getBitmapImage().getWidth() / 2, -oPicture.getBitmapImage().getHeight() / 2); // Centers image
                     matrix.postRotate(oPicture.getDiaryPictureRotation());
                     matrix.postTranslate(oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY());
                     mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), matrix, null);*/
                    if(!oPicture.getBitmapImage().isRecycled()) mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY(), mPaint);
                }
            }

            //Aggiungo l'icona per cancellare l'immagine
            //mCanvasForMovePicture.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_action_delete),iWidth/2,iHeight-50,new Paint());

            refresh(c);
            //c.drawBitmap(mPictureBitmapForCanvas, 0, 0, new Paint());
        }
        if (c != null) {
            try{
                unlockCanvasAndPost(c);
                //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
            }catch(IllegalArgumentException e){
                //TODO
                Log.e(this.getClass().getCanonicalName(),"movePicture thread error canvas");
            }
            c=null;
            //mCanvasForMovePicture =null;
            //mPictureBitmapForCanvas.recycle();
            //mPictureBitmapForCanvas=null;
        }
    }
    /**
     * carica le immagini della pagina salvata.
     *
     * @param pictureToMove
     * @param moveY
     * @param moveX
     * @deprecated
     * */
    private void zoomPicture(DiaryPicture pictureToMove) {
        if(mWritable) return;
        Canvas c=lockCanvas();
        if(c==null) return;
        synchronized (c) {
            if(mPictureBitmapForCanvas==null){
                mPictureBitmapForCanvas = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
                MatrixPictureCanvas = new Matrix();
                Log.v(this.getClass().getCanonicalName(),"Create Bitmap for move picture");
            }


            //Cancello la vecchia immagine
            mPictureBitmapForCanvas.eraseColor(Color.TRANSPARENT);

            if(mCanvasForMovePicture ==null){
                mCanvasForMovePicture = new Canvas();
                mCanvasForMovePicture.setBitmap(mPictureBitmapForCanvas);
                Log.v(this.getClass().getCanonicalName(), "Create Canvas for move picture");
            }

            //if(bmpPath!=null) mCanvasForMovePicture.drawBitmap(bmpPath, MatrixPictureCanvas, null);
            //if(mBitmapForPage !=null) mCanvasForMovePicture.drawBitmap(mBitmapForPage, MatrixPictureCanvas, null);
            //if(mTextBitmap!=null) mCanvasForMovePicture.drawBitmap(mTextBitmap, MatrixPictureCanvas, null);

            if(mCanvasForMovePicture!=null &&
                    pictureToMove!=null){
                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize=Const.SAMPLESIZEIMAGE;
                options.inSampleSize=-(mImageZoom/2);


                pictureToMove.setBitmapImage(BitmapFactory.decodeFile(pictureToMove.getDiaryImageURI(), options));

                mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), pictureToMove.getDiaryPictureX(), pictureToMove.getDiaryPictureY(), mPaint);

                mImages.put(pictureToMove.getDiaryPictureID(), pictureToMove);
                Log.v(this.getClass().getCanonicalName(), "Zoom Picture: "+mImageZoom);

            }
            //Metto le altre immagini
            for(DiaryPicture oPicture : sortedImages.values()){
                if(oPicture.getBitmapImage()==null) continue;
                if(mCanvasForMovePicture!=null &&
                        oPicture!=null
                        && oPicture!=pictureToMove){
                	/* Matrix matrix = new Matrix();
                     matrix.reset();
                     matrix.postTranslate(-oPicture.getBitmapImage().getWidth() / 2, -oPicture.getBitmapImage().getHeight() / 2); // Centers image
                     matrix.postRotate(oPicture.getDiaryPictureRotation());
                     matrix.postTranslate(oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY());
                     mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), matrix, null);*/
                    if(!oPicture.getBitmapImage().isRecycled()) mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY(), mPaint);
                }
            }

            //Aggiungo l'icona per cancellare l'immagine
            //mCanvasForMovePicture.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_action_delete),iWidth/2,iHeight-50,new Paint());

            refresh(c);
            //c.drawBitmap(mPictureBitmapForCanvas, 0, 0, new Paint());
        }
        if (c != null) {
            try{
                unlockCanvasAndPost(c);
                //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
            }catch(IllegalArgumentException e){
                //TODO
                Log.e(this.getClass().getCanonicalName(),"zoomPicture thread error canvas");
            }
            c=null;
            //mCanvasForMovePicture =null;
            //mPictureBitmapForCanvas.recycle();
            //mPictureBitmapForCanvas=null;
        }
    }
    /**
     * ruota l'immagine selezionata a step di 90°
     *
     *
     *
     * */
    private void rotatePicture(DiaryPicture pictureToRotate){
        if(mWritable) return;
        Canvas c=lockCanvas();
        synchronized (c) {
            if(mPictureBitmapForCanvas==null){
                mPictureBitmapForCanvas = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
                MatrixPictureCanvas = new Matrix();
                Log.v(this.getClass().getCanonicalName(),"Create Bitmap for move picture");
            }


            //Cancello la vecchia immagine
            mPictureBitmapForCanvas.eraseColor(Color.TRANSPARENT);

            if(mCanvasForMovePicture ==null){
                mCanvasForMovePicture = new Canvas();
                mCanvasForMovePicture.setBitmap(mPictureBitmapForCanvas);
                Log.v(this.getClass().getCanonicalName(),"Create Canvas for move picture");
            }


            if(mCanvasForMovePicture!=null &&
                    pictureToRotate!=null){
            	/*mAngle=pictureToRotate.getDiaryPictureRotation()+90;

            	//Ruoto l'immagine originale e quella scalata
            	Bitmap mOrigianlImage = BitmapFactory.decodeFile(pictureToRotate.getDiaryImageURI());
            	Canvas mRotateCanvas=new Canvas(mOrigianlImage);
            	Matrix matrix = new Matrix();
            	matrix.reset();
            	matrix.postTranslate(-pictureToRotate.getBitmapImage().getWidth() / 2, -pictureToRotate.getBitmapImage().getHeight() / 2); // Centers image
            	matrix.postRotate(mAngle);
            	matrix.postTranslate(pictureToRotate.getBitmapImage().getWidth(), pictureToRotate.getBitmapImage().getHeight());
            	mRotateCanvas.drawBitmap(mOrigianlImage, matrix,new Paint());

            	//Ruoto l'immagine originale e quella scalata

            	mCanvasForMovePicture.drawBitmap(pictureToRotate.getBitmapImage(), matrix, null);
            	*/
                pictureToRotate.setDiaryPictureRotation((int) mAngle);
                mImages.put(pictureToRotate.getDiaryPictureID(), pictureToRotate);
            }
            if(sortedImages==null) return;
            //Metto le altre immagini
            for(DiaryPicture oPicture : sortedImages.values()){
                if(oPicture.getBitmapImage()==null) continue;
                if(mCanvasForMovePicture!=null &&
                        oPicture!=null){
                    if(!oPicture.getBitmapImage().isRecycled()) mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY(), mPaint);
                }
            }

            refresh(c);
            //c.drawBitmap(mPictureBitmapForCanvas, 0, 0, new Paint());
        }
        if (c != null) {
            try{
                unlockCanvasAndPost(c);
                //Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
            }catch(IllegalArgumentException e){
                //TODO
                Log.e(this.getClass().getCanonicalName(),"rotatePicture thread error canvas");
            }
            c=null;
            //mCanvasForMovePicture =null;
            //mPictureBitmapForCanvas.recycle();
            //mPictureBitmapForCanvas=null;
        }
    }
    /**
     * Cancella l'immagine passata sia il file che il DB
     *
     * */
    private void deletePicture(){
        File _FileToDelete = new File(mPictureToMove.getDiaryImageURI());
        if(_FileToDelete.exists()){
            if(_FileToDelete.delete()){
                mImages.remove(mPictureToMove.getDiaryPictureID());
                DeletePictureAsyncTask asyncTask = new DeletePictureAsyncTask(mCurrentPage, mContext);
                asyncTask.execute(mPictureToMove.getDiaryPictureID());
                Toast.makeText(mContext, mContext.getString(R.string.delete), Toast.LENGTH_SHORT).show();
            }
        }else{
            //Cancello l'immagine anche se non esiste il file
            mImages.remove(mPictureToMove.getDiaryPictureID());
            DeletePictureAsyncTask asyncTask = new DeletePictureAsyncTask(mCurrentPage, mContext);
            asyncTask.execute(mPictureToMove.getDiaryPictureID());
            Toast.makeText(mContext, mContext.getString(R.string.delete), Toast.LENGTH_SHORT).show();
        }
        if(mPictureToMove!=null && mPictureToMove.getBitmapImage()!=null) mPictureToMove.getBitmapImage().recycle();
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 300 milliseconds
        v.vibrate(150);
        mPictureToMove=null;
        isPirtureDelete=false;
    }
    /***
     * TASK ASINCRONI
     *
     *
     * **/

    class PictureRotateTask extends AsyncTask<Void, Void, Boolean> {

        private DiaryPicture mPictureToSave;
        private String mFileName;
        private File mFile=null;
        private FileOutputStream out = null;
        private Bitmap.CompressFormat mCompress=null;
        private ProgressDialog oWaitForPage;

        public PictureRotateTask(DiaryPicture pictureTosave) {
            mPictureToSave = pictureTosave;
            if(pictureTosave!=null)  mFileName=mPictureToSave.getDiaryImageURI();
            oWaitForPage = null;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(mPictureToSave==null) return true;

            mAngle=mPictureToSave.getDiaryPictureRotation()+90;
            if(mAngle>360) mAngle = 0;

            //Ruoto l'immagine originale e quella scalata
            Bitmap immutable = BitmapFactory.decodeFile(mPictureToSave.getDiaryImageURI());
            Bitmap mOrigianlImage=null;
            if(immutable!=null){
                mOrigianlImage = immutable.copy(Bitmap.Config.ARGB_8888, true);
                immutable.recycle();
                immutable=null;
            }
            if(mOrigianlImage!=null){
                Canvas mRotateCanvas=new Canvas();
                Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postTranslate(-mPictureToSave.getBitmapImage().getWidth() / 2, -mPictureToSave.getBitmapImage().getHeight() / 2); // Centers image
                matrix.postRotate(mAngle);
                matrix.postTranslate(mPictureToSave.getBitmapImage().getWidth(), mPictureToSave.getBitmapImage().getHeight());
                Bitmap newRotateImage = Bitmap.createBitmap(mOrigianlImage,0,0,mOrigianlImage.getWidth(),mOrigianlImage.getHeight(),matrix,true);
                mRotateCanvas.drawBitmap(newRotateImage, 0,0,new Paint());

                try {
                    if(mPictureToSave!=null){
                        mFile = new File(mFileName);
                        out = new FileOutputStream(mFile);
                        if(mCompress==null){
                            mCompress=Bitmap.CompressFormat.JPEG;
                            Log.v(this.getClass().getCanonicalName(),"Compress JPEG");
                        }
                        newRotateImage.compress(mCompress, 90, out);
                        out.close();
                        out=null;
                        Log.v(this.getClass().getCanonicalName(),"Save Rotate image: "+mFileName);
                    }else{
                        Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
                    }
                } catch (IOException e) {
                    Log.e(this.getClass().getCanonicalName(),"Error saving image");
                }
            }
            //Reimposto il bitmap scalato
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize=Const.SAMPLESIZEIMAGE;
            options.inSampleSize=calculateInSampleSize();


            mPictureToSave.setBitmapImage(BitmapFactory.decodeFile(mFileName, options));
            return DiaryRepositoryHelper.updatePicturePosition(mContext, mPictureToSave);
        }

        @Override
        protected void onPreExecute() {
            oWaitForPage = ProgressDialog.show(mContext,mContext.getString(R.string.app_name),mContext.getString(R.string.wait),true,true,null);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(oWaitForPage!=null) oWaitForPage.dismiss();
            rotatePicture(mPictureToMove);
            super.onPostExecute(result);
        }
        /**
         * calcola il sample size
         * */
        public int calculateInSampleSize() {
            // Raw height and width of image
            final int height = Const.IMGHEIGHT;
            final int width = Const.IMGWIDTH;
            int inSampleSize = 1;
            int reqHeight = iWidth/Const.SAMPLESIZEIMAGE;
            int reqWidth = iHeight/Const.SAMPLESIZEIMAGE;
            //Log.v(BitmapFactoryHelper.class.getClass().getCanonicalName(),"reqWidth: "+reqWidth+" reqHeight:"+reqHeight);
            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);

                // Choose the smallest ratio as inSampleSize value, this will guarantee
                // a final image with both dimensions larger than or equal to the
                // requested height and width.
                inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
            }
            //Log.v(BitmapFactoryHelper.class.getClass().getCanonicalName(),"Sample rate: "+inSampleSize+" outH:"+height+" outW:"+width);
            return inSampleSize;
        }
    }
    class PictureTask extends  AsyncTask<Void, Void, Boolean> {

        private DiaryPicture mPictureToSave;

        public PictureTask(DiaryPicture pictureTosave) {
            mPictureToSave = pictureTosave;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return DiaryRepositoryHelper.updatePicturePosition(mContext, mPictureToSave);
        }
    }











    /**
     * End Old Class
     *
     *
     * **/


    /**
     * Inner class used to represent a 2D point.
     */
    private class Vector2D
    {
        public float x,y;
        public Vector2D(float x, float y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return "("+this.x+","+this.y+")";
        }

        public float length() {
            return (float) Math.sqrt(x * x + y * y);
        }

        public float lengthSquared() {
            return (x * x) + (y * y);
        }

        public boolean equals(Object o) {
            if (o instanceof Vector2D) {
                Vector2D p = (Vector2D) o;
                return p.x == x && p.y == y;
            }
            return false;
        }

        public Vector2D reverse() {
            return new Vector2D(-x,-y);
        }

        public Vector2D sum(Vector2D b) {
            return new Vector2D(x+b.x,y+b.y);
        }

        public Vector2D sub(Vector2D b) {
            return new Vector2D(x-b.x,y-b.y);
        }

        public float dot(Vector2D vec) {
            return (x * vec.x) + (y * vec.y);
        }

        public float cross(Vector2D a, Vector2D b) {
            return a.cross(b);
        }

        public float cross(Vector2D vec) {
            return x * vec.y - y * vec.x;
        }

        public float distanceSquared(Vector2D other) {
            float dx = other.x - x;
            float dy = other.y - y;

            return (dx * dx) + (dy * dy);
        }

        public float distance(Vector2D other) {
            return (float) Math.sqrt(distanceSquared(other));
        }

        public float dotProduct(Vector2D other) {
            return other.x * x + other.y * y;
        }

        public Vector2D normalize() {
            float magnitude = (float) Math.sqrt(dotProduct(this));
            return new Vector2D(x / magnitude, y / magnitude);
        }

        public Vector2D mult(float scalar) {
            return new Vector2D(x*scalar,y*scalar);
        }
    }

    /**
     * Inner class used to make a fixed timed animation of the curl effect.
     */
    class FlipAnimationHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            PageCurlView.this.FlipAnimationStep();
        }

        public void sleep(long millis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), millis);
        }
    }

    /**
     * Base
     * @param context
     */
    public PageCurlView(Context context, Diary diary, Page page) {
        super(context);
//		if(isInEditMode()) return;
        mContext=context;
        mCurrentDiary=diary;
        mCurrentPage=page;

        if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
            sPathImage=Const.EXTDIR+mContext.getPackageName()+"/"+mCurrentDiary.getDiaryID() + "/Pictures";
        }else{
            sPathImage=Const.INTERNALDIR+mContext.getPackageName()+"/"+mCurrentDiary.getDiaryID() + "/Pictures";
        }

        setDrawingCacheEnabled(true);
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.TRANSPARENT);
        setLongClickable(true);

        mGestureDetector = new GestureDetector(mContext,this,null);

        //mSurfaceHolder = getHolder();
        //mSurfaceHolder.addCallback(this);
        setSurfaceTextureListener(this);
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(mCurrentColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(6);
        mPaint.setAntiAlias(true);
        oRowEdit =new EditText(mContext);

        {

            // Get data
            bEnableDebugMode = true;
            mCurlSpeed = 10;
            mUpdateRate = 60;
            mInitialEdgeOffset = 10;
            mCurlMode =1;

            Log.i(TAG, "mCurlSpeed: " + mCurlSpeed);
            Log.i(TAG, "mUpdateRate: " + mUpdateRate);
            Log.i(TAG, "mInitialEdgeOffset: " + mInitialEdgeOffset);
            Log.i(TAG, "mCurlMode: " + mCurlMode);

        }
        init();
        ResetClipEdge();
    }

    /**
     * Initialize the view
     */
    private final void init() {
        // Foreground text paint
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(16);
        mTextPaint.setColor(0xFF000000);

        // The shadow
        mTextPaintShadow = new TextPaint();
        mTextPaintShadow.setAntiAlias(true);
        mTextPaintShadow.setTextSize(16);
        mTextPaintShadow.setColor(0x00000000);

        // Base padding
        setPadding(3, 3, 3, 3);

        // The focus flags are needed
        setFocusable(true);
        setFocusableInTouchMode(true);

        mMovement =  new Vector2D(0,0);
        mFinger = new Vector2D(0,0);
        mOldMovement = new Vector2D(0,0);

        // Create our curl animation handler
        mAnimationHandler = new FlipAnimationHandler();

        // Create our edge paint
        mCurlEdgePaint = new Paint();
        mCurlEdgePaint.setColor(Color.WHITE);
        mCurlEdgePaint.setAntiAlias(true);
        mCurlEdgePaint.setStyle(Paint.Style.FILL);
        mCurlEdgePaint.setShadowLayer(10, -5, 5, 0x99000000);

        // Set the default props, those come from an XML :D
        mCurlSpeed = 30;
        mUpdateRate = 33;
        mInitialEdgeOffset = 20;
        mCurlMode = 1;

        // LEGACY PAGE HANDLING!

        // Create pages
        mPages = new ArrayList<Bitmap>();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize=Const.SAMPLESIZEDIARY;
        Log.v(this.getClass().getCanonicalName(),"load page preview: "+sPathImage+"/"+mCurrentPage.getPageID()+Const.CAMERA_PREVIEW_EXT);

        mPages.add(BitmapFactory.decodeFile(sPathImage + "/" + mCurrentPage.getPageID() + Const.CAMERA_PREVIEW_EXT, options));
        mPages.add(BitmapFactory.decodeFile(sPathImage + "/" + mCurrentPage.getPageID() + Const.CAMERA_PREVIEW_EXT, options));

        // Create some sample images
        mForeground = mPages.get(0);
        mBackground = mPages.get(1);
    }

    /**
     * Reset points to it's initial clip edge state
     */
    public void ResetClipEdge()
    {
        // Set our base movement
        mMovement.x = mInitialEdgeOffset;
        mMovement.y = mInitialEdgeOffset;
        mOldMovement.x = 0;
        mOldMovement.y = 0;

        // Now set the points
        // TODO: OK, those points MUST come from our measures and
        // the actual bounds of the view!
        mA = new Vector2D(mInitialEdgeOffset, 0);
        mB = new Vector2D(this.getWidth(), this.getHeight());
        mC = new Vector2D(this.getWidth(), 0);
        mD = new Vector2D(0, 0);
        mE = new Vector2D(0, 0);
        mF = new Vector2D(0, 0);
        mOldF = new Vector2D(0, 0);

        // The movement origin point
        mOrigin = new Vector2D(this.getWidth(), 0);
    }

    /**
     * Return the context which created use. Can return null if the
     * context has been erased.
     */
    private Context GetContext() {
        return mContext;
    }

    /**
     * See if the current curl mode is dynamic
     * @return TRUE if the mode is CURLMODE_DYNAMIC, FALSE otherwise
     */
    public boolean IsCurlModeDynamic()
    {
        return mCurlMode == CURLMODE_DYNAMIC;
    }

    /**
     * Set the curl speed.
     * @param curlSpeed - New speed in px/frame
     * @throws IllegalArgumentException if curlspeed < 1
     */
    public void SetCurlSpeed(int curlSpeed)
    {
        if ( curlSpeed < 1 )
            throw new IllegalArgumentException("curlSpeed must be greated than 0");
        mCurlSpeed = curlSpeed;
    }

    /**
     * Get the current curl speed
     * @return int - Curl speed in px/frame
     */
    public int GetCurlSpeed()
    {
        return mCurlSpeed;
    }

    /**
     * Set the update rate for the curl animation
     * @param updateRate - Fixed animation update rate in fps
     * @throws IllegalArgumentException if updateRate < 1
     */
    public void SetUpdateRate(int updateRate)
    {
        if ( updateRate < 1 )
            throw new IllegalArgumentException("updateRate must be greated than 0");
        mUpdateRate = updateRate;
    }

    /**
     * Get the current animation update rate
     * @return int - Fixed animation update rate in fps
     */
    public int GetUpdateRate()
    {
        return mUpdateRate;
    }

    /**
     * Set the initial pixel offset for the curl edge
     * @param initialEdgeOffset - px offset for curl edge
     * @throws IllegalArgumentException if initialEdgeOffset < 0
     */
    public void SetInitialEdgeOffset(int initialEdgeOffset)
    {
        if ( initialEdgeOffset < 0 )
            throw new IllegalArgumentException("initialEdgeOffset can not negative");
        mInitialEdgeOffset = initialEdgeOffset;
    }

    /**
     * Get the initial pixel offset for the curl edge
     * @return int - px
     */
    public int GetInitialEdgeOffset()
    {
        return mInitialEdgeOffset;
    }

    /**
     * Set the curl mode.
     * <p>Can be one of the following values:</p>
     * <table>
     * <colgroup align="left" />
     * <colgroup align="left" />
     * <tr><th>Value</th><th>Description</th></tr>
     * <tr><td><code>{@link #CURLMODE_SIMPLE com.dcg.pagecurl:CURLMODE_SIMPLE}</code></td><td>Curl target will move only in one axis.</td></tr>
     * <tr><td><code>{@link #CURLMODE_DYNAMIC com.dcg.pagecurl:CURLMODE_DYNAMIC}</code></td><td>Curl target will move on both X and Y axis.</td></tr>
     * </table>
     * @see #CURLMODE_SIMPLE
     * @see #CURLMODE_DYNAMIC
     * @param curlMode
     * @throws IllegalArgumentException if curlMode is invalid
     */
    public void SetCurlMode(int curlMode)
    {
        if ( curlMode != CURLMODE_SIMPLE &&
                curlMode != CURLMODE_DYNAMIC )
            throw new IllegalArgumentException("Invalid curlMode");
        mCurlMode = curlMode;
    }

    /**
     * Return an integer that represents the current curl mode.
     * <p>Can be one of the following values:</p>
     * <table>
     * <colgroup align="left" />
     * <colgroup align="left" />
     * <tr><th>Value</th><th>Description</th></tr>
     * <tr><td><code>{@link #CURLMODE_SIMPLE com.dcg.pagecurl:CURLMODE_SIMPLE}</code></td><td>Curl target will move only in one axis.</td></tr>
     * <tr><td><code>{@link #CURLMODE_DYNAMIC com.dcg.pagecurl:CURLMODE_DYNAMIC}</code></td><td>Curl target will move on both X and Y axis.</td></tr>
     * </table>
     * @see #CURLMODE_SIMPLE
     * @see #CURLMODE_DYNAMIC
     * @return int - current curl mode
     */
    public int GetCurlMode()
    {
        return mCurlMode;
    }

    /**
     * Enable debug mode. This will draw a lot of data in the view so you can track what is happening
     * @param bFlag - boolean flag
     */
    public void SetEnableDebugMode(boolean bFlag)
    {
        bEnableDebugMode = bFlag;
    }

    /**
     * Check if we are currently in debug mode.
     * @return boolean - If TRUE debug mode is on, FALSE otherwise.
     */
    public boolean IsDebugModeEnabled()
    {
        return bEnableDebugMode;
    }

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int finalWidth, finalHeight;
        finalWidth = measureWidth(widthMeasureSpec);
        finalHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(finalWidth, finalHeight);
    }

    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = specSize;
        }

        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = specSize;
        }
        return result;
    }

    /**
     * Render the text
     *
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    //@Override
    //protected void onDraw(Canvas canvas) {
    //	super.onDraw(canvas);
    //	canvas.drawText(mText, getPaddingLeft(), getPaddingTop() - mAscent, mTextPaint);
    //}

    //---------------------------------------------------------------
    // Curling. This handles touch events, the actual curling
    // implementations and so on.
    //---------------------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(!mWritable) mGestureDetector.onTouchEvent(event);

        if (!bBlockTouchInput) {

            // Get our finger position
            mFinger.x = event.getX();
            mFinger.y = event.getY();
            int width = getWidth();

            // Depending on the action do what we need to
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:


                    if(mWritable){
                        if(path==null){

                            path = new HandWritePath();
                            Date date = new Date();
                            DateFormat df = new SimpleDateFormat("ddmmssSSS");
                            int mPathID=0;
                            try{
                                mPathID=Integer.parseInt(df.format(date));
                            }catch(NumberFormatException e){
                                mPathID=0;
                            }
                            //path.setPathID(mPathID);
                        }
                        path.moveTo(event.getX(), event.getY());
                        path.lineTo(event.getX(), event.getY());
                        mPrevX=event.getX();
                        mPrevY=event.getY();
                        //path.addPoint(event.getX(), event.getY());
                        //path.setDiaryID(mCurrentPage.getDiaryID());
                        //path.setPageID(mCurrentPage.getPageID());
                        if(mPaint==null){
                            mPaint = new Paint();
                        }
                        mPaint.setDither(true);
                        mPaint.setColor(mCurrentColor);
                        mPaint.setStyle(Paint.Style.STROKE);
                        mPaint.setStrokeJoin(Paint.Join.ROUND);
                        mPaint.setStrokeCap(Paint.Cap.ROUND);

                        if(mAutoFingerSize){
                            mCurrentStrokeWidth=event.getSize()* Const.STROKEFACTOR;

                        }
                        mPaint.setStrokeWidth(mCurrentStrokeWidth);
                        mPaint.setAntiAlias(true);
                        if(mDeleteMode){
                            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                            //mOneShot=true;
                            //mPaint.setStyle(Paint.Style.FILL);
                            mPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
                            mCanvas.drawPath(path,mPaint);

                        }
                        path.setmPaint(mPaint);

                        //CREO CANVAS E IMMAGINE o carico una esistente
                        if (bmpPath==null) bmpPath = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

                        if(mCanvasPath==null) mCanvasPath= new Canvas(bmpPath);

                        mCanvasPath.drawPath(path, mPaint);

                        //PathAsyncTask PathasyncTask = new PathAsyncTask(mContext);
                        //PathasyncTask.execute(path);
                        //Log.v(this.getClass().getCanonicalName(), "DOWN X: "+event.getRawX()+" - Y: "+event.getRawY());
                        isPirtureLongClick=false;
                        isPirtureClick=false;
                    }




                    mOldMovement.x = mFinger.x;
                    mOldMovement.y = mFinger.y;

                    // If we moved over the half of the display flip to next
                    if (mOldMovement.x > (width >> 1)) {
                        mMovement.x = mInitialEdgeOffset;
                        mMovement.y = mInitialEdgeOffset;

                        // Set the right movement flag
                        bFlipRight = true;
                    } else {
                        // Set the left movement flag
                        bFlipRight = false;

                        // go to next previous page
                        previousView();

                        // Set new movement
                        mMovement.x = IsCurlModeDynamic()?width<<1:width;
                        mMovement.y = mInitialEdgeOffset;
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    mImageZoom=0;
                    if(mWritable){
                        path.quadTo(mPrevX, mPrevY, event.getX(), event.getY());

                        if(mDeleteMode){
                            //mOneShot=true;
                            path.close();
                            mPaint.setStyle(Paint.Style.FILL);
                            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                            mPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
                            mCanvas.drawPath(path,mPaint);
                            ((WriteActivity)getContext()).saveFromDeleted();
                        }
                        _graphics.add(path);
                        if(mCanvasPath!=null && path!=null) mCanvasPath.drawPath(path, mPaint);
                        mPrevX=0f;
                        mPrevY=0f;
                        path=null;
                        mPaint=null;
                        //System.gc();
                        path = new HandWritePath();
                        mPaint = new Paint();

                    }else{
                        if(isPirtureClick &&
                                isPirtureLongClick &&
                                !isPirtureDelete){
                            //Salvo le coordinate nuove della picture
                            PictureTask updatePicturePosition = new PictureTask(mPictureToMove);
                            updatePicturePosition.execute();
                            isPirtureClick=false;
                            isPirtureLongClick=false;
                            mPictureToMove=null;
                            mDrawBackground=false;

                            return true;
                        }

                        if(isPirtureDelete){
                            deletePicture();
                            movePicture(null, 0, 0);
                        }
                    }

                    bUserMoves=false;
                    bFlipping=true;
                    FlipAnimationStep();
                    break;
                case MotionEvent.ACTION_MOVE:

                    if(mWritable){
                        if(mAutoFingerSize){
                            mCurrentStrokeWidth=event.getSize()*1500;
                        }
                        velocity.addMovement(event);
                        velocity.computeCurrentVelocity(1000);
                        float x_velocity = velocity.getXVelocity();
                        float y_velocity = velocity.getYVelocity();
                        //RESIZE PATH SIZE
                        if(!mDeleteMode && (y_velocity>700 || x_velocity>700)){
                            path.quadTo(mPrevX, mPrevY, event.getX(), event.getY());
                            mCanvasPath.drawPath(path, mPaint);
                            path = new HandWritePath();
                            path.moveTo(event.getX(), event.getY());
                            path.lineTo(event.getX(), event.getY());
                            mPaint.setDither(true);

                            mPaint.setColor(Color.argb(Color.alpha(mCurrentColor)-(int)(255*0.15), Color.red(mCurrentColor), Color.green(mCurrentColor),Color.blue(mCurrentColor)));
                            //mPaint.setAlpha((int) (200/(Math.abs(y_velocity)/1000)));
                            mPaint.setStyle(Paint.Style.STROKE);
                            mPaint.setStrokeJoin(Paint.Join.ROUND);
                            mPaint.setStrokeCap(Paint.Cap.ROUND);

                            if((Math.abs(y_velocity)/1000)>1){
                                mPaint.setStrokeWidth((float) (mCurrentStrokeWidth/(Math.abs(y_velocity)/1000)));
                            }else{
                                mPaint.setStrokeWidth(mCurrentStrokeWidth);
                            }
                            //Log.v(this.getClass().getCanonicalName(), "Old Size: "+mCurrentStrokeWidth+" - NEW Size: "+(mCurrentStrokeWidth/(Math.abs(y_velocity)/1000)));
                            mPaint.setAntiAlias(true);
                            path.setmPaint(mPaint);
                        }

                        path.quadTo(mPrevX, mPrevY, event.getX(), event.getY());
                        if(mDeleteMode){
                            mCanvas.drawPath(path,mPaint);
                        }
                        _graphics.add(path);
                        mCanvasPath.drawPath(path, mPaint);
                        mPrevX=event.getX();
                        mPrevY=event.getY();
                    }else{
                        if(isPirtureClick &&
                                isPirtureLongClick){
                            movePicture(mPictureToMove,event.getX(),event.getY());
                        }


                    }



                    bUserMoves=true;

                    // Get movement
                    mMovement.x -= mFinger.x - mOldMovement.x;
                    mMovement.y -= mFinger.y - mOldMovement.y;
                    mMovement = CapMovement(mMovement, true);

                    // Make sure the y value get's locked at a nice level
                    if ( mMovement.y  <= 1 )
                        mMovement.y = 1;

                    // Get movement direction
                    bFlipRight = mFinger.x < mOldMovement.x;

                    // Save old movement values
                    mOldMovement.x  = mFinger.x;
                    mOldMovement.y  = mFinger.y;

                    // Force a new draw call
                    DoPageCurl();
                    this.invalidate();
                    break;
                case MotionEvent.ACTION_POINTER_3_DOWN:

                    if(path!=null){
                        Log.v(this.getClass().getCanonicalName(),"ERASER");
                        path.close();
                        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                        mCanvasPath.drawPath(path,mPaint);
                    }
                    break;
            }

        }

        // TODO: Only consume event if we need to.
        return true;
    }

    /**
     * Make sure we never move too much, and make sure that if we
     * move too much to add a displacement so that the movement will
     * be still in our radius.
     * @param bMaintainMoveDir - Cap movement but do not change the
     * current movement direction
     * @return Corrected point
     */
    private Vector2D CapMovement(Vector2D point, boolean bMaintainMoveDir)
    {
        // Make sure we never ever move too much
        if (point.distance(mOrigin) > mFlipRadius)
        {
            if ( bMaintainMoveDir )
            {
                // Maintain the direction
                point = mOrigin.sum(point.sub(mOrigin).normalize().mult(mFlipRadius));
            }
            else
            {
                // Change direction
                if ( point.x > (mOrigin.x+mFlipRadius))
                    point.x = (mOrigin.x+mFlipRadius);
                else if ( point.x < (mOrigin.x-mFlipRadius) )
                    point.x = (mOrigin.x-mFlipRadius);
                point.y = (float) (Math.sin(Math.acos(Math.abs(point.x-mOrigin.x)/mFlipRadius))*mFlipRadius);
            }
        }
        return point;
    }

    /**
     * Execute a step of the flip animation
     */
    public void FlipAnimationStep() {
        if ( !bFlipping )
            return;

        int width = getWidth();

        // No input when flipping
        bBlockTouchInput = true;

        // Handle speed
        float curlSpeed = mCurlSpeed;
        if ( !bFlipRight )
            curlSpeed *= -1;

        // Move us
        mMovement.x += curlSpeed;
        mMovement = CapMovement(mMovement, false);

        // Create values
        DoPageCurl();

        // Check for endings :D
        if (mA.x < 1 || mA.x > width - 1) {
            bFlipping = false;
            if (bFlipRight) {
                //SwapViews();
                nextView();
            }
            ResetClipEdge();

            // Create values
            DoPageCurl();

            // Enable touch input after the next draw event
            bEnableInputAfterDraw = true;
        }
        else
        {
            mAnimationHandler.sleep(mUpdateRate);
        }

        // Force a new draw call
        this.invalidate();
    }

    /**
     * Do the page curl depending on the methods we are using
     */
    private void DoPageCurl()
    {
        if(bFlipping){
            if ( IsCurlModeDynamic() )
                doDynamicCurl();
            else
                doSimpleCurl();

        } else {
            if ( IsCurlModeDynamic() )
                doDynamicCurl();
            else
                doSimpleCurl();
        }
    }

    /**
     * Do a simple page curl effect
     */
    private void doSimpleCurl() {
        int width = getWidth();
        int height = getHeight();

        // Calculate point A
        mA.x = width - mMovement.x;
        mA.y = height;

        // Calculate point D
        mD.x = 0;
        mD.y = 0;
        if (mA.x > width / 2) {
            mD.x = width;
            mD.y = height - (width - mA.x) * height / mA.x;
        } else {
            mD.x = 2 * mA.x;
            mD.y = 0;
        }

        // Now calculate E and F taking into account that the line
        // AD is perpendicular to FB and EC. B and C are fixed points.
        double angle = Math.atan((height - mD.y) / (mD.x + mMovement.x - width));
        double _cos = Math.cos(2 * angle);
        double _sin = Math.sin(2 * angle);

        // And get F
        mF.x = (float) (width - mMovement.x + _cos * mMovement.x);
        mF.y = (float) (height - _sin * mMovement.x);

        // If the x position of A is above half of the page we are still not
        // folding the upper-right edge and so E and D are equal.
        if (mA.x > width / 2) {
            mE.x = mD.x;
            mE.y = mD.y;
        }
        else
        {
            // So get E
            mE.x = (float) (mD.x + _cos * (width - mD.x));
            mE.y = (float) -(_sin * (width - mD.x));
        }
    }

    /**
     * Calculate the dynamic effect, that one that follows the users finger
     */
    private void doDynamicCurl() {
        int width = getWidth();
        int height = getHeight();

        // F will follow the finger, we add a small displacement
        // So that we can see the edge
        mF.x = width - mMovement.x+0.1f;
        mF.y = height - mMovement.y+0.1f;

        // Set min points
        if(mA.x==0) {
            mF.x= Math.min(mF.x, mOldF.x);
            mF.y= Math.max(mF.y, mOldF.y);
        }

        // Get diffs
        float deltaX = width-mF.x;
        float deltaY = height-mF.y;

        float BH = (float) (Math.sqrt(deltaX * deltaX + deltaY * deltaY) / 2);
        double tangAlpha = deltaY / deltaX;
        double alpha = Math.atan(deltaY / deltaX);
        double _cos = Math.cos(alpha);
        double _sin = Math.sin(alpha);

        mA.x = (float) (width - (BH / _cos));
        mA.y = height;

        mD.y = (float) (height - (BH / _sin));
        mD.x = width;

        mA.x = Math.max(0,mA.x);
        if(mA.x==0) {
            mOldF.x = mF.x;
            mOldF.y = mF.y;
        }

        // Get W
        mE.x = mD.x;
        mE.y = mD.y;

        // Correct
        if (mD.y < 0) {
            mD.x = width + (float) (tangAlpha * mD.y);
            mE.y = 0;
            mE.x = width + (float) (Math.tan(2 * alpha) * mD.y);
        }
    }

    /**
     * Swap between the fore and back-ground.
     */
    @Deprecated
    private void SwapViews() {
        Bitmap temp = mForeground;
        mForeground = mBackground;
        mBackground = temp;
    }

    /**
     * Swap to next view
     */
    private void nextView() {
        int foreIndex = mIndex + 1;
        if(foreIndex >= mPages.size()) {
            foreIndex = 0;
        }
        int backIndex = foreIndex + 1;
        if(backIndex >= mPages.size()) {
            backIndex = 0;
        }
        mIndex = foreIndex;
        setViews(foreIndex, backIndex);
    }

    /**
     * Swap to previous view
     */
    private void previousView() {
        int backIndex = mIndex;
        int foreIndex = backIndex - 1;
        if(foreIndex < 0) {
            foreIndex = mPages.size()-1;
        }
        mIndex = foreIndex;
        setViews(foreIndex, backIndex);
    }

    /**
     * Set current fore and background
     * @param foreground - Foreground view index
     * @param background - Background view index
     */
    private void setViews(int foreground, int background) {
        mForeground = mPages.get(foreground);
        mBackground = mPages.get(background);
    }

    //---------------------------------------------------------------
    // Drawing methods
    //---------------------------------------------------------------

    /**
     * Called on the first draw event of the view
     * @param canvas
     */
    protected void onFirstDrawEvent(Canvas canvas) {

        mFlipRadius = getWidth();

        ResetClipEdge();
        DoPageCurl();
    }

    /**
     * Draw the foreground
     * @param canvas
     * @param rect
     * @param paint
     */
    private void drawForeground( Canvas canvas, Rect rect, Paint paint ) {
        canvas.drawBitmap(mForeground, null, rect, paint);

        // Draw the page number (first page is 1 in real life :D
        // there is no page number 0 hehe)
        drawPageNum(canvas, mIndex);
    }

    /**
     * Create a Path used as a mask to draw the background page
     * @return
     */
    private Path createBackgroundPath() {
        Path path = new Path();
        path.moveTo(mA.x, mA.y);
        path.lineTo(mB.x, mB.y);
        path.lineTo(mC.x, mC.y);
        path.lineTo(mD.x, mD.y);
        path.lineTo(mA.x, mA.y);
        return path;
    }

    /**
     * Draw the background image.
     * @param canvas
     * @param rect
     * @param paint
     */
    private void drawBackground( Canvas canvas, Rect rect, Paint paint ) {
        Path mask = createBackgroundPath();

        // Save current canvas so we do not mess it up
        canvas.save();
        canvas.clipPath(mask);
        canvas.drawBitmap(mBackground, null, rect, paint);

        // Draw the page number (first page is 1 in real life :D
        // there is no page number 0 hehe)
        drawPageNum(canvas, mIndex);

        canvas.restore();
    }

    /**
     * Creates a path used to draw the curl edge in.
     * @return
     */
    private Path createCurlEdgePath() {
        Path path = new Path();
        path.moveTo(mA.x, mA.y);
        path.lineTo(mD.x, mD.y);
        path.lineTo(mE.x, mE.y);
        path.lineTo(mF.x, mF.y);
        path.lineTo(mA.x, mA.y);
        return path;
    }

    /**
     * Draw the curl page edge
     * @param canvas
     */
    private void drawCurlEdge( Canvas canvas )
    {
        Path path = createCurlEdgePath();
        canvas.drawPath(path, mCurlEdgePaint);
    }

    /**
     * Draw page num (let this be a bit more custom)
     * @param canvas
     * @param pageNum
     */
    private void drawPageNum(Canvas canvas, int pageNum)
    {
        mTextPaint.setColor(Color.WHITE);
        String pageNumText = "- "+pageNum+" -";
        drawCentered(canvas, pageNumText,canvas.getHeight()-mTextPaint.getTextSize()-5,mTextPaint,mTextPaintShadow);
    }

    //---------------------------------------------------------------
    // Debug draw methods
    //---------------------------------------------------------------

    /**
     * Draw a text with a nice shadow
     */
    public static void drawTextShadowed(Canvas canvas, String text, float x, float y, Paint textPain, Paint shadowPaint) {
        canvas.drawText(text, x-1, y, shadowPaint);
        canvas.drawText(text, x, y+1, shadowPaint);
        canvas.drawText(text, x+1, y, shadowPaint);
        canvas.drawText(text, x, y-1, shadowPaint);
        canvas.drawText(text, x, y, textPain);
    }

    /**
     * Draw a text with a nice shadow centered in the X axis
     * @param canvas
     * @param text
     * @param y
     * @param textPain
     * @param shadowPaint
     */
    public static void drawCentered(Canvas canvas, String text, float y, Paint textPain, Paint shadowPaint)
    {
        float posx = (canvas.getWidth() - textPain.measureText(text))/2;
        drawTextShadowed(canvas, text, posx, y, textPain, shadowPaint);
    }

    /**
     * Draw debug info
     * @param canvas
     */
    private void drawDebug(Canvas canvas)
    {
        float posX = 10;
        float posY = 20;

        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setStyle(Style.STROKE);

        paint.setColor(Color.BLACK);
        canvas.drawCircle(mOrigin.x, mOrigin.y, getWidth(), paint);

        paint.setStrokeWidth(3);
        paint.setColor(Color.RED);
        canvas.drawCircle(mOrigin.x, mOrigin.y, getWidth(), paint);

        paint.setStrokeWidth(5);
        paint.setColor(Color.BLACK);
        canvas.drawLine(mOrigin.x, mOrigin.y, mMovement.x, mMovement.y, paint);

        paint.setStrokeWidth(3);
        paint.setColor(Color.RED);
        canvas.drawLine(mOrigin.x, mOrigin.y, mMovement.x, mMovement.y, paint);

        posY = debugDrawPoint(canvas,"A",mA,Color.RED,posX,posY);
        posY = debugDrawPoint(canvas,"B",mB,Color.GREEN,posX,posY);
        posY = debugDrawPoint(canvas,"C",mC,Color.BLUE,posX,posY);
        posY = debugDrawPoint(canvas,"D",mD,Color.CYAN,posX,posY);
        posY = debugDrawPoint(canvas,"E",mE,Color.YELLOW,posX,posY);
        posY = debugDrawPoint(canvas,"F",mF,Color.LTGRAY,posX,posY);
        posY = debugDrawPoint(canvas,"Mov",mMovement,Color.DKGRAY,posX,posY);
        posY = debugDrawPoint(canvas,"Origin",mOrigin,Color.MAGENTA,posX,posY);
        posY = debugDrawPoint(canvas,"Finger",mFinger,Color.GREEN,posX,posY);

        // Draw some curl stuff (Just some test)
		/*
		canvas.save();
		Vector2D center = new Vector2D(getWidth()/2,getHeight()/2);
	    //canvas.rotate(315,center.x,center.y);
	    
	    // Test each lines
		//float radius = mA.distance(mD)/2.f;
	    //float radius = mA.distance(mE)/2.f;
	    float radius = mA.distance(mF)/2.f;
		//float radius = 10;
	    float reduction = 4.f;
		RectF oval = new RectF();
		oval.top = center.y-radius/reduction;
		oval.bottom = center.y+radius/reduction;
		oval.left = center.x-radius;
		oval.right = center.x+radius;
		canvas.drawArc(oval, 0, 360, false, paint);
		canvas.restore();
		/**/
    }

    private float debugDrawPoint(Canvas canvas, String name, Vector2D point, int color, float posX, float posY) {
        return debugDrawPoint(canvas,name+" "+point.toString(),point.x, point.y, color, posX, posY);
    }

    private float debugDrawPoint(Canvas canvas, String name, float X, float Y, int color, float posX, float posY) {
        mTextPaint.setColor(color);
        drawTextShadowed(canvas,name,posX , posY, mTextPaint,mTextPaintShadow);
        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(color);
        canvas.drawPoint(X, Y, paint);
        return posY+15;
    }

}