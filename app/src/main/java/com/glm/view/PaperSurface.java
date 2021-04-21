package com.glm.view;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.glm.asyncTasks.RowAsyncTask;
import com.glm.bean.Diary;
import com.glm.bean.DiaryPicture;
import com.glm.bean.HandWritePath;
import com.glm.bean.Page;
import com.glm.bean.Row;
import com.glm.db.DiaryRepositoryHelper;
import com.glm.ememories.R;
import com.glm.labs.diary.ememories.Const;
import com.glm.utilities.image.InputConnetionEMS;

/** */
public class PaperSurface extends SurfaceView implements SurfaceHolder.Callback,  GestureDetector.OnGestureListener{
	private boolean mDeleteMode=false;
	private boolean cLocked = false;
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

	/**angolo di rotazione */
	private float mAngle=0f;

	/**Bitmap per canvas testo*/
	private Bitmap mTextBitmap = null;
	/**contiene le immagini della pagina se presenti*/
	private Hashtable<Long, DiaryPicture> mImages=null;
	private Map<Long, DiaryPicture> sortedImages=null;
	/**identifica se ho cliccato su un'immagine della pagina*/
	private boolean isPictureClick =false;
	/**identifica se ho long cliccato su un'immagine della pagina*/
	private boolean isPictureLongClick =false;
	/**identifica se durante lo spostamento ho spostato un'immagine nell'area di cancellazione*/
	private boolean isPictureDelete =false;
	private DiaryPicture mPictureToMove=null;
	private Canvas mCanvasForMovePicture = null;
	private Bitmap mPictureBitmapForCanvas = null;
	private Matrix mMatrixPictureCanvas=null;

	/**Canva nella quale disegno le path e poi salvo come immagine*/
	private Canvas mCanvasPath=null;
	private Bitmap bmpPath=null;

	private PaperSurface.DrawingThread _thread;

	/**contiene il bitmap della pagia corrente*/
	//private Bitmap mBmpPage;
	/**Pagina corrente che edito*/
	private Page mCurrentPage;
	private int mCurrentColor= Color.BLUE;
	private float mCurrentStrokeWidth=6;
	private Context mContext;
	private int iWidth=0;
	private int iHeight=0;
	private Diary mCurrentDiary;
	private boolean bScaled=true;
	private int mTextColor=Color.BLACK;
	/**identifica se disegnare il background e le vecchie linee per velocizzare la scrittura*/
	private boolean mDrawBackground=true;

	private float radius = 50.0f;
	private CornerPathEffect mCornerPathEffect =
			new CornerPathEffect(radius);
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
	/**Bitmap che contiene tutta la pagina*/
	private Bitmap mBitmapPageTotal;


