package com.glm.ememories;

import java.util.ArrayList;
import java.util.Hashtable;
// java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;


/*import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.RequestToken;*/
import com.glm.bean.Page;
import com.glm.db.DiaryRepositoryHelper;
import com.glm.bean.Diary;
import com.glm.utilities.Rate;
import com.glm.view.Card;
//import com.glm.utilities.SynchHelper;


public class DiaryActivity extends ActionBarActivity {

    private static final boolean DEVELOPER_MODE = false; //questa variabile andra' settata a false quando l' applicazione verra' rilasciata
    private LinearLayout oMainLayout;

    private int mWidth;
    private int mHeight;
    private int mCalculateInSampleSize = 0;
    private Context mContext;
    /**
     * Ricerca
     */
    private DiariesAdapter mDiariesAdapter = null;

    private Dialog mDialogLogin;
    private Button mLogin;
    private Button mCancel;
    private EditText mUserName;
    private EditText mPassword;
    private CheckBox mNewUser;

    //private SynchHelper mSynchHelper;
    //private RequestToken mRefresh;
    private static final String REFRESH_TOKEN_KEY = "refresh";
    private SharedPreferences mPrefs;
    private boolean isCloudEnebled=false;
    private ListView mGridview;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects() //or .detectAll() for all detectable problems
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        Log.v(this.getClass().getCanonicalName(), "Data storage " + Environment.getDataDirectory());


        //Typeface type 	= Typeface.createFromAsset(getAssets(),"fonts/font_1.ttf");
        //Non tutte le violazioni vanno corrette. Questo codice per esempio prende la plitica corrente, crea un politica simile
        //ma che ignora le violazioni da scrittura su disco, fa girare il codice che ha qualche scrittura su disco
        //(con doCorrectStuffThatWritesToDisk()) e poi riprende la politica originale.
        //Le SharedPreferences, se ci sono, entrano in conflitto con la StrictMode
        //		StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        //		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
        //		    .permitDiskWrites()
        //		    .build());
        //		doCorrectStuffThatWritesToDisk();
        //		StrictMode.setThreadPolicy(old);

        //Per informazioni varie vedere http://mobile.tutsplus.com/tutorials/android/android-sdk_strictmode/
        //oppure http://developer.android.com/reference/android/os/StrictMode.html
        //oppure http://developer.android.com/guide/practices/design/responsiveness.html

        super.onCreate(savedInstanceState);

        /*if (savedInstanceState != null) {
            mRefresh = RequestToken.loadAndResume(savedInstanceState, REFRESH_TOKEN_KEY, new BaasHandler<List<BaasDocument>>() {
                @Override
                public void handle(BaasResult<List<BaasDocument>> req) {
                    mRefresh = null;
                    Log.d(this.getClass().getCanonicalName(),"Refresh Token");


                }
            });
        }*/

        setContentView(R.layout.diary_screen);


        final ImageView oNewDiary = (ImageView) findViewById(R.id.newDiary);
        oNewDiary.setOnClickListener(new
                                             OnClickListener() {
                                                 @Override
                                                 public void onClick(View v) {
                                                     Intent intent = new Intent();
                                                     ActivityOptions options = ActivityOptions.makeScaleUpAnimation(oNewDiary, 0,
                                                             0, oNewDiary.getWidth(), oNewDiary.getHeight());
                                                     intent.setClass(DiaryActivity.this, TemplateActivity.class);
                                                     intent.putExtra("template", true);

                                                     Log.v(this.getClass().getCanonicalName(),"New On Click!");
                                                     DiaryActivity.this.startActivity(intent,options.toBundle());
                                                     DiaryActivity.this.finish();

                                                 }
                                             });
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if(size!=null) {
            mWidth = size.x;
            mHeight = size.y;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        oMainLayout = (LinearLayout) findViewById(R.id.main_screen);

        mCalculateInSampleSize = calculateInSampleSize();
        mContext = getApplicationContext();
        mGridview = (ListView) findViewById(R.id.diaries);

        DiariesAsyncTask oDiariesAsync = new DiariesAsyncTask();
        oDiariesAsync.execute();
    }

