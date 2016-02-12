package com.glm.view;

import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
/**@deprecated */
public class TestSurf extends SurfaceView implements SurfaceHolder.Callback{
	public TestSurf(Context context) {
		super(context);
		getHolder().addCallback(this);
	}

	/**TAG*/
	private final String TAG=this.getClass().getCanonicalName();
	
	private void tryDrawing(SurfaceHolder holder) {
        Log.i(TAG, "Trying to draw...");

        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null");
        } else {
            drawMyStuff(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawMyStuff(final Canvas canvas) {
        Random random = new Random();
        Log.i(TAG, "Drawing...");
        canvas.drawRGB(255, 128, 128);
    }
	@SuppressLint("WrongCall")
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		tryDrawing(holder);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        //_thread.setRunning(true);
        //_thread.start();
		tryDrawing(holder);
        Log.v(this.getClass().getCanonicalName(),"Surface Created");
        postInvalidate();
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

}
