package com.glm.bean;

import android.graphics.Paint;
import android.graphics.Path;

public class HandWritePath extends Path  {

	private Paint mPaint;
	private long PageID;
	private long DiaryID;
	
	private int mPathID=0;
	private float[] fPoint = new float[]{0,0};
	/**
	 * @return the mPaint
	 */
	public synchronized Paint getmPaint() {
		return mPaint;
	}

	/**
	 * @param mPaint the mPaint to set
	 */
	public synchronized void setmPaint(Paint mPaint) {
		this.mPaint = mPaint;
	}
	
	/**
	 * aggiunge un punto alla linea il primo punto è ovviamente l'inizio della linea
	 * 
	 * @param float x
	 * @param float y
	 * */
	public synchronized void addPoint(float x, float y){
		fPoint[0]=x;
		fPoint[1]=y;
	}

	/**
	 * @return the mPoint
	 */
	public synchronized float[] getmPoint() {
		return fPoint;
	}

	public synchronized void setPathID(int pathID) {
		mPathID=pathID;
	}

	/**
	 * @return the mPathID
	 */
	public synchronized int getmPathID() {
		return mPathID;
	}

	
	public synchronized void setDiaryID(long diaryID) {
		DiaryID=diaryID;
	}

	public synchronized void setPageID(long pageID) {
		PageID=pageID;
	}
	public synchronized long getDiaryID() {
		return DiaryID;
	}

	public synchronized long getPageID() {
		return PageID;
	}
}