	public PaperSurface(Context context) {
		super(context);
		getHolder().addCallback(this);

		if(isInEditMode()) return;
		mContext=context;
		setDrawingCacheEnabled(true);
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		//setZOrderOnTop(true);
		setFocusableInTouchMode(true);
		setBackgroundColor(Color.TRANSPARENT);
		setLongClickable(true);
		mGestureDetector = new GestureDetector(mContext,this,null);

		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

		mPaint = new Paint();
		mPaint.setPathEffect(mCornerPathEffect);
		mPaint.setDither(true);
		mPaint.setColor(mCurrentColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(6);
		mPaint.setAntiAlias(true);
		oRowEdit =new EditText(mContext);
	}

	public PaperSurface(Context context, Diary diary, Page page) {
		super(context);
		if(isInEditMode()) return;
		mContext=context;
		mCurrentDiary=diary;
		mCurrentPage=page;

		setFocusableInTouchMode(true);
		setLongClickable(true);

		mGestureDetector = new GestureDetector(mContext,this,null);

		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

		mPaint = new Paint();
		mPaint.setPathEffect(mCornerPathEffect);
		mPaint.setDither(true);
		mPaint.setColor(mCurrentColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(6);
		mPaint.setAntiAlias(true);
		oRowEdit =new EditText(mContext);

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		iWidth=getWidth();
		iHeight=getHeight();
		drawPicturePage();
		//createCanvasBackground();
		drawTextOnSurface();
		postInvalidate();
		PaperSurface.this.postInvalidate();
		Log.v(this.getClass().getCanonicalName(),"Surface Created");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        //_thread.setRunning(true);
        //_thread.start();
		iWidth=getWidth();
		iHeight=getHeight();
		drawPicturePage();
		createCanvasBackground();
		drawTextOnSurface();
		postInvalidate();
		PaperSurface.this.postInvalidate();
        Log.v(this.getClass().getCanonicalName(),"Surface Created");
		mBitmapPageTotal = Bitmap.createBitmap(iWidth, iHeight,Bitmap.Config.ARGB_8888);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        /*//_thread.setRunning(false);
        while (retry) {
            try {
                //_thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }*/
	}

	/**
	 * GESTURES METHODS
	 *
	 * */
	VelocityTracker velocity = VelocityTracker.obtain();

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		long touchDuration = 0;
		//if(_thread==null) _thread = new DrawingThread(this, this);

		if(!mWritable) mGestureDetector.onTouchEvent(event);

		/**Avvio il thread per vedere il longclick*/
		//oLongTouchThread.setEvent(event);
		//oLongTouchThread.run();
		//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "Finger Size: "+event.getSize()*1500);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
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
					mPaint.setPathEffect(mCornerPathEffect);
				}
				mPaint.setDither(true);
				mPaint.setColor(mCurrentColor);
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setStrokeJoin(Paint.Join.ROUND);
				mPaint.setStrokeCap(Paint.Cap.ROUND);

				if(mAutoFingerSize){
					mCurrentStrokeWidth=event.getSize()* Const.STROKEFACTOR;
					float mInitialStrokeWidth = mCurrentStrokeWidth;
				}
				mPaint.setStrokeWidth(mCurrentStrokeWidth);
				mPaint.setAntiAlias(true);
				if(mDeleteMode){
					mPaint.setStrokeWidth(mCurrentStrokeWidth);
					mPaint.setColor(Color.TRANSPARENT);
					mPaint.setAlpha(255);
					mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

					//mPaint.setColor(Color.DKGRAY);
					//mOneShot=true;
					mPaint.setStyle(Paint.Style.STROKE);
					//mPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
					//if(mCanvas!=null) mCanvas.drawPath(path,mPaint);
					init(mCurrentDiary,mCurrentPage);
					invalidate();
				}
				path.setmPaint(mPaint);

				//CREO CANVAS E IMMAGINE o carico una esistente
				if (bmpPath==null) bmpPath = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

				if(mCanvasPath==null) mCanvasPath= new Canvas(bmpPath);

				mCanvasPath.drawPath(path, mPaint);

				//PathAsyncTask PathasyncTask = new PathAsyncTask(mContext);
				//PathasyncTask.execute(path);
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "DOWN X: "+event.getRawX()+" - Y: "+event.getRawY());
				isPictureLongClick =false;
				isPictureClick =false;
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if(mWritable){
				if(mAutoFingerSize){
					mCurrentStrokeWidth=event.getSize()*1500;

               	    	/*if(((int) mInitialStrokeWidth-(int) mCurrentStrokeWidth)>3 ||
               	    			((int) mCurrentStrokeWidth-(int) mInitialStrokeWidth)<3){
               	    		path = new HandWritePath();
               	    		path.moveTo(event.getX(), event.getY());
               	    		mPaint.setDither(true);
                     	    mPaint.setColor(mCurrentColor);
                     	    mPaint.setStyle(Paint.Style.STROKE);
                     	    mPaint.setStrokeJoin(Paint.Join.ROUND);
                     	    mPaint.setStrokeCap(Paint.Cap.ROUND);
                     	    mPaint.setStrokeWidth(mCurrentStrokeWidth);
                     	    mPaint.setAntiAlias(true);
                     	    path.setmPaint(mPaint);
                    		 //path.setPathID(mPathID);
                             mPaint = new Paint();
                             if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "Finger Size approx: "+(int)mCurrentStrokeWidth);
                             mInitialStrokeWidth=mCurrentStrokeWidth;
               	    	}*/
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

					//mPaint.setColor(Color.argb(Color.alpha(mCurrentColor)-(int)(255*0.15), Color.red(mCurrentColor), Color.green(mCurrentColor),Color.blue(mCurrentColor)));
					mPaint.setColor(mCurrentColor);
					mPaint.setAlpha(Color.alpha(mCurrentColor));
					//mPaint.setAlpha((int) (200/(Math.abs(y_velocity)/1000)));

					mPaint.setStyle(Paint.Style.STROKE);
					mPaint.setStrokeJoin(Paint.Join.ROUND);
					mPaint.setStrokeCap(Paint.Cap.ROUND);
					//mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
					if((Math.abs(y_velocity)/1000)>1){
						mPaint.setStrokeWidth((float) (mCurrentStrokeWidth/(Math.abs(y_velocity)/1000)));
					}else{
						mPaint.setStrokeWidth(mCurrentStrokeWidth);
					}
					//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "Old Size: "+mCurrentStrokeWidth+" - NEW Size: "+(mCurrentStrokeWidth/(Math.abs(y_velocity)/1000)));
					mPaint.setAntiAlias(true);
					path.setmPaint(mPaint);
				}

				path.quadTo(mPrevX, mPrevY, event.getX(), event.getY());
				if(mDeleteMode){
					path.quadTo(mPrevX, mPrevY, event.getX(), event.getY());
					mCanvasPath.drawPath(path, mPaint);
					path = new HandWritePath();
					path.moveTo(event.getX(), event.getY());
					path.lineTo(event.getX(), event.getY());
					//mPaint.setDither(true);
					mPaint.setAlpha(255);
					mPaint.setColor(Color.TRANSPARENT);
					mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

					mPaint.setAntiAlias(true);
					path.setmPaint(mPaint);
					//mCanvas.drawPath(path,mPaint);
					init(mCurrentDiary,mCurrentPage);
					invalidate();
				}


				_graphics.add(path);
				mCanvasPath.drawPath(path, mPaint);
				mPrevX=event.getX();
				mPrevY=event.getY();
			}else{
				if(isPictureClick &&
						isPictureLongClick){
					movePicture(mPictureToMove,event.getX(),event.getY());
				}
				//TWO Finger Rotation
				if(event.getPointerCount()>1 && mPictureToMove!=null){
					rotateImageTwoFinger(mPictureToMove, event);
					//zoomPicture(mPictureToMove,event);
				}


				//TODO RIVEDERE LO ZOOM DELL'IMMAGINE
                     /*if(event.getPointerCount()>1){
                    	 float x= event.getX(0)-event.getX(1);
                    	 float y = event.getY(0)-event.getY(1);
                    	 newFingerDistance=(float) Math.sqrt(x*x+y*y);
                    	 findIfTouchPicture(event.getX(0), event.getY(0));
                    	 if(newFingerDistance>oldFingerDistance){
                    		 mImageZoom++;
                    		 zoomPicture(mPictureToMove);
                    		 oldFingerDistance=newFingerDistance;
                    	 }else{
                    		 mImageZoom--;
                    		 zoomPicture(mPictureToMove);
                    		 oldFingerDistance=newFingerDistance;
                    	 }

                     }*/
			}
			//PathAsyncTask PathasyncTask = new PathAsyncTask(mContext);
			//PathasyncTask.execute(path);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			mImageZoom=0;
			if(mWritable){
				//path.lineTo(event.getX(), event.getY());
				//path.addPoint(event.getX(), event.getY());
				path.quadTo(mPrevX, mPrevY, event.getX(), event.getY());

				if(mDeleteMode){
					//mOneShot=true;
					path.close();
					mPaint.setStyle(Paint.Style.STROKE);
					mPaint.setAlpha(255);
					mPaint.setColor(Color.TRANSPARENT);
					mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
					//mPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
					//mCanvas.drawPath(path,mPaint);
					//TODO ((DrawerWritePageActivity)getContext()).saveFromDeleted();

					saveHandWrite();
					init(mCurrentDiary,mCurrentPage);
					invalidate();
					postInvalidate();
					createCanvasBackground();
				}
				_graphics.add(path);
				if(mCanvasPath!=null && path!=null) mCanvasPath.drawPath(path, mPaint);
				mPrevX=0f;
				mPrevY=0f;
				//PathAsyncTask PathasyncTask = new PathAsyncTask(mContext);
				//PathasyncTask.execute(path);
				//mCurrentPage.setmPath(_graphics);
				path=null;
				mPaint=null;
				//System.gc();
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
				mPaint = new Paint();
				mPaint.setPathEffect(mCornerPathEffect);

			}else{
				if(isPictureClick &&
						isPictureLongClick &&
						!isPictureDelete){


					//Salvo le coordinate nuove della picture
					PaperSurface.PictureTask updatePicturePosition = new PaperSurface.PictureTask(mPictureToMove);
					updatePicturePosition.execute();
					isPictureClick =false;
					isPictureLongClick =false;
					mPictureToMove=null;
					mDrawBackground=false;

                         /*if(mPictureBitmapForCanvas!=null)mPictureBitmapForCanvas.recycle();
                         mPictureBitmapForCanvas=null;
                         MatrixPictureCanvas = null;
                         mCanvasForMovePicture=null;
                         freeBitmap();
                         init(mCurrentDiary, mCurrentPage);*/

					return true;
				}
				//DELETE PICTURE DISABLED
                    /*if(isPictureDelete){
                        deletePicture();
                        movePicture(null, 0, 0);
                    }*/
                    /*//stop timer
                    touchDuration = System.currentTimeMillis() - touchTime;
                    if(isPirtureClick &&
                    	 touchDuration < 800 ){


                        if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "Rotate Picture 90° per Step");
                    }*/
			}
                 /*//stop timer
                 touchDuration = System.currentTimeMillis() - touchTime;

                 if ( touchDuration < 800 ){
                     isPirtureLongClick=false;
                     // if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "SHORT CLICK X: "+event.getX()+" - Y: "+event.getY());
                 }else{
                      isPirtureLongClick=true;
                      //createCanvasBackground();
                      if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "LONG CLICK X: "+event.getX()+" - Y: "+event.getY());
                 }*/
		}else if(event.getAction() == MotionEvent.ACTION_POINTER_3_DOWN){

			if(path!=null){
				if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"ERASER");
				path.close();
				mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
				mCanvasPath.drawPath(path,mPaint);
			}

		}
		return true;
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
		findIfTouchPicture(motionEvent);

		if(mPictureToMove!=null){
            /*//Apri l'immagine selezionata
            Intent intentGallery = new Intent();
            intentGallery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentGallery.setAction(Intent.ACTION_VIEW);
            intentGallery.setDataAndType(Uri.parse("file://"+ mPictureToMove.getDiaryImageURI()), "image/*");
            mContext.getApplicationContext().startActivity(intentGallery);
            if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "onSingleTapUp Via GestureDetector");
            mPictureToMove=null;*/
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

		findIfTouchPicture(motionEvent);
		if(mPictureToMove!=null){
			isPictureLongClick =true;
			Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(VibrationEffect.createOneShot(150l,1));
			//v.vibrate(100);
			selectPicture(mPictureToMove,true);
		}

	}

	@Override
	public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {

		findIfTouchPicture(motionEvent);
		if(mPictureToMove!=null){
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"onFling Press Via GestureDetector");
			//PictureRotateTask updatePictureAngle = new PictureRotateTask(mPictureToMove);
			//updatePictureAngle.execute();
		}
		return false;
	}
	/**FINE GESTURES METHODS**/

	private void saveHandWrite(){
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//File mFile=null;
		//FileOutputStream out = null;
		//String sPathImage="";
        /*if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
            sPathImage=Const.EXTDIR+mContext.getPackageName()+"/"+mCurrentDiary.getDiaryID() + "/Pictures";
        }else{
            sPathImage=Const.INTERNALDIR+mContext.getPackageName()+"/"+mCurrentDiary.getDiaryID() + "/Pictures";
        }

        File dir = new File(sPathImage);

        if(!dir.exists()) {
            dir.mkdirs();
        }*/
		//Task async per salvare l'immagine.
		final Bitmap oBmp = getHandWritePath();
		try {
			if(oBmp!=null){
				//mFile = new File(sPathImage+"/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT);
				//out = new FileOutputStream(mFile);

				//.compress(Bitmap.CompressFormat.PNG, 90, out);
				new Thread(new Runnable() {
					@Override
					public void run() {
						oBmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
					}
				}).start();
				byte[] byteArray = stream.toByteArray();
				DiaryRepositoryHelper.dumpHandWritePage(mContext,mCurrentPage,byteArray);
				//out.close();
				//out=null;
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"saving hand write image: "+sPathImage+"/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT);
			}else{
				if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
			}
		} catch (Exception e) {
			if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"Error saving image");
		}
	}

	/**
	 * Rotazione dell'immagine con due dita
	 * */
	private void rotateImageTwoFinger(DiaryPicture pictureToRotate, MotionEvent event) {
		double deltaX = (event.getX(0) - event.getX(1));
		double deltaY = (event.getY(0) - event.getY(1));
		double radians = Math.atan2(deltaY, deltaX);
		float rotation = (float) Math.toDegrees(radians);
		rotatePicture(pictureToRotate, rotation);
	}
	/**
	 * ruota l'immagine in vase ai gradi passati
	 * **/
	private void rotatePicture(DiaryPicture pictureToRotate, float rotation){
		if(mWritable) return;
		Canvas c=mSurfaceHolder.lockHardwareCanvas();
		//c.setBitmap(mBitmapPageTotal);
		cLocked=true;
		if(c!=null && pictureToRotate!=null) {
			synchronized (c) {
				mMatrixPictureCanvas = new Matrix();

				mMatrixPictureCanvas.postTranslate(-BitmapFactory.decodeByteArray(pictureToRotate.getByteImage(),0,pictureToRotate.getByteImage().length).getWidth() / 2,
						-BitmapFactory.decodeByteArray(pictureToRotate.getByteImage(),0,pictureToRotate.getByteImage().length).getHeight() / 2);
				mMatrixPictureCanvas.postRotate(rotation);
				mMatrixPictureCanvas.postTranslate(pictureToRotate.getDiaryPictureX(),
						pictureToRotate.getDiaryPictureY());

				if (mPictureBitmapForCanvas == null) {
					mPictureBitmapForCanvas = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);

				}


				//Cancello la vecchia immagine
				mPictureBitmapForCanvas.eraseColor(Color.TRANSPARENT);

				if (mCanvasForMovePicture == null) {
					mCanvasForMovePicture = new Canvas();
					mCanvasForMovePicture.setBitmap(mPictureBitmapForCanvas);
				}


				if (mCanvasForMovePicture != null &&
						pictureToRotate != null) {
					Paint paint = new Paint();
					paint.setColor(Color.WHITE);
					paint.setStyle(Paint.Style.FILL);
					paint.setStrokeWidth(2);
					mCanvasForMovePicture.drawBitmap(addWhiteBorder(BitmapFactory.decodeByteArray(pictureToRotate.getByteImage(),0,pictureToRotate.getByteImage().length),4),mMatrixPictureCanvas,paint);

					pictureToRotate.setDiaryPictureRotation((int) rotation);
					mImages.put(pictureToRotate.getDiaryPictureID(), pictureToRotate);
					//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "Real rotate picture: "+rotation);
				}
				if (sortedImages == null) return;
				//Metto le altre immagini
				for (DiaryPicture oPicture : sortedImages.values()) {
					if (oPicture.getBitmapImage() == null) continue;
					if (mCanvasForMovePicture != null &&
							oPicture != null &&
							oPicture!=pictureToRotate) {

						if (!BitmapFactory.decodeByteArray(oPicture.getByteImage(),0,oPicture.getByteImage().length).isRecycled()) {
							mMatrixPictureCanvas.reset();
							mMatrixPictureCanvas.postRotate(oPicture.getDiaryPictureRotation());
							mMatrixPictureCanvas.postTranslate(oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY());
							mCanvasForMovePicture.drawBitmap(BitmapFactory.decodeByteArray(oPicture.getByteImage(),0,oPicture.getByteImage().length),mMatrixPictureCanvas,null);
							//mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY(), mPaint);
						}
					}
				}

				refresh(c);
				//c.drawBitmap(mPictureBitmapForCanvas, 0, 0, new Paint());
			}
		}
		if (c != null) {
			try{
				if(cLocked) mSurfaceHolder.unlockCanvasAndPost(c); cLocked=false;
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
			}catch(IllegalArgumentException e){
				//TODO
				if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"rotatePicture thread error canvas");
			}
			c=null;
		}
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
		Canvas c=mSurfaceHolder.lockHardwareCanvas();
		//c.setBitmap(mBitmapPageTotal);
		cLocked=true;
		synchronized (c) {
			if(mPictureBitmapForCanvas==null){
				mPictureBitmapForCanvas = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
				if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Create Bitmap for move picture");
			}


			//Cancello la vecchia immagine
			mPictureBitmapForCanvas.eraseColor(Color.TRANSPARENT);

			if(mCanvasForMovePicture ==null){
				mCanvasForMovePicture = new Canvas();
				mCanvasForMovePicture.setBitmap(mPictureBitmapForCanvas);
				if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "Create Canvas for move picture");
			}

			//if(bmpPath!=null) mCanvasForMovePicture.drawBitmap(bmpPath, MatrixPictureCanvas, null);
			//if(mBitmapForPage !=null) mCanvasForMovePicture.drawBitmap(mBitmapForPage, MatrixPictureCanvas, null);
			//if(mTextBitmap!=null) mCanvasForMovePicture.drawBitmap(mTextBitmap, MatrixPictureCanvas, null);

			if(mCanvasForMovePicture!=null &&
					pictureToMove!=null && !pictureToMove.isDiaryHandImage()){
				//Rect fromRect1 = new Rect(0, 0, , bgrH);
              /*  Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postTranslate(-pictureToMove.getBitmapImage().getWidth() / 2, -pictureToMove.getBitmapImage().getHeight() / 2); // Centers image
                matrix.postRotate(mAngle);
                matrix.postTranslate(moveX-(pictureToMove.getBitmapImage().getWidth()/2), moveY-(pictureToMove.getBitmapImage().getHeight()/2));
                mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), matrix, null);*/
				mMatrixPictureCanvas.reset();
				mMatrixPictureCanvas.postRotate(pictureToMove.getDiaryPictureRotation());
				mMatrixPictureCanvas.postTranslate(pictureToMove.getDiaryPictureX(), pictureToMove.getDiaryPictureY());
				mCanvasForMovePicture.drawBitmap(addWhiteBorder(BitmapFactory.decodeByteArray(pictureToMove.getByteImage(),0,pictureToMove.getByteImage().length), 4), mMatrixPictureCanvas, null);
				//mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), pictureToMove.getDiaryPictureX(), pictureToMove.getDiaryPictureY(), mPaint);
				//SE PORTO L'IMMAGINE IN BASSO OLTRE IL BORDO CANCELLO L'IMMAGINE.
				if((moveY+(BitmapFactory.decodeByteArray(pictureToMove.getByteImage(),0,pictureToMove.getByteImage().length).getHeight() / 3))>iHeight){
					//Toast.makeText(mContext, "DELETE IMAGE", Toast.LENGTH_LONG).show();
					//isPictureDelete =true;
					if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Delete Image DISABLED!!!");
				}
				//mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), moveX-(pictureToMove.getBitmapImage().getWidth()/2), moveY-(pictureToMove.getBitmapImage().getHeight()/2), new Paint());

                /*mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), new Rect((int)moveX-(pictureToMove.getBitmapImage().getWidth()/2),
                                                                                (int)moveY-(pictureToMove.getBitmapImage().getHeight()/2),
                                                                                pictureToMove.getBitmapImage().getWidth(), pictureToMove.getBitmapImage().getHeight()),
                                                                                new Rect((int)moveX-(pictureToMove.getBitmapImage().getWidth()/2),
                                                                                        (int)moveY-(pictureToMove.getBitmapImage().getHeight()/2), pictureToMove.getBitmapImage().getWidth(),
                                                                                        pictureToMove.getBitmapImage().getHeight()), mPaint);*/

				pictureToMove.setDiaryPictureX((int) moveX-(BitmapFactory.decodeByteArray(pictureToMove.getByteImage(),0,pictureToMove.getByteImage().length).getWidth()/2));
				pictureToMove.setDiaryPictureY((int) moveY-(BitmapFactory.decodeByteArray(pictureToMove.getByteImage(),0,pictureToMove.getByteImage().length).getHeight()/2));
				mImages.put(pictureToMove.getDiaryPictureID(), pictureToMove);
			}
			//Metto le altre immagini
			for(DiaryPicture oPicture : sortedImages.values()){
				if(oPicture.getByteImage()==null) continue;
				if(BitmapFactory.decodeByteArray(oPicture.getByteImage(),0,oPicture.getByteImage().length)==null) continue;
				if(mCanvasForMovePicture!=null &&
						oPicture!=null
						&& oPicture!=pictureToMove){
                	/* Matrix matrix = new Matrix();
                     matrix.reset();
                     matrix.postTranslate(-oPicture.getBitmapImage().getWidth() / 2, -oPicture.getBitmapImage().getHeight() / 2); // Centers image
                     matrix.postRotate(oPicture.getDiaryPictureRotation());
                     matrix.postTranslate(oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY());
                     mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), matrix, null);*/
					mMatrixPictureCanvas.reset();
					mMatrixPictureCanvas.postRotate(oPicture.getDiaryPictureRotation());
					mMatrixPictureCanvas.postTranslate(oPicture.getDiaryPictureX(),oPicture.getDiaryPictureY());
					//mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY(), mPaint);
					mCanvasForMovePicture.drawBitmap(BitmapFactory.decodeByteArray(oPicture.getByteImage(),0,oPicture.getByteImage().length), mMatrixPictureCanvas, mPaint);

					//if(!oPicture.getBitmapImage().isRecycled()) mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY(), mPaint);
				}
			}

			//Aggiungo l'icona per cancellare l'immagine
			mCanvasForMovePicture.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_action_delete),iWidth/2,iHeight-50,new Paint());

			refresh(c);
			//c.drawBitmap(mPictureBitmapForCanvas, 0, 0, new Paint());
		}
		if (c != null) {
			try{
				if(cLocked) mSurfaceHolder.unlockCanvasAndPost(c); cLocked=false;
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
			}catch(IllegalArgumentException e){
				if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"movePicture thread error canvas");
			}
			c=null;
			//mCanvasForMovePicture =null;
			//mPictureBitmapForCanvas.recycle();
			//mPictureBitmapForCanvas=null;
		}
	}
	/**
	 * identifica se sul touch ho cliccato su un'immagine
	 *
	 * @param motionEvent MotionEvent
	 * */
	private void findIfTouchPicture(MotionEvent motionEvent) {
		float xFinger;
		float yFinger;
		if(mImages==null || sortedImages==null) return;
		xFinger=motionEvent.getX(0);
		yFinger=motionEvent.getY(0);

		for(DiaryPicture oPicture : sortedImages.values()){
			if(oPicture.getByteImage()==null || oPicture.isDiaryHandImage()) continue;
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"oPicture X: "+oPicture.getDiaryPictureX()+" fingerX: "+xFinger+
					" - Y: "+oPicture.getDiaryPictureY()+" fingerY: "+yFinger);
			if((xFinger>=oPicture.getDiaryPictureX()
					&& xFinger<=(oPicture.getDiaryPictureX()+ BitmapFactory.decodeByteArray(oPicture.getByteImage(),0,oPicture.getByteImage().length).getWidth()))
					&& (yFinger>=oPicture.getDiaryPictureY()
					&& yFinger<=(oPicture.getDiaryPictureY()+ BitmapFactory.decodeByteArray(oPicture.getByteImage(),0,oPicture.getByteImage().length).getHeight()))
			){
				//La coordinata X Y rientra nell'immagine
				isPictureClick =true;
				mPictureToMove=oPicture;

				return;

				//Toast.makeText(mContext, "Bitmap: Cliccabled", Toast.LENGTH_SHORT).show();

			}
		}
		mPictureToMove=null;
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
	 * Ritorna se è writable o no
	 * */
	public boolean isWritable() {
		return mWritable;
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
				if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Interrupt Thread END Drawing");
			}

			_thread=null;
		}else{
			((LinearLayout) getParent()).removeView(oRowEdit);
			if(_thread!=null &&
					!_thread.isAlive()){
				if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Interrupt Thread and Create new Thread for Drawing");
				_thread.interrupt();
				//_thread.setRunning(mWritable);
				_thread=null;
				_thread = new PaperSurface.DrawingThread();
				//_thread.setRunning(mWritable);
				_thread.start();
			}else{
				if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Create new Thread for Drawing");
				_thread = new PaperSurface.DrawingThread();
				//_thread.setRunning(mWritable);
				_thread.start();
			}

		}
	}
	/**
	 * seleziona l'immagine aggiungendo un bordo.
	 *
	 * @param pictureToMove
	 *
	 * */
	private void selectPicture(DiaryPicture pictureToMove, boolean isSelected) {
		if(mWritable) return;
		Canvas c=mSurfaceHolder.lockHardwareCanvas();
		//c.setBitmap(mBitmapPageTotal);
		cLocked=true;
		synchronized (c) {
			if(mPictureBitmapForCanvas==null){
				mPictureBitmapForCanvas = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
				if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Create Bitmap for move picture");
			}


			//Cancello la vecchia immagine
			mPictureBitmapForCanvas.eraseColor(Color.TRANSPARENT);

			if(mCanvasForMovePicture ==null){
				mCanvasForMovePicture = new Canvas();
				mCanvasForMovePicture.setBitmap(mPictureBitmapForCanvas);
				if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "Create Canvas for move picture");
			}
			if(mCanvasForMovePicture!=null &&
					pictureToMove!=null){
				mMatrixPictureCanvas.reset();
				mMatrixPictureCanvas.postRotate(pictureToMove.getDiaryPictureRotation());
				mMatrixPictureCanvas.postTranslate(pictureToMove.getDiaryPictureX(),pictureToMove.getDiaryPictureY());
				if(isSelected)
					//mCanvasForMovePicture.drawBitmap(addWhiteBorder(pictureToMove.getBitmapImage(),4), mMatrixPictureCanvas, null);
					mCanvasForMovePicture.drawBitmap(addWhiteBorder(BitmapFactory.decodeByteArray(pictureToMove.getByteImage(),0,pictureToMove.getByteImage().length),4), mMatrixPictureCanvas, null);
				else
					//mCanvasForMovePicture.drawBitmap(pictureToMove.getBitmapImage(), mMatrixPictureCanvas, null);
					mCanvasForMovePicture.drawBitmap(BitmapFactory.decodeByteArray(pictureToMove.getByteImage(),0,pictureToMove.getByteImage().length), mMatrixPictureCanvas, null);
			}
			//Metto le altre immagini
			for(DiaryPicture oPicture : sortedImages.values()){
				if(oPicture==null) {
					continue;
				}
				if(oPicture.getByteImage()==null){
					continue;
				}
				if(mCanvasForMovePicture!=null &&
						oPicture!=null
						&& oPicture!=pictureToMove){
					mMatrixPictureCanvas.reset();
					mMatrixPictureCanvas.postRotate(oPicture.getDiaryPictureRotation());
					mMatrixPictureCanvas.postTranslate(oPicture.getDiaryPictureX(),oPicture.getDiaryPictureY());
					mCanvasForMovePicture.drawBitmap(BitmapFactory.decodeByteArray(oPicture.getByteImage(),0,oPicture.getByteImage().length), mMatrixPictureCanvas, mPaint);
				}
			}
			refresh(c);
		}
		if (c != null) {
			try{
				if(cLocked) mSurfaceHolder.unlockCanvasAndPost(c); cLocked=false;
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
			}catch(IllegalArgumentException e){
				if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"movePicture thread error canvas");
			}
			c=null;
		}
	}
	/**
	 * Rimuove la riga al bean del diario
	 *
	 * */
	private void removeRowToDiary(int rowToRemove) {
		if(iCurrentRow>0){
			long mRowID=rowToRemove;
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Delete Row String");
			mRows.remove(mRowID);
			aRowCustomPos.remove(rowToRemove);
			RowAsyncTask asyncTask = new RowAsyncTask(mContext);
			asyncTask.execute(mRowID,mCurrentDiary.getDiaryID(),mCurrentPage.getPageID());
		}
	}
	public void drawTextOnSurface() {


		if(mTextBitmap==null) return;
		if(mWritable) return;
		Canvas c=mSurfaceHolder.lockHardwareCanvas();
		//c.setBitmap(mBitmapPageTotal);
		cLocked=true;
		if(c==null) {
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Canvas NULL on drawTextOnSurface");
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
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"#############INIZIO##################### ");
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"First iCurrentOffset:"+iCurrentOffset);

				if(iCurrentOffset>iHeight) {
					//End of Page
					if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"END OF PAGE");
					return;
				}
				updateRowToDiary(i,getRowString(i),getMarginSX(), iCurrentOffset);
				mTextCanvas.drawText(getRowString(i), getMarginSX(), iCurrentOffset,
						getRow(i));
				lastX=getMarginSX();
				lastY=iCurrentOffset;

                /*if(mRows.get((long) i)!=null){
                    if(mRows.get((long) i).getRowPosY()>iHeight) {
                        //End of Page
                        if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"END OF PAGE");
                        return;
                    }
                    updateRowToDiary(i,getRowString(i),mRows.get((long) i).getRowPosX(), mRows.get((long) i).getRowPosY());
                    mTextCanvas.drawText(getRowString(i), mRows.get((long) i).getRowPosX(), mRows.get((long) i).getRowPosY(),
                            getRow(i));
                    lastX=mRows.get((long) i).getRowPosX();
                    lastY=mRows.get((long) i).getRowPosY();
                    //if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"case 1");

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
                            //if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"case 2->1");
                        }else{
                            if(iCurrentOffset>iHeight) {
                                //End of Page
                                if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"END OF PAGE");
                                return;
                            }
                            updateRowToDiary(i,getRowString(i),getMarginSX(), iCurrentOffset);
                            mTextCanvas.drawText(getRowString(i), getMarginSX(), iCurrentOffset,
                                    getRow(i));
                            lastX=getMarginSX();
                            lastY=iCurrentOffset;
                            //if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"case 2->2");
                        }
                    }catch (IndexOutOfBoundsException e){
                        if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"IndexOutOfBoundsException ");
                        return;
                    }
                }*/
				iCurrentOffset+=getOffsetRow();
				iLastOffset=iCurrentOffset;
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Stringa: "+getRowString(i)+" afther iCurrentOffset:"+iCurrentOffset+" - getOffsetRow: "+getOffsetRow()+" - getMarginSX():"+getMarginSX());
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"#############FINE##################### ");
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
				if(cLocked) mSurfaceHolder.unlockCanvasAndPost(c); cLocked=false;
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
			}catch(IllegalArgumentException e){
				//TODO
				if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"drawTextOnSurface thread error canvas");
			}
			c=null;
			//mTextBitmap.recycle();
			//mTextBitmap=null;
			mTextCanvas=null;
			//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost Canvas");
		}
	}
	/**
	 * Aggiunge un bordo punteggiato all'immagine
	 *
	 * */
	private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
		Bitmap bmpWithBorderTransparent = Bitmap.createBitmap(bmp.getWidth() + borderSize * 4, bmp.getHeight() + borderSize * 4, bmp.getConfig());
		Canvas canvasT = new Canvas(bmpWithBorderTransparent);
		canvasT.drawColor(Color.TRANSPARENT);
		//canvasT.drawBitmap(bmp, borderSize, borderSize, null);
		Paint selectPaint = new Paint();
		selectPaint.setStrokeWidth(2);
		selectPaint.setColor(Color.LTGRAY);
		selectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		selectPaint.setPathEffect(new DashPathEffect(new float[] { 4, 8, 4, 8 }, 0));
		canvasT.drawRect(0,0,bmpWithBorderTransparent.getWidth(),bmpWithBorderTransparent.getHeight(),selectPaint);

		//bmpWithBorderTransparent = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
		canvasT = new Canvas(bmpWithBorderTransparent);
		//canvasT.drawColor(Color.TRANSPARENT);
		canvasT.drawBitmap(bmp, borderSize+2, borderSize+2, null);
		return bmpWithBorderTransparent;
	}
	/**
	 * metodo utilizzato per disegnare sullo schermo richiamato in modalità sincrona dal thread interno.
	 *
	 * **/
	protected void refresh(Canvas canvas) {

		/*if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),
				"Bitmap Recycled mBitmapForPage:"+mBitmapForPage.isRecycled()+
				" mPictureBitmapForCanvas:"+mPictureBitmapForCanvas.isRecycled()+
				" bmpPath:"+bmpPath.isRecycled()+
				" mTextBitmap:"+mTextBitmap.isRecycled());*/
		int _Initializer = _graphics.size()-iUndo;

		if(canvas!=null && _Initializer<=0){
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			//Questa bitmap contiene il background
			if(mBitmapForPage !=null
					&& !mBitmapForPage.isRecycled()) canvas.drawBitmap(mBitmapForPage, identityMatrix, null);

			//Disegno lo spostamento dell'immagine
			if(mPictureBitmapForCanvas!=null
					&& !mPictureBitmapForCanvas.isRecycled()) canvas.drawBitmap(mPictureBitmapForCanvas, identityMatrix, null);

			//Disegno le vecchie path
			//ERRORE NELLE VECCHIE PATH
			if(bmpPath!=null
					&& !bmpPath.isRecycled()) canvas.drawBitmap(bmpPath, identityMatrix, null);

			if(mTextBitmap!=null
					&& !mTextBitmap.isRecycled()) canvas.drawBitmap(mTextBitmap, identityMatrix, null);
			//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"draw background no path");
			mOneShot=true;
		}

		if(canvas!=null && _Initializer>0){
			//canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"draw background with path");
			if(mWritable){
				//Questa bitmap contiene il background
				if(mBitmapForPage !=null && mOneShot && !mBitmapForPage.isRecycled()) canvas.drawBitmap(mBitmapForPage, identityMatrix, null);
				try{

					if(mCanvas!=null) {
						mCanvas.drawPath(_graphics.get(_Initializer), _graphics.get(_Initializer).getmPaint());
					}
				}catch (IndexOutOfBoundsException e){
					if(Const.DEVELOPER_MODE) Log.w(this.getClass().getCanonicalName(),"out of bound");
				}
				//Disegno lo spostamento dell'immagine
				if(mPictureBitmapForCanvas!=null && mOneShot && !mPictureBitmapForCanvas.isRecycled()) canvas.drawBitmap(mPictureBitmapForCanvas, identityMatrix, null);
				//Disegno le vecchie path
				if(bmpPath!=null
						&& !bmpPath.isRecycled()) canvas.drawBitmap(bmpPath, identityMatrix, null);
				//Questo nel caso in cui sto scrivento con path
				if(mTextBitmap!=null && mOneShot && !mTextBitmap.isRecycled()) canvas.drawBitmap(mTextBitmap, identityMatrix, null);
				if(mOneShot) mOneShot=false;
			}else{
				//Questa bitmap contiene il background
				if(mBitmapForPage !=null
						&& !mBitmapForPage.isRecycled()) canvas.drawBitmap(mBitmapForPage, identityMatrix, null);
				if(mCanvas!=null) mCanvas.drawPath(_graphics.get(_Initializer), _graphics.get(_Initializer).getmPaint());
				//Disegno lo spostamento dell'immagine
				if(mPictureBitmapForCanvas!=null && mOneShot && !mPictureBitmapForCanvas.isRecycled()) canvas.drawBitmap(mPictureBitmapForCanvas, identityMatrix, null);
				//Disegno le vecchie path
				if(bmpPath!=null
						&& !bmpPath.isRecycled()) canvas.drawBitmap(bmpPath, identityMatrix, null);
				//Questo nel caso in cui sto scrivento con path
				if(mTextBitmap!=null && mOneShot && !mTextBitmap.isRecycled()) canvas.drawBitmap(mTextBitmap, identityMatrix, null);
				if(mOneShot) mOneShot=false;
			}
		}
		drawPicturePage();
	}
	/**
	 * carica le immagini della pagina salvata.
	 *
	 * */
	private void drawPicturePage() {
		mMatrixPictureCanvas = new Matrix();

		PaperSurface.this.postInvalidate();
		if(mCurrentPage==null
				|| mCurrentPage.getDiaryImage()==null
				|| mCurrentPage.getDiaryImage().size()==0){
			if(Const.DEVELOPER_MODE) if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"NO Pucture on Page");
			return;
		}
		mImages = (Hashtable<Long, DiaryPicture>) mCurrentPage.getDiaryImage();
		sortedImages = new TreeMap<Long, DiaryPicture>(mImages);
		if(mPictureBitmapForCanvas==null){
			if(Const.DEVELOPER_MODE) if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"mPictureBitmapForCanvas is NULL created");
			mPictureBitmapForCanvas = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
		}

		if(mCanvasForMovePicture ==null){
			if(Const.DEVELOPER_MODE) if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"mCanvasForMovePicture is NULL created");
			mCanvasForMovePicture = new Canvas(mPictureBitmapForCanvas);
		}
		//Cancello la vecchia immagine
		if(mPictureBitmapForCanvas!=null
				&& !mPictureBitmapForCanvas.isRecycled())mPictureBitmapForCanvas.eraseColor(Color.TRANSPARENT);

		for(DiaryPicture oPicture : sortedImages.values()){
			if(mCanvasForMovePicture!=null &&
					oPicture!=null && oPicture.getByteImage()!=null){
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Draw old Picture ERROR HERE!!!!!");
				if(BitmapFactory.decodeByteArray(oPicture.getByteImage(),0,oPicture.getByteImage().length)==null){
					//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"No Picture Bitmap!!!!!");
					mImages.remove(oPicture.getDiaryPictureID());
					continue;
				}
               /* Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postTranslate(-oPicture.getBitmapImage().getWidth() / 2, -oPicture.getBitmapImage().getHeight() / 2); // Centers image
                matrix.postRotate(oPicture.getDiaryPictureRotation());
                matrix.postTranslate(oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY());
                mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), matrix, null);*/
				mMatrixPictureCanvas.reset();

				mMatrixPictureCanvas.postRotate(oPicture.getDiaryPictureRotation());

				mMatrixPictureCanvas.postTranslate(oPicture.getDiaryPictureX(),oPicture.getDiaryPictureY());

				//mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), oPicture.getDiaryPictureX(), oPicture.getDiaryPictureY(), mPaint);
				//ERRRORE HERE
				//mCanvasForMovePicture.drawBitmap(oPicture.getBitmapImage(), mMatrixPictureCanvas, mPaint);
				mCanvasForMovePicture.drawBitmap(BitmapFactory.decodeByteArray(oPicture.getByteImage(),0,oPicture.getByteImage().length), mMatrixPictureCanvas, mPaint);
			}
		}
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
		try{
			return aPaint.get(iRow);
		}catch (IndexOutOfBoundsException e){
			return TextPaintPage;
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
	 * libera memoria
	 * */
	public void freeBitmap(){
		if(bmpPath!=null && !bmpPath.isRecycled()) bmpPath.recycle();
		if(mBitmapForPage !=null && !mBitmapForPage.isRecycled()) mBitmapForPage.recycle();
		if(mTextBitmap!=null && !mTextBitmap.isRecycled()) mTextBitmap.recycle();
		//if(mBmpPage!=null) mBmpPage.recycle();
		if(mPictureBitmapForCanvas!=null && !mPictureBitmapForCanvas.isRecycled()) mPictureBitmapForCanvas.recycle();
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
		//mSurfaceHolder.unlockCanvasAndPost(mSurfaceHolder.lockHardwareCanvas());

		if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "########### free bitmap memory ##############");
		if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "########### free memory is "+
				Runtime.getRuntime().freeMemory()+" total is "+
				Runtime.getRuntime().totalMemory()+" ##############");

	}
	/**
	 * Inizializza la riga che comincio a scrivere
	 *
	 *
	 * */
	public void init(int iRow) {
		if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"init New Row:"+iCurrentRow);
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
	public void setDeleteMode(boolean deleteMode) {
		mDeleteMode = deleteMode;
	}
	public void setMarginTop(int marginTop) {
		mSystemHeight = marginTop;
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
	 * inizializza per una nuova pagina
	 *
	 * */
	public void newPage(){
		aRows.clear();
		aPaint.clear();

		mRows=(Hashtable<Long, Row>) mCurrentPage.getPageRows();

		if(mRows.size()>0){
			//Devo inserire il popolamento della pagina solo se ci sono righe
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Load non Empty Page");
			setRowsString();
		}else{
			iCurrentRow=0;
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Add empty Page");
			init(iCurrentRow);
		}

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
			//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"row add: "+iRow);
			iRow++;
		}
		//NewPageView.this.invalidate();
	}
	/**
	 * Applica il template selezionato
	 *
	 * TODO utilizzare Drawable per creare la pagina con un gradiente ed essere più leggera
	 * */
	private void applyTemplate() {
		if(mCurrentDiary==null || mCurrentPage==null) return;

		if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"Loading font...");
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
	 * @stubs
	 * */
	public void setColor(int currentColor) {
		mCurrentColor=currentColor;
		mPaint.setColor(mCurrentColor);
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
	 * Utilizzato per salvare la preview
	 * */
	public Bitmap getBitmap() {
		mBitmapPageTotal=mBitmapForPage;
		return mBitmapPageTotal;
		//return Bitmap.createBitmap(mWidth,mHeight,Bitmap.Config.ARGB_8888);
	}

	/**
	 * crea una canvas con le vecchie linee e background da usare poi nel refresh
	 *
	 * */
	public void createCanvasBackground(){

		if(mWritable) return;
		Canvas c=mSurfaceHolder.lockHardwareCanvas();
		//c.setBitmap(mBitmapPageTotal);
		cLocked=true;;

		if(c==null) return;
		synchronized (c) {
			//
			if(!mDrawBackground || iWidth<=0) return;
			//if(iWidth<=0 || iHeight<=0) return;
			//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(), "############ createCanvasBackground ######### "+iWidth+" - "+iHeight);
			//if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(), "############ createCanvasBackground ######### "+iWidth+" - "+iHeight);

			if(mTextBitmap!=null && !mTextBitmap.isRecycled()) mTextBitmap.recycle();
			mTextBitmap	= Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
			mTextBitmap.eraseColor(Color.TRANSPARENT);

			if(mBitmapForPage!=null && !mBitmapForPage.isRecycled()) mBitmapForPage.recycle();
			mBitmapForPage = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
			mBitmapForPage.eraseColor(Color.TRANSPARENT);

			if(mCanvas==null){
				mCanvas = new Canvas(mBitmapForPage);
				mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			}

			identityMatrix = new Matrix();

			if(mCanvas!=null){
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

			//TODO Ridisegno le immagini della pagina
			if (mCurrentDiary != null && mContext != null && mCurrentPage != null) {
				//File tmpImgFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + mContext.getPackageName() + "/" + mCurrentDiary.getDiaryID() + "/Pictures/h" + mCurrentPage.getPageID() + Const.PAGE_PREVIEW_EXT);
				//if (tmpImgFile.exists()) {
				//le vecchie path da usare in un task async
				if(mCurrentPage.getByteImageHW()!=null){
					Bitmap bmp = BitmapFactory.decodeByteArray(mCurrentPage.getByteImageHW(), 0, mCurrentPage.getByteImageHW().length);
					if(bmp!=null) {
						Bitmap immutable = bmp.copy(Bitmap.Config.ARGB_8888, true);
						//Canvas canvas = new Canvas(mutableBitmap); // now it should work ok
						//Bitmap immutable = BitmapFactoryHelper.decodeSampledBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/" + mContext.getPackageName() + "/" + mCurrentDiary.getDiaryID() + "/Pictures/h" + mCurrentPage.getPageID() + Const.PAGE_PREVIEW_EXT, 1);
						if (immutable != null) {
							if (bmpPath != null) {
								bmpPath.recycle();
								if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"###################RECYCLED bmpPath HERE!!!##################");
							}
							bmpPath = immutable.copy(Bitmap.Config.ARGB_8888, true);
							immutable.recycle();
						}
					}

				}
			}


			//Disegna la Data della pagina
			drawDate(mCanvas);
			//Disegna il testo della pagina
			//writeTextPage(mCanvas);
			drawPicturePage();
			mDrawBackground=true;
			refresh(c);
		}
		if (c != null) {
			try {

				if(cLocked) mSurfaceHolder.unlockCanvasAndPost(c); cLocked=false;
				//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
			} catch (IllegalArgumentException e) {
				//TODO
				if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(), "createCanvasBackground thread error canvas");
			}
			c = null;
			//mTextBitmap.recycle();
			//mTextBitmap=null;
			//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost Canvas");
		}
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
				canvas.drawText(sDay, (iWidth*Const.DATE_DAY_OFFSET_X)/Const.DEFAULT_WIDTH, ((iHeight*Const.DATE_DAY_OFFSET_Y)/Const.DEFAULT_HEIGTH)+15, TextPaintDay);
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
	 * disegna le linee del foglio
	 * */
	private void drawLinesPaper(Canvas canvas) {
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
			canvas.drawLine(10,iOffsetPage, iWidth-10, iOffsetPage,mLines);
		}
	}
	/**
	 * disegna le linee del foglio
	 * */
	private void drawGridsPaper(Canvas canvas) {
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
			canvas.drawLine(10,iOffsetPage, iWidth-10, iOffsetPage,mLines);
		}
		int ivLines=1;
		int ivOffset=getMarginSX()+20;

		while(ivOffset<iWidth){
			ivOffset=(getMarginSX()+20)*ivLines;
			ivLines++;
			canvas.drawLine(ivOffset,((iHeight*Const.DATE_MONTH_OFFSET_Y)/Const.DEFAULT_HEIGTH)+10, ivOffset, iHeight-10,mLines);
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
			if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"setWritable _thread interupted");
		}
		_thread=null;

		System.gc();
	}
	/**
	 * ritorna l'immagine con tutte le path per salvare l'immagine
	 *
	 *
	 * */
	public Bitmap getHandWritePath(){
		return bmpPath;
	}
	public boolean isDeleted() {
		return mDeleteMode;
	}
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(Const.DEVELOPER_MODE) Log.d(this.getClass().getCanonicalName(), "ACTION_DOWN: "+event+"keyboard: "+event.getKeyCode());
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
				//if(Const.DEVELOPER_MODE) Log.d(this.getClass().getCanonicalName(), "ACTION_DOWN: "+pressedKey+"keyboard: "+keyCode);
			}

			drawTextOnSurface();
			//iPosX++;
			//if(Const.DEVELOPER_MODE) Log.d(this.getClass().getCanonicalName(), "ACTION_DOWN keyboard: "+keyCode);
			return false;
		}
		//if(Const.DEVELOPER_MODE) Log.d(this.getClass().getCanonicalName(), "ACTION_DOWN keyboard: "+keyCode);
		return false;
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
	 * identifica la parola intera e va a capo tutta togliendola dalla riga corrente
	 *
	 * */
	private String findWord(String sCurrentText) {
		String sWord="";
		if(sCurrentText.endsWith(" ")){
			if(Const.DEVELOPER_MODE) Log.v(TAG,"nuova parola non devo fare nulla");
			return "";
		}else{
			if(sCurrentText.indexOf(" ")==-1){
				if(Const.DEVELOPER_MODE) Log.v(TAG,"nessun spazio sulla riga non devo fare nulla");
				return "";
			}else{
				sWord=sCurrentText.substring(sCurrentText.lastIndexOf(" ")+1,sCurrentText.length());
				sCurrentText = sCurrentText.substring(0,sCurrentText.lastIndexOf(" "));
				aRows.add(iCurrentRow-1,sCurrentText);
				return sWord;
			}
		}
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}
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
			if(Const.DEVELOPER_MODE) Log.i(TAG,"Delete prev. row");

		}catch(ArrayIndexOutOfBoundsException e){

			//Toast.makeText(mContext, mContext.getString(R.string.empty_page), Toast.LENGTH_SHORT).show();
			if(iCurrentRow<0){
				iCurrentRow=0;
				init(iCurrentRow);
			}
		}
	}


	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		InputConnetionEMS connection = new InputConnetionEMS(this, false);
		connection.setView(this);
		//outAttrs.inputType = InputType.;
		outAttrs.imeOptions = EditorInfo.IME_MASK_ACTION;
		//outAttrs.initialSelStart = -1;
		//outAttrs.initialSelEnd = -1;
		return connection;
	}

	/**
	 * OTHERS CLASS
	 *
	 * **/

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
			/*while (!isAvailable()) {
				try {
					sleep(1000);
					if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"TextureView is not Avaiable wait...");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/
			Canvas c= null;
          /*  c = _surfaceHolder.lockHardwareCanvas();
            synchronized (_surfaceHolder) {
                _panel.refresh(c);
            }
            if (c != null) {
                _surfaceHolder.unlockCanvasAndPost(c);
                //if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost Canvas");
            }*/
			//createCanvasBackground();

			if(isInterrupted()) return;
			while (mWritable) {

				if(isInterrupted()) return;
				if(mWritable){
					c = null;
					try {
						c = mSurfaceHolder.lockHardwareCanvas();
						//c.setBitmap(mBitmapPageTotal);
						cLocked=true;
						//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"H: "+c.getHeight()+" W: "+c.getWidth());
						if(c==null){
							if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"canvas _surfaceHolder is Null");
							interrupt();
							return;
						}
						synchronized (c) {

							//c.drawColor(Color.TRANSPARENT);
							//c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
							refresh(c);
							//c.drawColor(Color.TRANSPARENT);
						}
					}finally{
						try{
							if (c != null)
								if(cLocked) mSurfaceHolder.unlockCanvasAndPost(c); cLocked=false;


						}catch (IllegalArgumentException e){
							if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"thread unlockCanvasAndPost error");
							e.printStackTrace();
							interrupt();
						}
					}

				}
			}
			if (c != null) {
				try{
					c.save();
					if(cLocked) mSurfaceHolder.unlockCanvasAndPost(c); cLocked=false;
					//if(Const.DEVELOPER_MODE) Log.v(this.getClass().getCanonicalName(),"unlockCanvasAndPost 2 Canvas");
				}catch(IllegalArgumentException e){
					//TODO
					if(Const.DEVELOPER_MODE) Log.e(this.getClass().getCanonicalName(),"run thread error canvas");
					e.printStackTrace();
					interrupt();
				}
			}
		}
	}
	class PictureTask extends AsyncTask<Void, Void, Boolean> {

		private DiaryPicture mPictureToSave;

		public PictureTask(DiaryPicture pictureTosave) {
			mPictureToSave = pictureTosave;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return DiaryRepositoryHelper.updatePicturePosition(mContext, mPictureToSave);
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			super.onPostExecute(aBoolean);
			selectPicture(mPictureToSave,false);
		}
	}
}
