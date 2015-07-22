package com.glm.ememories;

import java.io.IOException;

import com.viewpagerindicator.CirclePageIndicator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class TemplateActivity extends ActionBarActivity {
    private Bitmap mPreview=null;
	private LinearLayout oMainLayout;
	private ViewPager pager;
    private ImageView oImgBG=null;
    public static TemplateOnPageChangeListener mPageChange;
	//@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_template);
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		
		
		LayoutParams layoutParams = new FrameLayout.LayoutParams(width,height);
		oMainLayout = (LinearLayout) findViewById(R.id.main);
		oMainLayout.setLayoutParams(layoutParams);
		//oImgBG = (ImageView) findViewById(R.id.imgMainBg);

		pager = (ViewPager)findViewById(R.id.pager);
		//pager = new ViewPager(this.getApplicationContext());
		pager.setAdapter(new mTemplateAdapter(this,getApplicationContext(),width,height));
		
		mPageChange= new TemplateOnPageChangeListener();
		
		CirclePageIndicator titleIndicator = (CirclePageIndicator)findViewById(R.id.titles);
		//CirclePageIndicator titleIndicator = new CirclePageIndicator(getApplicationContext());
		titleIndicator.setViewPager(pager);
		titleIndicator.setOnPageChangeListener(mPageChange);
		//pager.setOnPageChangeListener(new DetailOnPageChangeListener());
		/*viewFlow 	  = (ViewFlow) findViewById(R.id.viewflow);
	    indic 		  = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
	    viewFlow.setLayoutParams(layoutParams);
	    viewFlow.setAdapter(new mTemplateAdapter(TemplateActivity.this, getApplicationContext(),width,height-100));
	    viewFlow.setFlowIndicator(indic);	*/   
		//oMainLayout.addView(pager);
		//oMainLayout.addView(titleIndicator);
	}


	/**
	 * This method is called when the back button is pressed
	 * @author coluzza
	 */
	@Override
	public void onBackPressed() {
		Intent newIntent = new Intent();
		newIntent.setClass(this, DiaryActivity.class);
		this.startActivity(newIntent);
		this.finish();
	}

	/**
	 * This method is used to update the miniView every time the activity is resumed
	 */
	@Override
	public void onRestart() {
		super.onRestart();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//viewFlow.onConfigurationChanged(newConfig);
	}

    @Override
    protected void onPause() {
        super.onPause();
        try{
            if(mPreview!=null) mPreview.recycle();
            if(oImgBG!=null) oImgBG.getBackground().setCallback(null);
        }catch (NullPointerException e){
            Log.e(this.getClass().getCanonicalName(),"null point onSave");
        }

        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            if(mPreview!=null) mPreview.recycle();
            if(oImgBG!=null) oImgBG.getBackground().setCallback(null);
        }catch (NullPointerException e){
            Log.e(this.getClass().getCanonicalName(),"null point onDestroy");
        }
        System.gc();
    }

    /**
	 * Get the current view position from the ViewPager.
	 */
	public class TemplateOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

	    private int currentPage;

	    
	    @Override
	    public void onPageSelected(int position) {
	    	Log.v(this.getClass().getCanonicalName(),"DetailOnPageChangeListener: "+position);
	        currentPage = position;
	    }

	    public int getCurrentPage() {
	        return currentPage;
	    }
	}
	
	class mTemplateAdapter extends PagerAdapter{

		private Context mContext;
		private Activity mActivity;
		private int width = 0;
		private int height = 0;
		protected int nTemplate;
		
		public mTemplateAdapter(Activity activity, Context context,int w, int h){
			mContext=context;
			mActivity=activity;
			width=w;
			height=h;
		}
		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return super.getItemPosition(object);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			//Log.v(this.getClass().getCanonicalName(),"position: "+position);
			RelativeLayout oLinear = new RelativeLayout(mContext);
			
			ImageView mImage = new ImageView(mContext);
			
			mImage.setBackgroundColor(Color.TRANSPARENT);
			mImage.setImageBitmap(applyTemplate(position));
			
			mImage.setTop(50);
			mImage.setScaleType(ScaleType.FIT_XY);
			//mImage.setBackgroundColor(Color.RED);
			mImage.setLayoutParams(new LinearLayout.LayoutParams(width-30,height-130));
			mImage.setId(position+1);
			mImage.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View Template) {
					Log.v(this.getClass().getCanonicalName(),"Page Click Number:" +Template.getId());
                    Intent newIntent = new Intent();
                    newIntent.putExtra("template", Template.getId());
                    newIntent.putExtra("DiaryID", -1l);
                    newIntent.setClass(mContext, DrawerWritePageActivity.class);
                    TemplateActivity.this.startActivity(newIntent);
                    TemplateActivity.this.finish();
				}
			});
			oLinear.setY(50f);
			oLinear.addView(mImage);
			container.addView(oLinear,0);
			return oLinear;
		}
		/**
		 * Applica il template selezionato
		 * 
		 * 
		 * */
		private Bitmap applyTemplate(int template) {
			try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=Const.WORKIZEIMAGE;
				//if(mPreview!=null) mPreview.recycle();
				mPreview=BitmapFactory.decodeStream(mContext.getAssets().open("template/"+(template+1)+"/cover.png"),null,options);
				mPreview=Bitmap.createScaledBitmap(mPreview, width/2, height/2, true);
				return mPreview;
			} catch (IOException e) {
				Log.e(this.getClass().getCanonicalName(),"Error Apply template");
			}
			return null;
		}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 6;
		}
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return(view == object);
		}
		
	}
}


