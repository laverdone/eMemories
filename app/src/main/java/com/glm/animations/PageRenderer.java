package com.glm.animations;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.glm.bean.Page;

import android.content.Context;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;

public class PageRenderer implements Renderer {
	
	public PageCurl mPageCurl;	
	private Context context;
	private Page mPage;
	private String mPathImage;
	private int mWidth;
	private int mHeight;
	public PageRenderer(Context context, String pathImage,Page page, int width, int height) {
		this.context = context;
		mPage=page;
		mPathImage=pathImage;
		mWidth=width;
		mHeight=height;
		mPageCurl = new PageCurl();
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {		
		mPageCurl.loadGLTexture(gl, this.context, mPathImage, mPage);
		
		gl.glEnable(GL10.GL_TEXTURE_2D);		
		gl.glShadeModel(GL10.GL_SMOOTH); 			
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	
		gl.glClearDepthf(1.0f); 					
		gl.glEnable(GL10.GL_DEPTH_TEST); 		
		gl.glDepthFunc(GL10.GL_LEQUAL); 	
		
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
	}

	
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	
		gl.glLoadIdentity();					
		
		gl.glTranslatef(0.0f, 0.0f, -2.0f);
		gl.glTranslatef(-0.5f, -0.5f, 0.0f);
		
		mPageCurl.draw(gl);							
		
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						
			height = 1; 					
		}
		
		gl.glViewport(0, 0, mWidth, mHeight); 	
		gl.glMatrixMode(GL10.GL_PROJECTION); 
		gl.glLoadIdentity(); 				

		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	
		gl.glLoadIdentity(); 					
	}
}
