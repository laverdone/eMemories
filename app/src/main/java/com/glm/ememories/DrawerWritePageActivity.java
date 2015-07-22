package com.glm.ememories;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import android.app.ActionBar;
import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.glm.bean.Diary;
import com.glm.bean.DiaryPicture;
import com.glm.bean.Page;
import com.glm.db.DiaryRepositoryHelper;
import com.glm.db.Repository;
import com.glm.utilities.BitmapFactoryHelper;
import com.glm.utilities.DiaryHelper;
//import com.glm.utilities.ODFHelper;
import com.glm.utilities.PdfBuilder;
import com.glm.utilities.adapter.ImageFilterListAdapter;
import com.glm.utilities.animation.FlipAnimation;
import com.glm.utilities.image.ImageFilters;
import com.glm.view.AnimationPageLayout;
import com.glm.view.ColorPickerView;
import com.glm.view.FloatingButton;
import com.glm.view.SlidingLayer;
import com.glm.view.TextureHandWrite;


public class DrawerWritePageActivity extends ActionBarActivity implements OnKeyListener, AnimationPageLayout.Listener{
    /**layout principale*/
    private DrawerLayout mMainLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar oToolbar;
    private Button oColorSelected;
    private ImageButton oColorPickerBtn;
    private ColorPickerView oColorPicker;
    /**Lista con i colori standard*/
    private SlidingLayer oColorPalette;
    private ListView mImageEffetView =null;

    /**Dimensione SideBar in Open*/
    private float mSideBarW;

    private RelativeLayout mPageLayout;

    /**Contiene tutte le image view delle preview*/
    private ArrayList<ImageView> mPagesPreview = new ArrayList<ImageView>();
    /**SurfaceView per scrittura immagini e path*/
    private TextureHandWrite oSurface;
    /**Vecchia Pagina*/
    private ImageView oOldPage;
    //private PageCurlView oSurface;

    private ScrollView oScrollView;
    private LinearLayout oLinearLayout;

    private Bitmap mBitmap=null;
    private ListView oGridPreview;
    /**diario corrente*/
    private Diary mDiary;
    /**Pagina corrente*/
    private Page mCurrentPage;
    /**flip/flop tastiera si/no*/
    private boolean isSoftKeyShow=false;

    /**Colore delle path*/
    private int mCurrentColor=Color.BLACK;
    /**larghezza della riga*/
    private int mCurrentStrokeWidth=1;

    private String sPathImage;
    private String sImageName;
    /**bitmap della fotocamera in scala*/
    private Bitmap oScaledSizeBmp;
    private boolean isInflateView=true;

    private int mWidth;
    private int mHeight;
    private MenuItem mMenuEdit;
    private long mCurrentPageID;
    private int statusBarHeight=19;
    private static final int LOW_DPI_STATUS_BAR_HEIGHT = 19;
    private static final int MEDIUM_DPI_STATUS_BAR_HEIGHT = 25;
    private static final int HIGH_DPI_STATUS_BAR_HEIGHT = 38;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources res = getResources();
        mSideBarW = res.getDimension(R.dimen.sideBar);

        Intent intent = getIntent();
		/*Template ID*/
        int iTemplate = intent.getIntExtra("template", 0);
		/*diario corrente id*/
        long mDiaryID = getIntent().getLongExtra("DiaryID", -1l);
        
		/*ID pagina corrente*/
        mCurrentPageID = getIntent().getLongExtra("CurrentPage", -1l);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();

        //Display display = this.getWindow().getWindowManager().getDefaultDisplay();
        //Point size = new Point();
        //display.getSize(size);

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }else{
            statusBarHeight =  (int) this.getResources().getDimension(R.dimen.status_bar);
        }
        /*switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_HIGH:
                statusBarHeight = HIGH_DPI_STATUS_BAR_HEIGHT;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                statusBarHeight = MEDIUM_DPI_STATUS_BAR_HEIGHT;
                break;
            case DisplayMetrics.DENSITY_LOW:
                statusBarHeight = LOW_DPI_STATUS_BAR_HEIGHT;
                break;
            default:
                statusBarHeight = MEDIUM_DPI_STATUS_BAR_HEIGHT;
        }*/
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //WindowManager.LayoutParams.SYSTEM_U
        //statusBarHeight=0;
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;

        this.getWindow().setType(LayoutParams.TYPE_APPLICATION);

        if(mDiaryID==-1) {
            mDiary=DiaryHelper.initNewDiary(iTemplate);
            /**Goto Last Page*/
            Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) mDiary.getDiaryPages();
            Map<Long, Page> sortedPages = new TreeMap<Long, Page>(mPages);

            for(Page oPpage : sortedPages.values()){
                mCurrentPage = oPpage;
            }
            sortedPages=null;
            loadDiary(false);
        }else{
            mDiary=new Diary();
            mDiary.setDiaryID(mDiaryID);
            LoadDiaryTask oDiariesAsync = new LoadDiaryTask(getApplicationContext());
            oDiariesAsync.execute();
        }

        Log.v(this.getClass().getCanonicalName(),"on Create WritePage");

        //this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }


    /*@Override
    protected void onResume() {
        if(oSurface!=null) oSurface.setWritable(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(oSurface!=null){
            //Fermo il Thread di scrittura manuale
            oSurface.stopDrawingThread();
            oSurface.freeBitmap();
        }

        super.onPause();
    }*/

    /**
     * This method implements the back button
     * @author coluzza
     */
    @Override
    public void onBackPressed() {
        //if(!oSurface.isWritable()){
        try {
            oSurface.freeBitmap();
            mBitmap = oSurface.getBitmap(mWidth / 2, mHeight / 2);
            SaveDiaryTask oSaveDiary = new SaveDiaryTask(getApplicationContext(),true);
            oSaveDiary.execute();
        }catch(java.lang.OutOfMemoryError e){
            Log.e(this.getClass().getCanonicalName(),"Out Of Memory");
            SaveDiaryTask oSaveDiary = new SaveDiaryTask(getApplicationContext(),true);
            oSaveDiary.execute();
        }

        /*}else{
            oSurface.fireUndo();
        }*/

    }

    private void backToHome(){
        Intent newIntent = new Intent();
        newIntent.setClass(this, DiaryActivity.class);
        this.startActivity(newIntent);
        this.finish();
        System.gc();
    }

    /**
     * This method create an action bar menu
     * @author coluzza
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //if(mDiary.getDiaryTemplate()==5){
            inflater.inflate(R.menu.write_page_options_menu_dark, menu);
        //}else{
        //    inflater.inflate(R.menu.write_page_options_menu, menu);
        //}
        mMenuEdit=menu.findItem(R.id.manualWrite);
        mMenuEdit.setIcon(R.drawable.ic_action_edit);
        if(isSoftKeyShow) toggleSoftKeyboard();
        return true;
    }
    /**
     * This methods calls private methods that implements
     * the functionality of the menu
     * @author coluzza
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMainLayout.closeDrawers();

        switch (item.getItemId()) {

            case R.id.save:

                forceCloseKeyboard();
                Log.v(this.getClass().getCanonicalName(),"Save page");
                //Log.i(this.getClass().getCanonicalName(),"json: "+oGson.toJson(mDiary));
                mBitmap=oSurface.getBitmap(mWidth/2,mHeight/2);
                if(oSurface.isWritable()) oSurface.setWritable(false);
                SaveDiaryTask oSaveDiary = new SaveDiaryTask(getApplicationContext(),false);
                oSaveDiary.execute();

                //createBitmapFromPage(false);
                //saveHandWrite(false);
                break;
            case R.id.newPage:
                selectPageFormat();

                //createBitmapFromPage(false);
                //pageCurlAnimation();
                //mPage.getHandWrite().resetPage();
                //Add New Page

                break;
            case R.id.deletePage:
                if(mDiary.getDiaryPages().size()>1){
                    //Cancello la pagina corrente
                    DeletePageTask oDeletePage = new DeletePageTask();
                    oDeletePage.execute();
                }else{
                    AlertDialog oWarning = new AlertDialog.Builder(this).create();
                    oWarning.setTitle(getString(R.string.app_name));
                    oWarning.setMessage(getString(R.string.cannotdeletepage));
                    oWarning.show();
                }

                break;
            case R.id.manualWrite:
                if(isSoftKeyShow) toggleSoftKeyboard();
                oSurface.setDeleteMode(false);



                showColorPiker();
                /*ColorPickerDialog oColor = new ColorPickerDialog(this,new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(String key, int color) {
                        mCurrentColor=color;
                        oSurface.setPage(mCurrentPage);
                        //mMenuEdit.setIcon(R.drawable.ic_action_edit_grey);
                        oSurface.setColor(mCurrentColor);
                        if(!oSurface.isWritable()) oSurface.setWritable(true);
                    }
                },"Color",Color.BLUE,Color.BLACK);
                oColor.show();*/
                break;
            case R.id.exportPDF:
                ExportPDFTask exportPDFTask = new ExportPDFTask();
                exportPDFTask.execute();

                break;
           /* case R.id.exportODF:
                ODFHelper odfHelper = new ODFHelper(this,mDiary);
                odfHelper.startExportToODF();

                break;*/
            case R.id.share:
                sharePage();

                //createBitmapFromPage(false);
                //pageCurlAnimation();
                //mPage.getHandWrite().resetPage();
                //Add New Page

                break;
            case R.id.clearPage:
                /*Cancella l'handWrite Page
                File oFileToDelete = new File(sPathImage+"/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT);
                if(oFileToDelete.exists()){
                    oFileToDelete.delete();
                    oSurface.freeBitmap();
                    loadDiary();
                }*/





                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oSurface.setPage(mCurrentPage);

                oSurface.setColor(Color.BLACK);
                oSurface.setStrokeWidth(mCurrentStrokeWidth*2);
                oSurface.setDeleteMode(true);
                break;
            case R.id.photo:
                //saveHandWrite(true);
                //PageAsyncTask = new SavePageAsyncTask(this, mPage, getApplicationContext());
                //PageAsyncTask.execute(mCurrentPage);
                //oSurface.setWritable(false);
                SaveDiaryTask oSaveDiary1 = new SaveDiaryTask(getApplicationContext(),false);
                oSaveDiary1.execute();

                takePhoto();
                break;
            case R.id.gallery:

                takeImageFromGallery();
                break;
            case R.id.edit:

                toggleSoftKeyboard();
                break;
            case R.id.help:
                helpGesture(0);
                break;
            case android.R.id.home:
                forceCloseKeyboard();
                onBackPressed();
                Log.v(this.getClass().getCanonicalName(),"Home Pressed");

                break;
        }
        return true;
    }
    /**
     * Comndivide la pagina corrente.
     *
     * */
    private void sharePage() {
        try{
            mBitmap =  oSurface.getBitmap(mWidth,mHeight);

            AsyncTask.execute(new Runnable() {


                @Override
                public void run() {
                    if(sPathImage==null) return;
                    File dir = new File(sPathImage);
                    File mFile=null;
                    if(!dir.exists()) {
                        dir.mkdirs();
                    }

                    //Task async per salvare l'immagine.

                    try {
                        if(mBitmap!=null){
                            mFile = new File(sPathImage+"/"+mCurrentPage.getPageID()+Const.CAMERA_SHARE_EXT);
                            FileOutputStream out = new FileOutputStream(mFile);

                            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.close();
                            out=null;
                            MimeTypeMap mime = MimeTypeMap.getSingleton();
                            String type = mime.getMimeTypeFromExtension("png");
                            Intent sharingIntent = new Intent("android.intent.action.SEND");
                            sharingIntent.setType(type);
                            sharingIntent.putExtra("android.intent.extra.STREAM",Uri.fromFile(mFile));
                            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(Intent.createChooser(sharingIntent,"Share using"));


                        }else{
                            Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
                        }
                    } catch (IOException e) {
                        Log.e(this.getClass().getCanonicalName(),"Error saving image");
                    }

                }


            });

        }catch (Exception e) {
            Log.e(this.getClass().getCanonicalName(), "sharing error:"+e.getMessage());
        }
    }


    public void saveFromDeleted(){
        if(oSurface.isWritable()) oSurface.setWritable(false);
        SaveEraserTask oEraser = new SaveEraserTask(getApplicationContext(),false);
        oEraser.execute();
    }


    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        //TODO Selezionare il filtro da applicare
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.effectdialog);
        Window window = dialog.getWindow();
        //window.setLayout(mWidth/2, mHeight/2);
        window.setLayout(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);


        if(oColorPalette !=null){
            mImageEffetView = (ListView) dialog.findViewById(R.id.imageListFilter);
            mImageEffetView.setAdapter(new ImageFilterListAdapter(this,Const.PICTURE_FILTER));
            mImageEffetView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if(requestCode==0){
                        //Take from Camera
                        BitmapWorkerTask oBitmapWorkerTask= new BitmapWorkerTask(null, i);
                        oBitmapWorkerTask.execute();
                    }else if(requestCode==1){
                        //Take from Gallery
                        GalleryWorkerTask oGalleryWorkerTask= new GalleryWorkerTask(data, i);
                        oGalleryWorkerTask.execute();
                    }
                    dialog.dismiss();
                }
            });
        }
        if(data!=null && requestCode==1) dialog.show();
        else if (requestCode==0 && resultCode!=RESULT_CANCELED) dialog.show();

        //int randomNum = 1 + (int)(Math.random()*3);
        //helpGesture(randomNum);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
           Log.v(this.getClass().getCanonicalName(),"Menu Press");
           oSurface.setWritable(false);
        }
        return super.onKeyUp(keyCode, event);
    }
