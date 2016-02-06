package com.glm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class AnimationPageLayout extends LinearLayout {

    public final static int DURATION = 500;

    protected boolean mPlaceLeft = true;
    protected boolean mOpened;
    protected View mSidebar;
    protected View mContent;
    protected int mSidebarWidth = 150; /* assign default value. It will be overwrite
                                          in onMeasure by Layout xml resource. */

    protected Animation mAnimation;
    protected OpenListener    mOpenListener;
    protected CloseListener   mCloseListener;
    protected Listener mListener;

    protected boolean mPressed = false;
    protected Context mContext;
    public AnimationPageLayout(Context context) {
        this(context, null);
        mContext=context;
    }

    public AnimationPageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
       /* mSidebar = findViewById(R.id.animation_layout_page);
        mContent = findViewById(R.id.animation_layout_content);

        if (mSidebar == null) {
            throw new NullPointerException("no view id = animation_sidebar");
        }

        if (mContent == null) {
            throw new NullPointerException("no view id = animation_content");
        }*/

        mOpenListener = new OpenListener(mSidebar, mContent);
        mCloseListener = new CloseListener(mSidebar, mContent);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
    	super.onLayout(changed, l, t, r, b);
    	/* the title bar assign top padding, drop it */
        int sidebarLeft = l;
        if (!mPlaceLeft) {
            sidebarLeft = r - mSidebarWidth;
        }
        //Log.v(this.getClass().getCanonicalName(), "onLayout sidebarLeft:"+sidebarLeft+" - mSidebarWidth:"+mSidebarWidth);
        //Log.v(this.getClass().getCanonicalName(), "onLayout l:"+l+" - r:"+r+" - b:"+b);
        if(mSidebar!=null){
	        mSidebar.layout(sidebarLeft,
	                0,
	                sidebarLeft + mSidebarWidth,
                    mSidebar.getMeasuredHeight());
	
	        if (mOpened) {
	            if (mPlaceLeft) {
	                mContent.layout(l + mSidebarWidth, 0, r + mSidebarWidth, b);
	            } else  {
	                mContent.layout(l - mSidebarWidth, 0, r - mSidebarWidth, b);
	            }
	        } else {
	            mContent.layout(l, 0, r+300, b);
	        }
        } 
    }

    @Override
    public void onMeasure(int w, int h) {
        super.onMeasure(w, h);
        super.measureChildren(w, h);
        if(mSidebar!=null) mSidebarWidth = mSidebar.getMeasuredWidth();
    }

    @Override
    protected void measureChild(View child, int parentWSpec, int parentHSpec) {
        /* the max width of Sidebar is 90% of Parent */
        if (child == mSidebar) {
            int mode = MeasureSpec.getMode(parentWSpec);
            int width = (int)(getMeasuredWidth() * 0.9);
            super.measureChild(child, MeasureSpec.makeMeasureSpec(width, mode), parentHSpec);
        } else {
            super.measureChild(child, parentWSpec, parentHSpec);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isOpening()) {
            return false;
        }

        int action = ev.getAction();

        if (action != MotionEvent.ACTION_UP
                && action != MotionEvent.ACTION_DOWN) {
            return false;
        }

        /* if user press and release both on Content while
         * sidebar is opening, call listener. otherwise, pass
         * the event to child. */
        int x = (int)ev.getX();
        int y = (int)ev.getY();
        if (mContent.getLeft() < x
                && mContent.getRight() > x
                && mContent.getTop() < y
                && mContent.getBottom() > y) {
            if (action == MotionEvent.ACTION_DOWN) {
                mPressed = true;
            }

            if (mPressed
                    && action == MotionEvent.ACTION_UP
                    && mListener != null) {
                mPressed = false;
                return mListener.onContentTouchedWhenOpening();
            }
        } else {
            mPressed = false;
        }

        return false;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    /* to see if the Sidebar is visible */
    public boolean isOpening() {
        return mOpened;
    }

    public void toggleSidebar() {
        if (mContent.getAnimation() != null) {
            return;
        }

        if (mOpened) {
            /* opened, make close animation*/
            if (mPlaceLeft) {
                mAnimation = new TranslateAnimation(0, -mSidebarWidth, 0, 0);
            } else {
                mAnimation = new TranslateAnimation(0, mSidebarWidth, 0, 0);
            }
            mAnimation.setAnimationListener(mCloseListener);
        } else {
        	//if(activity!=null) activity.reloadPagePreview();
            /* not opened, make open animation */
            if (mPlaceLeft) {
                mAnimation = new TranslateAnimation(0, mSidebarWidth, 0, 0);
            } else {
                mAnimation = new TranslateAnimation(0, -mSidebarWidth, 0, 0);
            }
            mAnimation.setAnimationListener(mOpenListener);
        }
        mAnimation.setDuration(DURATION);
        mAnimation.setFillAfter(true);
        mAnimation.setFillEnabled(true);
        mContent.startAnimation(mAnimation);
    }

    public void openSidebar() {
        if (!mOpened) {
            toggleSidebar();
        }
    }

    public void closeSidebar() {
        if (mOpened) {
            toggleSidebar();
        }
    }

    class OpenListener implements Animation.AnimationListener {
        View iSidebar;
        View iContent;

        OpenListener(View sidebar, View content) {
            iSidebar = sidebar;
            iContent = content;
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
            iSidebar.setVisibility(View.VISIBLE);
        }

        public void onAnimationEnd(Animation animation) {
            iContent.clearAnimation();
            mOpened = !mOpened;
            requestLayout();
            if (mListener != null) {
                mListener.onSidebarOpened();
            }
        }
    }

    class CloseListener implements Animation.AnimationListener {
        View iSidebar;
        View iContent;

        CloseListener(View sidebar, View content) {
            iSidebar = sidebar;
            iContent = content;
        }

        public void onAnimationRepeat(Animation animation) {
        }
        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            iContent.clearAnimation();
            iSidebar.setVisibility(View.INVISIBLE);
            mOpened = !mOpened;
            requestLayout();
            if (mListener != null) {
                mListener.onSidebarClosed();
            }
        }
    }

    public interface Listener {
        public void onSidebarOpened();
        public void onSidebarClosed();
        public boolean onContentTouchedWhenOpening();
    }

	public void setSideBarAndContent(RelativeLayout currentPage,
			LinearLayout sideBarLayout) {
		mSidebar = sideBarLayout;
        mContent = currentPage;
        mOpenListener = new OpenListener(mSidebar, mContent);
        mCloseListener = new CloseListener(mSidebar, mContent);
	}
}