    /**
     * Aggiunde un bottone per andare al diario presente in DB
     * <p/>
     * TODO Aggiungere un task asincrono
     */
    public void addDiary(ArrayList<Card> diariesPreview) {
        //mDiaries=diaries;
        if (diariesPreview.size() > 0) {
            mDiariesAdapter = new DiariesAdapter(this, diariesPreview);
            //Aggiorno la gridView

            mDiariesAdapter.notifyDataSetChanged();
            mDiariesAdapter.notifyDataSetInvalidated();
            mGridview.setAdapter(mDiariesAdapter);
        }
    }

    /*protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mRefresh != null) {
            mRefresh.suspendAndSave(outState, REFRESH_TOKEN_KEY);
        }
    }*/

	/**
	 * This method is called when the back button of the table is
	 * pressed
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitByBackKey();

			//moveTaskToBack(false);

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    protected void onPause() {
        super.onPause();
        oMainLayout.getBackground().setCallback(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        oMainLayout.getBackground().setCallback(null);
    }

    protected void exitByBackKey() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				DiaryActivity.this);

		// set title
		alertDialogBuilder.setTitle(getString(R.string.dAttention));

		// set dialog message
		alertDialogBuilder
		.setMessage(getString(R.string.dExit))
		.setCancelable(false)
		.setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //if(mSynchHelper!=null) mSynchHelper.userLogOut();

                        // if this button is clicked, close
                        // current activity
                        DiaryActivity.this.finish();
                    }
                }).start();




			}
		})
		.setNegativeButton(getString(R.string.no),new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, just close
				// the dialog box and do nothing
				dialog.dismiss();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}

	/**
	 * This method create an action bar menu
	 * @author coluzza
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_page_options_menu, menu);


		return true;
	}

    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
           case R.id.btnSearch:
            createSearchDialog();
            break;
        /*
        case R.id.btnCloud:
            createLoginToCloudDialog();
            break;
		case R.id.save:
			Intent intent1=nu.l;
			 
			intent1 = ActivityHelper.createActivityIntent(DiaryActivity.this,TestSurfaceViewActivity.class);
			intent1.putExtra("template", 1);
			intent1.putExtra("DiaryID", "20130429192410180");
			
			ActivityHelper.startNewActivityAndFinish(DiaryActivity.this, intent1);
			break;*/
		}
		return true;
	}
    /**
     * Dialog di ricerca testo
     * */
    private void createSearchDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.search);
        Window window = dialog.getWindow();
        //window.setLayout(mWidth/2, mHeight/2);
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        dialog.setTitle(getString(R.string.search));
        /**Search*/
        Button btnSearch = (Button) dialog.findViewById(R.id.search);
        final EditText txtSearch = (EditText) dialog.findViewById(R.id.txtSearch);
        txtSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER) {
                    DiariesSearchAsyncTask oSearch = new DiariesSearchAsyncTask(txtSearch.getText().toString());
                    oSearch.execute();
                    dialog.dismiss();
                }
                return false;
            }
        });

        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DiariesSearchAsyncTask oSearch = new DiariesSearchAsyncTask(txtSearch.getText().toString());
                oSearch.execute();
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    /**
     * Dialog di ricerca testo
     * *//*
    private void createLoginToCloudDialog() {
        mDialogLogin = new Dialog(this);
        mDialogLogin.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialogLogin.setContentView(R.layout.cloudactivity);
        Window window = mDialogLogin.getWindow();
        window.setLayout(mWidth/2, mHeight/2);
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        mDialogLogin.setTitle(getString(R.string.cloud));

        mLogin      = (Button) mDialogLogin.findViewById(R.id.cmbOk);
        mCancel     = (Button) mDialogLogin.findViewById(R.id.cmdCancel);
        mNewUser    = (CheckBox) mDialogLogin.findViewById(R.id.chkNewUser);
        mUserName   = (EditText) mDialogLogin.findViewById(R.id.txtUserName);
        mPassword   = (EditText) mDialogLogin.findViewById(R.id.txtPassword);

        if(isCloudEnebled){
            mNewUser.setChecked(false);
        }else{
            mNewUser.setChecked(true);
        }
        if(mPrefs!=null){
            mUserName.setText(mPrefs.getString("username",""));
            mPassword.setText(mPrefs.getString("password",""));
            if(mUserName.getText().toString().length()>0){
                //Cambio il nome del pulsante mLogin
                mLogin.setText(R.string.logout);
                mLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       //if(mSynchHelper!=null) mSynchHelper.userLogOut();
                       SharedPreferences.Editor mEditor = mPrefs.edit();
                       mEditor.putString("username", "");
                       mEditor.putString("password", "");
                       mEditor.putBoolean("isCloudEnabled", false);
                       mEditor.commit();
                       mDialogLogin.dismiss();
                    }
                });
            }else{
                mLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LoginToCloud mLoginAsych = new LoginToCloud();
                        mLoginAsych.execute();
                    }
                });
            }
        }


        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialogLogin.dismiss();
            }
        });



        mDialogLogin.show();
    }*/

    /**
	 * calcola il sample size
	 * */
	public int calculateInSampleSize() {
	    // Raw height and width of image
	    final int height = Const.IMGHEIGHT;
	    final int width = Const.IMGWIDTH;
	    int inSampleSize = 1;
	    int reqHeight = mWidth/Const.SAMPLESIZEDIARY;
	    int reqWidth = mHeight/Const.SAMPLESIZEDIARY;
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
	
	
	
	class DiariesAdapter extends BaseAdapter {
	    private Context mContext;
	    private ArrayList<Card> mDiariesPreview;
	    public DiariesAdapter(Context c, ArrayList<Card> diariesPreview) {
	        mContext = c;
	        mDiariesPreview=diariesPreview;
	    }

	    public int getCount() {
	        return mDiariesPreview.size();
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
	        return mDiariesPreview.get(position);
	    }

	}



    /**
     *
     * ASYNCH TASK
     *
     * */
    class DiariesAsyncTask extends AsyncTask<Void, Void, Boolean> {


        private DiaryActivity mDActivity;
        private ArrayList<Diary> mDiaries;
        private ProgressDialog oWaitForSave=null;
        private Bitmap mCover = null;

        private ArrayList<Card> mDiariesPreview = new ArrayList<Card>();

        public DiariesAsyncTask() {

        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Looper.getMainLooper().prepare();
                if (oWaitForSave != null) {
                    oWaitForSave.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            oWaitForSave.dismiss();
                            Log.v(this.getClass().getCanonicalName(), "Cancel Save");
                        }
                    });
                }
            }catch (RuntimeException e){
                Log.e(this.getClass().getCanonicalName(),"RUNTIME ERROR Skyp");
                e.printStackTrace();
            }
            Log.v(this.getClass().getCanonicalName(), "Loading diaries");

            //mCover = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.diary_preview);
            mDiaries = DiaryRepositoryHelper.getDiaries(mContext);

            mPrefs = getSharedPreferences(Const.CLOUD_COLLECTION, Context.MODE_PRIVATE);
            isCloudEnebled=mPrefs.getBoolean("isCloudEnabled",false);

            //Auto Login
            if(isCloudEnebled){
                //if(mSynchHelper==null) mSynchHelper = new SynchHelper(getApplicationContext());
                Log.d(this.getClass().getCanonicalName(),"Auto Login on Create");
                //mSynchHelper.userLogIn(mPrefs.getString("username",""),mPrefs.getString("password",""));
            }

            //TODO Gestione del Synch on Cloud
            //if(isCloudEnebled) mDiaries=mSynchHelper.getCloudDiaries(mDiaries);

            int mNumberOfDiaries=mDiaries.size();
            for(int i=0;i<mNumberOfDiaries;i++){

                //Log.v(this.getClass().getCanonicalName(),"Start Time :"+new Date());
                final Diary diary = mDiaries.get(i);

                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize=mCalculateInSampleSize;
                    mCover = BitmapFactory.decodeStream(mContext.getAssets().open("template/"+diary.getDiaryTemplate()+"/cover.png"),null,options);

                    /*LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    View rootView= inflater.inflate(R.layout.cardview,null);
                    */
                    final Card oCard = new Card(mContext);
                    oCard.init(diary);

                    oCard.getPreview().setImageBitmap(getFirstPage(diary));
                    oCard.getPreview().setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(oCard, 0,
                                    0, oCard.getWidth(), oCard.getHeight());
                            Intent newIntent = new Intent();
                            newIntent.putExtra("template", diary.getDiaryTemplate());
                            newIntent.putExtra("DiaryID", diary.getDiaryID());
                            newIntent.setClass(mContext, DrawerWritePageActivity.class);
                            DiaryActivity.this.startActivity(newIntent, options.toBundle());
                            DiaryActivity.this.finish();
                        }
                    });
                    oCard.getPreview().setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            //Cancellazione diario
                            deleteDiary(diary);
                            return false;
                        }
                    });
                    oCard.getMainLayout().setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(oCard, 0,
                                    0, oCard.getWidth(), oCard.getHeight());
                            Intent newIntent = new Intent();
                            newIntent.putExtra("template", diary.getDiaryTemplate());
                            newIntent.putExtra("DiaryID", diary.getDiaryID());
                            newIntent.setClass(mContext, DrawerWritePageActivity.class);
                            DiaryActivity.this.startActivity(newIntent, options.toBundle());
                            DiaryActivity.this.finish();
                        }
                    });
                    oCard.getMainLayout().setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            //Cancellazione diario
                            deleteDiary(diary);
                            return false;
                        }
                    });

                    mDiariesPreview.add(oCard);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.getClass().getCanonicalName(),"Error load diary page");
                }


                //Log.v(this.getClass().getCanonicalName(),"End Time :"+new Date());
            }
            //Mostro la nuova Finestra di Rate ogni 7 avvii
            Rate.app_launched(DiaryActivity.this);
            return true;
        }
        /**
         * Metodo per la cancellazione del diario
         *
         * */
        private void deleteDiary(final Diary diary) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    DiaryActivity.this);

            // set title
            alertDialogBuilder.setTitle(mContext.getString(R.string.dAttention));

            // set dialog message
            alertDialogBuilder
                    .setMessage(mContext.getString(R.string.deleteDiary))
                    .setCancelable(false)
                    .setPositiveButton(mContext.getString(R.string.yes),new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int id) {
                            DiaryRepositoryHelper.deleteDiary(mContext,diary);
                            DiariesAsyncTask oDiariesAsync = new DiariesAsyncTask();
                            oDiariesAsync.execute();
                        }
                    })
                    .setNegativeButton(mContext.getString(R.string.no),new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.dismiss();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }


        /***
         * RITORNA LA PRIMA PAGINA DEL DIARIO COME ANTEPRIMA
         *
         * */
        private Bitmap getFirstPageWithCover(Diary currentDiary) {
            String sDiaryPreviewImage;
            Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) currentDiary.getDiaryPages();
            if(mPages==null) return null;
            Map<Long, Page> sortedPages = new TreeMap<Long, Page>(mPages);
            long mLastPageID =0;
            for(Page oPpage : sortedPages.values()){
                mLastPageID = oPpage.getPageID();
            }
            sortedPages=null;
            Bitmap cs = Bitmap.createBitmap(mCover.getWidth(), mCover.getHeight(), Bitmap.Config.ARGB_8888);

            sDiaryPreviewImage= Environment.getExternalStorageDirectory().getPath() + "/"+mContext.getPackageName()+"/"+currentDiary.getDiaryID() + "/Pictures/"+mLastPageID+Const.CAMERA_PREVIEW_EXT;

            Bitmap page = BitmapFactory.decodeFile(sDiaryPreviewImage);
            Bitmap preview = null; //BitmapFactoryHelper.decodeSampledBitmapFromFile(sDiaryPreviewImage,7);
            if(page!=null){
                preview = Bitmap.createScaledBitmap(page, (int) (mCover.getWidth()/1.4), (int) (mCover.getHeight()/1.2), true);
            }

            Canvas comboImage = new Canvas(cs);

            comboImage.drawBitmap(mCover, 0f, 0f, null);
            if(preview!=null) comboImage.drawBitmap(preview, (mCover.getWidth()-preview.getWidth())/2, (mCover.getHeight()-preview.getHeight())/2, null);
            return cs;
        }

        /***
         * RITORNA LA PRIMA PAGINA DEL DIARIO COME ANTEPRIMA
         *
         * */
        private Bitmap getFirstPage(Diary currentDiary) {
            String sDiaryPreviewImage;
            Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) currentDiary.getDiaryPages();
            if(mPages==null) return null;
            Map<Long, Page> sortedPages = new TreeMap<Long, Page>(mPages);
            long mLastPageID =0;
            for(Page oPpage : sortedPages.values()){
                mLastPageID = oPpage.getPageID();
            }

            sDiaryPreviewImage= Environment.getExternalStorageDirectory().getPath() + "/"+mContext.getPackageName()+"/"+currentDiary.getDiaryID() + "/Pictures/"+mLastPageID+Const.CAMERA_PREVIEW_EXT;

            Bitmap page = BitmapFactory.decodeFile(sDiaryPreviewImage);

            Bitmap preview = null; //BitmapFactoryHelper.decodeSampledBitmapFromFile(sDiaryPreviewImage,7);
            if(page!=null){
                if(mWidth<mHeight)
                    //Verticale
                    preview = Bitmap.createScaledBitmap(page, (int) (mWidth/2), (int) (mHeight/3), true);
                else
                    //Orizzontale
                    preview = Bitmap.createScaledBitmap(page, (int) (mHeight/2), (int) (mWidth/3), true);
            }
            //Log.d(this.getClass().getCanonicalName(),"mWidth: "+(mWidth)+" (mHeight/3): "+(mHeight));
            return preview;
        }


        @Override
        protected void onPostExecute(Boolean result) {

            addDiary(mDiariesPreview);
            try{
                if(oWaitForSave!=null) oWaitForSave.dismiss();
            }catch (IllegalArgumentException e){
                Log.e(this.getClass().getCanonicalName(),"error dismiss wait");
            }

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            try {
                if(DiaryActivity.this!=null)
                    oWaitForSave = ProgressDialog.show(DiaryActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);
            }catch (Exception e){
                Log.e(this.getClass().getCanonicalName(),"Error on startUP");
            }
            super.onPreExecute();
        }

    }

    class DiariesSearchAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<Card> mDiariesSearch = new ArrayList<Card>();
        private ProgressDialog oWaitForSave=null;

        private String mSearch;
        private ArrayList<Page> mPages;

        public DiariesSearchAsyncTask(String searchTxt){
            mSearch=searchTxt;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            mPages=DiaryRepositoryHelper.searchText(mContext, mSearch);
            int mNumberOfDiaries=mPages.size();
            for(int i=0;i<mNumberOfDiaries;i++){

                //Log.v(this.getClass().getCanonicalName(),"Start Time :"+new Date());
                final Page page = mPages.get(i);

                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize=mCalculateInSampleSize;

                    String sDiaryPreviewImage = Environment.getExternalStorageDirectory().getPath() + "/"+mContext.getPackageName()+"/"+page.getDiaryID() + "/Pictures/"+page.getPageID()+Const.CAMERA_PREVIEW_EXT;

                    Bitmap pageBmp = BitmapFactory.decodeFile(sDiaryPreviewImage);

                    //Immagine Corrente per il diario
                    ImageView imageView;// = (ImageView) mPagePreview.findViewById(R.id.imgPreview);
                    imageView = new ImageView(mContext);
                    Card oCard = new Card(mContext);
                    //oCard.setCardBackgroundColor(Color.TRANSPARENT);

                    //oCard.init(diary);

                    oCard.getPreview().setImageBitmap(pageBmp);
                    imageView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent newIntent = new Intent();
                            newIntent.putExtra("CurrentPage", page.getPageID());
                            newIntent.putExtra("DiaryID", page.getDiaryID());
                            newIntent.setClass(mContext, DrawerWritePageActivity.class);
                            DiaryActivity.this.startActivity(newIntent);
                            DiaryActivity.this.finish();
                        }
                    });

                    //oCard.addView(imageView);
                    mDiariesSearch.add(oCard);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.getClass().getCanonicalName(),"Error load diary page");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            addDiary(mDiariesSearch);
            try{
                if(oWaitForSave!=null) oWaitForSave.dismiss();
            }catch (IllegalArgumentException e){
                Log.e(this.getClass().getCanonicalName(),"error dismiss wait");
            }

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            if(DiaryActivity.this!=null)
                oWaitForSave = ProgressDialog.show(DiaryActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);
            super.onPreExecute();
        }
    }

    /*//Task per Login
    private class LoginToCloud extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog oWaitForPage;
        private boolean isLoginorSignIn=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            oWaitForPage = ProgressDialog.show(DiaryActivity.this,getString(R.string.app_name),getString(R.string.wait),true,true,null);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(oWaitForPage!=null) oWaitForPage.dismiss();
            if(isLoginorSignIn) mDialogLogin.dismiss();
            else Toast.makeText(mContext, "Error Login on server", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(mSynchHelper==null)  mSynchHelper = new SynchHelper(getApplicationContext());
            if(mNewUser.isChecked()){
                isLoginorSignIn=mSynchHelper.userSignUp(mUserName.getText().toString(),
                        mPassword.getText().toString());
            }else{
                isLoginorSignIn=mSynchHelper.userLogIn(mUserName.getText().toString(),
                        mPassword.getText().toString());
            }

            return isLoginorSignIn;
        }
    }*/
}
