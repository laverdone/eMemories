package com.glm.view;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.glm.bean.Diary;
import com.glm.ememories.R;


import java.text.SimpleDateFormat;

/**
 * Created by gianluca on 15/04/15.
 */
public class Card extends LinearLayout {
    private View mView;
    private Context mContext;
    private ImageView mPreview;
    private LinearLayout mMainLayout;
    public Card(Context context) {
        super(context);
        mView=inflate(getContext(), R.layout.cardview, this);
        mMainLayout= (LinearLayout) mView.findViewById(R.id.diaryLL);
        //Immagine Corrente per il diario
        mPreview = (ImageView) mView.findViewById(R.id.previewDiary);
        mPreview.setScaleType(ImageView.ScaleType.MATRIX);
        mContext=context;
    }

    public void init(Diary diary){

        //final CardView oCard = (CardView) mView.findViewById(R.id.card_view); // new CardView(mContext);//
        //oCard.setCardElevation(10f);
        //oCard.setTranslationZ(1f);
        //AbsListView.LayoutParams vp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //oCard.setLayoutParams(vp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "template/"+diary.getDiaryTemplate()+"/font.ttf");
        TextView oTitle = (TextView) mView.findViewById(R.id.title);
        if(diary.getDiaryName()!=null){
            oTitle.setText("> "+diary.getDiaryName()+" "+sdf.format(diary.getDiaryDTCreation()));
            oTitle.setTypeface(tf);
        }else{
            oTitle.setText("> "+mContext.getString(R.string.app_name)+" "+sdf.format(diary.getDiaryDTCreation()));
            oTitle.setTypeface(tf);
        }
    }

    public ImageView getPreview() {
        return mPreview;
    }

    public LinearLayout getMainLayout() {
        return mMainLayout;
    }

}