*/


    /**
     * #############################################*
     * #############################################*
     * #############################################*
     * ###############PERSONAL METHOD###############*
     * #############################################*
     * #############################################*
     * #############################################*
     * */

    /**
     * richiamato dopo il task asincrono load.
     * */
    public void loadDiary(boolean isDeletedMode){
        if(isInflateView){
            setContentView(R.layout.new_write_diary);
            isInflateView=false;

            oToolbar = (Toolbar) findViewById(R.id.toolbar);
            applyTemplate();

            setSupportActionBar(oToolbar);


            mMainLayout 	= (DrawerLayout) findViewById(R.id.drawer_layout);
        	/*Relative layout per pagina*/
            mPageLayout = (RelativeLayout) findViewById(R.id.pageLayout);
            mDrawerToggle = new ActionBarDrawerToggle(this, mMainLayout,
                    R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    //getActionBar().setTitle("CLOSED");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    applyTemplate();
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    //getActionBar().setTitle("OPEND");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    applyTemplate();
                }
            };
            // Set the drawer toggle as the DrawerListener
            mMainLayout.setDrawerListener(mDrawerToggle);
            oSurface 		= new TextureHandWrite(this, mDiary, mCurrentPage);
            //oSurface 		= new PageCurlView(this, mDiary, mCurrentPage);
            oScrollView     = new ScrollView(this);
            oLinearLayout   = new LinearLayout(this);

			/*Contriene le preview delle immagini*/
            oGridPreview = (ListView) findViewById(R.id.preview);

            //android.widget.LinearLayout.LayoutParams sideBarParams = new LinearLayout.LayoutParams((int) mSideBarW,LayoutParams.MATCH_PARENT);
            //mSideBarLayout.setLayoutParams(sideBarParams);

            //mMainLayout.setSideBarAndContent(mPageLayout, mSideBarLayout);

            oGridPreview.setAdapter(new PreviewPagesAdapter(mPagesPreview));
        }
        //Devo cambiare oriemtamento della pagina
        if(mCurrentPage!=null){
            if(getRequestedOrientation()!=mCurrentPage.getPageOrientation()){
                oSurface.freeBitmap();
                //System.gc();
            }
            setRequestedOrientation(mCurrentPage.getPageOrientation());
            File shareDelete = new File(sPathImage+"/"+mCurrentPage.getPageID()+Const.CAMERA_SHARE_EXT);
            shareDelete.delete();
            shareDelete=null;
        }

        Rect r = new Rect();
        Window w = getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);
        statusBarHeight=r.top;

        /**init del surface*/
        oSurface.init(mDiary, mCurrentPage);
        oSurface.setDeleteMode(isDeletedMode);
        oSurface.setMarginTop(statusBarHeight + oToolbar.getHeight());
        oLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mPageLayout.removeAllViews();
        oScrollView.removeAllViews();
        oLinearLayout.removeAllViews();

        oScrollView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        oScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Log.v(this.getClass().getCanonicalName(),"Touch Scrool");
                oSurface.onTouchEvent(motionEvent);
                return false;
            }
        });

        oLinearLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        oSurface.setLayoutParams(new RelativeLayout.LayoutParams(mWidth, ((mHeight-statusBarHeight)-oToolbar.getMinimumHeight())));
        oLinearLayout.addView(oSurface);
        oScrollView.addView(oLinearLayout);

        mPageLayout.addView(oScrollView);

        //Floating Button
        FloatingButton mFButton = new FloatingButton(this);
        mFButton.getButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(oColorPalette.isOpened()){
                    oColorPalette.closeLayer(true);
                    oColorPalette.setOpenOnTapEnabled(true);
                }else{
                    oColorPalette.openLayer(true);
                }
            }
        });
        mPageLayout.addView(mFButton);

        //oTavolozza = (SlidingLayer) findViewById(R.id.tavolozza);
        oColorPickerBtn = (ImageButton) findViewById(R.id.showHideColorPalette);
        oColorPickerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(oColorPalette.isOpened()){
                    oColorPalette.closeLayer(true);
                    oColorPalette.setOpenOnTapEnabled(true);
                }else{
                    oColorPalette.openLayer(true);
                }
            }
        });
        oColorSelected = (Button) findViewById(R.id.colorSelected);

        oColorPicker = (ColorPickerView) findViewById(R.id.colorPicker);
        oColorPicker.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {

                oColorPalette.setOpenOnTapEnabled(false);
                mCurrentColor=newColor;
                oSurface.setPage(mCurrentPage);
                //mMenuEdit.setIcon(R.drawable.ic_action_edit_grey);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });



        /**BLACK*/
        Button btnBlack = (Button) findViewById(R.id.black);
        btnBlack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oSurface.setPage(mCurrentPage);
                mCurrentColor=Color.BLACK;
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });
        /**WHITE*/
        Button btnWhite = (Button) findViewById(R.id.white);
        btnWhite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oSurface.setPage(mCurrentPage);
                mCurrentColor=Color.WHITE;
                mMenuEdit.setIcon(R.drawable.ic_action_edit);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });
        /**RED*/
        Button btnRed = (Button) findViewById(R.id.red);
        btnRed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oSurface.setPage(mCurrentPage);
                mCurrentColor=Color.RED;
                mMenuEdit.setIcon(R.drawable.ic_action_edit_red);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });
        /**GREEN*/
        Button btnGreen = (Button) findViewById(R.id.green);
        btnGreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oSurface.setPage(mCurrentPage);
                mCurrentColor=Color.GREEN;
                mMenuEdit.setIcon(R.drawable.ic_action_edit_green);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });
        /**BLUE*/
        Button btmBlue = (Button) findViewById(R.id.blue);
        btmBlue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oSurface.setPage(mCurrentPage);
                mCurrentColor=Color.BLUE;
                mMenuEdit.setIcon(R.drawable.ic_action_edit_blue);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });
        /**YELLOW*/
        Button btmYellow = (Button) findViewById(R.id.yellow);
        btmYellow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oSurface.setPage(mCurrentPage);
                mCurrentColor=Color.YELLOW;
                mMenuEdit.setIcon(R.drawable.ic_action_edit_yellow);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });
        /**cyan*/
        Button btmCyan = (Button) findViewById(R.id.cyan);
        btmCyan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oSurface.setPage(mCurrentPage);
                mCurrentColor=Color.CYAN;
                mMenuEdit.setIcon(R.drawable.ic_action_edit_cyan);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });
        /**PURPLE*/
        Button btmPurple = (Button) findViewById(R.id.purple);
        btmPurple.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oSurface.setPage(mCurrentPage);
                mCurrentColor=Color.MAGENTA;
                mMenuEdit.setIcon(R.drawable.ic_action_edit_purple);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });
        /**GRAY*/
        Button btmGray = (Button) findViewById(R.id.grey);
        btmGray.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oSurface.setPage(mCurrentPage);
                mMenuEdit.setIcon(R.drawable.ic_action_edit_grey);
                mCurrentColor=Color.GRAY;
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
                oColorSelected.setBackgroundColor(mCurrentColor);
            }
        });





        SeekBar oSeek = (SeekBar) findViewById(R.id.stroke);
        oSeek.setMax(100);
        oSeek.setProgress(mCurrentStrokeWidth);
        oSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCurrentStrokeWidth=seekBar.getProgress();
                oSurface.setStrokeWidth(mCurrentStrokeWidth);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

            }
        });
        oColorPalette = (SlidingLayer) findViewById(R.id.colorPalette);
        oColorPalette.setOffsetWidth(20);
        oColorPalette.setSlidingEnabled(true);
        //oTavolozza.setOffsetWidth(10);
        //oTavolozza.setShadowDrawable(R.drawable.sidebar_shadow);
        //oTavolozza.setStickTo(SlidingLayer.STICK_TO_RIGHT);
        //oTavolozza.setCloseOnTapEnabled(true);
        //oTavolozza.setOpenOnTapEnabled(true);
        //oTavolozza.addView(new ColorPickerView(this));
        //mPageLayout.addView(oTavolozza);

        if(oSurface.isWritable()) oSurface.setWritable(false);
        //oSurface.drawEmptyPage();
        // oSurface.drawTextOnSurface();
        //oSurface.createCanvasBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(this.getClass().getCanonicalName(),"onDestroy event");
        if(oSurface!=null) oSurface.freeBitmap();
        System.gc();
    }

    /**
     * applica il template alla action bar
     * */
    private void applyTemplate() {

        Drawable d=null;
        if(mDiary.getDiaryTemplate()==1){
            oToolbar.setBackgroundColor(Const.colorTheme1);
        }else if(mDiary.getDiaryTemplate()==2){
            oToolbar.setBackgroundColor(Const.colorTheme2);
        }else if(mDiary.getDiaryTemplate()==3){
            oToolbar.setBackgroundColor(Const.colorTheme3);
        }else if(mDiary.getDiaryTemplate()==4){
            oToolbar.setBackgroundColor(Const.colorTheme4);
        }else if(mDiary.getDiaryTemplate()==5){
            oToolbar.setBackgroundColor(Const.colorTheme5);
        }else{
            oToolbar.setBackgroundColor(Const.colorTheme6);
        }


//        getActionBar().setBackgroundDrawable(d);
    }

    /**
     * Mostra nasconte la tastiera soft
     * */
    private void toggleSoftKeyboard() {
        //oSurface.setLayoutParams(new RelativeLayout.LayoutParams(mWidth, ((mHeight-statusBarHeight)-oToolbar.getHeight())));
        oSurface.requestFocus();
        if(!isSoftKeyShow){
            //getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            // show the keyboard so we can enter text
            InputMethodManager imm = (InputMethodManager) getApplicationContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            //isSoftKeyShow=imm.showSoftInput(oSurface, InputMethodManager.SHOW_FORCED);
            isSoftKeyShow=true;
            if(oSurface.isWritable()) oSurface.setWritable(false);

        }else{
            InputMethodManager imm = (InputMethodManager) getApplicationContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN,0);

            //imm.hideSoftInputFromInputMethod(oSurface.getWindowToken(), 0);
            //getWindow().setSoftInputMode(
            //	      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            isSoftKeyShow=false;

        }
    }


    
    /**
     * Forza la chiusira della tastiera
     * */
    private void forceCloseKeyboard(){
        if(oSurface!=null) oSurface.clearFocus();
        if(!isSoftKeyShow) return;
        InputMethodManager imm = (InputMethodManager) getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN,0);
        //imm.hideSoftInputFromInputMethod(oSurface.getWindowToken(), 0);
        //getWindow().setSoftInputMode(
        //	      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        isSoftKeyShow=false;
    }

    /**
     * carica le immagini della pagina salvata.
     *
     * */
    public void reloadImageForCurrentPage() {
        oSurface.createCanvasBackground();
		/*if(mCurrentPage.getDiaryImage()==null) return;
		Hashtable<Long, DiaryPicture> mImages = (Hashtable<Long, DiaryPicture>) mCurrentPage.getDiaryImage();
		Map<Long, DiaryPicture> sortedImages = new TreeMap<Long, DiaryPicture>(mImages);
		
		for(DiaryPicture oPicture : sortedImages.values()){
			if(!oPicture.isDiaryHandImage()){
				Bitmap oBmp=BitmapFactoryHelper.decodeSampledBitmapFromFile(oPicture.getDiaryImageURI(),mWidth/4,mHeight/4);
				DiaryPhotoWorker oImg = new DiaryPhotoWorker(getApplicationContext(),mDiary,mCurrentPage.getPageID(),oPicture.getDiaryPictureID());
				oImg.setImageBitmap(oBmp);
				oImg.setX(oPicture.getDiaryPictureX());
				oImg.setY(oPicture.getDiaryPictureY());
				//oImg.setScaleX(0.2f); 
				//oImg.setScaleY(0.f);
				oImg.setImageRotation(oPicture.getDiaryPictureRotation());
				//mPage.addView(oImg);
				oImg=null;
			}
		}	*/
    }
    /**
     * carica le immagini dopo averla ridimensionata
     *
     * */
    public void loadPictureAfterResize(){
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        long mPictureID=0;
        try{
            mPictureID=Long.parseLong(df.format(date));
        }catch(NumberFormatException e){
            mPictureID=0;
        }
        Hashtable<Long, DiaryPicture> mImages = (Hashtable<Long, DiaryPicture>) mCurrentPage.getDiaryImage();
        if(mImages==null) mImages = new Hashtable<Long, DiaryPicture>();

        if(oScaledSizeBmp!=null){
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize=Const.SAMPLESIZEIMAGE;
            options.inSampleSize=calculateInSampleSize();

            DiaryPicture oPicture = new DiaryPicture();
            oPicture.setDiaryImageURI(sPathImage+"/"+sImageName);
            oPicture.setPageID(mCurrentPage.getPageID());
            //TODO oPicture.setDiaryPictureView(oImg);
            oPicture.setDiaryPictureX(mWidth/2);
            oPicture.setDiaryPictureY(mHeight/2);
            oPicture.setDiaryPictureW(oScaledSizeBmp.getWidth());
            oPicture.setDiaryPictureH(oScaledSizeBmp.getHeight());
            //oPicture.setDiaryPictureRotation((int) oImg.getRotation());
            //TODO Strinct Mode ALERT
            oPicture.setBitmapImage(BitmapFactory.decodeFile(sPathImage+"/"+sImageName, options));
            oPicture.setDiaryPictureID(mPictureID);
            mImages.put(mPictureID, oPicture);
            mCurrentPage.setDiaryImage(mImages);
            //oSurface.addPicture(oPicture, oScaledSizeBmp);
            oSurface.drawTextOnSurface();
            oScaledSizeBmp.recycle();
        }

        //TODO caricare il surface con la nuova immagine
        //reloadImageForCurrentPage();
    }


    /**
     * Mostra una dialog per l'help sulle gesture
     *
     * @param typeHelp int  0=swipe to page
     *                      1=zoom photo
     *                      2=rotate photo
     *                      3=delete photo
     * */
    private void helpGesture(final int typeHelp){
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.help);
        Window window = dialog.getWindow();
        window.setLayout(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

        /**Gesture*/
        RelativeLayout lDialog = (RelativeLayout) dialog.findViewById(R.id.dialog);
        ImageView imgGesture    = (ImageView) dialog.findViewById(R.id.imgGesture);
        ImageView imgPhoto      = (ImageView) dialog.findViewById(R.id.imgPhoto);
        TextView txtTitle       = (TextView) dialog.findViewById(R.id.txtTitle);
        TextView txtHelp        = (TextView) dialog.findViewById(R.id.txtHelp);
        Button btnNextTips      = (Button) dialog.findViewById(R.id.btnNextTips);

        switch (typeHelp){
            case 0:
                txtTitle.setText(getString(R.string.helpPage));
                txtHelp.setText(getString(R.string.helpPageDesc));
                imgPhoto.setVisibility(View.INVISIBLE);
                imgGesture.setImageResource(R.drawable.help_swipe_to_page);
                break;
            case 1:
                txtTitle.setText(getString(R.string.helpPhoto));
                txtHelp.setText(getString(R.string.helpPhotoDesc));
                imgPhoto.setVisibility(View.VISIBLE);
                imgGesture.setImageResource(R.drawable.help_single_tap);
                break;
            case 2:
                txtTitle.setText(getString(R.string.helpRotate));
                txtHelp.setText(getString(R.string.helpRotateDesc));
                imgPhoto.setVisibility(View.VISIBLE);
                imgGesture.setImageResource(R.drawable.help_rotate_tap);
                break;
            case 3:
                txtTitle.setText(getString(R.string.helpDelete));
                txtHelp.setText(getString(R.string.helpDeleteDesc));
                imgPhoto.setVisibility(View.VISIBLE);
                imgGesture.setImageResource(R.drawable.help_hold_tap);
                break;

        }
        //Next Tip
        btnNextTips.setOnClickListener(new OnClickListener() {
           int iHelp=typeHelp;
            @Override
            public void onClick(View view) {
                iHelp++;
                if(iHelp>3) iHelp=0;

                dialog.dismiss();
                helpGesture(iHelp);

            }
        });

        lDialog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    /**
     * Seleziona l'orientamento della pagina
     *
     *
     * */
    private void selectPageFormat(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.newpage);
        Window window = dialog.getWindow();
        window.setLayout(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        /**Landspace*/
        Button btnLandspace = (Button) dialog.findViewById(R.id.btnLandspace);
        btnLandspace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                //saveHandWrite(false);

                try {
                    oSurface.freeBitmap();
                    mBitmap=oSurface.getBitmap(mWidth/2,mHeight/2);
                    SavePageTask oSavePage = new SavePageTask(getApplicationContext(),ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    oSavePage.execute();
                    if(oSurface.isWritable()) oSurface.setWritable(false);
                }catch(java.lang.OutOfMemoryError e){
                    Log.e(this.getClass().getCanonicalName(),"Out Of Memory");
                    SavePageTask oSavePage = new SavePageTask(getApplicationContext(),ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    oSavePage.execute();
                    if(oSurface.isWritable()) oSurface.setWritable(false);
                }

                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });
        /**Portrain*/
        Button btnPortrain = (Button) dialog.findViewById(R.id.btnPortrain);
        btnPortrain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                //saveHandWrite(false);

                try {
                    oSurface.freeBitmap();
                    mBitmap=oSurface.getBitmap(mWidth/2,mHeight/2);
                    SavePageTask oSavePage = new SavePageTask(getApplicationContext(),ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    oSavePage.execute();
                    if(oSurface.isWritable()) oSurface.setWritable(false);
                }catch(java.lang.OutOfMemoryError e){
                    Log.e(this.getClass().getCanonicalName(),"Out Of Memory");
                    SavePageTask oSavePage = new SavePageTask(getApplicationContext(),ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    oSavePage.execute();
                    if(oSurface.isWritable()) oSurface.setWritable(false);
                }

                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });

        dialog.show();
    }

    /**
     * Crea la selezione dei colori
     *
     * */
    private void showColorPiker() {
        oColorPalette.setOffsetWidth(20);
        oColorPalette.openLayer(true);
        //oColorPalette.closeLayer(true);
        oColorPalette.setSlidingEnabled(true);

       /* final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.colorpiker);
        Window window = dialog.getWindow();
        //window.setLayout(mWidth/2, mHeight/2);
        window.setLayout(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        RelativeLayout oMainDialog=  (RelativeLayout)window.findViewById(R.id.mainDialog);
        ColorPickerView oColor = (ColorPickerView) window.findViewById(R.id.colorPicker);
        oColor.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                mCurrentColor=newColor;
                oSurface.setPage(mCurrentPage);
                //mMenuEdit.setIcon(R.drawable.ic_action_edit_grey);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
            }
        });
        ColorPickerDialog.ColorPickerView oColor = new ColorPickerDialog.ColorPickerView(this, new ColorPickerDialog.OnColorChangedListener() {
            @Override
            public void colorChanged(String key, int color) {
                mCurrentColor=color;
                oSurface.setPage(mCurrentPage);
                //mMenuEdit.setIcon(R.drawable.ic_action_edit_grey);
                oSurface.setColor(mCurrentColor);
                if(!oSurface.isWritable()) oSurface.setWritable(true);
            }
        }, mCurrentColor,
        mCurrentColor, window.getWindowManager().getDefaultDisplay());

        oMainDialog.addView(oColor);*/

        //dialog.setTitle(getString(R.string.colorpicker));*/

        /**Eraser*//*
		Button btmEraser = (Button) dialog.findViewById(R.id.eraser);
		btmEraser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentColor=Color.TRANSPARENT;
				oSurface.setColor(mCurrentColor);
				oSurface.setWritable(true);
                oColorPalette.setOffsetWidth(0);
                oColorPalette.closeLayer(true);
			}
		});
*/
        /*SeekBar oSeek = (SeekBar) dialog.findViewById(R.id.stroke);
        oSeek.setMax(100);
        oSeek.setProgress(mCurrentStrokeWidth);
        oSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCurrentStrokeWidth=seekBar.getProgress();
                oSurface.setStrokeWidth(mCurrentStrokeWidth);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub

            }
        });

        CheckBox oAutoFingerSize = (CheckBox) dialog.findViewById(R.id.ckAutoFinger);
        oAutoFingerSize.setChecked(oSurface.getAutoFingerSize());
        oAutoFingerSize.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(((CheckBox) v).isChecked()){
                    oSurface.setAutoFingerSize(true);
                }else{
                    oSurface.setAutoFingerSize(false);
                }
                dialog.dismiss();
            }
        });

        dialog.show();*/

    }
    /**
     * Prende una nuova immagine dalla galleria o dalla camera,
     *
     *
     * */
    private void takePhoto() {
        // Take from gallery
        //Intent pickPhoto = new Intent(Intent.ACTION_PICK,
        //           android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //startActivityForResult(pickPhoto , 1);//one can be

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        PackageManager pm = this.getPackageManager();

        if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) && sPathImage!=null) {
            File dir = new File(sPathImage);

            if(!dir.exists()) {
                dir.mkdirs();
            }

            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            sImageName=df.format(date) + Const.CAMERA_PREVIEW_EXT;
				/*gestione Foto*/
            File file = new File(dir, sImageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, android.net.Uri.fromFile(file));
            startActivityForResult(intent, 0);

        }
    }
    /**
     * Prende una nuova immagine dalla galleria o dalla camera,
     *
     *
     * */
    private void takeImageFromGallery() {
        // Take from gallery
        Intent pickPhoto = new Intent(Intent.ACTION_PICK);
        pickPhoto.setType("image/*");

        //           android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);
    }



    /**
     * calcola il sample size
     * */
    public int calculateInSampleSize() {
        // Raw height and width of image
        final int height = Const.IMGHEIGHT;
        final int width = Const.IMGWIDTH;
        int inSampleSize = 1;
        int reqHeight = mWidth/Const.SAMPLESIZEIMAGE;
        int reqWidth = mHeight/Const.SAMPLESIZEIMAGE;
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
    /**
     * Calcola la rotazione dell'immagine per posizionarla correttamente
     *
     * */
    public int getCameraPhotoOrientation(String imagePath){
        int rotate = 0;
        try {
            //context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }


            Log.v(this.getClass().getCanonicalName(), "Exif orientation: " + orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    /**
     * #############################################*
     * ###########END PERSONAL METHOD###############*
     * #############################################*
     * */


    @Override
    public void onSidebarOpened() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSidebarClosed() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onContentTouchedWhenOpening() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
        // TODO Auto-generated method stub
        return false;
    }



    /**
     * carica le image preview
     *
     * */
    private void loadImagePreview(){
        mPagesPreview.clear();
        Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) mDiary.getDiaryPages();
        Map<Long, Page> sortedImages = new TreeMap<Long, Page>(mPages);

        for(final Page oPpage : sortedImages.values()){
            //Immagine Corrente per il diario

            ImageView imageView;// = (ImageView) mPagePreview.findViewById(R.id.imgPreview);
            imageView = new ImageView(this);
            //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            AbsListView.LayoutParams vp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(vp);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(8, 4, 8, 4);
            //Log.v(this.getClass().getCanonicalName(),"Load Preview Page: "+oPpage.getPageID());

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize=Const.SAMPLESIZEDIARY;

            File checkFile = new File(sPathImage + "/" + oPpage.getPageID() + Const.CAMERA_PREVIEW_EXT);
            if(checkFile.exists()){
              imageView.setImageBitmap(BitmapFactory.decodeFile(sPathImage + "/" + oPpage.getPageID() + Const.CAMERA_PREVIEW_EXT, options));
            }else{
            //Impossibile trovale la previe, ne metto una standard
              imageView.setImageResource(R.drawable.diary_preview);
            }
            if(oPpage==mCurrentPage){
                imageView.setAlpha(0.5f);
            }else {
                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //SaveDiaryTask oSaveDiary = new SaveDiaryTask(getApplicationContext());
                        //oSaveDiary.execute();
                        oSurface.freeBitmap();
                        oSurface.destroyDrawingCache();
                        Log.v(this.getClass().getCanonicalName(), "GoTo Page:" + oPpage.getPageID());
                        mCurrentPage = oPpage;
                        getIntent().putExtra("CurrentPage", oPpage.getPageID());
                        PagePictureWorkerTask oTaskAsync = new PagePictureWorkerTask();
                        oTaskAsync.execute();
                    }
                });
            }
            mPagesPreview.add(imageView);
        }
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(oGridPreview!=null && mPagesPreview!=null) oGridPreview.setAdapter(new PreviewPagesAdapter(mPagesPreview));
            }
        });
    }









    /**
     * TASK ACYNC PER CARICAMENTO DIARIO
     *
     *
     * */
    class LoadDiaryTask extends AsyncTask<Object, Void, Boolean>{
        private Context mContext;
        private ProgressDialog oWaitForPage=null;
        public LoadDiaryTask(Context applicationContext) {

            mContext = applicationContext;

        }


        @Override
        protected Boolean doInBackground(Object... params) {
            if(oWaitForPage!=null){
                oWaitForPage.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        oWaitForPage.dismiss();
                        Log.v(this.getClass().getCanonicalName(),"Cancel Load");
                    }
                });
            }
            Repository mRepository=null;
            mDiary.setDiaryID(mDiary.getDiaryID());
            mRepository = new Repository(mContext);
            mDiary=mRepository.reloadDiary(mDiary);
            if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
                sPathImage=Const.EXTDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }else{
                sPathImage=Const.INTERNALDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }
            /**Goto Last Page*/
            Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) mDiary.getDiaryPages();
            Map<Long, Page> sortedPages = new TreeMap<Long, Page>(mPages);

            for(Page oPpage : sortedPages.values()){
                mCurrentPage = oPpage;
                if(mCurrentPageID==oPpage.getPageID()){
                    break;
                }

            }
            sortedPages=null;
            /**End Goto Last Page*/


            mCurrentPage=mRepository.reloadImageForPage(mCurrentPage, calculateInSampleSize());
            loadImagePreview();

            return true;
        }

        @Override
        protected void onPreExecute() {
//			oWaitForPage = ProgressDialog.show(NewWritePageActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            loadDiary(false);
//			oWaitForPage.dismiss();
            super.onPostExecute(result);
        }
        /**
         * carica le image preview
         *
         * *//*
        private void loadImagePreview(){
            Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) mDiary.getDiaryPages();
            Map<Long, Page> sortedImages = new TreeMap<Long, Page>(mPages);

            for(final Page oPpage : sortedImages.values()){
                //Immagine Corrente per il diario
                ImageView imageView;// = (ImageView) mPagePreview.findViewById(R.id.imgPreview);
                imageView = new ImageView(mContext);
                //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                AbsListView.LayoutParams vp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                imageView.setLayoutParams(vp);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 4, 8, 4);
                //Log.v(this.getClass().getCanonicalName(),"Load Preview Page: "+oPpage.getPageID());

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=Const.SAMPLESIZEDIARY;
                //Log.v(this.getClass().getCanonicalName(),"load page preview: "+sPathImage+"/"+oPpage.getPageID()+Const.CAMERA_PREVIEW_EXT);
                imageView.setImageBitmap(BitmapFactory.decodeFile(sPathImage+"/"+oPpage.getPageID()+Const.CAMERA_PREVIEW_EXT, options));
                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //SaveDiaryTask oSaveDiary = new SaveDiaryTask(getApplicationContext());
                        //oSaveDiary.execute();
                        if(oColorPalette.isOpened()) oColorPalette.closeLayer(true);
                        mMainLayout.closeDrawers();
                        oSurface.freeBitmap();
                        oSurface.destroyDrawingCache();
                        Log.v(this.getClass().getCanonicalName(),"GoTo Page:"+oPpage.getPageID());
                        mCurrentPage= oPpage;
                        getIntent().putExtra("CurrentPage", oPpage.getPageID());
                        PagePictureWorkerTask oTaskAsync = new PagePictureWorkerTask();
                        oTaskAsync.execute();
                    }
                });

                mPagesPreview.add(imageView);
            }
        }*/
    }

    /**
     * Task per salvare il diario
     *
     * */
    class SaveDiaryTask extends AsyncTask<Void, Void, Boolean>{
        private Context mContext;
        private ProgressDialog oWaitForSave=null;
        private File mFile=null;
        private FileOutputStream out = null;
        private boolean mBackToHome=false;
        //private Bitmap mBitmap;
        public SaveDiaryTask(Context applicationContext, boolean backToHome) {
            mContext = applicationContext;
            mBackToHome=backToHome;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(oWaitForSave!=null){
                oWaitForSave.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        oWaitForSave.dismiss();
                        Log.v(this.getClass().getCanonicalName(),"Cancel Save");
                    }
                });
            }
            if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
                //Memorizzo il path per le immagini
                sPathImage=Const.EXTDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }else{
                sPathImage=Const.INTERNALDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }


            saveHandWrite();

            savePagePreview();

            loadImagePreview();

            return DiaryRepositoryHelper.dumpDiary(mContext, mDiary);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //refreshCurrentPage();
            //reloadPagePreview();
            if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
                sPathImage=Const.EXTDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }else{
                sPathImage=Const.INTERNALDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }
            oGridPreview.setAdapter(new PreviewPagesAdapter(mPagesPreview));

            try{
                if(oWaitForSave!=null) oWaitForSave.dismiss();
            }catch (IllegalArgumentException e){
                Log.e(this.getClass().getCanonicalName(),"Error Dismiss wait");
            }

            super.onPostExecute(result);
            if(mBackToHome) {
                //Fermo il Thread di scrittura manuale
                if(oSurface!=null){
                    oSurface.stopDrawingThread();
                }
                backToHome();
            }

        }
        @Override
        protected void onPreExecute() {
           try {
               oWaitForSave = ProgressDialog.show(DrawerWritePageActivity.this, getString(R.string.app_name), getString(R.string.wait), true, true, null);
               super.onPreExecute();
           } catch (Exception e){
               Log.e(this.getClass().getCanonicalName(),"error displaing dialog");
           }

        }
        private void saveHandWrite(){

            File dir = new File(sPathImage);

            if(!dir.exists()) {
                dir.mkdirs();
            }
            //Task async per salvare l'immagine.
            Bitmap oBmp = oSurface.getHandWritePath();
            try {
                if(oBmp!=null){
                    mFile = new File(sPathImage+"/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT);
                    out = new FileOutputStream(mFile);

                    oBmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.close();
                    out=null;
                    Log.v(this.getClass().getCanonicalName(),"saving hand write image: "+sPathImage+"/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT);
                }else{
                    Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
                }
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(),"Error saving image");
            }
        }
        /**
         * Salvo la pagepreview
         * */
        private void savePagePreview(){
            try {
                File dir = new File(sPathImage);

                if(!dir.exists()) {
                    dir.mkdirs();
                }
                //Imposto il layer software per prendere la preview della pagina
                //oSurface.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                //oSurface.setDrawingCacheEnabled(true);
                //if(mCurrentPage!=null) mBitmap=oSurface.getBitmap();
                if(mBitmap!=null){
                    mFile = new File(sPathImage+"/"+mCurrentPage.getPageID()+Const.CAMERA_PREVIEW_EXT);
                    out = new FileOutputStream(mFile);
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();
                    out=null;
                    Log.v(this.getClass().getCanonicalName(),"saving Page Preview image: "+sPathImage+"/"+mCurrentPage.getPageID()+Const.CAMERA_PREVIEW_EXT);
                }else{
                    Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
                }
                //oSurface.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(),"Error saving image");
            }
        }
        /**
         * carica le image preview
         *
         * *//*
        private void loadImagePreview(){
            mPagesPreview.clear();
            Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) mDiary.getDiaryPages();
            Map<Long, Page> sortedImages = new TreeMap<Long, Page>(mPages);

            for(final Page oPpage : sortedImages.values()){
                //Immagine Corrente per il diario
                ImageView imageView;// = (ImageView) mPagePreview.findViewById(R.id.imgPreview);
                imageView = new ImageView(mContext);
                //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                AbsListView.LayoutParams vp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                imageView.setLayoutParams(vp);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(8, 4, 8, 4);
                //Log.v(this.getClass().getCanonicalName(),"Load Preview Page: "+oPpage.getPageID());

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=Const.SAMPLESIZEDIARY;
                //Log.v(this.getClass().getCanonicalName(),"load page preview: "+sPathImage+"/"+oPpage.getPageID()+Const.CAMERA_PREVIEW_EXT);
                imageView.setImageBitmap(BitmapFactory.decodeFile(sPathImage+"/"+oPpage.getPageID()+Const.CAMERA_PREVIEW_EXT, options));
                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //SaveDiaryTask oSaveDiary = new SaveDiaryTask(getApplicationContext());
                        //oSaveDiary.execute();
                        oSurface.freeBitmap();
                        oSurface.destroyDrawingCache();
                        Log.v(this.getClass().getCanonicalName(),"GoTo Page:"+oPpage.getPageID());
                        mCurrentPage= oPpage;
                        getIntent().putExtra("CurrentPage", oPpage.getPageID());
                        PagePictureWorkerTask oTaskAsync = new PagePictureWorkerTask();
                        oTaskAsync.execute();
                    }
                });
                mPagesPreview.add(imageView);
            }
        }*/
    }

    /**
     * Task per salvare il diario
     *
     * */
    class SaveEraserTask extends AsyncTask<Void, Void, Boolean>{
        private Context mContext;
        private ProgressDialog oWaitForSave=null;
        private File mFile=null;
        private FileOutputStream out = null;
        private boolean mBackToHome=false;
        //private Bitmap mBitmap;
        public SaveEraserTask(Context applicationContext, boolean backToHome) {
            mContext = applicationContext;
            mBackToHome=backToHome;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(oWaitForSave!=null){
                oWaitForSave.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        oWaitForSave.dismiss();
                        Log.v(this.getClass().getCanonicalName(),"Cancel Save");
                    }
                });
            }
            if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
                //Memorizzo il path per le immagini
                sPathImage=Const.EXTDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }else{
                sPathImage=Const.INTERNALDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }
            saveHandWrite();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
                sPathImage=Const.EXTDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }else{
                sPathImage=Const.INTERNALDIR+getPackageName()+"/"+mDiary.getDiaryID() + "/Pictures";
            }
            oGridPreview.setAdapter(new PreviewPagesAdapter(mPagesPreview));

            if(oWaitForSave!=null) oWaitForSave.dismiss();
            super.onPostExecute(result);

            if(oSurface.isDeleted()){
                oSurface.freeBitmap();
                oSurface.destroyDrawingCache();
                loadDiary(true);
                oSurface.setDeleteMode(true);
            }
        }
        @Override
        protected void onPreExecute() {
            oWaitForSave = ProgressDialog.show(DrawerWritePageActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);

            super.onPreExecute();
        }
        private void saveHandWrite(){

            File dir = new File(sPathImage);

            if(!dir.exists()) {
                dir.mkdirs();
            }
            //Task async per salvare l'immagine.
            Bitmap oBmp = oSurface.getHandWritePath();
            try {
                if(oBmp!=null){
                    mFile = new File(sPathImage+"/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT);
                    out = new FileOutputStream(mFile);

                    oBmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.close();
                    out=null;
                    Log.v(this.getClass().getCanonicalName(),"saving hand write image: "+sPathImage+"/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT);
                }else{
                    Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
                }
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(),"Error saving image");
            }
        }
    }
    /**
     * Task per salvare la pagina corrente nel diario corrente
     *
     * */
    class SavePageTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private ProgressDialog oWaitForSave=null;
        //private Bitmap mBitmap;
        private File mFile=null;
        private FileOutputStream out = null;
        private int mPageOrientation=0;
        public SavePageTask(Context applicationContext) {
            mContext = applicationContext;
        }

        public SavePageTask(Context applicationContext,int orientation) {
            mContext = applicationContext;
            mPageOrientation=orientation;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(oWaitForSave!=null){
                oWaitForSave.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        oWaitForSave.dismiss();
                        Log.v(this.getClass().getCanonicalName(),"Cancel Save");
                    }
                });
            }
            saveHandWrite();

            savePagePreview();

            DiaryRepositoryHelper.dumpPage(mContext, mCurrentPage);
            mCurrentPage=DiaryHelper.factoryNewPageBuilder();
            mCurrentPage.setPageOrientation(mPageOrientation);
            mCurrentPage.setDiaryImage(new Hashtable<Long, DiaryPicture>());
            mDiary=DiaryHelper.addPageToDiary(mDiary, mCurrentPage);
            savePagePreview();
            if(oWaitForSave!=null && oWaitForSave.isShowing()){
                try{
                    oWaitForSave.dismiss();
                }catch (IllegalArgumentException e){
                    Log.e(this.getClass().getCanonicalName(),"no dismiss error");
                }
            }
            return DiaryRepositoryHelper.dumpPage(mContext, mCurrentPage);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //mCurrentPage=DiaryHelper.factoryNewPageBuilder();
            //mCurrentPage.setPageOrientation(mPageOrientation);
            //mDiary=DiaryHelper.addPageToDiary(mDiary, mCurrentPage);//loadDiaryNewPage(true);

            ImageView oImage= new ImageView(DrawerWritePageActivity.this);
            oImage.setImageBitmap(BitmapFactoryHelper.decodeSampledBitmapFromFile(sPathImage+"/"+mCurrentPage.getPageID()+Const.CAMERA_PREVIEW_EXT));
            oOldPage= oImage;

            //oSurface.freeBitmap();
            //oSurface.addThreadForRefresh();


            if(mPageOrientation!=getRequestedOrientation()){
                if(oWaitForSave!=null && !oWaitForSave.isShowing()){
                   try{
                       oWaitForSave.dismiss();
                   }catch (IllegalArgumentException e){
                       Log.e(this.getClass().getCanonicalName(),"no dismiss error");
                   }
                }
            }else{
                if(oWaitForSave!=null && !oWaitForSave.isShowing()){
                    try{
                        oWaitForSave.dismiss();
                    }catch (IllegalArgumentException e){
                        Log.e(this.getClass().getCanonicalName(),"no dismiss error");
                    }
                }
            }

            getIntent().putExtra("template",mDiary.getDiaryTemplate());
            getIntent().putExtra("DiaryID",mCurrentPage.getDiaryID());
            getIntent().putExtra("CurrentPage",mCurrentPage.getPageID());
            oSurface.freeBitmap();
            loadDiary(false);

            //TEST ANIMATION
            FlipAnimation flipAnimation = new FlipAnimation(oOldPage,oSurface);
            flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    mMainLayout.postInvalidate();
                    mMainLayout.invalidate();
                    oSurface.invalidate();
                    oOldPage.invalidate();
                    //oSurface.init(mDiary,mCurrentPage);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if (oOldPage!=null && oOldPage.getVisibility() == View.GONE)
            {
                flipAnimation.reverse();
            }
            mMainLayout.startAnimation(flipAnimation);

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            if(mPageOrientation!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                oWaitForSave = ProgressDialog.show(DrawerWritePageActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);
            }
            super.onPreExecute();
        }

        /**
         * Salvo la pagepreview
         * */
        private void savePagePreview(){
            try {
                //if(mCurrentPage!=null) mBitmap=oSurface.getBitmap();
                if(mBitmap!=null){
                    mFile = new File(sPathImage+"/"+mCurrentPage.getPageID()+Const.CAMERA_PREVIEW_EXT);
                    out = new FileOutputStream(mFile);
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();
                    out=null;
                    Log.v(this.getClass().getCanonicalName(),"Page Preview saving image: "+sPathImage+"/"+mCurrentPage.getPageID()+Const.CAMERA_PREVIEW_EXT);
                }else{
                    Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
                }
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(),"Error saving image");
            }
        }
        /**
         * Salvo le vecchie path
         * */
        private void saveHandWrite(){
            //Task async per salvare l'immagine.
            Bitmap oBmp = oSurface.getHandWritePath();
            try {
                if(oBmp!=null){
                    mFile = new File(sPathImage+"/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT);
                    out = new FileOutputStream(mFile);

                    oBmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.close();
                    out=null;
                    Log.v(this.getClass().getCanonicalName(),"Page Preview saving image: "+sPathImage+"/h"+mCurrentPage.getPageID()+Const.PAGE_PREVIEW_EXT);
                }else{
                    Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
                }
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(),"Error saving image");
            }
        }
    }

    /**
     * cancella la pagina corrente
     * */
    class DeletePageTask extends AsyncTask<Void, Void, Boolean> {
        private Repository mRepository;

        public DeletePageTask(){
            mRepository=new Repository(getApplicationContext());

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(mCurrentPage!=null
                    && mCurrentPage.getDiaryImage()!=null
                    && mCurrentPage.getDiaryImage().size()>0){

                //Cancello le immagini della pagina
                Hashtable<Long, DiaryPicture> mImages = (Hashtable<Long, DiaryPicture>) mCurrentPage.getDiaryImage();
                TreeMap<Long, DiaryPicture> sortedImages = new TreeMap<Long, DiaryPicture>(mImages);
                for(DiaryPicture oPicture : sortedImages.values()){
                    File removeFile = new File(oPicture.getDiaryImageURI());
                    if(!removeFile.delete()){
                        Log.e(DiaryRepositoryHelper.class.getClass().getCanonicalName(),"Error removing image for page: "+removeFile.getAbsoluteFile());
                    }
                    removeFile=null;
                }
            }
            boolean bDelete = mRepository.deletePage(mCurrentPage);
            oSurface.freeBitmap();
            mDiary.getDiaryPages().remove(mCurrentPage.getPageID());

            loadImagePreview();
            return bDelete;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) mDiary.getDiaryPages();
            Map<Long, Page> sortedPages = new TreeMap<Long, Page>(mPages);

            for(Page oPpage : sortedPages.values()){
                mCurrentPage = oPpage;
            }
            reloadImageForCurrentPage();
            loadDiary(false);
        }
        /**
         * carica le image preview
         *
         * *//*
        private void loadImagePreview(){
            mPagesPreview.clear();
            Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) mDiary.getDiaryPages();
            Map<Long, Page> sortedImages = new TreeMap<Long, Page>(mPages);

            for(final Page oPpage : sortedImages.values()){
                //Immagine Corrente per il diario
                ImageView imageView;// = (ImageView) mPagePreview.findViewById(R.id.imgPreview);
                imageView = new ImageView(getApplicationContext());
                //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                AbsListView.LayoutParams vp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                imageView.setLayoutParams(vp);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(8, 4, 8, 4);
                //Log.v(this.getClass().getCanonicalName(),"Load Preview Page: "+oPpage.getPageID());

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=Const.SAMPLESIZEDIARY;
                //Log.v(this.getClass().getCanonicalName(),"load page preview: "+sPathImage+"/"+oPpage.getPageID()+Const.CAMERA_PREVIEW_EXT);
                imageView.setImageBitmap(BitmapFactory.decodeFile(sPathImage+"/"+oPpage.getPageID()+Const.CAMERA_PREVIEW_EXT, options));
                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //SaveDiaryTask oSaveDiary = new SaveDiaryTask(getApplicationContext());
                        //oSaveDiary.execute();
                        oSurface.freeBitmap();
                        oSurface.destroyDrawingCache();
                        Log.v(this.getClass().getCanonicalName(),"GoTo Page:"+oPpage.getPageID());
                        mCurrentPage= oPpage;
                        getIntent().putExtra("CurrentPage", oPpage.getPageID());
                        PagePictureWorkerTask oTaskAsync = new PagePictureWorkerTask();
                        oTaskAsync.execute();
                    }
                });
                mPagesPreview.add(imageView);
            }
        }*/
    }

    class PreviewPagesAdapter extends BaseAdapter {

        public PreviewPagesAdapter(ArrayList<ImageView> pagesPreview) {
            mPagesPreview=pagesPreview;
        }

        public int getCount() {
            return mPagesPreview.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }
        //TODO DA VELOCIZZARE
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(position>=mPagesPreview.size()) return mPagesPreview.get(position-1);
            else return mPagesPreview.get(position);
        }

    }

    class BitmapWorkerTask extends AsyncTask<Void, String, Boolean> {
        private ProgressDialog oWaitForSave=null;
        private File mFile=null;
        private FileOutputStream out = null;
        private CompressFormat mCompress=null;
        private Bitmap oOriginalSizeBmp=null;
        private int mRotationAngle=0;
        private int mImageFilter=0;
        public BitmapWorkerTask(CompressFormat compress,int imageFilter) {
            mCompress=compress;
            mImageFilter=imageFilter;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            if(oWaitForSave!=null){
                oWaitForSave.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        oWaitForSave.dismiss();
                        Log.v(this.getClass().getCanonicalName(),"Cancel Save");
                    }
                });
            }
            /*bitmap della fotocamera in dimensione originale*/
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize=Const.SAMPLESIZEDIARY;
            oOriginalSizeBmp = BitmapFactory.decodeFile(sPathImage + "/" + sImageName,options);
            //Calcolo la rotazione dell'immagine
            mRotationAngle=getCameraPhotoOrientation(sPathImage + "/" + sImageName);

            if(oOriginalSizeBmp==null) return false;
            if(oOriginalSizeBmp.getWidth()> oOriginalSizeBmp.getHeight()){
                oScaledSizeBmp = Bitmap.createScaledBitmap(oOriginalSizeBmp, Const.IMGWIDTH, Const.IMGHEIGHT, true);
            }else{
                oScaledSizeBmp = Bitmap.createScaledBitmap(oOriginalSizeBmp, Const.IMGHEIGHT, Const.IMGWIDTH, true);
            }

            ImageFilters oFilter = new ImageFilters();
            switch (mImageFilter){
                case 0:
                    //- NOTHING -

                    break;
                case 1:
                    //HighlightEffect
                    oScaledSizeBmp=oFilter.applyHighlightEffect(oScaledSizeBmp);
                    break;
                case 2:
                    //InvertEffect
                    oScaledSizeBmp=oFilter.applyInvertEffect(oScaledSizeBmp);
                    break;
                case 3:
                    //GreyscaleEffect
                    oScaledSizeBmp=oFilter.applyGreyscaleEffect(oScaledSizeBmp);
                    break;
                case 4:
                    //GammaEffect
                    oScaledSizeBmp=oFilter.applyGammaEffect(oScaledSizeBmp,100,50,25);
                    break;
                case 5:
                    //ColorFilterEffect
                    //oScaledSizeBmp=oFilter.applyColorFilterEffect(oScaledSizeBmp);
                    break;
                case 6:
                    //SepiaToningEffect
                    //oScaledSizeBmp=oFilter.applySepiaToningEffect(oScaledSizeBmp);
                    break;
                case 7:
                    //DecreaseColorDepthEffect
                    oScaledSizeBmp=oFilter.applyDecreaseColorDepthEffect(oScaledSizeBmp,20);
                    break;
                case 8:
                    //ContrastEffect
                    oScaledSizeBmp=oFilter.applyContrastEffect(oScaledSizeBmp,2d);
                    break;
                case 9:
                    //BrightnessEffect
                    oScaledSizeBmp=oFilter.applyBrightnessEffect(oScaledSizeBmp,2);
                    break;
                case 10:
                    //HighlightEffect
                    oScaledSizeBmp=oFilter.applyGaussianBlurEffect(oScaledSizeBmp);
                    break;
                case 11:
                    //HighlightEffect
                    oScaledSizeBmp=oFilter.applySharpenEffect(oScaledSizeBmp,2d);
                    break;
                case 12:
                    //InvertEffect
                    oScaledSizeBmp=oFilter.applyMeanRemovalEffect(oScaledSizeBmp);
                    break;
                case 13:
                    //GreyscaleEffect
                    oScaledSizeBmp=oFilter.applySmoothEffect(oScaledSizeBmp,2d);
                    break;
                case 14:
                    //GammaEffect
                    oScaledSizeBmp=oFilter.applyEmbossEffect(oScaledSizeBmp);
                    break;
                case 15:
                    //EngraveEffect
                    oScaledSizeBmp=oFilter.applyEngraveEffect(oScaledSizeBmp);
                    break;
                case 16:
                    //BoostEffect
                    oScaledSizeBmp=oFilter.applyBoostEffect(oScaledSizeBmp,2,50);
                    break;
                case 17:
                    //RoundCornerEffect
                    oScaledSizeBmp=oFilter.applyRoundCornerEffect(oScaledSizeBmp,35);
                    break;
                case 18:
                    //WaterMarkEffect
                    //oScaledSizeBmp=oFilter.applyWaterMarkEffect(oScaledSizeBmp);
                    break;
                case 19:
                    //BlackFilter
                    oScaledSizeBmp=oFilter.applyBlackFilter(oScaledSizeBmp);
                    break;
                case 20:
                    //SnowEffect
                    oScaledSizeBmp=oFilter.applySnowEffect(oScaledSizeBmp);
                    break;
                case 21:
                    //ShadingFilter
                    oScaledSizeBmp=oFilter.applyShadingFilter(oScaledSizeBmp,Color.WHITE);
                    break;
                case 22:
                    //SaturationFilter
                    oScaledSizeBmp=oFilter.applySaturationFilter(oScaledSizeBmp,4);
                    break;
                case 23:
                    //HueFilter
                    oScaledSizeBmp=oFilter.applyHueFilter(oScaledSizeBmp,4);
                    break;
                case 24:
                    //Reflection
                    oScaledSizeBmp=oFilter.applyReflection(oScaledSizeBmp);
                    break;
                case 25:
                    //TintEffect
                    oScaledSizeBmp=oFilter.applyTintEffect(oScaledSizeBmp,30);
                    break;
                case 26:
                    //FleaEffect
                    oScaledSizeBmp=oFilter.applyFleaEffect(oScaledSizeBmp);
                    break;
                case 27:
                    //Old Paper
                    oScaledSizeBmp=combineImages(oScaledSizeBmp,"1");
                    break;
                case 28:
                    //Frame
                    oScaledSizeBmp=combineImages(oScaledSizeBmp,"2");
                    break;
                case 29:
                    //Seam
                    oScaledSizeBmp=combineImages(oScaledSizeBmp,"3");
                    break;
                case 30:
                    //Clip Paper
                    oScaledSizeBmp=combineImages(oScaledSizeBmp,"4");
                    break;
                case 31:
                    //Pellicle
                    oScaledSizeBmp=combineImages(oScaledSizeBmp,"5");
                    break;
                case 32:
                    //Broker Glass
                    oScaledSizeBmp=combineImages(oScaledSizeBmp,"6");
                    break;
            }

            //ROTAZIONE DELL'IMMAGINE
            Canvas mRotateCanvas=new Canvas();
            Matrix matrix = new Matrix();
            matrix.reset();
            matrix.postTranslate(-oScaledSizeBmp.getWidth() / 2, -oScaledSizeBmp.getHeight() / 2); // Centers image
            matrix.postRotate(mRotationAngle);
            matrix.postTranslate(oScaledSizeBmp.getWidth(), oScaledSizeBmp.getHeight());
            oScaledSizeBmp = Bitmap.createBitmap(oScaledSizeBmp,0,0,oScaledSizeBmp.getWidth(),oScaledSizeBmp.getHeight(),matrix,true);
            mRotateCanvas.drawBitmap(oScaledSizeBmp, 0,0,new Paint());


            try {
                if(oScaledSizeBmp!=null){
                    mFile = new File(sPathImage+"/"+sImageName);
                    out = new FileOutputStream(mFile);
                    if(mCompress==null){
                        mCompress=Bitmap.CompressFormat.JPEG;
                        Log.v(this.getClass().getCanonicalName(),"Compress JPEG");
                    }
                    oScaledSizeBmp.compress(mCompress, 90, out);
                    out.close();
                    out=null;
                    Log.v(this.getClass().getCanonicalName(),"Saving Scalated Image: "+sPathImage+"/"+sImageName);
                }else{
                    Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
                }
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(),"Error saving image");
            }
            //Salvo prima l'immagine
            loadPictureAfterResize();
            return DiaryRepositoryHelper.dumpDiary(getApplicationContext(), mDiary);
        }
        /**Combina le due immagini per generare la photo effects*/
        private Bitmap combineImages(Bitmap scalatedPhoto, String preMadeEffect){
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=1;//Const.WORKIZEIMAGE;
                Bitmap bmpEffect = BitmapFactory.decodeStream(getAssets().open("template/"+preMadeEffect+"/photo.png"),null,options);
                Bitmap cs = null;

                //TODO POSSIBILE OUT OF MEMORY
                cs = Bitmap.createBitmap(bmpEffect.getWidth(), bmpEffect.getHeight(), Bitmap.Config.ARGB_8888);

                Canvas comboImage = new Canvas(cs);

                comboImage.drawBitmap(scalatedPhoto, (bmpEffect.getWidth()-scalatedPhoto.getWidth())/2, (bmpEffect.getHeight()-scalatedPhoto.getHeight())/2, null);
                comboImage.drawBitmap(bmpEffect, 0f, 0f, null);

                return cs;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            oWaitForSave = ProgressDialog.show(DrawerWritePageActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //Show image on view
            oSurface.freeBitmap();
            if(oOriginalSizeBmp!=null){

                loadDiary(false);
            }
            if(oWaitForSave!=null && oWaitForSave.isShowing()){
                try{
                    oWaitForSave.dismiss();
                }catch (IllegalArgumentException e){
                    Log.e(this.getClass().getCanonicalName(),"no dismiss error");
                }
            }
            super.onPostExecute(result);
        }
    }
    /**
     * elaborazione delle immagini dalla galleria
     * */
    class GalleryWorkerTask extends AsyncTask<Void, String, Boolean> {
        private ProgressDialog oWaitForSave=null;
        private File mFile=null;
        private FileOutputStream out = null;
        private CompressFormat mCompress=null;
        private Intent mData;
        private int mRotationAngle=0;
        private int mImageFilter=0;
        public GalleryWorkerTask(Intent data, int imageFilter) {
            mData=data;
            mImageFilter=imageFilter;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            try {
                if(oWaitForSave!=null){
                    oWaitForSave.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            oWaitForSave.dismiss();
                            Log.v(this.getClass().getCanonicalName(),"Cancel Save");
                        }
                    });
                }
                Date date = new Date();
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                sImageName=df.format(date) + Const.CAMERA_PREVIEW_EXT;
                if(mData==null) return false;
                Uri imageFromGallery = mData.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageFromGallery);
                /*
                TODO OUTOFMEMORY
                bitmap della galleria in dimensione originale
                */
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=Const.WORKIZEIMAGE;
                Bitmap oOriginalSizeBmp = BitmapFactory.decodeStream(imageStream,null,options);
                imageStream.close();

                Cursor cursor = getContentResolver().query(imageFromGallery, null, null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

                //Calcolo la rotazione dell'immagine
                //mRotationAngle=getCameraPhotoOrientation(cursor.getString(idx));
                cursor.close();

                if(oOriginalSizeBmp!=null && (oOriginalSizeBmp.getWidth()> oOriginalSizeBmp.getHeight())){
                    oScaledSizeBmp = Bitmap.createScaledBitmap(oOriginalSizeBmp, Const.IMGWIDTH, Const.IMGHEIGHT, true);
                }else{
                    oScaledSizeBmp = Bitmap.createScaledBitmap(oOriginalSizeBmp, Const.IMGHEIGHT, Const.IMGWIDTH, true);
                }

                ImageFilters oFilter = new ImageFilters();
                switch (mImageFilter){
                    case 0:
                        //- NOTHING -

                        break;
                    case 1:
                        //HighlightEffect
                        oScaledSizeBmp=oFilter.applyHighlightEffect(oScaledSizeBmp);
                        break;
                    case 2:
                        //InvertEffect
                        oScaledSizeBmp=oFilter.applyInvertEffect(oScaledSizeBmp);
                        break;
                    case 3:
                        //GreyscaleEffect
                        oScaledSizeBmp=oFilter.applyGreyscaleEffect(oScaledSizeBmp);
                        break;
                    case 4:
                        //GammaEffect
                        oScaledSizeBmp=oFilter.applyGammaEffect(oScaledSizeBmp,100,50,25);
                        break;
                    case 5:
                        //ColorFilterEffect
                        //oScaledSizeBmp=oFilter.applyColorFilterEffect(oScaledSizeBmp);
                        break;
                    case 6:
                        //SepiaToningEffect
                        //oScaledSizeBmp=oFilter.applySepiaToningEffect(oScaledSizeBmp);
                        break;
                    case 7:
                        //DecreaseColorDepthEffect
                        oScaledSizeBmp=oFilter.applyDecreaseColorDepthEffect(oScaledSizeBmp,20);
                        break;
                    case 8:
                        //ContrastEffect
                        oScaledSizeBmp=oFilter.applyContrastEffect(oScaledSizeBmp,2d);
                        break;
                    case 9:
                        //BrightnessEffect
                        oScaledSizeBmp=oFilter.applyBrightnessEffect(oScaledSizeBmp,2);
                        break;
                    case 10:
                        //HighlightEffect
                        oScaledSizeBmp=oFilter.applyGaussianBlurEffect(oScaledSizeBmp);
                        break;
                    case 11:
                        //HighlightEffect
                        oScaledSizeBmp=oFilter.applySharpenEffect(oScaledSizeBmp,2d);
                        break;
                    case 12:
                        //InvertEffect
                        oScaledSizeBmp=oFilter.applyMeanRemovalEffect(oScaledSizeBmp);
                        break;
                    case 13:
                        //GreyscaleEffect
                        oScaledSizeBmp=oFilter.applySmoothEffect(oScaledSizeBmp,2d);
                        break;
                    case 14:
                        //GammaEffect
                        oScaledSizeBmp=oFilter.applyEmbossEffect(oScaledSizeBmp);
                        break;
                    case 15:
                        //EngraveEffect
                        oScaledSizeBmp=oFilter.applyEngraveEffect(oScaledSizeBmp);
                        break;
                    case 16:
                        //BoostEffect
                        oScaledSizeBmp=oFilter.applyBoostEffect(oScaledSizeBmp,2,50);
                        break;
                    case 17:
                        //RoundCornerEffect
                        oScaledSizeBmp=oFilter.applyRoundCornerEffect(oScaledSizeBmp,35);
                        break;
                    case 18:
                        //WaterMarkEffect
                        //oScaledSizeBmp=oFilter.applyWaterMarkEffect(oScaledSizeBmp);
                        break;
                    case 19:
                        //BlackFilter
                        oScaledSizeBmp=oFilter.applyBlackFilter(oScaledSizeBmp);
                        break;
                    case 20:
                        //SnowEffect
                        oScaledSizeBmp=oFilter.applySnowEffect(oScaledSizeBmp);
                        break;
                    case 21:
                        //ShadingFilter
                        oScaledSizeBmp=oFilter.applyShadingFilter(oScaledSizeBmp,Color.WHITE);
                        break;
                    case 22:
                        //SaturationFilter
                        oScaledSizeBmp=oFilter.applySaturationFilter(oScaledSizeBmp,4);
                        break;
                    case 23:
                        //HueFilter
                        oScaledSizeBmp=oFilter.applyHueFilter(oScaledSizeBmp,4);
                        break;
                    case 24:
                        //Reflection
                        oScaledSizeBmp=oFilter.applyReflection(oScaledSizeBmp);
                        break;
                    case 25:
                        //TintEffect
                        oScaledSizeBmp=oFilter.applyTintEffect(oScaledSizeBmp,30);
                        break;
                    case 26:
                        //FleaEffect
                        oScaledSizeBmp=oFilter.applyFleaEffect(oScaledSizeBmp);
                        break;
                    case 27:
                        //Old Paper
                        oScaledSizeBmp=combineImages(oScaledSizeBmp,"1");
                        break;
                    case 28:
                        //Frame
                        oScaledSizeBmp=combineImages(oScaledSizeBmp,"2");
                        break;
                    case 29:
                        //Seam
                        oScaledSizeBmp=combineImages(oScaledSizeBmp,"3");
                        break;
                    case 30:
                        //Clip Paper
                        oScaledSizeBmp=combineImages(oScaledSizeBmp,"4");
                        break;
                    case 31:
                        //Pellicle
                        oScaledSizeBmp=combineImages(oScaledSizeBmp,"5");
                        break;
                    case 32:
                        //Broker Glass
                        oScaledSizeBmp=combineImages(oScaledSizeBmp,"6");
                        break;
                }

                //ROTAZIONE DELL'IMMAGINE
                Canvas mRotateCanvas=new Canvas();
                Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postTranslate(-oScaledSizeBmp.getWidth() / 2, -oScaledSizeBmp.getHeight() / 2); // Centers image
                matrix.postRotate(mRotationAngle);
                matrix.postTranslate(oScaledSizeBmp.getWidth(), oScaledSizeBmp.getHeight());
                oScaledSizeBmp = Bitmap.createBitmap(oScaledSizeBmp,0,0,oScaledSizeBmp.getWidth(),oScaledSizeBmp.getHeight(),matrix,true);
                mRotateCanvas.drawBitmap(oScaledSizeBmp, 0,0,new Paint());

                if(oScaledSizeBmp!=null){
                    mFile = new File(sPathImage+"/"+sImageName);
                    out = new FileOutputStream(mFile);
                    if(mCompress==null){
                        mCompress=Bitmap.CompressFormat.JPEG;
                        Log.v(this.getClass().getCanonicalName(),"Compress JPEG");
                    }
                    oScaledSizeBmp.compress(mCompress, 90, out);
                    out.close();
                    out=null;
                    Log.v(this.getClass().getCanonicalName(),"Saving Scalated Image: "+sPathImage+"/"+sImageName);
                }else{
                    Log.e(this.getClass().getCanonicalName(),"NULL Page Preview saving image");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(),"Error saving image");
            }
            //Salvo prima l'immagine
            loadPictureAfterResize();
            return DiaryRepositoryHelper.dumpDiary(getApplicationContext(), mDiary);
        }
        /**Combina le due immagini per generare la photo effects*/
        private Bitmap combineImages(Bitmap scalatedPhoto, String preMadeProject){
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=1;//Const.WORKIZEIMAGE;
                Bitmap bmpEffect = BitmapFactory.decodeStream(getAssets().open("template/"+preMadeProject+"/photo.png"),null,options);
                Bitmap cs = null;

                //TODO POSSIBILE OUT OF MEMORY
                cs = Bitmap.createBitmap(bmpEffect.getWidth(), bmpEffect.getHeight(), Bitmap.Config.ARGB_8888);

                Canvas comboImage = new Canvas(cs);

                comboImage.drawBitmap(scalatedPhoto, (bmpEffect.getWidth()-scalatedPhoto.getWidth())/2, (bmpEffect.getHeight()-scalatedPhoto.getHeight())/2, null);
                comboImage.drawBitmap(bmpEffect, 0f, 0f, null);

                return cs;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            oWaitForSave = ProgressDialog.show(DrawerWritePageActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean result) {
            //Show image on view
            oSurface.freeBitmap();

            loadDiary(false);
            if(oWaitForSave!=null && oWaitForSave.isShowing()){
                try{
                    oWaitForSave.dismiss();
                }catch (IllegalArgumentException e){
                    Log.e(this.getClass().getCanonicalName(),"no dismiss error");
                }
            }
            super.onPostExecute(result);
        }
    }
    /**
     * task per caricare le immagini della pagina corrente in
     * un task asincrono.
     * */
    class PagePictureWorkerTask extends AsyncTask<Void, String, Boolean> {
        Repository mRepository=null;
        @Override
        protected Boolean doInBackground(Void... voids) {
            mRepository = new Repository(getApplicationContext());
            mCurrentPage=mRepository.reloadImageForPage(mCurrentPage, calculateInSampleSize());
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            ImageView oImage= new ImageView(DrawerWritePageActivity.this);
            oImage.setImageBitmap(BitmapFactoryHelper.decodeSampledBitmapFromFile(sPathImage+"/"+mCurrentPage.getPageID()+Const.CAMERA_PREVIEW_EXT));
            oOldPage= oImage;

            //TEST ANIMATION
            FlipAnimation flipAnimation = new FlipAnimation(oOldPage,oSurface);
            flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mMainLayout.closeDrawers();
                    loadImagePreview();
                    loadDiary(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Log.v(this.getClass().getCanonicalName(), "Animation End");
                    mMainLayout.postInvalidate();
                    mMainLayout.invalidate();
                    oSurface.invalidate();
                    oOldPage.invalidate();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if (oOldPage!=null && oOldPage.getVisibility() == View.GONE)
            {
                flipAnimation.reverse();
            }
            mMainLayout.startAnimation(flipAnimation);

        }
    }

    /**
     * task per caricare le immagini della pagina corrente in
     * un task asincrono.
     * */
    class ExportPDFTask extends AsyncTask<Void, String, Boolean> {
        private ProgressDialog oWaitForSave=null;
        private Repository mRepository=null;
        private PdfBuilder oPdf;
        @Override
        protected Boolean doInBackground(Void... voids) {
            if(oWaitForSave!=null){
                oWaitForSave.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        oWaitForSave.dismiss();
                        Log.v(this.getClass().getCanonicalName(),"Cancel Save");
                    }
                });
            }
            mRepository = new Repository(getApplicationContext());
            mDiary=mRepository.reloadDiaryForExport(mDiary);

            oPdf = new PdfBuilder(getApplicationContext(),mDiary,mWidth,mHeight);
            return true;
        }

        @Override
        protected void onPreExecute() {
            oWaitForSave = ProgressDialog.show(DrawerWritePageActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(oWaitForSave!=null && oWaitForSave.isShowing()){
                try{
                    oWaitForSave.dismiss();
                }catch (IllegalArgumentException e){
                    Log.e(this.getClass().getCanonicalName(),"no dismiss error");
                }
            }
            if(oPdf.pdfBuilder()){
                Intent myIntent = new Intent(Intent.ACTION_VIEW);
                myIntent.setDataAndType(Uri.fromFile(oPdf.getPdfFile()), "application/pdf");
                try{
                    startActivity(myIntent);
                }catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Unable to Open File", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}