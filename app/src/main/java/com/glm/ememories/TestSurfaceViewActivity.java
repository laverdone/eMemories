package com.glm.ememories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.glm.bean.Diary;
import com.glm.bean.Page;
import com.glm.utilities.DiaryHelper;
import com.glm.view.TextureHandWrite;

import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

@SuppressLint("NewApi")
public class TestSurfaceViewActivity extends Activity {

	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.write_diary);

        RelativeLayout oPageLayout = (RelativeLayout) findViewById(R.id.pageLayout);
        Diary mDiary= DiaryHelper.initNewDiary(0);
        Page mCurrentPage=null;
        /**Goto Last Page*/
        Hashtable<Long, Page> mPages = (Hashtable<Long, Page>) mDiary.getDiaryPages();
        Map<Long, Page> sortedPages = new TreeMap<Long, Page>(mPages);

        for(Page oPpage : sortedPages.values()){
            mCurrentPage = oPpage;
        }

        TextureHandWrite oTexture = new TextureHandWrite(getApplicationContext(),mDiary,mCurrentPage);

        oPageLayout.addView(oTexture);

	}
}
