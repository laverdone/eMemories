package com.glm.labs.diary.ememories.fragment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.glm.bean.Diary;
import com.glm.bean.Page;
import com.glm.db.DiaryRepositoryHelper;
import com.glm.labs.diary.ememories.Const;
import com.glm.ememories.R;
import com.glm.labs.diary.ememories.WriteActivity;
import com.glm.utilities.Rate;
import com.glm.view.Card;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DiariesFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private ListView mGridview;
    private Activity mContext;
    private int mWidth;
    private int mHeight;
    private int mCalculateInSampleSize = 0;
    private SharedPreferences mPrefs;
    private boolean isCloudEnebled = false;

    /**
     * Ricerca
     */
    private DiariesAdapter mDiariesAdapter = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DiariesFragment() {


    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DiariesFragment newInstance(int columnCount) {
        DiariesFragment fragment = new DiariesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=getActivity();
        mCalculateInSampleSize = calculateInSampleSize();
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (size != null) {
            mWidth = size.x;
            mHeight = size.y;
        }

        View view = inflater.inflate(R.layout.diaries, container, false);

        mGridview = (ListView) view.findViewById(R.id.diaries);

        if(getActivity().getPackageName().equals(Const.ADS_APP_PACKAGE_NAME)){
            AdView mAdView = (AdView) view.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
//            mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
//            mAdView.setAdSize(AdSize.SMART_BANNER);
            mAdView.loadAd(adRequest);
        }else{
            AdView mAdView = (AdView) view.findViewById(R.id.adView);
            mAdView.setVisibility(View.GONE);
        }

        DiariesAsyncTask oDiariesAsync = new DiariesAsyncTask();
        oDiariesAsync.execute();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void search(String search) {
        DiariesSearchAsyncTask oSearch = new DiariesSearchAsyncTask(search);
        oSearch.execute();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    /**
     * Aggiunde un bottone per andare al diario presente in DB
     * <p/>
     * TODO Aggiungere un task asincrono
     */
    public void addDiary(ArrayList<Card> diariesPreview) {
        //mDiaries=diaries;
        if (diariesPreview.size() > 0) {
            mDiariesAdapter = new DiariesAdapter(mContext, diariesPreview);
            //Aggiorno la gridView

            mDiariesAdapter.notifyDataSetChanged();
            mDiariesAdapter.notifyDataSetInvalidated();
            mGridview.setAdapter(mDiariesAdapter);
        }
    }

    public int calculateInSampleSize() {
        // Raw height and width of image
        final int height = Const.IMGHEIGHT;
        final int width = Const.IMGWIDTH;
        int inSampleSize = 1;
        int reqHeight = mWidth / Const.SAMPLESIZEDIARY;
        int reqWidth = mHeight / Const.SAMPLESIZEDIARY;
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
            mDiariesPreview = diariesPreview;
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
     * ASYNCH TASK
     */
    class DiariesAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<Diary> mDiaries;
        private ProgressDialog oWaitForSave = null;
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
            } catch (RuntimeException e) {
                Log.e(getClass().getCanonicalName(), "RUNTIME ERROR Skyp");
                e.printStackTrace();
            }
            Log.v(getClass().getCanonicalName(), "Loading diaries");

            //mCover = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.diary_preview);
            mDiaries = DiaryRepositoryHelper.getDiaries(mContext.getApplicationContext());

            mPrefs = mContext.getApplicationContext().getSharedPreferences(Const.CLOUD_COLLECTION, Context.MODE_PRIVATE);
            isCloudEnebled = mPrefs.getBoolean("isCloudEnabled", false);

            //Auto Login
            if (isCloudEnebled) {
                //if(mSynchHelper==null) mSynchHelper = new SynchHelper(getApplicationContext());
                Log.d(this.getClass().getCanonicalName(), "Auto Login on Create");
                //mSynchHelper.userLogIn(mPrefs.getString("username",""),mPrefs.getString("password",""));
            }

            //TODO Gestione del Synch on Cloud
            //if(isCloudEnebled) mDiaries=mSynchHelper.getCloudDiaries(mDiaries);

            int mNumberOfDiaries = mDiaries.size();
            for (int i = 0; i < mNumberOfDiaries; i++) {

                //Log.v(this.getClass().getCanonicalName(),"Start Time :"+new Date());
                final Diary diary = mDiaries.get(i);

                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = mCalculateInSampleSize;
                    mCover = BitmapFactory.decodeStream(mContext.getAssets().open("template/" + diary.getDiaryTemplate() + "/cover.png"), null, options);

                    /*LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    View rootView= inflater.inflate(R.layout.cardview,null);
                    */
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Card oCard = new Card(mContext);
                            oCard.init(diary);

                            oCard.getPreview().setImageBitmap(getFirstPage(diary));
                            oCard.getPreview().setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    ActivityOptions options = ActivityOptions.makeScaleUpAnimation(oCard, 0,
                                            0, oCard.getWidth(), oCard.getHeight());
                                    Intent newIntent = new Intent();
                                    newIntent.putExtra("template", diary.getDiaryTemplate());
                                    newIntent.putExtra("DiaryID", diary.getDiaryID());
                                    newIntent.setClass(mContext, WriteActivity.class);
                                    mContext.startActivity(newIntent, options.toBundle());
                                    //mContext.finish();
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
                            oCard.getMainLayout().setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    ActivityOptions options = null;
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                        options = ActivityOptions.makeScaleUpAnimation(oCard, 0,
                                                0, oCard.getWidth(), oCard.getHeight());
                                    }
                                    Intent newIntent = new Intent();
                                    newIntent.putExtra("template", diary.getDiaryTemplate());
                                    newIntent.putExtra("DiaryID", diary.getDiaryID());
                                    newIntent.setClass(mContext, WriteActivity.class);
                                    mContext.startActivity(newIntent, options.toBundle());
                                    //mContext.finish();
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
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.getClass().getCanonicalName(), "Error load diary page");
                }


                //Log.v(this.getClass().getCanonicalName(),"End Time :"+new Date());
            }
            //Mostro la nuova Finestra di Rate ogni 7 avvii
            Rate.app_launched(mContext);
            return true;
        }

        /**
         * Metodo per la cancellazione del diario
         */
        private void deleteDiary(final Diary diary) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    mContext);

            // set title
            alertDialogBuilder.setTitle(mContext.getString(R.string.dAttention));

            // set dialog message
            alertDialogBuilder
                    .setMessage(mContext.getString(R.string.deleteDiary))
                    .setCancelable(false)
                    .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            DiaryRepositoryHelper.deleteDiary(mContext, diary);
                            DiariesAsyncTask oDiariesAsync = new DiariesAsyncTask();
                            oDiariesAsync.execute();
                        }
                    })
                    .setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
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
         */
        private Bitmap getFirstPageWithCover(Diary currentDiary) {
            String sDiaryPreviewImage;
            Hashtable<Long, Page> mPages = currentDiary.getDiaryPages();
            if (mPages == null) return null;
            Map<Long, Page> sortedPages = new TreeMap<Long, Page>(mPages);
            long mLastPageID = 0;
            for (Page oPpage : sortedPages.values()) {
                mLastPageID = oPpage.getPageID();
            }
            sortedPages = null;
            Bitmap cs = Bitmap.createBitmap(mCover.getWidth(), mCover.getHeight(), Bitmap.Config.ARGB_8888);

            sDiaryPreviewImage = Environment.getExternalStorageDirectory().getPath() + "/" + mContext.getPackageName() + "/" + currentDiary.getDiaryID() + "/Pictures/" + mLastPageID + Const.CAMERA_PREVIEW_EXT;

            Bitmap page = BitmapFactory.decodeFile(sDiaryPreviewImage);
            Bitmap preview = null; //BitmapFactoryHelper.decodeSampledBitmapFromFile(sDiaryPreviewImage,7);
            if (page != null) {
                preview = Bitmap.createScaledBitmap(page, (int) (mCover.getWidth() / 1.4), (int) (mCover.getHeight() / 1.2), true);
            }

            Canvas comboImage = new Canvas(cs);

            comboImage.drawBitmap(mCover, 0f, 0f, null);
            if (preview != null)
                comboImage.drawBitmap(preview, (mCover.getWidth() - preview.getWidth()) / 2, (mCover.getHeight() - preview.getHeight()) / 2, null);
            return cs;
        }

        /***
         * RITORNA LA PRIMA PAGINA DEL DIARIO COME ANTEPRIMA
         */
        private Bitmap getFirstPage(Diary currentDiary) {
            String sDiaryPreviewImage;
            Hashtable<Long, Page> mPages = currentDiary.getDiaryPages();
            if (mPages == null) return null;
            Map<Long, Page> sortedPages = new TreeMap<Long, Page>(mPages);
            long mLastPageID = 0;
            for (Page oPpage : sortedPages.values()) {
                mLastPageID = oPpage.getPageID();
            }

            sDiaryPreviewImage = Environment.getExternalStorageDirectory().getPath() + "/" + mContext.getPackageName() + "/" + currentDiary.getDiaryID() + "/Pictures/" + mLastPageID + Const.CAMERA_PREVIEW_EXT;

            Bitmap page = BitmapFactory.decodeFile(sDiaryPreviewImage);

            Bitmap preview = null; //BitmapFactoryHelper.decodeSampledBitmapFromFile(sDiaryPreviewImage,7);
            if (page != null) {
                if (mWidth < mHeight)
                    //Verticale
                    preview = Bitmap.createScaledBitmap(page, mWidth / 2, mHeight / 3, true);
                else
                    //Orizzontale
                    preview = Bitmap.createScaledBitmap(page, mHeight / 2, mWidth / 3, true);
            }
            //Log.d(this.getClass().getCanonicalName(),"mWidth: "+(mWidth)+" (mHeight/3): "+(mHeight));
            return preview;
        }


        @Override
        protected void onPostExecute(Boolean result) {

            addDiary(mDiariesPreview);
            try {
                if (oWaitForSave != null) oWaitForSave.dismiss();
            } catch (IllegalArgumentException e) {
                Log.e(this.getClass().getCanonicalName(), "error dismiss wait");
            }

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            try {
                if (mContext != null)
                    oWaitForSave = ProgressDialog.show(mContext, getString(R.string.app_name), getString(R.string.wait), true, true, null);
            } catch (Exception e) {
                Log.e(this.getClass().getCanonicalName(), "Error on startUP");
            }
            super.onPreExecute();
        }

    }


    class DiariesSearchAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<Card> mDiariesSearch = new ArrayList<Card>();
        private ProgressDialog oWaitForSave = null;

        private String mSearch;
        private ArrayList<Page> mPages;

        public DiariesSearchAsyncTask(String searchTxt) {
            mSearch = searchTxt;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            mPages = DiaryRepositoryHelper.searchText(mContext, mSearch);
            int mNumberOfDiaries = mPages.size();
            for (int i = 0; i < mNumberOfDiaries; i++) {

                //Log.v(this.getClass().getCanonicalName(),"Start Time :"+new Date());
                final Page page = mPages.get(i);

                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = mCalculateInSampleSize;

                    String sDiaryPreviewImage = Environment.getExternalStorageDirectory().getPath() + "/" + mContext.getPackageName() + "/" + page.getDiaryID() + "/Pictures/" + page.getPageID() + Const.CAMERA_PREVIEW_EXT;

                    final Bitmap pageBmp = BitmapFactory.decodeFile(sDiaryPreviewImage);

                    //Immagine Corrente per il diario

                    //oCard.setCardBackgroundColor(Color.TRANSPARENT);

                    //oCard.init(diary);

                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Card oCard = new Card(mContext);
                            oCard.getPreview().setImageBitmap(pageBmp);
                            oCard.getMainLayout().setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Intent newIntent = new Intent();
                                    newIntent.putExtra("CurrentPage", page.getPageID());
                                    newIntent.putExtra("DiaryID", page.getDiaryID());
                                    newIntent.setClass(mContext, WriteActivity.class);
                                    mContext.startActivity(newIntent);
                                    //mContext.finish();
                                }
                            });
                            oCard.getPreview().setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Intent newIntent = new Intent();
                                    newIntent.putExtra("CurrentPage", page.getPageID());
                                    newIntent.putExtra("DiaryID", page.getDiaryID());
                                    newIntent.setClass(mContext, WriteActivity.class);
                                    mContext.startActivity(newIntent);
                                    //mContext.finish();
                                }
                            });
                            mDiariesSearch.add(oCard);
                        }
                    });



                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.getClass().getCanonicalName(), "Error load diary page");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            addDiary(mDiariesSearch);
            try {
                if (oWaitForSave != null) oWaitForSave.dismiss();
            } catch (IllegalArgumentException e) {
                Log.e(this.getClass().getCanonicalName(), "error dismiss wait");
            }

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            if (mContext != null)
                oWaitForSave = ProgressDialog.show(mContext, getString(R.string.app_name), getString(R.string.wait), true, true, null);
            super.onPreExecute();
        }
    }
}
