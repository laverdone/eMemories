package com.glm.view;

import android.content.Context;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.glm.ememories.R;

/**
 * Created by gianluca on 14/04/15.
 */
public class FloatingButton extends RelativeLayout {
    private View mView;
    public FloatingButton(Context context) {
        super(context);
        mView=inflate(getContext(), R.layout.fb_button, this);
    }
    /**
     * Return the button layout inflated
     * */
    public ImageView getButton() {
        return (ImageView)mView.findViewById(R.id.fab_3);


    }
}
